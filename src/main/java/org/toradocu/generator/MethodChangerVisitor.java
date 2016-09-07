package org.toradocu.generator;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;

public class MethodChangerVisitor extends VoidVisitorAdapter<Object> {

  /** {@code DocumentedMethod} for which generate an aspect */
  private DocumentedMethod method;
  /** {@code Logger} for this class. */
  private static final Logger LOG = LoggerFactory.getLogger(MethodChangerVisitor.class);
  /** Holds Toradocu configuration options. */
  private final Configuration conf = Toradocu.configuration;

  public MethodChangerVisitor(DocumentedMethod method) {
    this.method = method;
  }

  @Override
  public void visit(MethodDeclaration n, Object arg) {
    if (n.getName().equals("advice")) {
      String pointcut = "";

      if (method.isConstructor()) {
        pointcut = "execution(" + getPointcut(method) + ")";
      } else {
        pointcut = "call(" + getPointcut(method) + ")";
        pointcut += " && within(" + conf.getTestClass() + ")";
      }

      AnnotationExpr annotation =
          new SingleMemberAnnotationExpr(new NameExpr("Around"), new StringLiteralExpr(pointcut));
      List<AnnotationExpr> annotations = n.getAnnotations();
      annotations.add(annotation);
      n.setAnnotations(annotations);
    } else if (n.getName().equals("getExpectedExceptions")) {
      String addExpectedExceptionPart1 = "{try{expectedExceptions.add(";
      String addExpectedExceptionPart2 =
          ");} catch (ClassNotFoundException e) {"
              + "System.err.println(\"Class not found!\" + e);}}";

      for (ThrowsTag tag : method.throwsTags()) {
        String condition = tag.getCondition().orElse("");
        if (condition.isEmpty()) {
          continue;
        }

        condition = addCasting(condition);

        IfStmt ifStmt = new IfStmt();
        Expression conditionExpression;
        try {
          conditionExpression = JavaParser.parseExpression(condition);
          ifStmt.setCondition(conditionExpression);
          String addExpectedException =
              addExpectedExceptionPart1
                  + "Class.forName(\""
                  + tag.exception()
                  + "\")"
                  + addExpectedExceptionPart2;
          ifStmt.setThenStmt(JavaParser.parseBlock(addExpectedException));

          ClassOrInterfaceType exceptionType =
              new ClassOrInterfaceType("java.lang.NullPointerException");
          List<com.github.javaparser.ast.type.Type> types = new ArrayList<>();
          types.add(exceptionType);
          CatchClause catchClause =
              new CatchClause(
                  0, null, types, new VariableDeclaratorId("e"), JavaParser.parseBlock("{}"));
          List<CatchClause> catchClauses = new ArrayList<>();
          catchClauses.add(catchClause);

          TryStmt nullCheckTryCatch = new TryStmt();
          nullCheckTryCatch.setTryBlock(JavaParser.parseBlock("{" + ifStmt.toString() + "}"));
          nullCheckTryCatch.setCatchs(catchClauses);

          ASTHelper.addStmt(n.getBody(), nullCheckTryCatch);
        } catch (ParseException e) {
          LOG.error("Parsing error during the apect creation.", e);
          e.printStackTrace();
        }
      }

      try {
        ASTHelper.addStmt(n.getBody(), JavaParser.parseStatement("return expectedExceptions;"));
      } catch (ParseException e) {
        LOG.error("Parsing error during the apect creation.", e);
        e.printStackTrace();
      }
    }
  }

  /**
   * Generates the AspectJ pointcut definition to be used to match the given
   * {@code DocumentedMethod}. A pointcut definition looks like {@code call(void C.foo()}. Given a
   * {@code DocumentedMethod} describing the method C.foo(), this method returns the string
   * {@code call(void C.foo())}.
   *
   * @param method {@code DocumentedMethod} for which generate the pointcut definition
   * @return the pointcut definition matching {@code method}
   */
  private String getPointcut(DocumentedMethod method) {
    StringBuilder pointcut = new StringBuilder();

    if (!method.isConstructor()) { // Regular methods
      pointcut.append(method.getReturnType() + " " + method.getName() + "(");
    } else { // Constructors
      pointcut.append(method.getContainingClass() + ".new(");
    }

    Iterator<Parameter> parametersIterator = method.getParameters().iterator();
    while (parametersIterator.hasNext()) {
      Parameter parameter = parametersIterator.next();
      pointcut.append(parameter.getType());
      if (parametersIterator.hasNext()) {
        pointcut.append(", ");
      }
    }

    pointcut.append(")");
    return pointcut.toString();
  }

  private String addCasting(String condition) {
    int index = 0;
    for (Parameter parameter : method.getParameters()) {
      String type = parameter.getType().getQualifiedName();
      condition = condition.replace("args[" + index + "]", "((" + type + ") args[" + index + "])");
      index++;
    }

    // Casting of target object in check
    condition = condition.replace("target.", "((" + method.getContainingClass() + ") target).");
    return condition;
  }
}
