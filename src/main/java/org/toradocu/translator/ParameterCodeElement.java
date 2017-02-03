package org.toradocu.translator;

import java.lang.reflect.Parameter;

/**
 * This class represents a parameter code element for use in translation. It holds String
 * identifiers for the parameter and a Java expression representation of the parameter to build Java
 * conditions.
 */
public class ParameterCodeElement extends CodeElement<Parameter> {

  /** The 0-based index of this parameter in its associated method's parameter list. */
  private int index;

  /**
   * Constructs and initializes a {@code ParameterCodeElement} that identifies the given parameter.
   *
   * @param parameter the backing parameter that this code element identifies
   * @param name the name of the parameter
   * @param index the 0-based index of the parameter in the parameter list of its associated method
   */
  public ParameterCodeElement(Parameter parameter, String name, int index) {
    super(parameter);
    this.index = index;

    // Add name identifiers.
    addIdentifier("parameter");
    addIdentifier("argument");
    addIdentifier("param");
    addIdentifier(name);
    addIdentifier(parameter.getType().getSimpleName() + " " + name);
    addIdentifier(name + " " + parameter.getType().getSimpleName());
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
  boolean isCompatibleWith(String predicateTranslation) {
    final Parameter subject = getJavaCodeElement();
    final Class<?> subjectType = subject.getType();

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
          || predicateTranslation.startsWith("==");
    }

    // Non-primitives
    if (!subjectType.isPrimitive()
        && !predicateTranslation.equals("==null")
        && !predicateTranslation.equals("==target")
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
