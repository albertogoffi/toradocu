package org.toradocu.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import java.util.Optional;
import java.util.StringJoiner;
import org.apache.commons.lang3.tuple.Pair;
import org.toradocu.Toradocu;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.util.Checks;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.ThrowsSpecification;

/**
 * Visitor that modifies the aspect template (see method {@code visit}) to generate an aspect
 * (oracle) for a {@code ExecutableMember}.
 */
public class MethodChangerVisitor
    extends ModifierVisitor<Pair<DocumentedExecutable, OperationSpecification>> {

  /** Holds Toradocu configuration options. */
  private final Configuration conf = Toradocu.configuration;

  /**
   * Modifies the methods {@code advice} and {@code getExpectedExceptions} of the aspect template,
   * injecting the appropriate source code to get an aspect (oracle) for the method arg.
   *
   * @param methodDeclaration the method declaration of the method to visit
   * @param spec the {@code ExecutableMember} for which to generate the aspect (oracle)
   * @return the {@code methodDeclaration} modified as and when needed
   * @throws NullPointerException if {@code methodDeclaration} or {@code executableMember} is null
   */
  @Override
  public Node visit(
      MethodDeclaration methodDeclaration,
      Pair<DocumentedExecutable, OperationSpecification> spec) {
    Checks.nonNullParameter(methodDeclaration, "methodDeclaration");
    Checks.nonNullParameter(spec, "spec");

    DocumentedExecutable documentedExecutable = spec.getKey();
    OperationSpecification operationSpec = spec.getValue();

    switch (methodDeclaration.getName().asString()) {
      case "advice":
        adviceChanger(methodDeclaration, documentedExecutable);
        break;
      case "getExpectedExceptions":
        getExpectedExceptionChanger(methodDeclaration, documentedExecutable, operationSpec);
        break;
      case "paramTagsSatisfied":
        paramTagSatisfiedChanger(methodDeclaration, documentedExecutable, operationSpec);
        break;
      case "checkResult":
        checkResultChanger(methodDeclaration, documentedExecutable, operationSpec);
        break;
    }
    return methodDeclaration;
  }

  private void checkResultChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      OperationSpecification spec) {
    for (PostSpecification postSpecification : spec.getPostSpecifications()) {
      String guard = addCasting(postSpecification.getGuard().getConditionText(), executableMember);
      String property =
          addCasting(postSpecification.getProperty().getConditionText(), executableMember);
      String check = createBlock("if ((" + property + ")==false) { fail(\"Error!\"); }");
      IfStmt ifStmt = createIfStmt(guard, postSpecification.getDescription(), check);
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(ifStmt));
    }
    ReturnStmt returnResultStmt = new ReturnStmt(new NameExpr("result"));
    methodDeclaration.getBody().ifPresent(body -> body.addStatement(returnResultStmt));
  }

  private void paramTagSatisfiedChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      OperationSpecification specification) {
    boolean returnStmtNeeded = true;

    for (PreSpecification preSpecification : specification.getPreSpecifications()) {
      String condition = preSpecification.getGuard().getConditionText();
      if (condition.isEmpty()) {
        continue; // TODO Does it make sense to have empty guards here? We should avoid that.
      }
      condition = addCasting(condition, executableMember);
      String thenBlock = createBlock("return true;");
      String elseBlock = createBlock("return false;");
      IfStmt ifStmt =
          createIfStmt(condition, preSpecification.getDescription(), thenBlock, elseBlock);
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(ifStmt));
      returnStmtNeeded = false;
    }

    if (returnStmtNeeded) {
      ReturnStmt returnTrueStmt = new ReturnStmt(new BooleanLiteralExpr(true));
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(returnTrueStmt));
    }
  }

  private void getExpectedExceptionChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      OperationSpecification operationSpec) {

    for (ThrowsSpecification throwsSpecification : operationSpec.getThrowsSpecifications()) {
      String condition =
          addCasting(throwsSpecification.getGuard().getConditionText(), executableMember);

      IfStmt ifStmt = new IfStmt();
      Expression conditionExpression;
      conditionExpression = JavaParser.parseExpression(condition);
      ifStmt.setCondition(conditionExpression);
      // Add a try-catch block to prevent runtime error when looking for an exception type
      // that is not on the classpath.
      String addExpectedException =
          "{try{expectedExceptions.add("
              + "Class.forName(\""
              + throwsSpecification.getExceptionTypeName()
              + "\")"
              + ");} catch (ClassNotFoundException e) {"
              + "System.err.println(\"Class not found!\" + e);}}";
      ifStmt.setThenStmt(JavaParser.parseBlock(addExpectedException));

      // Add a try-catch block to avoid NullPointerException to be raised while evaluating a
      // boolean condition generated by Toradocu. For example, suppose that the first argument
      // of a method is null, and that Toradocu generates a condition like
      // args[0].isEmpty()==true. The condition generates a NullPointerException that we want
      // to ignore.
      ClassOrInterfaceType nullPointerException =
          JavaParser.parseClassOrInterfaceType("java.lang.NullPointerException");
      CatchClause catchClause =
          new CatchClause(new Parameter(nullPointerException, "e"), JavaParser.parseBlock("{}"));
      NodeList<CatchClause> catchClauses = new NodeList<>();
      catchClauses.add(catchClause);

      TryStmt nullCheckTryCatch = new TryStmt();
      nullCheckTryCatch.setTryBlock(JavaParser.parseBlock("{" + ifStmt.toString() + "}"));
      nullCheckTryCatch.setCatchClauses(catchClauses);

      // Add comment to if condition. The comment is the original comment in the Java source
      // code that has been translated by Toradocu in the commented boolean condition.
      // Comment has to be added here, cause otherwise is ignored by JavaParser.parseBlock.
      final Optional<Statement> ifCondition =
          nullCheckTryCatch
              .getTryBlock()
              .getStatements()
              .stream()
              .filter(stm -> stm instanceof IfStmt)
              .findFirst();
      if (ifCondition.isPresent()) {
        String comment = throwsSpecification.getDescription();
        ifCondition.get().setComment(new LineComment(comment));
      }

      methodDeclaration.getBody().ifPresent(body -> body.addStatement(nullCheckTryCatch));
    }

    methodDeclaration
        .getBody()
        .ifPresent(blockStmt -> blockStmt.addStatement("return expectedExceptions;"));
  }

  private void adviceChanger(
      MethodDeclaration methodDeclaration, DocumentedExecutable executableMember) {
    String pointcut;

    if (executableMember.isConstructor()) {
      pointcut = "execution(" + getPointcut(executableMember) + ")";
    } else {
      pointcut = "call(" + getPointcut(executableMember) + ")";
      String testClassName = conf.getTestClass();
      if (testClassName != null) {
        pointcut += " && within(" + testClassName + ")";
      }
    }

    AnnotationExpr annotation =
        new SingleMemberAnnotationExpr(new Name("Around"), new StringLiteralExpr(pointcut));
    NodeList<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
    annotations.add(annotation);
    methodDeclaration.setAnnotations(annotations);
  }

  private static String createBlock(String content) {
    return "{" + content + "}";
  }

  private static IfStmt createIfStmt(String condition, String comment, String thenBlock) {
    return createIfStmt(condition, comment, thenBlock, "");
  }

  private static IfStmt createIfStmt(
      String condition, String comment, String thenBlock, String elseBlock) {
    IfStmt ifStmt = new IfStmt();
    Expression conditionExpression;
    conditionExpression = JavaParser.parseExpression(condition);
    ifStmt.setCondition(conditionExpression);
    ifStmt.setThenStmt(JavaParser.parseBlock(thenBlock));
    if (!elseBlock.isEmpty()) {
      ifStmt.setElseStmt(JavaParser.parseBlock(elseBlock));
    }
    ifStmt.setComment(new LineComment(" " + comment));
    return ifStmt;
  }

  /**
   * Generates the AspectJ pointcut definition to be used to match the given {@code
   * ExecutableMember}. A pointcut definition looks like {@code call(void C.foo())}. Given a {@code
   * ExecutableMember} describing the method C.foo(), this method returns the string {@code
   * call(void C.foo())}.
   *
   * @param executable {@code ExecutableMember} for which to generate the pointcut definition
   * @return the pointcut definition matching {@code method}
   */
  private static String getPointcut(DocumentedExecutable executable) {
    StringBuilder pointcut = new StringBuilder();

    if (executable.isConstructor()) { // Constructors
      pointcut.append(executable.getDeclaringClass()).append(".new(");
    } else { // Regular methods
      pointcut
          .append(executable.getReturnType().getType())
          .append(" ")
          .append(executable.getDeclaringClass().getName())
          .append(".")
          .append(executable.getName());
    }

    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    for (DocumentedParameter documentedParameter : executable.getParameters()) {
      joiner.add(documentedParameter.getType().getName());
    }
    pointcut.append(joiner.toString());
    return pointcut.toString();
  }

  /**
   * Add the appropriate cast to each mention of a method argument or target in a given Java boolean
   * condition.
   *
   * @param condition the Java boolean condition to which add the casts
   * @param method the method to which the {@code condition} belongs
   * @return the input condition with casted method arguments and target
   * @throws NullPointerException if {@code condition} or {@code method} is null
   */
  private static String addCasting(String condition, DocumentedExecutable method) {
    Checks.nonNullParameter(condition, "condition");
    Checks.nonNullParameter(method, "method");

    int index = 0;
    for (DocumentedParameter parameter : method.getParameters()) {
      String type = parameter.getType().getSimpleName();
      condition = condition.replace("args[" + index + "]", "((" + type + ") args[" + index + "])");
      index++;
    }

    // Casting of result object in condition.
    String returnType = method.getReturnType().getType().toString();
    if (returnType != null && !returnType.equals("void")) {
      condition = condition.replace("result", "((" + returnType + ") result)");
    }

    // Casting of target object in condition.
    condition = condition.replace("target.", "((" + method.getDeclaringClass() + ") target).");
    return condition;
  }
}
