package org.toradocu.generator;

import static org.toradocu.Toradocu.configuration;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.util.Checks;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Specification;
import randoop.condition.specification.ThrowsSpecification;

/**
 * The test case generator. The method {@code createTests} of this class creates
 * the test cases for a list of {@code ExecutableMember}.
 */
public class TestGeneratorValidation {
	public static final int MAX_EVALUATORS_PER_EVOSUITE_CALL = 10;
	public static final String VALIDATORS_FOLDER = "validation";
	public static final String EVALUATORS_FOLDER = "evaluators";
	public static final String TESTCASES_FOLDER = "testcasesForValidation";
	/** {@code Logger} for this class. */
	private static final Logger log = LoggerFactory.getLogger(TestGeneratorValidation.class);

	/**
	 * Creates evaluators to allow EvoSuite to check fitness wrt the given
	 * {@code specs}, and then launches EvoSuite with the evaluators as fitness
	 * functions to generate the test cases. This method creates two evaluators for
	 * each property {@code spec} learned with Toradocu for each method:
	 * 
	 * - an evaluator aimed at generating a test case that shows an execution for
	 * which the {@code spec} holds - an evaluator aimed at generating a test case
	 * that shows an execution for which the {@code spec} is violated (if any)
	 * 
	 * Recall that, in the case of Postconditions, The evaluator will enforce that
	 * the test cases reach method-exits with no exception.
	 *
	 * Usually we expect that the first evaluator can be satisfied, while the second
	 * could be unsatisfiable if there are no bugs. However, there is also the risk
	 * that we end up with discovering the weakness of some property {@code spec}
	 * derived with Toradocu.
	 *
	 * NB: we generate test cases for Throws-specs and Postcondition-specs with
	 * non-empty guards. This method raises an error if the following assumption is
	 * vilated: The specs with empty guards can include either a single Throws spec
	 * with empty guard, or a set of Postcondition-spec (but no Throws-spec).
	 * 
	 * @param specifications the specifications (to be tested) learned with Toadocu.
	 *                       Must not be null.
	 */
	public static void createTests(Map<DocumentedExecutable, OperationSpecification> specifications)
			throws IOException {
		Checks.nonNullParameter(specifications, "specifications");

		// Output Dir
		final Path outputDir = Paths.get(configuration.getTestOutputDir());

		// Create output directory where test cases are saved.

		final Path testsDir = outputDir.resolve(TESTCASES_FOLDER);
		final boolean testsDirCreationSucceeded = createOutputDir(testsDir.toString(), true);
		if (!testsDirCreationSucceeded || specifications.isEmpty()) {
			log.error("Test generation failed, cannot create dir:" + testsDir);
			return;
		}

		// Step 1/2: Generate test cases for the target class by launching EvoSuite
		String targetClass = configuration.getTargetClass();
		log.info("Going to generate validation test cases for " + targetClass + " oracles");

		// Launch EvoSuite
		List<String> evosuiteCommand = buildEvoSuiteCommand(outputDir, testsDir);
		final Path evosuiteLogFilePath = testsDir.resolve("evosuite-log.txt");
		try {
			Process processEvosuite = launchProcess(evosuiteCommand, evosuiteLogFilePath);
			log.info("Launched EvoSuite process, command line: " + evosuiteCommand.stream().reduce("", (s1, s2) -> {
				return s1 + " " + s2;
			}));
			try {
				processEvosuite.waitFor();
			} catch (InterruptedException e) {
				// the performer was shut down: kill the EvoSuite job
				log.info("Unexpected InterruptedException while running EvoSuite: " + e);
				processEvosuite.destroy();
			}
		} catch (IOException e) {
			log.error("Unexpected I/O error while running EvoSuite: " + e);
			throw new RuntimeException(e);
		}

		// Step 2/2: Enrich the generated test cases with assumptions and assertions
		enrichTestWithOracle(testsDir, targetClass, specifications);

	}

	private static void enrichTestWithOracle(Path testsDir, String targetClass,
			Map<DocumentedExecutable, OperationSpecification> allSpecs) {
		String testName = targetClass + "_ESTest";
		final Path testCaseAbsPath = testsDir.resolve(testName.replace('.', File.separatorChar) + ".java");
		File currentTestCase = new File(testCaseAbsPath.toUri());
		if (!currentTestCase.exists()) {
			return; // nothing to do, since EvoSuite failed to generate this test case
		}
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
		combinedTypeSolver.add(new JavaParserTypeSolver(configuration.sourceDir));
		combinedTypeSolver.add(new ReflectionTypeSolver());
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
		StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
		CompilationUnit cu = null;
		try {
			cu = StaticJavaParser.parse(currentTestCase);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		List<ClassOrInterfaceDeclaration> claxes = cu.findAll(ClassOrInterfaceDeclaration.class);
		if (claxes.isEmpty())
			throw new RuntimeException("Unexpected test fail structure");
		else {
			ClassOrInterfaceDeclaration clax = claxes.get(0);
			ClassOrInterfaceType t = StaticJavaParser.parseClassOrInterfaceType("java.util.HashSet<String>");
			Expression e = StaticJavaParser.parseExpression("new java.util.HashSet<String>()");
			clax.addFieldWithInitializer(t, "guard_lta", e, Keyword.PUBLIC);
		}
		int identifier = 0;
		for (DocumentedExecutable de : allSpecs.keySet()) {
			OperationSpecification os = allSpecs.get(de);
			identifier = enrichTestWithOracle(cu, testsDir, testName, de, os, allSpecs, identifier);
		}
		// write out the enriched test case
		try (FileOutputStream output = new FileOutputStream(currentTestCase)) {
			output.write(cu.toString().getBytes());
		} catch (IOException e) {
			log.error("Error while writing the enriched test case to file: " + currentTestCase, e);
		}
	}

	private static int enrichTestWithOracle(CompilationUnit cu, Path testsDir, String testName,
			DocumentedExecutable targetMethod, OperationSpecification os,
			Map<DocumentedExecutable, OperationSpecification> allSpecs, int identifier) {

		String targetMethodName = targetMethod.getName().substring(targetMethod.getName().lastIndexOf('.') + 1);
		List<ExpressionStmt> callsToTargetMethodTmp = cu.findAll(ExpressionStmt.class,
				c -> c.getExpression().isVariableDeclarationExpr()
						&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().isPresent()
						&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
								.isMethodCallExpr()
						&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
								.asMethodCallExpr().getNameAsString().equals(targetMethodName));
		callsToTargetMethodTmp.addAll(cu.findAll(ExpressionStmt.class,
				c -> c.getExpression().isAssignExpr() && c.getExpression().asAssignExpr().getValue().isMethodCallExpr()
						&& c.getExpression().asAssignExpr().getValue().asMethodCallExpr().getNameAsString()
								.equals(targetMethodName)));
		callsToTargetMethodTmp.addAll(cu.findAll(ExpressionStmt.class, c -> c.getExpression().isMethodCallExpr()
				&& c.getExpression().asMethodCallExpr().getNameAsString().equals(targetMethodName)));
		callsToTargetMethodTmp.addAll(cu.findAll(ExpressionStmt.class,
				c -> c.getExpression().isVariableDeclarationExpr()
						&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().isPresent()
						&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
								.isObjectCreationExpr()
						&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
								.asObjectCreationExpr().getType().getNameAsString().equals(targetMethodName)));
		callsToTargetMethodTmp.addAll(cu.findAll(ExpressionStmt.class,
				c -> c.getExpression().isAssignExpr()
						&& c.getExpression().asAssignExpr().getValue().isObjectCreationExpr()
						&& c.getExpression().asAssignExpr().getValue().asObjectCreationExpr().getType()
								.getNameAsString().equals(targetMethodName)));
		callsToTargetMethodTmp.addAll(cu.findAll(ExpressionStmt.class, c -> c.getExpression().isObjectCreationExpr()
				&& c.getExpression().asObjectCreationExpr().getType().getNameAsString().equals(targetMethodName)));
		List<ExpressionStmt> callsToTargetMethod = new ArrayList<ExpressionStmt>();
		for (ExpressionStmt es : callsToTargetMethodTmp) {
			NodeList<Expression> argsMethod = null;
			if (es.getExpression().isObjectCreationExpr())
				argsMethod = es.getExpression().asObjectCreationExpr().getArguments();
			else if (es.getExpression().isAssignExpr()
					&& es.getExpression().asAssignExpr().getValue().isObjectCreationExpr()) {
				argsMethod = es.getExpression().asAssignExpr().getValue().asObjectCreationExpr().getArguments();
			} else if (es.getExpression().isVariableDeclarationExpr() && es.getExpression().asVariableDeclarationExpr()
					.getVariable(0).getInitializer().get().isObjectCreationExpr()) {
				argsMethod = es.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
						.asObjectCreationExpr().getArguments();
			} else if (es.getExpression().isMethodCallExpr()) {
				argsMethod = es.getExpression().asMethodCallExpr().getArguments();
			} else if (es.getExpression().isAssignExpr()
					&& es.getExpression().asAssignExpr().getValue().isMethodCallExpr()) {
				argsMethod = es.getExpression().asAssignExpr().getValue().asMethodCallExpr().getArguments();
			} else if (es.getExpression().isVariableDeclarationExpr() && es.getExpression().asVariableDeclarationExpr()
					.getVariable(0).getInitializer().get().isMethodCallExpr()) {
				argsMethod = es.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
						.asMethodCallExpr().getArguments();
			} else {
				throw new RuntimeException("Unexpected call to target method: " + es);
			}
			List<DocumentedParameter> argsWanted = targetMethod.getParameters();
			boolean found = false;
			if (argsMethod.size() == argsWanted.size()) {
				found = false;
				// TODO:
				// Il seguente codice non può funzionare in quanto non siamo a conoscenza dei
				// tipi degli argomenti

				for (int i = 0; i < argsMethod.size(); i++) {
					found = false;
					Expression argMethod = argsMethod.get(i);
					DocumentedParameter argWanted = argsWanted.get(i);
					try {
						ResolvedType argMethodType = argMethod.calculateResolvedType();
						String argMethodTypeString = argMethodType.describe();
						if (argMethodTypeString.contains("<"))
							argMethodTypeString = argMethodTypeString.substring(0, argMethodTypeString.indexOf("<"));

						VariableDeclarationExpr argWantedJP = StaticJavaParser
								.parseVariableDeclarationExpr(argWanted.toString());
						String argWantedTypeString = argWantedJP.getCommonType().toString();
						if (argWantedTypeString.contains("<"))
							argWantedTypeString = argWantedTypeString.substring(0, argWantedTypeString.indexOf("<"));

						/*
						 * TypeSolver typeSolver = new ReflectionTypeSolver();
						 * ResolvedReferenceTypeDeclaration argWantedTypeSolved =
						 * typeSolver.solveType(argWantedType.getCanonicalName()); ReferenceTypeImpl
						 * referenceTypeImpl = new ReferenceTypeImpl(argWantedTypeSolved, typeSolver);
						 * argMethodType.isAssignableBy(referenceTypeImpl);
						 */
						if (argMethodType.isPrimitive())
							if (argWantedTypeString.equals(argMethodTypeString)) {
								found = true;
							} else {
								found = false;
							}
						else {
							found = true;
						}
					} catch (Exception e) {
						found = true;
					}
					if (!found)
						break;
				}

				if (found)
					callsToTargetMethod.add(es);
			}

		}

		for (int i = 0; i < callsToTargetMethod.size(); i++) {
			ExpressionStmt targetCall = callsToTargetMethod.get(i);
			ArrayList<Specification> targetSpecs = new ArrayList<>();
			targetSpecs.addAll(os.getThrowsSpecifications());
			targetSpecs.addAll(os.getPostSpecifications());
			for (Specification spec : targetSpecs) {
				// Call for which we shall add the oracle
				NodeList<Statement> insertionPoint = null;
				try {
					insertionPoint = ((BlockStmt) targetCall.getParentNode().get()).getStatements();
				} catch (Exception e) {
					String targetCallWithAssignmentStmt = " _methodResult__ = " + targetCall;
					Statement targetCallWithAssignment = StaticJavaParser.parseStatement(targetCallWithAssignmentStmt);
					insertionPoint = ((BlockStmt) targetCallWithAssignment.getParentNode().get()).getStatements();
				}

				// add assumptions: the guard of the oracle, plus all preconditions
				String clause = composeGuardClause(spec, targetCall, insertionPoint, targetMethodName, "");
				// Skip unmodeled guard
				if (clause.contains("SKIP_UNMODELED")) {
					String comment = "Skipped check due to unmodeled guard: " + spec.getGuard() + " "
							+ spec.getDescription();
					targetCall.addOrphanComment(new LineComment(comment));
					continue;
				}
				String guardsComment = "Automatically generated test oracle with guard:" + spec.getGuard().toString();
				for (PreSpecification precond : allSpecs.get(targetMethod).getPreSpecifications()) {
					clause = composeGuardClause(precond, targetCall, insertionPoint, targetMethodName, clause);
					// Skip unmodeled guard
					if (clause.contains("SKIP_UNMODELED")) {
						String comment = "Skipped check due to unmodeled guard precondition: " + precond.getGuard()
								+ " " + spec.getDescription();
						targetCall.addOrphanComment(new LineComment(comment));
						continue;
					}
					guardsComment = guardsComment + precond.getDescription();
				}
				// Skip unmodeled guard
				if (clause.contains("SKIP_UNMODELED")) {
					continue;
				}

				// Skip non satisfiable pre-conditions
				if (clause.contains("null.")) {
					String comment = "Skipped check due to non satisfiable pre-conditions: " + clause + " "
							+ spec.getDescription();
					targetCall.addOrphanComment(new LineComment(comment));
					continue;
				}
				addIfGuard(clause, targetCall, targetMethod.getReturnType().getType(), insertionPoint, identifier,
						guardsComment);

				// add assert
				if (spec instanceof PostSpecification) {
					// the postCondition of the oracle, plus all postConditions with empty guards
					ExpressionStmt newTargetCall = addAssertClause((PostSpecification) spec, clause, targetCall,
							insertionPoint, targetMethodName, targetMethod.getReturnType().getType(), false,
							identifier);
					callsToTargetMethod.set(i, newTargetCall);
					targetCall = newTargetCall;
				} else if (spec instanceof ThrowsSpecification) {
					// the try-catch block to check for the expected
					addFailClause((ThrowsSpecification) spec, clause, targetCall, insertionPoint, targetMethodName,
							targetMethod.getReturnType().getType(), false, identifier);
				} else {
					// exception
					throw new RuntimeException(
							"Spec of unexpected type " + spec.getClass().getName() + ": " + spec.getDescription());
				}
				identifier++;
			}
		}
		return identifier;

	}

	private static void addIfGuard(String clause, ExpressionStmt targetCall, Type targetCallReturnType,
			NodeList<Statement> insertionPoint, int identifier, String guardsComment) {
		if (!clause.isEmpty()) {
			Expression clauseExp = StaticJavaParser.parseExpression(clause);
			IfStmt i = new IfStmt();
			i.setCondition(clauseExp);
			MethodCallExpr mce = new MethodCallExpr();
			mce.setScope(StaticJavaParser.parseExpression("guard_lta"));
			mce.setName("add");
			mce.addArgument(new StringLiteralExpr(Integer.toString(identifier)));
			BlockStmt bs = new BlockStmt();
			bs.addStatement(mce);
			i.setThenStmt(bs);
			i.setLineComment(guardsComment);
			try {
				insertionPoint.addBefore(i, targetCall);
			} catch (Throwable e) {
				String assignStmt = targetCallReturnType.getTypeName() + " _methodResult__ = " + targetCall;
				Statement targetCallWithAssignment = StaticJavaParser.parseStatement(assignStmt);
				try {
					insertionPoint.addBefore(i, targetCallWithAssignment);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	private static void addFailClause(ThrowsSpecification postCond, String clause, ExpressionStmt targetCall,
			NodeList<Statement> insertionPoint, String targetMethodName, Type targetMethodReturnType,
			boolean isGeneralPostCond, int identifier) {
		Statement assertStmt = StaticJavaParser
				.parseStatement("if(guard_lta.contains(\"" + identifier + "\")) {org.junit.Assert.fail();}");
		String comment = "Automatically generated test oracle:" + postCond.getDescription();
		assertStmt.setLineComment(comment);
		insertionPoint.addAfter(assertStmt, targetCall);
		Optional<Node> n = insertionPoint.getParentNode();
		if (n.isPresent()) {
			n = n.get().getParentNode();
			if (n.isPresent() && (n.get() instanceof TryStmt)) {
				TryStmt ts = (TryStmt) n.get();
				for (CatchClause cc : ts.getCatchClauses()) {
					cc.getParameter().setType(Exception.class);

					BlockStmt bs = cc.getBody();
					Statement assertStmt2 = StaticJavaParser.parseStatement("if(guard_lta.contains(\"" + identifier
							+ "\")) {org.junit.Assert.assertTrue(" + cc.getParameter().getName() + " instanceof "
							+ postCond.getExceptionTypeName() + ");}");
					String comment2 = "Automatically generated test oracle:" + postCond.getDescription();
					assertStmt2.setLineComment(comment2);
					bs.addStatement(assertStmt2);
					cc.setBody(bs);
				}
			}
		}
	}

	private static ExpressionStmt addAssertClause(PostSpecification postCond, String clause, ExpressionStmt targetCall,
			NodeList<Statement> insertionPoint, String targetMethodName, Type targetMethodReturnType,
			boolean isGeneralPostCond, int identifier) {
		ExpressionStmt targetCallToConsider = targetCall;
		String postCondCondition = postCond.getProperty().getConditionText();
		String oracle = null;
		// String comment = null;
		if (postCondCondition != null && !postCondCondition.isEmpty()) {
			oracle = replaceFormalParamsWithActualValues(postCondCondition, targetCall);
			// comment = "** Postcondition on which " + (isGeneralPostCond ? "" : "the
			// oracle of ") + "method "
			// + targetMethodName + " depends: " + postCondCondition;
		} else {
			/*
			 * This test case was searched even if the postCond was not explicitly modelled.
			 * Thus, we must manually set the post condition in the test case. We set a
			 * RuntimeException to pinpoint this test case to the analyst.
			 */
			/*
			 * Statement checkpointStmt =
			 * JavaParser.parseStatement("if (true) throw new RuntimeException();");
			 * checkpointStmt.
			 * setLineComment("Checkpoint as memento of manually adding the assertion");
			 * insertionPoint.addBefore(checkpointStmt, targetCall); oracle =
			 * "\"PLEASE_ADD_RIGHT_CONDITIONS_HERE\".equals(\"\")"; comment =
			 * "** Toradocu failed to model the postcondition of this oracle, add assertion manually below: "
			 * + postCond.getProperty().getDescription();
			 */

		}
		if (oracle.contains("_methodResult__") && !targetCall.toString().contains("_methodResult__ =")) {
			// targetCall has a return value which is currently not assigned to any variable
			// in the test case
			String assignStmt = targetMethodReturnType.getTypeName() + " _methodResult__ = " + targetCall;
			try {
				ExpressionStmt targetCallWithAssignment = StaticJavaParser.parseStatement(assignStmt)
						.asExpressionStmt();
				Optional<Node> n = targetCall.getParentNode();
				try {
					Node parent = n.get();
					targetCallWithAssignment.setParentNode(parent);
					// targetCall.replace(targetCallWithAssignment);
					insertionPoint.replace(targetCall, targetCallWithAssignment);
					targetCallToConsider = targetCallWithAssignment;
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Target call missing parent problem while adapting test case with statement: \n"
							+ "\n postcond is: " + postCond + "\n oracle is: " + oracle + "\n target call is: "
							+ targetCall + "\n adapted assignment statement is: " + assignStmt + "\n test case is: "
							+ insertionPoint.getParentNode());
				}
			} catch (Exception e) {
				log.error("Parse problem while adapting test case with statement: \n" + "\n postcond is: " + postCond
						+ "\n oracle is: " + oracle + "\n target call is: " + targetCall
						+ "\n adapted assignment statement is: " + assignStmt + "\n test case is: "
						+ insertionPoint.getParentNode());
			}
		}
		Statement assertStmt = StaticJavaParser.parseStatement(
				"if(guard_lta.contains(\"" + identifier + "\")) {org.junit.Assert.assertTrue(" + oracle + ");}");
		String comment = "Automatically generated test oracle:" + postCond.getDescription();
		assertStmt.setLineComment(comment);
		insertionPoint.addAfter(assertStmt, targetCallToConsider);
		Optional<Node> n = insertionPoint.getParentNode();
		if (n.isPresent()) {
			n = n.get().getParentNode();
			if (n.isPresent() && (n.get() instanceof TryStmt)) {
				TryStmt ts = (TryStmt) n.get();
				for (CatchClause cc : ts.getCatchClauses()) {
					BlockStmt bs = cc.getBody();
					Statement assertStmt2 = StaticJavaParser.parseStatement(
							"if(guard_lta.contains(\"" + identifier + "\")) {org.junit.Assert.fail();}");
					String comment2 = "Automatically generated test oracle:" + postCond.getDescription();
					assertStmt2.setLineComment(comment2);
					bs.addStatement(assertStmt2);
					cc.setBody(bs);
				}
			}
		}
		return targetCallToConsider;
	}

	private static String composeGuardClause(Specification spec, ExpressionStmt targetCall,
			NodeList<Statement> insertionPoint, String targetMethodName, String existingClause) {
		String guard = spec.getGuard().getConditionText();
		String condToAssume = null;
		if (guard != null && !guard.isEmpty()) {
			condToAssume = replaceFormalParamsWithActualValues(guard, targetCall);
		} else if (!(spec instanceof PreSpecification)) {
			if (!spec.getGuard().getDescription().isEmpty()) {
				/*
				 * This test case was searched even if the guard was not explicitly modelled.
				 * Thus, we must manually validate that the guard actually holds. We set the
				 * assumeTrue(false) to be manually adapted, and assertTrue(false) to enforce
				 * the noticing this checkpoint
				 */
				/*
				 * Statement checkpointStmt1 = JavaParser
				 * .parseStatement("boolean ___WANT_TO_HIGHLIGHT_UNMODELED_CONDITIONS__ = false;"
				 * ); checkpointStmt1
				 * .setLineComment("Checkpoint as memento of manually checking that the proper assumptions hold."
				 * ); insertionPoint.addBefore(checkpointStmt1, targetCall); Statement
				 * checkpointStmt2 = JavaParser.parseStatement(
				 * "if (___WANT_TO_HIGHLIGHT_UNMODELED_CONDITIONS__) throw new RuntimeException();"
				 * ); insertionPoint.addBefore(checkpointStmt2, targetCall); condToAssume =
				 * "\"PLEASE_ADD_RIGHT_CONDITION_HERE\".equals(\"PLEASE_ADD_RIGHT_CONDITION_HERE\")";
				 * comment =
				 * "** Toradocu failed to model the guard of this oracle, adapt this assumption manually: "
				 * + spec.getGuard().getDescription();
				 */
				return "SKIP_UNMODELED";
			} else {
				// According to Toradocu, this oracle has no guard (no condition/description
				// were identified)
				condToAssume = "true";
			}
		}
		if (condToAssume != null) {
			if (existingClause.isEmpty()) {
				existingClause = condToAssume;
			} else {
				existingClause = existingClause + "&&" + condToAssume;
			}
		}
		return existingClause;
	}

	private static String replaceFormalParamsWithActualValues(String guardString, ExpressionStmt callStmt) {
		String ret = guardString;

		// replace receiverObject with receiver from target
		Expression callExpr = callStmt.getExpression();
		if (callExpr.isVariableDeclarationExpr()) {
			callExpr = callExpr.asVariableDeclarationExpr().getVariable(0).getInitializer().get();
		} else if (callExpr.isAssignExpr()) {
			callExpr = callExpr.asAssignExpr().getValue();
		}
		if (!callExpr.isMethodCallExpr() && !callExpr.isObjectCreationExpr()) {
			throw new RuntimeException("This type of target call is not handled yet: " + callExpr);
		}
		if (callExpr.isMethodCallExpr() && callExpr.asMethodCallExpr().getScope().isPresent()) {
			ret = ret.replace("receiverObjectID", callExpr.asMethodCallExpr().getScope().get().toString());
		}

		// replace methodResult with result from target
		if (callStmt.getExpression().isVariableDeclarationExpr()) {
			ret = ret.replace("methodResultID",
					callStmt.getExpression().asVariableDeclarationExpr().getVariable(0).getName().toString());
		} else if (callStmt.getExpression().isAssignExpr()) {
			ret = ret.replace("methodResultID",
					callStmt.getExpression().asAssignExpr().getTarget().asNameExpr().getName().toString());
		} else if (ret.contains("methodResultID")) {
			ret = ret.replace("methodResultID", "_methodResult__");
			// throw new RuntimeException("Condition contains methodResultID (" +
			// guardString + ") but the test case does not store the return value into a
			// variable: " + callStmt);
		}

		// replace args[i] with arguments from target
		int index = 0;
		NodeList<Expression> args = callExpr.isMethodCallExpr() ? callExpr.asMethodCallExpr().getArguments()
				: callExpr.asObjectCreationExpr().getArguments();
		for (Expression arg : args) {
			ret = ret.replace("args[" + index + "]", /* "((" + type + ") */ arg.toString());
			++index;
		}
		return ret;
	}

	/**
	 * Creates and launches an external process.
	 * 
	 * @param commandLine a {@link List}{@code <}{@link String}{@code >}, the
	 *                    command line to launch the process in the format expected
	 *                    by {@link ProcessBuilder}.
	 * @param logFilePath a {@link Path} to a log file where stdout and stderr of
	 *                    the process will be redirected.
	 * @return the created {@link Process}.
	 * @throws IOException if thrown by {@link ProcessBuilder#start()}.
	 */
	private static Process launchProcess(List<String> commandLine, Path logFilePath) throws IOException {
		final ProcessBuilder pb = new ProcessBuilder(commandLine).redirectErrorStream(true)
				.redirectOutput(logFilePath.toFile());
		final Process pr = pb.start();
		return pr;
	}

	/**
	 * Builds the command line for invoking EvoSuite.
	 * 
	 * @param evaluatorDefsForEvoSuite
	 * @param outputDir
	 * @param testsDir
	 * 
	 * @return a command line in the format of a
	 *         {@link List}{@code <}{@link String}{@code >}, suitable to be passed
	 *         to a {@link ProcessBuilder}.
	 */
	private static List<String> buildEvoSuiteCommand(Path outputDir, Path testsDir) {
		final String targetClass = configuration.getTargetClass();
		final List<String> retVal = new ArrayList<String>();
		String classpathTarget = outputDir.toString();
		for (URL cp : configuration.classDirs) {
			classpathTarget += ":" + cp.getPath();
		}
		retVal.add("/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java");
		retVal.add("-Xmx4G");
		// enabled assertions since evosuite is generating failing test cases for them
		// retVal.add("-ea");
		retVal.add("-jar");
		retVal.add("/mnt/Backup/toradocu/lib-evosuite/evosuite-1.2.0.jar");
		retVal.add("-class");
		retVal.add(targetClass);
		retVal.add("-mem");
		retVal.add("2048");
		retVal.add("-DCP=" + classpathTarget);
		retVal.add("-Dassertions=false");
		retVal.add("-Dsearch_budget=" + configuration.getEvoSuiteBudget()); // configuration.getEvoSuiteBudget()
		retVal.add("-Dreport_dir=" + outputDir);
		retVal.add("-Dtest_dir=" + testsDir);
		// retVal.add("-Dvirtual_fs=false");
		retVal.add("-Dcriterion=LINE:BRANCH:EXCEPTION:WEAKMUTATION:OUTPUT:METHOD:METHODNOEXCEPTION:CBRANCH");
		// retVal.add("-Dno_runtime_dependency");

		return retVal;
	}

	/**
	 * Creates the directory specified by {@code outputDir}.
	 *
	 * @param outputDir the directory to be created
	 * @return {@code true} if the creation succeeded, {@code false} otherwise
	 */
	private static boolean createOutputDir(String outputDir, boolean clear) {
		boolean creationSucceeded = true;
		final File outDir = new File(outputDir);
		if (outDir.exists()) {
			if (clear) {
				try {
					FileUtils.deleteDirectory(outDir);
				} catch (IOException e) {
					log.error("Error while deleting previous content of folder: " + outputDir);
				}
			}
		}
		if (!outDir.exists()) {
			creationSucceeded = outDir.mkdirs();
		}
		if (!creationSucceeded) {
			log.error("Error during creation of directory: " + outputDir);
		}
		return creationSucceeded;
	}

}
