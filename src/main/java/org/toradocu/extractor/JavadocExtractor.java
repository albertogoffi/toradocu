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
import org.toradocu.util.Reflection;

/**
 * {@code JavadocExtractor} extracts {@code ExecutableMember}s from a class by means of {@code
 * extract(String, String)}.
 */
public final class JavadocExtractor {

  /**
   * Returns a list of {@code ExecutableMember}s extracted from the class with name {@code
   * className}.
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
    String classFile = sourcePath + "/" + className.replaceAll("\\.", "/") + ".java";
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
    // Map reflection executable members to corresponding source members.
    Map<Executable, CallableDeclaration<?>> executablesMap =
        mapExecutables(reflectionExecutables, sourceExecutables);

    // Create the list of ExecutableMembers.
    List<ExecutableMember> members = new ArrayList<>(reflectionExecutables.size());
    for (Entry<Executable, CallableDeclaration<?>> entry : executablesMap.entrySet()) {
      final Executable reflectionMember = entry.getKey();
      final CallableDeclaration<?> sourceMember = entry.getValue();
      final List<Parameter> parameters =
          getParameters(sourceMember.getParameters(), reflectionMember.getParameters());
      List<org.toradocu.extractor.Tag> tags =
          createTags(classesInPackage, sourceMember, parameters);
      members.add(new ExecutableMember(reflectionMember, parameters, tags));
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
   * @param sourceMember the source member the tags are referred to
   * @param parameters the list of parameters useful to find the ones associated to param kind tags
   * @return the list of instantiated tags
   * @throws ClassNotFoundException if the class of the eventual exception type couldn't be found
   */
  private List<org.toradocu.extractor.Tag> createTags(
      List<String> classesInPackage,
      CallableDeclaration<?> sourceMember,
      List<Parameter> parameters)
      throws ClassNotFoundException {
    List<org.toradocu.extractor.Tag> tags = new ArrayList<>();
    final Optional<Javadoc> javadocOpt = sourceMember.getJavadoc();
    if (javadocOpt.isPresent()) {
      final Javadoc javadocComment = javadocOpt.get();
      final List<JavadocBlockTag> blockTags = javadocComment.getBlockTags();
      for (JavadocBlockTag blockTag : blockTags) {
        org.toradocu.extractor.Tag newTag = null;
        switch (blockTag.getType()) {
          case PARAM:
            newTag = createParamTag(blockTag, parameters);
            break;
          case RETURN:
            newTag = createReturnTag(blockTag);
            break;
          case THROWS:
            newTag = createThrowsTag(classesInPackage, blockTag, sourceMember);
            break;
          case EXCEPTION:
            newTag = createThrowsTag(classesInPackage, blockTag, sourceMember);
            break;
        }
        if (newTag != null) {
          tags.add(newTag);
        }
      }
    }
    return tags;
  }

  /**
   * Instantiate a tag of throws kind.
   *
   * @param classesInPackage list of class names in sourceMember's package
   * @param blockTag the block containing the tag
   * @param sourceMember the source member the tag is referred to
   * @return the instantiated tag
   * @throws ClassNotFoundException if the class of the exception type couldn't be found
   */
  private ThrowsTag createThrowsTag(
      List<String> classesInPackage, JavadocBlockTag blockTag, CallableDeclaration<?> sourceMember)
      throws ClassNotFoundException {
    // Javaparser library does not provide a nice parsing of @throws tags. We have to parse the
    // comment text by ourselves.
    String comment = blockTag.getContent().toText();
    final String[] tokens = comment.split(" ", 2);
    final String exceptionName = tokens[0];
    Class<?> exceptionType = findExceptionType(classesInPackage, sourceMember, exceptionName);
    Comment commentObject = new Comment(tokens[1]);
    return new ThrowsTag(exceptionType, commentObject);
  }

  /**
   * Instantiate a tag of return kind.
   *
   * @param blockTag the block containing the tag
   * @return the instantiated tag
   */
  private ReturnTag createReturnTag(JavadocBlockTag blockTag) {
    String content = blockTag.getContent().toText();
    //correct bug in Javaparser
    if (content.startsWith("@code ")) content = "{" + content;

    Comment commentObject = new Comment(content);
    return new ReturnTag(commentObject);
  }

  /**
   * Instantiate a tag of param kind.
   *
   * @param blockTag the block containing the tag
   * @param parameters the formal parameter list in which looking for the one associated to the tag
   * @return the instantiated tag
   */
  private ParamTag createParamTag(JavadocBlockTag blockTag, List<Parameter> parameters) {
    String paramName = blockTag.getName().orElse("");

    // Return null if blockTag refers to a @param tag documenting a generic type parameter.
    if (paramName.startsWith("<") && paramName.endsWith(">")) {
      return null;
    }

    final List<Parameter> matchingParams =
        parameters.stream().filter(p -> p.getName().equals(paramName)).collect(toList());
    // TODO If paramName not present in paramNames => issue a warning about incorrect documentation!
    // TODO If more than one matching parameter found => issue a warning about incorrect documentation!
    Comment commentObject = new Comment(blockTag.getContent().toText());
    return new ParamTag(matchingParams.get(0), commentObject);
  }

  /**
   * Instantiate the parameters of type org.toradocu.extractor.Parameter
   *
   * @param sourceParams the NodeList of parameters found in source
   * @param reflectionParams the array of parameters found through reflection
   * @return the list of org.toradocu.extractor.Parameter
   */
  private List<Parameter> getParameters(
      NodeList<com.github.javaparser.ast.body.Parameter> sourceParams,
      java.lang.reflect.Parameter[] reflectionParams) {

    if (sourceParams.size() != reflectionParams.length) {
      throw new IllegalArgumentException(
          "Source param types and reflection param types should be of the same size.");
    }

    List<Parameter> parameters = new ArrayList<>(sourceParams.size());
    for (int i = 0; i < sourceParams.size(); i++) {
      final com.github.javaparser.ast.body.Parameter parameter = sourceParams.get(i);
      final String paramName = parameter.getName().asString();
      final Boolean nullable = isNullable(parameter);
      parameters.add(new Parameter(reflectionParams[i], paramName, nullable));
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
    notNullAnnotations.retainAll(Parameter.notNullAnnotations);
    List<String> nullableAnnotations = new ArrayList<>(parameterAnnotations);
    nullableAnnotations.retainAll(Parameter.nullableAnnotations);

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
   * Collects members through reflection.
   *
   * @param clazz the Class containing the members to collect
   * @return an unmodifiable list of Executable
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
   * Collects members from source code.
   *
   * @param className the String class name
   * @param sourcePath the String source path
   * @return a list of CallableDeclaration
   * @throws FileNotFoundException if the source path couldn't be resolved
   */
  private List<CallableDeclaration<?>> getExecutables(String className, String sourcePath)
      throws FileNotFoundException {
    final CompilationUnit cu = JavaParser.parse(new File(sourcePath));
    final Optional<ClassOrInterfaceDeclaration> sourceClassOpt = cu.getClassByName(className);
    Optional<ClassOrInterfaceDeclaration> sourceIntOpt = cu.getInterfaceByName(className);
    final List<CallableDeclaration<?>> sourceExecutables = new ArrayList<>();
    if (sourceClassOpt.isPresent()) {
      final ClassOrInterfaceDeclaration sourceClass = sourceClassOpt.get();
      sourceExecutables.addAll(sourceClass.getConstructors());
      sourceExecutables.addAll(sourceClass.getMethods());
      sourceExecutables.removeIf(NodeWithPrivateModifier::isPrivate); // Ignore private members.
    }
    if (sourceIntOpt.isPresent()) {
      final ClassOrInterfaceDeclaration sourceClass = sourceIntOpt.get();
      sourceExecutables.addAll(sourceClass.getConstructors());
      sourceExecutables.addAll(sourceClass.getMethods());
      sourceExecutables.removeIf(NodeWithPrivateModifier::isPrivate); // Ignore private members.
    }
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

    //    if (reflectionExecutables.size() != sourceExecutables.size()) {
    //      throw new IllegalArgumentException("Error: Provided lists have different size.");
    //    }

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
   * Search for the type of the exception with the given type name.
   *
   * @param classesInPackage list of class names in sourceMember's package
   * @param sourceMember the source member for which the exception with type name {@code
   *     exceptionTypeName} is expected
   * @param exceptionTypeName the exception type name
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
      // Intentionally empty.
    }
    if (exceptionType == null) {
      // Look in classes of package
      for (String classInPackage : classesInPackage) {
        if (classInPackage.contains(exceptionTypeName)) {
          if (classInPackage.contains("$")) classInPackage = classInPackage.replace(".class", "");

          exceptionType = Reflection.getClass(classInPackage);
        }
      }
    }
    if (exceptionType == null) {
      try {
        exceptionType = Reflection.getClass("java.lang." + exceptionTypeName);
      } catch (ClassNotFoundException e) {
        // Intentionally empty.
      }
    }
    if (exceptionType == null) {
      // Look for an import statement to complete exception type name.
      Optional<Node> nodeOpt = sourceMember.getParentNode();
      Node node = nodeOpt.orElse(null);
      while (!(node instanceof CompilationUnit)) {
        nodeOpt = node.getParentNode();
        node = nodeOpt.orElse(null);
      }
      CompilationUnit cu = (CompilationUnit) node;
      final NodeList<ImportDeclaration> imports = cu.getImports();
      for (ImportDeclaration anImport : imports) {
        String importedType = anImport.getNameAsString();
        if (importedType.contains(exceptionTypeName)) {
          exceptionType = Reflection.getClass(importedType);
        }
      }
    }
    if (exceptionType == null) {
      throw new ClassNotFoundException("Impossible to load exception type " + exceptionTypeName);
    }
    return exceptionType;
  }
}
