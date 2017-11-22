package org.toradocu.generator;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.toradocu.Toradocu;

/** Modifies TestCaseAspect.java adding the proper "within" declaration. */
public class JUnitTestCaseAspectChangerVisitor extends VoidVisitorAdapter<Void> {

  @Override
  public void visit(MethodDeclaration methodDeclaration, Void arg) {
    String testClass = Toradocu.configuration.getTestClass();
    if (methodDeclaration.getName().asString().equals("advice") && testClass != null) {
      for (AnnotationExpr annotation : methodDeclaration.getAnnotations()) {
        if (annotation instanceof SingleMemberAnnotationExpr) {
          final Expression annotationValue =
              ((SingleMemberAnnotationExpr) annotation).getMemberValue();
          if (annotationValue instanceof StringLiteralExpr) {
            StringLiteralExpr expr = (StringLiteralExpr) annotationValue;
            expr.setValue(expr.getValue() + " && within(" + testClass + ")");
          }
        }
      }
    }
  }
}
