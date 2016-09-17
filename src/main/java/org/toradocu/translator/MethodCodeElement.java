package org.toradocu.translator;

import java.lang.reflect.Method;

/**
 * This class represents an instance method code element for use in translation. It holds String identifiers
 * for the instance method and a Java expression representation of the method to build Java conditions.
 */
public class MethodCodeElement extends CodeElement<Method> {

  /** The name of the class/object on which this method is called. */
  private final String receiver;

  /**
   * Constructs and initializes a {@code MethodCodeElement} that identifies the given method. The
   * given method must take no arguments.
   *
   * @param receiver the class/object in which the method is called
   * @param method the no-argument method that this code element identifies
   * @throws IllegalArgumentException if method takes arguments
   */
  public MethodCodeElement(String receiver, Method method) {
    super(method);
    if (method.getParameterCount() > 0) {
      throw new IllegalArgumentException(
          "Method '"
              + method.getName()
              + "' has "
              + method.getParameterCount()
              + " parameters. Expected 0.");
    }
    this.receiver = receiver;

    // Add name identifier.
    String methodName = method.getName();
    if (methodName.startsWith("get")) {
      methodName = methodName.replaceFirst("get", "");
    }

    addIdentifier(methodName);
  }

  /**
   * Builds and returns the Java expression representation of this method code element. The returned
   * string is formatted as "RECEIVER.METHOD_NAME" where RECEIVER is the name of the class on which
   * this method is called and METHOD_NAME is the name of this method.
   *
   * @return the Java expression representation of this method code element after building it
   */
  @Override
  protected String buildJavaExpression() {
    return receiver + "." + getJavaCodeElement().getName() + "()";
  }
}
