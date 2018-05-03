package org.toradocu.translator;

import org.apache.commons.lang3.ClassUtils;
import org.toradocu.conf.Configuration;

/**
 * This class represents a {@code Class} code element for use in translation. It holds String
 * identifiers (words or sequences or words that can be used in Javadoc comments to refer to this
 * Java code element) for the class and a Java expression representation of the class to build Java
 * conditions.
 */
public class ClassCodeElement extends CodeElement<Class<?>> {

  /**
   * Constructs and initializes a {@code ClassCodeElement} that identifies the given class.
   *
   * @param backingClass the backing class that this code element identifies
   */
  public ClassCodeElement(Class<?> backingClass) {
    super(backingClass);

    // Add class name as identifier.
    String className = backingClass.getSimpleName();
    addIdentifier(className);
    addIdentifier("this " + className);
    addIdentifier("this");
    // Add last word in class name as identifier.
    for (int i = className.length() - 1; i > 0; i--) {
      if (Character.isUpperCase(className.charAt(i))) {
        addIdentifier(className.substring(i));
        break;
      }
    }

    // Add implemented interfaces as identifiers.
    for (Class<?> implementedInterface : ClassUtils.getAllInterfaces(backingClass)) {
      addIdentifier(implementedInterface.getSimpleName());
    }
  }

  /**
   * Builds and returns the string representing the receiver object as the Java expression
   * representation of the class code element.
   *
   * @return the string representing the receiver object as the Java expression representation of
   *     the class code element
   */
  @Override
  public String buildJavaExpression() {
    return Configuration.RECEIVER;
  }
}
