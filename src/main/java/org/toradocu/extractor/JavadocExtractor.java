package org.toradocu.extractor;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.doclet.formats.html.HtmlDocletWriter;
import org.toradocu.doclet.internal.toolkit.taglets.TagletWriter;
import org.toradocu.doclet.internal.toolkit.util.DocPath;

/**
 * {@code JavadocExtractor} extracts {@code DocumentedMethod}s from {@code ClassDoc}s. The entry
 * point for this class is the {@link #extract(ClassDoc)} method.
 */
public final class JavadocExtractor {

  /** Holds Javadoc doclet configuration options. */
  private final ConfigurationImpl configuration;

  private static final Logger log = LoggerFactory.getLogger(JavadocExtractor.class);

  /**
   * Constructs a {@code JavadocExtractor} with the given doclet {@code configuration}.
   *
   * @param configuration the Javadoc doclet configuration
   */
  public JavadocExtractor(ConfigurationImpl configuration) {
    this.configuration = configuration;
  }

  /**
   * Returns a list of {@code DocumentedMethod}s extracted from the given {@code classDoc}.
   *
   * @param classDoc the {@code ClassDoc} from which to extract method documentation
   * @return a list containing documented methods from the class
   * @throws IOException if the method encounters an error while reading/generating class
   *     documentation
   */
  public List<DocumentedMethod> extract(ClassDoc classDoc) throws IOException {
    List<DocumentedMethod> methods = new ArrayList<>();

    // Loop on constructors and methods (also inherited) of the target class.
    for (ExecutableMemberDoc member : getConstructorsAndMethods(classDoc)) {
      ClassDoc containingClass = member.containingClass();
      String containingClassName = containingClass.name();
      if (containingClassName.contains(".")) { // Containing class is not a top-level class.
        containingClassName = containingClassName.replace(".", "$");
      }
      List<ThrowsTag> memberThrowsTags = extractThrowsTags(member, classDoc);
      List<ParamTag> memberParamTags = extractParamTags(member, classDoc);
      List<ReturnTag> memberReturnTags = extractReturnTag(member, classDoc);

      ReturnTag finalReturnTag = null;
      if (!memberReturnTags.isEmpty()) finalReturnTag = memberReturnTags.get(0);

      methods.add(
          new DocumentedMethod(
              new org.toradocu.extractor.Type(
                  containingClass.containingPackage() + "." + containingClassName),
              member.name(),
              getReturnType(member),
              getParameters(member),
              memberParamTags,
              member.isVarArgs(),
              memberThrowsTags,
              finalReturnTag));
    }

    return methods;
  }

  /**
   * Returns all constructors and methods (including inherited ones) from the given {@code ClassDoc}
   * . Notice that methods inherited from the class {@code java.lang.Object} are ignored and not
   * included in the returned list.
   *
   * @param classDoc the {@code ClassDoc} from which to extract constructors and methods
   * @return a list of {@code ExecutableMemberDoc}s representing the constructors and methods of
   *     {@code classDoc}
   */
  private List<ExecutableMemberDoc> getConstructorsAndMethods(ClassDoc classDoc) {
    /* Constructors of the class {@code classDoc} to be returned by this method */
    List<ExecutableMemberDoc> constructors = new ArrayList<>();

    // Collect non-default constructors.
    for (ConstructorDoc constructor : classDoc.constructors(false)) {
      // This is a workaround to strange behavior of method Doc.position(). It does not return null
      // for default constructors. It instead returns the line number of the start of the class.
      SourcePosition position = constructor.position();
      if (!constructor.isSynthetic()
          && !constructor.isPrivate()
          && position != null
          && !position.toString().equals(classDoc.position().toString())) {
        constructors.add(constructor);
      }
    }

    // Collect non-private non-synthetic methods (i.e. those methods that have not been synthesized
    // by the compiler).
    List<MethodDoc> methods =
        Arrays.stream(classDoc.methods(false))
            .filter(m -> !m.isSynthetic() && !m.isPrivate())
            .collect(Collectors.toList());

    List<ExecutableMemberDoc> members = new ArrayList<>();
    members.addAll(constructors);
    members.addAll(methods);
    return members;
  }

  /**
   * Returns the return type of the given {@code member}. Returns {@code null} if {@code member} is
   * a constructor.
   *
   * @param member the executable member (constructor or method) to return the return type
   * @return the return type of the given member or null if the member is a constructor
   */
  private org.toradocu.extractor.Type getReturnType(ExecutableMemberDoc member) {
    if (member instanceof MethodDoc) {
      MethodDoc method = (MethodDoc) member;
      Type returnType = method.returnType();
      return new org.toradocu.extractor.Type(
          returnType.qualifiedTypeName() + returnType.dimension());
    } else {
      return null;
    }
  }

  /**
   * Returns the {@code Parameter}s of the given constructor or method.
   *
   * @param member the constructor or method from which to extract parameters
   * @return an array of parameters
   */
  private List<Parameter> getParameters(ExecutableMemberDoc member) {
    com.sun.javadoc.Parameter[] params = member.parameters();
    Parameter[] parameters = new Parameter[params.length];
    for (int i = 0; i < parameters.length; i++) {
      // Determine nullness constraints from parameter annotations.
      Boolean nullable = null;
      for (AnnotationDesc annotation : params[i].annotations()) {
        String annotationTypeName = annotation.annotationType().name().toLowerCase();
        if (annotationTypeName.equals("nullable")) {
          nullable = true;
          break;
        } else if (annotationTypeName.equals("notnull") || annotationTypeName.equals("nonnull")) {
          nullable = false;
          break;
        }
      }

      String parameterType = getParameterType(params[i].type());
      parameters[i] =
          new Parameter(new org.toradocu.extractor.Type(parameterType), params[i].name(), nullable);
    }
    return Arrays.asList(parameters);
  }

  /**
   * Returns the qualified name (with dimension information) of the specified parameter type.
   *
   * @param parameterType the type (of a parameter)
   * @return the qualified name (with dimension information) of the specified parameter type
   */
  private String getParameterType(Type parameterType) {
    String qualifiedName = "";

    TypeVariable pTypeAsTypeVariable = parameterType.asTypeVariable();
    // If the parameter is a type variable.
    if (pTypeAsTypeVariable != null) { // pTypeAsTypeVarialble can be like <E extends T>
      Type[] bounds = pTypeAsTypeVariable.bounds(); // bounds[0] is "T"
      do {
        if (bounds.length == 0) {
          qualifiedName = "java.lang.Object";
        } else {
          // FIXME What if the parameter type has multiple bounds? (e.g., <T extends B1 & B2>)
          TypeVariable boundType = bounds[0].asTypeVariable(); // boundType is "T"
          if (boundType == null) {
            qualifiedName = bounds[0].qualifiedTypeName();
            break;
          } else {
            bounds = boundType.bounds();
          }
        }
      } while (qualifiedName.isEmpty());
    } else {
      qualifiedName = parameterType.qualifiedTypeName();
    }
    /* Add dimension information when appropriate. Add "[]" if parameterType is a vararg. */
    return qualifiedName + parameterType.dimension();
  }

  /**
   * This method tries to return the qualified name of the exception in the {@code throwsTag}. If
   * the source code of the exception is not available, then just the simple name in the Javadoc
   * comment is returned.
   *
   * @param throwsTag throws tag to extract exception name from
   * @param member the method to which this throws tag belongs
   * @return the name of the exception in the throws tag (qualified, if possible)
   */
  @SuppressWarnings("deprecation")
  private String getExceptionName(com.sun.javadoc.ThrowsTag throwsTag, ExecutableMemberDoc member) {
    // We use deprecated method in Javadoc API. No alternative solution is documented.
    Type exceptionType = throwsTag.exceptionType();
    if (exceptionType != null) {
      return exceptionType.qualifiedTypeName();
    }
    // Try to collect the exception's name from the import declarations.
    String exceptionName = throwsTag.exceptionName();
    ClassDoc[] importedClasses;
    try {
      importedClasses = member.containingClass().importedClasses();
    } catch (NullPointerException e) {
      importedClasses = new ClassDoc[0];
    }
    for (ClassDoc importedClass : importedClasses) {
      if (importedClass.name().equals(exceptionName)) {
        return importedClass.qualifiedName();
      }
    }
    // If fully qualified exception's name cannot be collected from import statements, return the simple name
    return exceptionName;
  }

  /**
   * Given a set of @{code ParamTag} tags, returns a map that associates a parameter with its
   * {@code @param} comment. The key of the map is the parameter name.
   *
   * @param tags an array of {@code ParamTag}s
   * @return a map, parameter name -> @param comment
   */
  private Map<String, Tag> getParamTags(Tag[] tags) {
    Map<String, Tag> paramTags = new LinkedHashMap<>();
    for (Tag tag : tags) {
      com.sun.javadoc.ParamTag paramTag = (com.sun.javadoc.ParamTag) tag;
      paramTags.putIfAbsent(paramTag.parameterName(), paramTag);
    }
    return paramTags;
  }

  /**
   * This method extracts the throwsTags from the class we want.
   *
   * @param member the constructor or method from which to extract the throws tags
   * @param classDoc the class that we use to extract the tags
   * @return the list that contains the ThrowsTags of the class we gave
   * @throws IOException if the method encounters an error while reading/generating class
   *     documentation
   */
  private List<ThrowsTag> extractThrowsTags(ExecutableMemberDoc member, ClassDoc classDoc)
      throws IOException {
    // list that will contain the throws tags
    List<Tag> throwsTags = new ArrayList<>();

    // Collect tags in the current method's documentation. This is needed because DocFinder.search
    // does not load tags of a method when the method overrides a superclass' method also
    // overwriting the Javadoc documentation.
    throwsTags.addAll(throwsTagsOf(member));

    //    Doc holder = DocFinder.search(new DocFinder.Input(member)).holder;

    // Collect tags that are automatically inherited (i.e., when there is no comment for a method
    // overriding another one).
    //    throwsTags.addAll(throwsTagsOf(holder));

    // Collect tags from method definitions in interfaces. This is not done by DocFinder.search
    // (at least in the way we use it).
    //    if (holder instanceof MethodDoc) {
    //      ImplementedMethods implementedMethods =
    //          new ImplementedMethods((MethodDoc) holder, configuration);
    //      for (MethodDoc implementedMethod : implementedMethods.build()) {
    //        throwsTags.addAll(throwsTagsOf(implementedMethod));
    //      }
    //    }

    List<ThrowsTag> memberThrowsTags = new ArrayList<>();
    for (Tag tag : throwsTags) {
      if (!(tag instanceof com.sun.javadoc.ThrowsTag)) {
        throw new IllegalStateException(
            tag
                + " is not a @Throws tag. This should not happen. Toradocu only considers @throws tags.");
      }

      com.sun.javadoc.ThrowsTag throwsTag = (com.sun.javadoc.ThrowsTag) tag;
      // Handle inline taglets such as {@inheritDoc}.
      TagletWriter tagletWriter =
          new HtmlDocletWriter(configuration, DocPath.forClass(classDoc))
              .getTagletWriterInstance(false);
      String comment = tagletWriter.commentTagsToOutput(tag, tag.inlineTags()).toString();

      /* taggedComment is the output of the Jsoup parsing: it can be exploited to extract
       * text contained in a certain tag. For now we're interested in <code> */
      Document taggedComment = Jsoup.parse(comment);

      /* Remove HTML tags (also generated by inline taglets). In the future, perhaps retain those tags,
       * because they contain information that can be exploited. */
      comment = taggedComment.text();

      // Words tagged with <code></code> (@code) found in the Javadoc comment of the parsed method
      List<String> stringCodeTags =
          taggedComment.select("code").stream().map(Element::text).collect(Collectors.toList());

      ThrowsTag tagToProcess =
          new ThrowsTag(
              new org.toradocu.extractor.Type(getExceptionName(throwsTag, member)),
              comment,
              stringCodeTags);
      memberThrowsTags.add(tagToProcess);
    }

    return memberThrowsTags;
  }

  /**
   * Returns all the @throws and @exception tags in the documentation of the given class member.
   *
   * @param member a class member
   * @return a list with all the @throws and @exception tags of member
   */
  private static List<Tag> throwsTagsOf(Doc member) {
    final List<Tag> throwsTags = new ArrayList<>();
    Collections.addAll(throwsTags, member.tags("@exception"));
    Collections.addAll(throwsTags, member.tags("@throws"));
    return throwsTags;
  }

  /**
   * @param member the constructor or method from which to extract the throws tags
   * @param classDoc the class that we use to extract the tags
   * @return the list that contains the ParamTags of the class we gave
   * @throws IOException if the method encounters an error while reading/generating class
   *     documentation
   */
  private List<ParamTag> extractParamTags(ExecutableMemberDoc member, ClassDoc classDoc)
      throws IOException {

    // List that will contain all the paramTags in the method.
    List<Tag> paramTags = new ArrayList<>();

    // Map parameter name -> @param tag.
    Map<String, Tag> paramTagsMap = new LinkedHashMap<>();

    // Param tag support.
    paramTagsMap.putAll(getParamTags(member.tags("@param")));

    //    Doc holder = DocFinder.search(new DocFinder.Input(member)).holder;

    // Extract the inherited tags.
    //    paramTagsMap.putAll(getParamTags(holder.tags("@param")));

    // Collect tags from method definitions in interfaces. This is not done by DocFinder.search
    // (at least in the way we use it).
    //    if (holder instanceof MethodDoc) {
    //      ImplementedMethods implementedMethods =
    //          new ImplementedMethods((MethodDoc) holder, configuration);
    //      for (MethodDoc implementedMethod : implementedMethods.build()) {
    //        //param
    //        paramTagsMap.putAll(getParamTags(implementedMethod.tags("@param")));
    //      }
    //    }

    // List that will contain the ParamTags of the method.
    paramTags.addAll(paramTagsMap.values());
    List<ParamTag> memberParamTags = new ArrayList<>();
    for (Tag tag : paramTags) { // For each of the tags in paramTags.
      if (!(tag instanceof com.sun.javadoc.ParamTag)) { // If it is not an instanceof ParamTag.
        throw new IllegalStateException(
            tag
                + " is not a @param tag. This should not happen."
                + " Toradocu only considers @param and @throws tags.");
      }

      // We create a paramsTag that will be the cast of tag to ParamTag in order to work with it.
      com.sun.javadoc.ParamTag paramsTag = (com.sun.javadoc.ParamTag) tag;

      // Handle inline taglets such as {@inheritDoc}.
      TagletWriter tagletWriter =
          new HtmlDocletWriter(configuration, DocPath.forClass(classDoc))
              .getTagletWriterInstance(false);
      String comment = tagletWriter.commentTagsToOutput(tag, tag.inlineTags()).toString();

      // Remove HTML tags (also generated by inline taglets). In the future, perhaps retain those
      // tags, because they contain information that can be exploited.
      comment = Jsoup.parse(comment).text();

      //The list of the parameters of the method that we'll use in order to match
      //the parameterName of the tag with the parameter itself
      List<Parameter> parameters = getParameters(member);

      String name = null; //Name of the ParamTag that we'll introduce
      org.toradocu.extractor.Type type = null; //type of the parameter of the ParamTag
      Boolean nullable = null; // Nullability of the parameter.

      boolean found = false; //Boolean for control
      for (int i = 0; !found && i < parameters.size(); i++) {
        if (parameters.get(i).getName().equals(paramsTag.parameterName())) {
          // If the tag parameterName matches any parameter name in the method, then we stop the
          // loop.
          found = true;
          name = parameters.get(i).getName(); //then we assign values to the variables
          type = parameters.get(i).getType();
          nullable = parameters.get(i).getNullability();
        }
      }

      if (!found) { //At the exit of the loop, if found == true, we didn't find anything
        log.trace(
            tag
                + " this param tag does not have a name that matches any of the parameters in "
                + "the method.");
      } else { //if not, then, the variables will have the value we want
        ParamTag tagToProcess = new ParamTag(new Parameter(type, name, nullable), comment);
        memberParamTags.add(tagToProcess); //And then, we'll add it in the list we had before
      }
    }
    return memberParamTags;
  }

  /**
   * This method extracts the returnTag from the class we want.
   *
   * @param member the constructor or method from which to extract the return tags
   * @param classDoc the class that we use to extract the tags
   * @return the list that contains the ReturnTags of the class we gave
   * @throws IOException if the method encounters an error while reading/generating class
   *     documentation
   */
  private List<ReturnTag> extractReturnTag(ExecutableMemberDoc member, ClassDoc classDoc)
      throws IOException {
    // List that will contain the return tags and will be returned by this method.
    List<Tag> returnTags = new ArrayList<>();
    final String TAG_NAME = "@return";

    // Collect tags in the current method's documentation. This is needed because DocFinder.search
    // does not load tags of a method when the method overrides a superclass' method also
    // overwriting the Javadoc documentation.
    Collections.addAll(returnTags, member.tags(TAG_NAME));

    //    if (returnTags.isEmpty()) { // Inherit @return comments if necessary.
    //      Doc holder = DocFinder.search(new DocFinder.Input(member)).holder;
    //
    //      // Collect tags that are automatically inherited (i.e., when there is no comment for a method
    //      // overriding another one).
    //      Collections.addAll(returnTags, holder.tags(TAG_NAME));
    //
    //      // Collect tags from method definitions in interfaces. This is not done by DocFinder.search
    //      // (at least in the way we use it).
    //      if (holder instanceof MethodDoc) {
    //        ImplementedMethods implementedMethods =
    //            new ImplementedMethods((MethodDoc) holder, configuration);
    //        for (MethodDoc implementedMethod : implementedMethods.build()) {
    //          Collections.addAll(returnTags, implementedMethod.tags(TAG_NAME));
    //        }
    //      }
    //    }

    List<ReturnTag> memberReturnTags = new ArrayList<>();
    for (Tag tag : returnTags) {

      // Handle inline taglets such as {@inheritDoc}.
      TagletWriter tagletWriter =
          new HtmlDocletWriter(configuration, DocPath.forClass(classDoc))
              .getTagletWriterInstance(false);
      String comment = tagletWriter.commentTagsToOutput(tag, tag.inlineTags()).toString();

      /* taggedComment is the output of the Jsoup parsing: it can be exploited to extract
       * text contained in a certain tag. For now we're interested in <code> */
      Document taggedComment = Jsoup.parse(comment);

      // Remove HTML tags (also generated by inline taglets). In the future, perhaps retain those
      // tags, because they contain information that can be exploited.
      comment = taggedComment.text();

      ReturnTag tagToProcess = new ReturnTag(comment);
      memberReturnTags.add(tagToProcess);
    }

    if (memberReturnTags.size() > 1) {
      log.trace(
          "There are more than one return tag in this method. This is not handled by Toradocu");
    }

    return memberReturnTags;
  }
}
