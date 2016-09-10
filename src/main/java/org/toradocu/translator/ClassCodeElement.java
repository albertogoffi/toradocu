package org.toradocu.translator;

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
    // Add last word in class name as identifier.
    for (int i = className.length() - 1; i > 0; i--) {
      if (Character.isUpperCase(className.charAt(i))) {
        addIdentifier(className.substring(i));
        break;
      }
    }

    // Add implemented interfaces as identifiers.
    // TODO: This only account for the directly implemented interfaces. It omits their
    // superinterfaces, and interfaces implemented by superclasses.
    for (Class<?> implementedInterface : backingClass.getInterfaces()) {
      addIdentifier(implementedInterface.getSimpleName());
    }
  }

  /**
   * Builds and returns the string "target" as the Java expression representation of this parameter
   * code element.
   *
   * @return the string "target" as the Java expression representation of this parameter code
   * element
   */
  @Override
  public String buildJavaExpression() {
    return "target";
  }
}
