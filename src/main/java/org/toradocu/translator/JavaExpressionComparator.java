package org.toradocu.translator;

import java.util.Comparator;

public class JavaExpressionComparator implements Comparator<CodeElement<?>> {
  @Override
  public int compare(CodeElement<?> arg0, CodeElement<?> arg1) {
    return arg1.getJavaExpression().compareTo(arg0.getJavaExpression());
  }
}
