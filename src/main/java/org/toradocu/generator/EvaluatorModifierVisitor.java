package org.toradocu.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import java.lang.reflect.Modifier;

import org.jetbrains.annotations.NotNull;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.util.Checks;

/**
 * Visitor that modifies the evaluator template (see method {@code visit}) to generate an evaluator
 * for a {@code ExecutableMember}.
 */
public class EvaluatorModifierVisitor extends ModifierVisitor<Object> {

	public static final String RECEIVEROBJECT_FIELD = "___INTERNAL__receiverObjectID__";
	public static final String ARGS_FIELD = "___INTERNAL__args__";
	public static final String RETVAL_FIELD = "___INTERNAL__retVal_";

	public static class InstrumentationData {
		private final DocumentedExecutable method;
		private final String[] preconds;
		private final String[] postconds;
		private final boolean lookForPostCondViolation;
		InstrumentationData(DocumentedExecutable method, String[] preconds, String[] postconds, boolean lookForPostCondViolation) {
			Checks.nonNullParameter(method, "method");
			Checks.nonNullParameter(preconds, "preconds");
			Checks.nonNullParameter(postconds, "postconds");

			this.method = method;
			this.preconds = preconds;
			this.postconds = postconds;
			this.lookForPostCondViolation = lookForPostCondViolation;
		}
	}

	/**
	 * Modifies the methods {@code populateCalculators} of the evaluator template,
	 * injecting the appropriate source code to get an evaluator for the method.
	 *
	 * @param methodDeclaration the method declaration of the method to visit
	 * @param method the {@code ExecutableMember} for which to generate the evaluator
	 * @return the {@code methodDeclaration} modified as and when needed
	 * @throws NullPointerException if {@code methodDeclaration} or {@code executableMember} is null
	 */
	@Override
	public Node visit(MethodDeclaration methodDeclaration, Object data) {
		Checks.nonNullParameter(methodDeclaration, "methodDeclaration");
		Checks.nonNullParameter(data, "data");
		if (!(data instanceof InstrumentationData)) {
			throw new ClassCastException("expecting data of type InstrumentationData");
		}
		InstrumentationData instrumentationData = (InstrumentationData) data;
		String methodName = methodDeclaration.getName().asString();
		if (methodName.equals("populateCalculators_preconds")) {
			addConditionDistanceCalculators(methodDeclaration, instrumentationData.method, instrumentationData.preconds, false);    		
		} else if (methodName.equals("populateCalculators_postconds") && instrumentationData.postconds.length > 0) {
			if (instrumentationData.postconds.length == 1 && instrumentationData.postconds[0].endsWith("Exception")) {
				addExceptionDistanceCalculator(methodDeclaration, instrumentationData.method, instrumentationData.postconds[0], instrumentationData.lookForPostCondViolation);    		
			} else {
				addConditionDistanceCalculators(methodDeclaration, instrumentationData.method, instrumentationData.postconds, instrumentationData.lookForPostCondViolation);    		
			}
		} else if (methodName.equals("test0") || methodName.equals("test1")) {
			// Set the correct input parameters for the newly created evaluator
			if (!Modifier.isStatic(instrumentationData.method.getExecutable().getModifiers())) {
				String type = "java.lang.Object";
				String name = "___receiver__object___";
				methodDeclaration.addParameter(type, name);
				Statement stmt = StaticJavaParser.parseStatement(RECEIVEROBJECT_FIELD + " = " + name + ";");
				methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.addStatement(0, stmt));
			}

			String allNames = "";
			int numArgs = instrumentationData.method.getParameters().size();
			for (int i = 0; i < numArgs; ++i) {
				String type = "java.lang.Object";
				String name = "___arg" + i + "__object___";
				methodDeclaration.addParameter(type, name);
				allNames += (allNames.isEmpty() ? "" : ", ") + name;
			}
			Statement stmt = StaticJavaParser.parseStatement(ARGS_FIELD + " = new Object[] {" + allNames + "};");
			methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.addStatement(0, stmt));
		} else if (methodName.equals("toString")) { 
			Statement stmt = StaticJavaParser.parseStatement("s += \"" + 
					(instrumentationData.preconds.length > 0 ? instrumentationData.preconds[0] : "true") + " ---> " +
					(instrumentationData.postconds.length > 0 ? instrumentationData.postconds[0] : "true") + "\";");
			methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.addStatement(1, stmt));			
		}
		return methodDeclaration;
	}

	private void addExceptionDistanceCalculator(MethodDeclaration methodDeclaration, DocumentedExecutable spec, String exceptionCanonicalName, boolean lookForFailure) {
		String typeOfCalculator = lookForFailure ? "NegExceptionDistanceCalculator" : "ExceptionDistanceCalculator";
		String code = "algo.calculators.add(new " + typeOfCalculator + "() {\n" + 
				"    String exceptionCanonicalName()  {\n" + 
				"        return \"" + exceptionCanonicalName + "\";\n" + 
				"    }\n" + 
				"});\n";  
		methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.addStatement(1, StaticJavaParser.parseStatement(code)));
	}

	private void addConditionDistanceCalculators(MethodDeclaration methodDeclaration, DocumentedExecutable documentedExecutable, String[] conds, boolean lookForFailure) {
		if (lookForFailure) {
			String code = "DistanceAlgo algo = computeDistanceWithDisjunctiveAlgo();";
			methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.replace(blockStmt.getStatement(0), StaticJavaParser.parseStatement(code)));			
		}
		for (int i = 0; i < conds.length; ++i) {
			String cond = addCasting(conds[i], documentedExecutable);
			Expression condExpr = StaticJavaParser.parseExpression(cond);
			Expression condDistance = turnConditionToConditionDistance(condExpr, false);
			String formattedCond = condExpr.toString().
					replace("receiverObjectID", RECEIVEROBJECT_FIELD).
					replace("args", ARGS_FIELD).
					replace("methodResultID", RETVAL_FIELD);
			String formattedCondDistance = condDistance.toString().
					replace("receiverObjectID", RECEIVEROBJECT_FIELD).
					replace("args", ARGS_FIELD).
					replace("methodResultID", RETVAL_FIELD);
			String typeOfCalculator = lookForFailure ? "NegConditionDistanceCalculator" : "ConditionDistanceCalculator";
			String code = "algo.calculators.add(new " +  typeOfCalculator + "() {\n" + 
					"    boolean condition()  {\n" + 
					"        return " + formattedCond + ";\n" + 
					"    }\n" + 
					"    double cdistance()  {\n" + 
					"        return " + formattedCondDistance + ";\n" +
					"    }\n" + 
					"});\n";  
			methodDeclaration.getBody().ifPresent(blockStmt -> blockStmt.addStatement(1, StaticJavaParser.parseStatement(code)));
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
				boolean constantDistance = 
						(turnedOp == Operator.NOT_EQUALS) || 
						(turnedOp == Operator.EQUALS && (left.isNullLiteralExpr() || right.isNullLiteralExpr()));
				if (constantDistance) {
					return new DoubleLiteralExpr(1);				  
				} else {
					BinaryExpr distance = new BinaryExpr(new EnclosedExpr(left), new EnclosedExpr(right), Operator.MINUS);
					return distance;
				}
			}
		} else {
			return new DoubleLiteralExpr(1);
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
			type = removeParametersFromType(type).replace('$', '.');
			condition = condition.replace("args[" + index + "]", "((" + type + ") args[" + index + "])");
			index++;
		}

		// Casting of result object in condition.
		String returnType = method.getReturnType().getType().getTypeName();
		returnType = removeParametersFromType(returnType).replace('$', '.');
		if (!returnType.equals("void")) {
			condition =
					condition.replace(
							Configuration.RETURN_VALUE,
							"((" + returnType + ")" + Configuration.RETURN_VALUE + ")");
		}

		// Casting of target object in condition.
		String type = method.getDeclaringClass().getName();
		type = removeParametersFromType(type).replace('$', '.');
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
