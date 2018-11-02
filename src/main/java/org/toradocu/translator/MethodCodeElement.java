package org.toradocu.translator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import org.toradocu.conf.Configuration;

/**
 * This class represents an instance method code element for use in translation. It holds String
 * identifiers for the instance method and a Java expression representation of the method to build
 * Java conditions.
 */
public class MethodCodeElement extends CodeElement<Method> {

  /** The name of the class/object on which this method is called. */
  private final String receiver;
  /** The actual parameters used to invoke this method. */
  private String[] parameters;
  /** The arguments of this method. */
  private String[] args;
  /** Expression that checks the nullness of the receiver. */
  private String nullDereferenceCheck;

  /**
   * Constructs and initializes a {@code MethodCodeElement} that identifies the given method. The
   * given method must take no arguments.
   *
   * @param receiver the class/object in which the method is called
   * @param method the no-argument method that this code element identifies
   */
  public MethodCodeElement(String receiver, Method method) {
    super(method);
    this.receiver = receiver;
    if (!receiver.equals(Configuration.RECEIVER)) {
      this.nullDereferenceCheck = "(" + receiver + "==null)==false";
    } else {
      this.nullDereferenceCheck = null;
    }
    // Add name identifier.
    String methodName = method.getName();
    if (methodName.startsWith("get")) {
      methodName = methodName.replaceFirst("get", "");
    }

    addIdentifier(methodName);

    if (method.getParameterCount() != 0) {
      String methodString = method.toGenericString();
      String methodArgs =
          methodString.substring(methodString.indexOf("(") + 1, methodString.indexOf(")"));
      if (method.getParameterCount() == 1) args = new String[] {methodArgs};
      else if (method.getParameterCount() > 1) {
        args = methodArgs.split(" *,");
        this.parameters = new String[args.length];
      }
    }
  }

  public String[] getArgs() {
    return args;
  }

  public String getReceiver() {
    return receiver;
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

  public String getNullDereferenceCheck() {
    return nullDereferenceCheck;
  }

  @Override
  boolean isCompatibleWith(Class<?> declaringClass, String predicateTranslation) {
    if (predicateTranslation.contains(".")) {
      // if the translation is a method invocation, it must be this subject
      String cleanPredicate = predicateTranslation.replaceAll("\\(", "").replaceAll("\\)", "");

      String cleanExpression = getJavaExpression().replaceAll("\\(", "").replaceAll("\\)", "");

      return cleanPredicate.startsWith(cleanExpression);
    }
    return true;
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
    StringJoiner paramsJoiner = new StringJoiner(", ", "(", ")");
    if (args != null) {
      Arrays.stream(args).forEach(paramsJoiner::add);
      return receiver + "." + getJavaCodeElement().getName() + paramsJoiner;
    } else return receiver + "." + getJavaCodeElement().getName() + "()";
  }
}
