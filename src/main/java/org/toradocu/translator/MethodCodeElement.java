package org.toradocu.translator;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This class represents an instance method code element for use in translation. It holds String
 * identifiers for the instance method and a Java expression representation of the method to build
 * Java conditions.
 */
public class MethodCodeElement extends CodeElement<Method> {

  /** The name of the class/object on which this method is called. */
  private final String receiver;

  private String[] parameters;

  /**
   * Constructs and initializes a {@code MethodCodeElement} that identifies the given method. The
   * given method must take no arguments.
   *
   * @param receiver the class/object in which the method is called
   * @param method the no-argument method that this code element identifies
   * @param parameters the actual parameters to use to invoke this method
   */
  public MethodCodeElement(String receiver, Method method, String... parameters) {
    super(method);
    this.receiver = receiver;
    this.parameters = parameters;

    // Add name identifier.
    String methodName = method.getName();
    if (methodName.startsWith("get")) {
      methodName = methodName.replaceFirst("get", "");
    }

    addIdentifier(methodName);
  }

  /**
   * Set the actual parameters to be used to invoke this method.
   *
   * @param parameters the actual parameters of this method
   * @throws NullPointerException if {@code parameters} is null
   */
  public void setParameters(List<String> parameters) {
    this.parameters = parameters.toArray(new String[0]);
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
