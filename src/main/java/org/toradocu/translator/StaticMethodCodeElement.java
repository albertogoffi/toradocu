package org.toradocu.translator;

import java.lang.reflect.Method;

/**
 * This class represents a static method code element for use in translation. It holds String identifiers
 * for the static method and a Java expression representation of the method to build Java conditions.
 */
public class StaticMethodCodeElement extends CodeElement<Method> {

  /**
   * Constructs and initializes a {@code StaticMethodCodeElement} that identifies the given static method. The
   * given method must have one parameter of the same type as the method's containing class.
   *
   * @param method the static method that this code element identifies. Must have one parameter of the same type as the method's containing class.
   * @throws IllegalArgumentException if method does not have only one parameter or if the parameter type is not the method's containing class
   */
  public StaticMethodCodeElement(Method method) {
    super(method);
    if (method.getParameterCount() != 1) {
      throw new IllegalArgumentException(
          "Method '"
              + method.getName()
              + "' has "
              + method.getParameterCount()
              + " parameters. Expected 1.");
    } else if (!method.getParameterTypes()[0].equals(method.getDeclaringClass())) {
      throw new IllegalArgumentException(
          "Method '"
              + method.getName()
              + "' has incorrect parameter type: "
              + method.getParameterTypes()[0]);
    }

    // Add name identifier.
    String methodName = method.getName();
    if (methodName.startsWith("get")) {
      methodName = methodName.replaceFirst("get", "");
    }

    addIdentifier(methodName);
  }

  /**
   * Builds and returns the Java expression representation of this method code element. The returned
   * string is formatted as "CLASS.METHOD_NAME(target)" where CLASS is the name of the class on which
   * this method is called and METHOD_NAME is the name of this method.
   *
   * @return the Java expression representation of this static method code element after building it
   */
  @Override
  protected String buildJavaExpression() {
    return getJavaCodeElement().getDeclaringClass().getName()
        + "."
        + getJavaCodeElement().getName()
        + "(target)";
  }
}
