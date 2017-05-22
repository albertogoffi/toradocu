package org.toradocu.extractor;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.toradocu.extractor.Tag.Kind;

/**
 * {@code JavadocExtractor} extracts {@code ExecutableMember}s from {@code ClassDoc}s. The entry
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
   * Returns a list of {@code ExecutableMember}s extracted from the given {@code classDoc}.
   *
   * @param classDoc the {@code ClassDoc} from which to extract method documentation
   * @return a list containing documented methods from the class
   * @throws IOException if the method encounters an error while reading/generating class
   *     documentation
   */
  public List<ExecutableMember> extract(ClassDoc classDoc)
      throws IOException, ClassNotFoundException {
    List<ExecutableMember> methods = new ArrayList<>();

    // Loop on constructors and methods (also inherited) of the target class.
    for (ExecutableMemberDoc member : getConstructorsAndMethods(classDoc)) {
      ClassDoc containingClass = member.containingClass();
      String containingClassName = containingClass.name();
      if (containingClassName.contains(".")) { // Containing class is not a top-level class.
        containingClassName = containingClassName.replace(".", "$");
      }
      String packageName = containingClass.containingPackage().toString();
      StringBuilder signature = new StringBuilder();
      if (!packageName.isEmpty()) {
        signature.append(packageName).append(".");
      }
      signature.append(containingClassName).append(".");
      signature.append(member.name()).append(member.signature());

      List<ThrowsTag> memberThrowsTags = extractThrowsTags(member, classDoc);
      List<ParamTag> memberParamTags = extractParamTags(member, classDoc);
      ReturnTag returnTag = extractReturnTag(member);
      List<org.toradocu.extractor.Tag> tags = new ArrayList<>();
      tags.addAll(memberParamTags);
      tags.addAll(memberThrowsTags);
      tags.add(returnTag);

      methods.add(new ExecutableMember(signature.toString(), getParameters(member), tags));
    }
    return methods;
  }

  /**
   * Returns all non-private non-synthetic constructors and methods of the given {@code ClassDoc}.
   *
   * @param classDoc the {@code ClassDoc} from which to extract constructors and methods
   * @return a list of {@code ExecutableMemberDoc}s representing the constructors and methods of
   *     {@code classDoc}
   */
  private List<ExecutableMemberDoc> getConstructorsAndMethods(ClassDoc classDoc) {
    List<ExecutableMemberDoc> members = new ArrayList<>();

    // Collect non-default constructors.
    for (ConstructorDoc constructor : classDoc.constructors(false)) {
      // This is a workaround to strange behavior of method Doc.position(). It does not return null
      // for default constructors. It instead returns the line number of the start of the class.
      SourcePosition position = constructor.position();
      if (position != null && !position.toString().equals(classDoc.position().toString())) {
        members.add(constructor);
      }
    }

    // Collect non-private non-synthetic methods (i.e. those methods that have not been synthesized
    // by the compiler).
    members.addAll(Arrays.asList(classDoc.methods(false)));

    return members
        .stream()
        .filter(m -> !m.isSynthetic() && !m.isPrivate())
        .collect(Collectors.toList());
  }

  //  /**
  //   * Returns the return type of the given {@code member}. Returns {@code null} if {@code member} is
  //   * a constructor.
  //   *
  //   * @param member the executable member (constructor or method) to return the return type
  //   * @return the return type of the given member or null if the member is a constructor
  //   */
  //  private org.toradocu.extractor.Type getReturnType(ExecutableMemberDoc member) {
  //    if (member instanceof MethodDoc) {
  //      MethodDoc method = (MethodDoc) member;
  //      Type returnType = method.returnType();
  //      return new org.toradocu.extractor.Type(
  //          returnType.qualifiedTypeName() + returnType.dimension());
  //    } else {
  //      return null;
  //    }
  //  }

  /**
   * Returns the {@code Parameter}s of the given constructor or method.
   *
   * @param member the constructor or method from which to extract parameters
   * @return an array of parameters
   */
  private List<Parameter> getParameters(ExecutableMemberDoc member) throws ClassNotFoundException {
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
      parameters[i] = new Parameter(Reflection.getClass(parameterType), params[i].name(), nullable);
    }
    return Arrays.asList(parameters);
  }

  //  private void loadParameters(java.lang.reflect.Parameter[] parameters,
  //      List<String> parameterNames) {
  //    for (int i = 0; i < parameters.length; i++) {
  //      java.lang.reflect.Parameter parameter = parameters[i];
  //      String paramName;
  //      if (parameterNames.isEmpty()) {
  //        if (parameter.isNamePresent()) {
  //          paramName = parameter.getName();
  //        } else {
  //          throw new IllegalArgumentException("Parameter names of executable " + executable +
  //              " cannot be loaded with reflection. Please provide parameter names using the "
  //              + "appropriate constructor ExecutableMember(String, List<String>, List<Tag>");
  //        }
  //      } else {
  //        paramName = parameterNames.get(i);
  //      }
  //      this.parameters.add(new Parameter(parameter.getType(), paramName));
  //    }
  //  }

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
  //  @SuppressWarnings("deprecation")
  private Class<?> getException(com.sun.javadoc.ThrowsTag throwsTag, ExecutableMemberDoc member)
      throws ClassNotFoundException {
    // We use deprecated method in Javadoc API. No alternative solution is documented.
    Type exceptionType = throwsTag.exceptionType();
    if (exceptionType != null) {
      return Reflection.getClass(exceptionType.qualifiedTypeName());
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
        return Class.forName(importedClass.qualifiedName());
      }
    }
    // If fully qualified exception's name cannot be collected from import statements, return the simple name
    return Class.forName(exceptionName);
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
      throws IOException, ClassNotFoundException {
    // list that will contain the throws tags
    List<Tag> throwsTags = new ArrayList<>();

    // Collect tags in the current method's documentation. This is needed because DocFinder.search
    // does not load tags of a method when the method overrides a superclass' method also
    // overwriting the Javadoc documentation.
    throwsTags.addAll(throwsTagsOf(member));

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
          new ThrowsTag(getException(throwsTag, member), comment, stringCodeTags);
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
      throws IOException, ClassNotFoundException {

    // List that will contain all the paramTags in the method.
    List<ParamTag> paramTags = new ArrayList<>();

    for (Tag tag : member.tags(Kind.PARAM.toString())) {
      if (!(tag instanceof com.sun.javadoc.ParamTag)) {
        throw new IllegalStateException(tag + " is not a @param tag. This should not happen.");
      }

      // We create a paramsTag that will be the cast of tag to ParamTag in order to work with it.
      com.sun.javadoc.ParamTag paramsTag = (com.sun.javadoc.ParamTag) tag;

      // Remove HTML tags (also generated by inline taglets). In the future, perhaps retain those
      // tags, because they contain information that can be exploited.
      String comment = Jsoup.parse(paramsTag.parameterComment()).text();

      //The list of the parameters of the method that we'll use in order to match
      //the parameterName of the tag with the parameter itself
      List<Parameter> parameters = getParameters(member);

      String name = null; //Name of the ParamTag that we'll introduce
      Class<?> type = null; //type of the parameter of the ParamTag
      Boolean nullable = null; // Nullability of the parameter.

      boolean found = false; //Boolean for control
      for (int i = 0; !found && i < parameters.size(); i++) {
        if (parameters.get(i).getName().equals(paramsTag.parameterName())) {
          // If the tag parameterName matches any parameter name in the method, then we stop the
          // loop.
          found = true;
          name = parameters.get(i).getName(); //then we assign values to the variables
          type = Reflection.getClass(parameters.get(i).getType().toString());
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
        paramTags.add(tagToProcess); //And then, we'll add it in the list we had before
      }
    }
    return paramTags;
  }

  /**
   * This method extracts the returnTag from the class we want.
   *
   * @param member the constructor or method from which to extract the return tags
   * @return the @return tag of the class if present, null otherwise
   * @throws IOException if the method encounters an error while reading/generating class
   *     documentation
   */
  private ReturnTag extractReturnTag(ExecutableMemberDoc member) throws IOException {
    final Tag[] returnTags = member.tags(Kind.RETURN.toString());
    if (returnTags.length > 1) {
      log.warn(
          "There are more than one @return tag in this method. Toradocu only considers the first one.");
    }

    Tag tag = returnTags[0];
    String comment = tag.text();
    /* taggedComment is the output of the Jsoup parsing: it can be exploited to extract text
     * contained in a certain tag. For now we're interested in <code> */
    Document taggedComment = Jsoup.parse(comment);

    // Remove HTML tags (also generated by inline taglets). In the future, perhaps retain those
    // tags, because they contain information that can be exploited.
    return new ReturnTag(taggedComment.text());
  }
}
