package org.toradocu.translator;

import java.lang.reflect.Method;

/**
 * This class represents a static method code element for use in translation. It holds String
 * identifiers for the static method and a Java expression representation of the method to build
 * Java conditions.
 */
public class StaticMethodCodeElement extends CodeElement<Method> {

  /**
   * Actual parameter values used to build the Java expression corresponding to the invocation of
   * this static method
   */
  private final String[] parameters;

  /**
   * Constructs and initializes a {@code StaticMethodCodeElement} that identifies the given static
   * method. The given method must have one parameter of the same type as the method's containing
   * class.
   *
   * @param method the static method that this code element identifies. Must have one parameter of
   *     the same type as the method's containing class.
   * @param parameters actual parameter values used to build the Java expression corresponding to
   *     the invocation of this static method
   * @throws IllegalArgumentException if method takes a number of parameters different than the
   *     specified parameters
   */
  public StaticMethodCodeElement(Method method, String... parameters) {
    super(method);
    this.parameters = parameters;
    if (method.getParameterCount() != parameters.length) {
      throw new IllegalArgumentException(
          "Method '"
              + method.getName()
              + "' has "
              + method.getParameterCount()
              + " parameters. Expected "
              + parameters.length);
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
   * string is formatted as "CLASS.METHOD_NAME(parameter1, parameter2, ...)" where CLASS is the name
   * of the class on which this method is called and METHOD_NAME is the name of this method.
   *
   * @return the Java expression representation of this static method code element after building it
   */
  @Override
  protected String buildJavaExpression() {
    final Method method = getJavaCodeElement();
    String javaExpression = method.getDeclaringClass().getName() + "." + method.getName() + "(";

    for (String parameter : parameters) {
      javaExpression += parameter + ", ";
    }
    javaExpression = javaExpression.substring(0, javaExpression.length() - 2);
    return javaExpression + ")";
  }
}
