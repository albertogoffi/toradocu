package org.toradocu.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.util.Checks;

/**
 * Visitor that modifies the evaluator template (see method {@code visit}) to generate an evaluator
 * for a {@code ExecutableMember}.
 */
public class EvaluatorModifierVisitor
    extends ModifierVisitor<Pair<DocumentedExecutable, String>> {

  /**
   * Modifies the methods {@code populateCalculators} of the evaluator template,
   * injecting the appropriate source code to get an evaluator for the method.
   *
   * @param methodDeclaration the method declaration of the method to visit
   * @param spec the {@code ExecutableMember} for which to generate the evaluator
   * @return the {@code methodDeclaration} modified as and when needed
   * @throws NullPointerException if {@code methodDeclaration} or {@code executableMember} is null
   */
  @Override
  public Node visit(
      MethodDeclaration methodDeclaration,
      Pair<DocumentedExecutable, String> spec) {
    Checks.nonNullParameter(methodDeclaration, "methodDeclaration");
    Checks.nonNullParameter(spec, "spec");

    if (methodDeclaration.getName().asString().equals("populateCalculators")) {

    	DocumentedExecutable documentedExecutable = spec.getKey();
    	String guardString = spec.getValue();
    	String guardStringWithCasting = addCasting(guardString, documentedExecutable);
    	addEvaluators(methodDeclaration, JavaParser.parseExpression(guardStringWithCasting));
    }
    return methodDeclaration;
  }
  
  private void addEvaluators(MethodDeclaration methodDeclaration, Expression... conditions) {
	  for (Expression cond: conditions) {
		  Expression condDistance = turnConditionToConditionDistance(cond, false);
		  String expressionString = ""
				+ 
				"calculators.add(new ValueCalculator() {\n" + 
				"    boolean condition()  {\n" + 
				"        return " + cond + ";\n" + 
				"    }\n" + 
				"    double cdistance()  {\n" + 
				"        return " + condDistance + ";\n" +
				"    }\n" + 
				"});";  
		  methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.addStatement(JavaParser.parseStatement(expressionString)));
	  }
  }
  
  private Expression turnConditionToConditionDistance(Expression cond, boolean negate) {
	  if (cond.isEnclosedExpr()) {
		  EnclosedExpr enclosed = cond.asEnclosedExpr();
		  return new EnclosedExpr(turnConditionToConditionDistance(enclosed.getInner(), negate));		  
	  } else if (cond.isBinaryExpr()) {
		  BinaryExpr binaryCond = cond.asBinaryExpr();
		  Expression left = binaryCond.getLeft();
		  Expression right = binaryCond.getRight();
		  Operator op = binaryCond.getOperator();
		  BooleanLiteralExpr FALSE = new BooleanLiteralExpr(false);
		  if (right.isBooleanLiteralExpr()) {
			  return turnConditionToConditionDistance(left, negate ? !right.equals(FALSE) : right.equals(FALSE));
		  } else if (left.isBooleanLiteralExpr()) {
			  return turnConditionToConditionDistance(right, negate ? !left.equals(FALSE) : left.equals(FALSE));
		  } else if (op.equals(Operator.AND) || op.equals(Operator.OR)) {
			  Expression turnedL = turnConditionToConditionDistance(left, negate);
			  Expression turnedR = turnConditionToConditionDistance(right, negate);
			  Operator turnedOp = op.equals(Operator.AND) ?
					  (negate ? Operator.MULTIPLY : Operator.PLUS) :
						  (negate ? Operator.PLUS : Operator.MULTIPLY);				
			  return new EnclosedExpr(new BinaryExpr(turnedL, turnedR, turnedOp));
		  } else {
			  Operator turnedOp = op;
			  if (negate) {
				  switch (op) {
				  case EQUALS: turnedOp = Operator.NOT_EQUALS; break;
				  case NOT_EQUALS: turnedOp = Operator.EQUALS; break;
				  case GREATER: turnedOp = Operator.LESS_EQUALS; break;
				  case GREATER_EQUALS: turnedOp = Operator.LESS; break;
				  case LESS: turnedOp = Operator.GREATER_EQUALS; break;
				  case LESS_EQUALS: turnedOp = Operator.GREATER; break;
				  default: throw new RuntimeException("Unexpected comparison op in: " + cond);
				  }
			  }
			  BinaryExpr turnedCond = new BinaryExpr(left, right, turnedOp);
			  boolean useUnitaryDistance = turnedOp == Operator.NOT_EQUALS || 
					  (turnedOp == Operator.EQUALS && (left.isNullLiteralExpr() || right.isNullLiteralExpr()));
			  if (!useUnitaryDistance) {
				  BinaryExpr distance = new BinaryExpr(left, right, Operator.MINUS);
				  return new EnclosedExpr(new ConditionalExpr(turnedCond, new DoubleLiteralExpr(0), distance));
			  } else {
				  return new EnclosedExpr(new ConditionalExpr(turnedCond, new DoubleLiteralExpr(0), new DoubleLiteralExpr(1)));				  
			  }
		  }
	  } else {
		  Expression turnedCond; 
		  if (!negate) {
			  turnedCond = cond;
		  } else {
			  turnedCond = JavaParser.parseExpression("!(" + cond + ")");			  
		  }
		  return new EnclosedExpr(new ConditionalExpr(turnedCond, new DoubleLiteralExpr(0), new DoubleLiteralExpr(1)));
	  }
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
      String type = parameter.getType().getTypeName();
      type = removeParametersFromType(type);
      condition = condition.replace("args[" + index + "]", "((" + type + ") args[" + index + "])");
      index++;
    }

    // Casting of result object in condition.
    String returnType = method.getReturnType().getType().getTypeName();
    returnType = removeParametersFromType(returnType);
    if (!returnType.equals("void")) {
      condition =
          condition.replace(
              Configuration.RETURN_VALUE,
              "((" + returnType + ")" + Configuration.RETURN_VALUE + ")");
    }

    // Casting of target object in condition.
    String type = method.getDeclaringClass().getName();
    type = removeParametersFromType(type);
    condition =
        condition.replace(
            Configuration.RECEIVER, "((" + type + ") " + Configuration.RECEIVER + ")");
    return condition;
  }

  /**
   * Remove the type parameter(s) from a generic type and returns the resulting string.
   *
   * @param genericType the generic type to be cleaned
   * @return the type without parameter(s)
   */
  @NotNull
  private static String removeParametersFromType(String genericType) {
    int generics = genericType.indexOf("<");
    if (generics != -1) {
      return genericType.substring(0, generics);
    }
    return genericType;
  }
}
