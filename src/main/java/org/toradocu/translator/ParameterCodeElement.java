package org.toradocu.translator;

import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.StringJoiner;
import org.toradocu.conf.Configuration;

/**
 * This class represents a parameter code element for use in translation. It holds String
 * identifiers for the parameter and a Java expression representation of the parameter to build Java
 * conditions.
 */
public class ParameterCodeElement extends CodeElement<Parameter> {

  /** The 0-based index of this parameter in its associated method's parameter list. */
  private int index;

  /** Additional identifiers coming directly from param comments. */
  private Set<String> extractedIdentifiers;
  /**
   * Constructs and initializes a {@code ParameterCodeElement} that identifies the given parameter.
   *
   * @param parameter the backing parameter that this code element identifies
   * @param name the name of the parameter
   * @param index the 0-based index of the parameter in the parameter list of its associated method
   * @param extractedIds the additional IDs extracted from the param comment
   */
  public ParameterCodeElement(
      Parameter parameter, String name, int index, Set<String> extractedIds) {
    super(parameter);
    this.index = index;
    this.extractedIdentifiers = extractedIds;

    // Add name identifiers.
    addIdentifier("parameter");
    addIdentifier("argument");
    addIdentifier("param");
    addIdentifier(name);
    addIdentifier(parameter.getType().getSimpleName() + " " + name);
    addIdentifier(name + " " + parameter.getType().getSimpleName());
    // Add name identifier splitting camel case name into different words. We consider as
    // identifier the single words, and a string composed by the words separated by whitespace.
    StringJoiner joiner = new StringJoiner(" ");
    for (String word : name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
      addIdentifier(word);
      joiner.add(word);
    }
    addIdentifier(joiner.toString().toLowerCase());

    // Add type identifiers
    if (parameter.getType().isArray()) {
      addIdentifier("array");
      addIdentifier(parameter.getType().getSimpleName() + " array");
    } else {
      addIdentifier(parameter.getType().getSimpleName());
      if (parameter.getType().getName().equals("java.lang.Iterable")) {
        addIdentifier("iterator");
        addIdentifier("collection");
      }
    }
  }

  /**
   * Returns the additional identifiers extractedIdentifiers.
   *
   * @return a list of strings that identify this code element
   */
  public Set<String> getOtherIdentifiers() {
    return extractedIdentifiers;
  }

  /**
   * Remove a string identifier from extractedIdentifiers.
   *
   * @param identifier a string that identifies this code element
   */
  public void removeIdentifier(String identifier) {
    extractedIdentifiers.remove(identifier);
  }

  /** Merge the default identifiers with the additional ones extractedIdentifiers. */
  public void mergeIdentifiers() {
    getIdentifiers().addAll(extractedIdentifiers);
  }
  /**
   * Builds and returns the Java expression representation of this parameter code element. The
   * returned string is formatted as "args[i]" where i is the index of this parameter in a parameter
   * list.
   *
   * @return the Java expression representation of this parameter code element after building it
   */
  @Override
  public String buildJavaExpression() {
    return "args[" + index + "]";
  }

  @Override
  boolean isCompatibleWith(Class<?> declaringClass, String predicateTranslation) {
    final Parameter subject = getJavaCodeElement();
    final Class<?> subjectType = subject.getType();

    // Comparison with receiver object
    if (predicateTranslation.equals("==" + Configuration.RECEIVER)) {
      return subjectType.equals(declaringClass);
    }
    // Boolean or boolean
    if (subjectType.equals(boolean.class) || subjectType.equals(Boolean.class)) {
      return predicateTranslation.equals("==true") || predicateTranslation.equals("==false");
    }

    // Primitives and their wrappers (boolean excluded)
    if (subjectType.isPrimitive()) {
      return predicateTranslation.startsWith("<")
          || predicateTranslation.startsWith("<=")
          || predicateTranslation.startsWith(">")
          || predicateTranslation.startsWith(">=")
          || (predicateTranslation.startsWith("==") && !predicateTranslation.equals("==null"));
    }
    if (subjectType.equals(Byte.class)
        || subjectType.equals(Short.class)
        || subjectType.equals(Integer.class)
        || subjectType.equals(Long.class)
        || subjectType.equals(Float.class)
        || subjectType.equals(Double.class)) {
      return predicateTranslation.startsWith("<")
          || predicateTranslation.startsWith("<=")
          || predicateTranslation.startsWith(">")
          || predicateTranslation.startsWith(">=")
          || predicateTranslation.startsWith("==")
          || predicateTranslation.contains(".equals(");
    }

    // Non-primitives
    if (!subjectType.isPrimitive()
        && !predicateTranslation.equals("==null")
        && (predicateTranslation.startsWith("==")
            || predicateTranslation.startsWith("<")
            || predicateTranslation.startsWith("<=")
            || predicateTranslation.startsWith(">")
            || predicateTranslation.startsWith(">="))) {
      return false;
    }

    return true;
  }
}
