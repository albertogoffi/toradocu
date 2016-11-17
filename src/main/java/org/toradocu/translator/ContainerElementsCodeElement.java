package org.toradocu.translator;

import java.lang.reflect.Parameter;
import java.util.Collection;
import org.toradocu.util.Checks;

public class ContainerElementsCodeElement extends CodeElement {

  private final String expression;

  protected ContainerElementsCodeElement(Object javaCodeElement, String expression) {
    super(javaCodeElement);
    if (!containerCheck(javaCodeElement)) {
      throw new IllegalArgumentException();
    }
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

  @Override
  protected String buildJavaExpression() {
    return ""; // The java expression can be built only knowing the predicate translation.
  }

  protected String getJavaExpression(String predicate) {
    Checks.nonNullParameter(predicate, "predicate");
    final Class type = getType();
    if (type.isArray()) {
      return "java.util.Arrays.stream(" + expression + ").anyMatch(e -> e" + predicate + ")";
    }
    if (Class.class.isAssignableFrom(type)) {
      return "stream(" + expression + ").anyMatch(e -> e" + predicate + ")";
    }
    return "";
  }

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

  private Class getType() {
    Object codeElement = getJavaCodeElement();
    if (codeElement instanceof Parameter) {
      return ((Parameter) codeElement).getType();
    }
    return null;
  }
}
