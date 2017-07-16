package org.toradocu.extractor;

import static java.util.stream.Collectors.toList;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithPrivateModifier;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.toradocu.util.Reflection;

/**
 * {@code JavadocExtractor} extracts {@code DocumentedExecutable}s from a class by means of {@code
 * extract(String, String)}.
 */
public final class JavadocExtractor {

  /**
   * Returns a list of {@code DocumentedExecutable}s extracted from the class with name {@code
   * className}. The JavadocExtractor parses the Java source code of the specified class ({@code
   * className}), and stores information about the executable members of the specified class
   * (including the Javadoc comments).
   *
   * @param className the qualified class name of the class from which to extract documentation
   * @param sourcePath the path to the project source root folder
   * @return a list of documented executable members
   * @throws ClassNotFoundException if some reflection information cannot be loaded
   * @throws FileNotFoundException if the source code of the class with name {@code className}
   *     cannot be found in path {@code sourcePath}
   */
  public DocumentedType extract(String className, String sourcePath)
      throws ClassNotFoundException, FileNotFoundException {

    // Obtain executable members by means of reflection.
    final Class<?> clazz = Reflection.getClass(className);
    final List<Executable> reflectionExecutables = getExecutables(clazz);
    // Obtain executable members in the source code.
    // TODO Add support for nested classes.
    String classFile =
        sourcePath + File.separator + className.replaceAll("\\.", File.separator) + ".java";
    final List<CallableDeclaration<?>> sourceExecutables =
        getExecutables(clazz.getSimpleName(), classFile);

    String packagePath = classFile.substring(0, classFile.lastIndexOf("/"));
    File folder = new File(packagePath);
    File[] listOfFiles = folder.listFiles();
    List<String> classesInPackage = new ArrayList<String>();
    for (File file : listOfFiles) {
      // this loop extracts files in the same directory of the class being analysed
      // in order to find eventual Exception classes located in the same package.
      // package-info files are not useful for this purpose
      String name = extractClassNameForSource(file.getName(), className);
      if (name != null && !name.equals(className) && !name.contains("package-info")) {
        classesInPackage.add(name);
      }
    }
    // Maps each reflection executable member to its corresponding source member.
    Map<Executable, CallableDeclaration<?>> executablesMap =
        mapExecutables(reflectionExecutables, sourceExecutables);

    // Create the list of ExecutableMembers.
    List<DocumentedExecutable> members = new ArrayList<>(reflectionExecutables.size());
    for (Entry<Executable, CallableDeclaration<?>> entry : executablesMap.entrySet()) {
      final Executable reflectionMember = entry.getKey();
      final CallableDeclaration<?> sourceMember = entry.getValue();
      final List<DocumentedParameter> parameters =
          getParameters(sourceMember.getParameters(), reflectionMember.getParameters());
      Triple<List<ParamTag>, ReturnTag, List<ThrowsTag>> tags =
          createTags(classesInPackage, sourceMember, parameters);
      // TODO Consider to change DocumentedExecutable constructor to take as arguments the different
      // TODO tags, and not one single list like it does now.
      List<Tag> tagList = new ArrayList<>();
      tagList.addAll(tags.getLeft());
      tagList.add(tags.getMiddle());
      tagList.addAll(tags.getRight());
      members.add(new DocumentedExecutable(reflectionMember, parameters, tagList));
    }

    // Create the documented class.
    return new DocumentedType(clazz, members);
  }

  /**
   * Given the file name of a source located in the same package of the Class being analysed and the
   * name of that Class, this method compose the class name corresponding to the source.
   *
   * @param sourceFileName name of the source file found in package
   * @param analyzedClassName class name of the class being analysed
   * @return the class name of the source
   */
  private String extractClassNameForSource(String sourceFileName, String analyzedClassName) {
    int lastDot = analyzedClassName.lastIndexOf(".");
    String parsedName = null;
    if (lastDot != -1)
      parsedName =
          analyzedClassName.substring(0, lastDot) + "." + sourceFileName.replace(".java", "");

    return parsedName;
  }

  /**
   * Instantiates tags (of param, return or throws kind) referred to a source member.
   *
   * @param classesInPackage list of class names in sourceMember's package
   * @param sourceMember the source member the tags refer to
   * @param parameters {@code sourceMember}'s parameters
   * @return a triple of instantiated tags: list of @param tags, return tag, list of @throws tags
   * @throws ClassNotFoundException if a type described in a Javadoc comment cannot be loaded (e.g.,
   *     the type is not on the classpath.)
   */
  private Triple<List<ParamTag>, ReturnTag, List<ThrowsTag>> createTags(
      List<String> classesInPackage,
      CallableDeclaration<?> sourceMember,
      List<DocumentedParameter> parameters)
      throws ClassNotFoundException {

    List<ParamTag> paramTags = new ArrayList<>();
    ReturnTag returnTag = null;
    List<ThrowsTag> throwsTags = new ArrayList<>();

    final Optional<Javadoc> javadocOpt = sourceMember.getJavadoc();
    if (javadocOpt.isPresent()) {
      final Javadoc javadocComment = javadocOpt.get();
      final List<JavadocBlockTag> blockTags = javadocComment.getBlockTags();
      for (JavadocBlockTag blockTag : blockTags) {
        switch (blockTag.getType()) {
          case PARAM:
            paramTags.add(createParamTag(blockTag, parameters));
            break;
          case RETURN:
            returnTag = createReturnTag(blockTag);
            break;
          case EXCEPTION:
          case THROWS:
            throwsTags.add(createThrowsTag(classesInPackage, blockTag, sourceMember));
            break;
        }
      }
    }
    return new ImmutableTriple<>(paramTags, returnTag, throwsTags);
  }

  /**
   * Instantiate a tag of throws kind.
   *
   * @param classesInPackage list of class names in sourceMember's package
   * @param blockTag the @throws or @exception Javadoc block comment containing the tag
   * @param sourceMember the source member the tag refers to
   * @return the instantiated tag
   * @throws ClassNotFoundException if the class of the exception type couldn't be found
   */
  private ThrowsTag createThrowsTag(
      List<String> classesInPackage, JavadocBlockTag blockTag, CallableDeclaration<?> sourceMember)
      throws ClassNotFoundException {
    // Javaparser library does not provide a nice parsing of @throws tags. We have to parse the
    // comment text by ourselves.
    final Type blockTagType = blockTag.getType();
    if (!blockTagType.equals(Type.THROWS) && !blockTagType.equals(Type.EXCEPTION)) {
      throw new IllegalArgumentException(
          "The block tag " + blockTag + " does not refer to an" + " @throws or @exception tag");
    }

    String comment = blockTag.getContent().toText();
    final String[] tokens = comment.split("[\\s\\t]+", 2);
    final String exceptionName = tokens[0];
    Class<?> exceptionType = findExceptionType(classesInPackage, sourceMember, exceptionName);
    Comment commentObject = new Comment(tokens[1]);
    return new ThrowsTag(exceptionType, commentObject);
  }

  /**
   * Instantiate a tag of return kind.
   *
   * @param blockTag the @return block containing the tag
   * @return the instantiated tag
   */
  private ReturnTag createReturnTag(JavadocBlockTag blockTag) {
    final Type blockTagType = blockTag.getType();
    if (!blockTagType.equals(Type.RETURN)) {
      throw new IllegalArgumentException(
          "The block tag " + blockTag + " does not refer to an" + " @return tag");
    }

    String content = blockTag.getContent().toText();
    // Fix bug in Javaparser: missing open bracket of {@code} inline tag.
    if (content.startsWith("@code ")) {
      content = "{" + content;
    }

    Comment commentObject = new Comment(content);
    return new ReturnTag(commentObject);
  }

  /**
   * Instantiate a tag of param kind.
   *
   * @param blockTag the block containing the tag
   * @param parameters the formal parameter list in which looking for the one associated to the tag
   * @return the instantiated tag, null if {@code blockTag} refers to a @param tag documenting a
   *     generic type parameter.
   */
  private ParamTag createParamTag(JavadocBlockTag blockTag, List<DocumentedParameter> parameters) {
    final Type blockTagType = blockTag.getType();
    if (!blockTagType.equals(Type.PARAM)) {
      throw new IllegalArgumentException(
          "The block tag " + blockTag + " does not refer to an" + " @param tag");
    }

    String paramName = blockTag.getName().orElse("");

    // Return null if blockTag refers to a @param tag documenting a generic type parameter.
    if (paramName.startsWith("<") && paramName.endsWith(">")) {
      return null;
    }

    final List<DocumentedParameter> matchingParams =
        parameters.stream().filter(p -> p.getName().equals(paramName)).collect(toList());
    // TODO If paramName not present in paramNames => issue a warning about incorrect documentation!
    // TODO If more than one matching parameter found => issue a warning about incorrect documentation!
    Comment commentObject = new Comment(blockTag.getContent().toText());
    return new ParamTag(matchingParams.get(0), commentObject);
  }

  /**
   * Instantiate the parameters of type org.toradocu.extractor.DocumentedParameter
   *
   * @param sourceParams the NodeList of parameters found in source
   * @param reflectionParams the array of parameters found through reflection
   * @return the list of org.toradocu.extractor.DocumentedParameter
   */
  private List<DocumentedParameter> getParameters(
      NodeList<com.github.javaparser.ast.body.Parameter> sourceParams,
      java.lang.reflect.Parameter[] reflectionParams) {

    if (sourceParams.size() != reflectionParams.length) {
      throw new IllegalArgumentException(
          "Source param types and reflection param types should be of the same size.");
    }

    List<DocumentedParameter> parameters = new ArrayList<>(sourceParams.size());
    for (int i = 0; i < sourceParams.size(); i++) {
      final com.github.javaparser.ast.body.Parameter parameter = sourceParams.get(i);
      final String paramName = parameter.getName().asString();
      final Boolean nullable = isNullable(parameter);
      parameters.add(new DocumentedParameter(reflectionParams[i], paramName, nullable));
    }
    return parameters;
  }

  /**
   * Checks whether the given parameter is annotated with @NotNull or @Nullable or similar.
   *
   * @param parameter the parameter to check
   * @return true if the parameter is annotated with @Nullable, false if the parameter is annotated
   *     with @NonNull, null otherwise or if it's both nullable and notNull
   */
  private Boolean isNullable(com.github.javaparser.ast.body.Parameter parameter) {
    final List<String> parameterAnnotations =
        parameter.getAnnotations().stream().map(a -> a.getName().asString()).collect(toList());
    List<String> notNullAnnotations = new ArrayList<>(parameterAnnotations);
    notNullAnnotations.retainAll(DocumentedParameter.notNullAnnotations);
    List<String> nullableAnnotations = new ArrayList<>(parameterAnnotations);
    nullableAnnotations.retainAll(DocumentedParameter.nullableAnnotations);

    if (!notNullAnnotations.isEmpty() && !nullableAnnotations.isEmpty()) {
      // Parameter is annotated as both nullable and notNull.
      // TODO Log a warning about wrong specification!
      return null;
    }
    if (!notNullAnnotations.isEmpty()) {
      return false;
    }
    if (!nullableAnnotations.isEmpty()) {
      return true;
    }
    return null;
  }

  /**
   * Collects non-private members through reflection.
   *
   * @param clazz the Class containing the members to collect
   * @return non private-members of {@code clazz}
   */
  private List<Executable> getExecutables(Class<?> clazz) {
    List<Executable> executables = new ArrayList<>();
    executables.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
    executables.addAll(Arrays.asList(clazz.getDeclaredMethods()));
    executables.removeIf(Executable::isSynthetic);
    executables.removeIf(e -> Modifier.isPrivate(e.getModifiers())); // Ignore private members.
    return Collections.unmodifiableList(executables);
  }

  /**
   * Collects non-private members from source code.
   *
   * @param className the String class name
   * @param sourcePath the String source path
   * @return non private-members of the class with name {@code className}
   * @throws FileNotFoundException if the source path couldn't be resolved
   */
  private List<CallableDeclaration<?>> getExecutables(String className, String sourcePath)
      throws FileNotFoundException {
    final CompilationUnit cu = JavaParser.parse(new File(sourcePath));
    Optional<ClassOrInterfaceDeclaration> definitionOpt = cu.getClassByName(className);
    if (!definitionOpt.isPresent()) {
      definitionOpt = cu.getInterfaceByName(className);
    }
    if (!definitionOpt.isPresent()) {
      throw new IllegalArgumentException(
          "Impossible to find a class or interface with name " + className + " in " + sourcePath);
    }
    final List<CallableDeclaration<?>> sourceExecutables = new ArrayList<>();
    final ClassOrInterfaceDeclaration sourceClass = definitionOpt.get();
    sourceExecutables.addAll(sourceClass.getConstructors());
    sourceExecutables.addAll(sourceClass.getMethods());
    sourceExecutables.removeIf(NodeWithPrivateModifier::isPrivate); // Ignore private members.
    return Collections.unmodifiableList(sourceExecutables);
  }

  /**
   * Maps reflection members to source code members.
   *
   * @param reflectionExecutables the list of reflection members
   * @param sourceExecutables the list of source code members
   * @return a map holding the correspondences
   */
  private Map<Executable, CallableDeclaration<?>> mapExecutables(
      List<Executable> reflectionExecutables, List<CallableDeclaration<?>> sourceExecutables) {

    if (reflectionExecutables.size() != sourceExecutables.size()) {
      throw new IllegalArgumentException("Error: Provided lists have different size.");
    }

    Map<Executable, CallableDeclaration<?>> map = new LinkedHashMap<>(reflectionExecutables.size());
    for (CallableDeclaration<?> sourceMember : sourceExecutables) {
      final List<Executable> matches =
          reflectionExecutables
              .stream()
              .filter(
                  e ->
                      removePackage(e.getName()).equals(sourceMember.getName().asString())
                          && sameParTypes(e.getParameters(), sourceMember.getParameters()))
              .collect(toList());
      if (matches.size() < 1) {
        throw new AssertionError(
            "Cannot find reflection executable member corresponding to "
                + sourceMember.getSignature());
      }
      if (matches.size() > 1) {
        throw new AssertionError(
            "Found multiple reflection executable members corresponding to "
                + sourceMember.getSignature());
      }
      map.put(matches.get(0), sourceMember);
    }
    return map;
  }

  /**
   * Checks that reflection param types and source param types are the same.
   *
   * @param reflectionParams array of reflection param types
   * @param sourceParams NodeList of source param types
   * @return true if the param types are the same, false otherwise
   */
  private boolean sameParTypes(
      java.lang.reflect.Parameter[] reflectionParams,
      NodeList<com.github.javaparser.ast.body.Parameter> sourceParams) {
    if (reflectionParams.length != sourceParams.size()) {
      return false;
    }

    for (int i = 0; i < reflectionParams.length; i++) {
      final java.lang.reflect.Parameter reflectionParam = reflectionParams[i];
      final String reflectionQualifiedTypeName =
          removeGenerics(reflectionParam.getParameterizedType().getTypeName());
      String reflectionSimpleTypeName = removePackage(reflectionQualifiedTypeName);

      final com.github.javaparser.ast.body.Parameter sourceParam = sourceParams.get(i);
      String sourceTypeName = removeGenerics(sourceParam.getType().asString());
      if (sourceParam.isVarArgs()) sourceTypeName += "[]";
      if (reflectionParam.isVarArgs() && !reflectionSimpleTypeName.contains("[]"))
        reflectionSimpleTypeName += "[]";
      int dollar = reflectionSimpleTypeName.indexOf("$");
      if (dollar != -1) {
        if (sourceTypeName.contains("."))
          reflectionSimpleTypeName = reflectionSimpleTypeName.replace("$", ".");
        else
          reflectionSimpleTypeName =
              reflectionSimpleTypeName.substring(dollar + 1, reflectionSimpleTypeName.length());
      }

      if (!reflectionSimpleTypeName.equals(sourceTypeName)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Clean generics type.
   *
   * @param type the String type
   * @return the cleaned type
   */
  private String removeGenerics(String type) {
    int i = type.indexOf("<");
    if (i != -1) { // If type contains "<".
      return type.substring(0, i);
    }
    return type;
  }

  /**
   * Remove package from a type name. For example, given "java.lang.String", this method returns
   * "String".
   *
   * @param type the type name from which remove the package prefix
   * @return the given type with package prefix removed
   */
  private String removePackage(String type) {
    // Constructor names contain package name.
    int i = type.indexOf(".");
    if (i != -1) {
      return type.substring(type.lastIndexOf(".") + 1);
    }
    return type;
  }

  /**
   * Search for the type of the exception with the given type name. The type name is allowed to be
   * fully-qualified or simple, in which case this method tries to guess the package name.
   *
   * @param classesInPackage list of class names in sourceMember's package
   * @param sourceMember the source member for which the exception with type name {@code
   *     exceptionTypeName} is expected
   * @param exceptionTypeName the exception type name (can be fully-qualified or simple)
   * @return the exception class
   * @throws ClassNotFoundException if exception class couldn't be loaded
   */
  private Class<?> findExceptionType(
      List<String> classesInPackage, CallableDeclaration<?> sourceMember, String exceptionTypeName)
      throws ClassNotFoundException {
    Class<?> exceptionType = null;
    try {
      exceptionType = Reflection.getClass(exceptionTypeName);
    } catch (ClassNotFoundException e) {
      // Intentionally empty: Apply other heuristics to load the exception type.
    }
    if (exceptionType == null) {
      // Look in classes of package.
      for (String classInPackage : classesInPackage) {
        if (classInPackage.contains(exceptionTypeName)) {
          // TODO Add a comment explaining why the following check is needed.
          if (classInPackage.contains("$")) {
            classInPackage = classInPackage.replace(".class", "");
          }
          exceptionType = Reflection.getClass(classInPackage);
        }
      }
    }
    if (exceptionType == null) {
      try {
        exceptionType = Reflection.getClass("java.lang." + exceptionTypeName);
      } catch (ClassNotFoundException e) {
        // Intentionally empty: Apply other heuristics to load the exception type.
      }
    }
    if (exceptionType == null) {
      // Look for an import statement to complete exception type name.
      CompilationUnit cu = getCompilationUnit(sourceMember);
      final NodeList<ImportDeclaration> imports = cu.getImports();
      for (ImportDeclaration anImport : imports) {
        String importedType = anImport.getNameAsString();
        if (importedType.endsWith(exceptionTypeName)) {
          exceptionType = Reflection.getClass(importedType);
        }
      }
    }
    if (exceptionType == null) {
      // TODO Improve error message.
      throw new ClassNotFoundException(
          "Unable to load exception type " + exceptionTypeName + ". Is it on the classpath?");
    }
    return exceptionType;
  }

  /**
   * Returns the compilation unit where {@code member} is defined.
   *
   * @param member an executable member
   * @return the compilation unit where {@code member} is defined
   */
  private CompilationUnit getCompilationUnit(CallableDeclaration<?> member) {
    Optional<Node> nodeOpt = member.getParentNode();
    Node node = nodeOpt.orElse(null);
    while (!(node instanceof CompilationUnit)) {
      nodeOpt = node.getParentNode();
      node = nodeOpt.orElse(null);
    }
    return (CompilationUnit) node;
  }
}
