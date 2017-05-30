package org.toradocu.translator;

import java.lang.reflect.Parameter;
import java.util.Collection;
import org.toradocu.util.Checks;

public class ContainerElementsCodeElement extends CodeElement<Object> {

  /** The container whose elements this code element refers to */
  private final String container;

  /**
   * Constructs a new {@code ContainerElementsCodeElement} that represents the given element. The
   * Java expression is built using the given {@code container} string.
   *
   * @param javaCodeElement the element that this code element contains data on
   * @param container the container used to build the Java expression and that contains the elements
   *     this code element refers to
   * @throws IllegalArgumentException if {@code javaCodeElement} is not of a supported type
   */
  protected ContainerElementsCodeElement(Object javaCodeElement, String container) {
    super(javaCodeElement);
    if (!containerCheck(javaCodeElement)) {
      throw new IllegalArgumentException();
    }
    this.container = container;
  }

  @Override
  protected String buildJavaExpression() {
    return ""; // The java expression can be built only knowing the predicate translation.
  }

  /**
   * Returns the Java expression representation of this code element for use in building Java
   * conditions.
   *
   * @param predicate the predicate translation to use to build the Java expression
   * @return the Java expression representation of this code element
   * @throws NullPointerException if predicate is null
   */
  protected String getJavaExpression(String predicate) {
    Checks.nonNullParameter(predicate, "predicate");
    final Class type = getType();
    if (type.isArray()) {
      return "java.util.Arrays.stream(" + container + ").anyMatch(e -> e" + predicate + ")";
    }
    if (Class.class.isAssignableFrom(type)) {
      return "stream(" + container + ").anyMatch(e -> e" + predicate + ")";
    }
    return "";
  }

  /**
   * Returns true if {@code javaCodeElement} is a {@code java.lang.Parameter} of type array or
   * {@code java.lang.Collection}, false otherwise.
   *
   * @param javaCodeElement the java code element to check
   * @return true if {@code javaCodeElement} is a {@code java.lang.Parameter} of type array or
   *     {@code java.lang.Collection}, false otherwise
   */
  private boolean containerCheck(Object javaCodeElement) {
    if (javaCodeElement instanceof Parameter) {
      Parameter codeElement = (Parameter) javaCodeElement;
      Class type = codeElement.getType();
      if (type.isArray() || Collection.class.isAssignableFrom(type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the Class of the Java code element this object is wrapping.
   *
   * @return the Class of the Java code element this object is wrapping
   */
  private Class getType() {
    Object codeElement = getJavaCodeElement();
    if (codeElement instanceof Parameter) {
      return ((Parameter) codeElement).getType();
    }
    return null;
  }
}
