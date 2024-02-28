package org.toradocu.generator;

import static org.toradocu.Toradocu.configuration;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import edu.stanford.nlp.semgraph.SemanticGraph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.translator.Parser;
import org.toradocu.translator.PropositionSeries;
import org.toradocu.translator.Matcher;
import org.toradocu.translator.Matcher.WordType;
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
public class TestGenerator {
	public static final int MAX_EVALUATORS_PER_EVOSUITE_CALL = 10;
	public static final String EVALUATORS_FOLDER = "evaluators";
	public static final String TESTCASES_FOLDER = "testcases";
	private static final String EVALUATOR_TEMPLATE_NAME = "EvoSuiteEvaluator_Template";
	private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	/** {@code Logger} for this class. */
	private static final Logger log = LoggerFactory.getLogger(TestGenerator.class);

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

		// Create output directory where evaluators are saved.
		final Path evaluatorsDir = Paths.get(configuration.getTestOutputDir()).resolve(EVALUATORS_FOLDER);
		final boolean evaluatorsDirCreationSucceeded = createOutputDir(evaluatorsDir.toString(), true);
		if (!evaluatorsDirCreationSucceeded || specifications.isEmpty()) {
			log.error("Test generation failed, cannot create dir:" + evaluatorsDir);
			return;
		}

		// Create output directory where test cases are saved.
		final Path testsDir = Paths.get(configuration.getTestOutputDir()).resolve(TESTCASES_FOLDER);
		final boolean testsDirCreationSucceeded = createOutputDir(testsDir.toString(), true);
		if (!testsDirCreationSucceeded || specifications.isEmpty()) {
			log.error("Test generation failed, cannot create dir:" + testsDir);
			return;
		}

		// Step 1/3: Create evaluators
		String classpathTarget = ".";
		for (URL cp : configuration.classDirs) {
			classpathTarget += ":" + cp.getPath();
		}
		int evaluatorNumber = 0;
		Stack<Pair<String, Map<String, Pair<DocumentedExecutable, Specification>>>> evaluatorDefsForEvosuite = new Stack<>(); // <evaluatorDefs,
																																// map:
																																// test
																																// case
																																// -->
																																// assertion>
		for (DocumentedExecutable method : specifications.keySet()) {
			String formattedMethodSignature = bytecodeStyleSignature(method);
			String packageName = method.getDeclaringClass().getPackage().getName();
			if (!createOutputDir(evaluatorsDir + File.separator + packageName.replace('.', File.separator.charAt(0)),
					false)) {
				throw new RuntimeException(
						"Problems with creating package dir: " + packageName.replace('.', File.separator.charAt(0)));
			}

			OperationSpecification specification = specifications.get(method);
			ArrayList<Specification> targetSpecs = new ArrayList<>();
			targetSpecs.addAll(specification.getThrowsSpecifications());
			targetSpecs.addAll(specification.getPostSpecifications());
			ArrayList<String> guards = new ArrayList<>();
			for (Specification spec : targetSpecs) {
				guards.clear();
				String grd = spec.getGuard().getConditionText();
				boolean specGuardEmpty = false;
				boolean unmodeledGuard = false;
				if (grd != null && !grd.isEmpty()) {
					log.debug("* Method " + configuration.getTargetClass() + "." + method.getSignature()
							+ ": Guarded spec found: " + spec);
					// The perspective test shall satisfy the guard of the target spec
					guards.add(grd);
				} else if (spec.getGuard().getDescription().isEmpty()) {
					// In this case there is no guard indeed
					specGuardEmpty = true;
					log.debug("* Method " + configuration.getTargetClass() + "." + method.getSignature()
							+ ": Unguarded spec found: " + spec);
					if (spec instanceof ThrowsSpecification) {
						if (targetSpecs.size() > 1)
							log.error("Method " + configuration.getTargetClass() + "." + method.getSignature()
									+ ": We have " + targetSpecs.size()
									+ " specs. However, it does not make sense to have more than a spec when there is an unguarded thorws-spec.");
						log.error("** SPECS ARE: " + targetSpecs);
						TestGeneratorSummaryData._I().incTestGenerationErrors();
					}
				} else {
					unmodeledGuard = true;
					log.info("* Method " + configuration.getTargetClass() + "." + method.getSignature()
							+ ": Unmodeled guard found for spec: " + spec);
					TestGeneratorSummaryData._I().incUnmodeledGuards();
					// TODO: how do we deal with specs for which there exist a guard, but Toradocu
					// failed to map the guard to a condition?
					// At the moment, we try to generate a test case that satisfies the path
					// condition, but it is not clear if this makes sense.
				}
				// The perspective test shall satisfy all preconditions of the method
				for (PreSpecification precond : specification.getPreSpecifications()) {
					String precondGuard = precond.getGuard().getConditionText();
					if (precondGuard != null && !precondGuard.isEmpty()) {
						guards.add(precondGuard);
					}
				}
				// The perspective test shall not hit any throws-spec (but the current one, if
				// the current one is a throws-spec)
				for (ThrowsSpecification thowsSpec : specification.getThrowsSpecifications()) {
					if (thowsSpec == spec) {
						continue;
					}
					String throwsGuard = thowsSpec.getGuard().getConditionText();
					if (throwsGuard != null && !throwsGuard.isEmpty()) {
						String precondStr = "!(" + throwsGuard + ")"; // negate the guard of the throws spec
						guards.add(precondStr);
					}
				}

				// We generate test cases aimed to the target postconditions
				String postCond;
				if (spec instanceof PostSpecification) {
					postCond = ((PostSpecification) spec).getProperty().getConditionText();
					if (postCond == null || postCond.isEmpty()) {
						if (((PostSpecification) spec).getProperty().getDescription().isEmpty()) {
							log.error("* Method " + configuration.getTargetClass() + "." + method.getSignature()
									+ ": Post conditions has empty property for spec: " + spec);
							TestGeneratorSummaryData._I().incEmptyPostConditions();
						} else {
							log.info("* Method " + configuration.getTargetClass() + "." + method.getSignature()
									+ ": Unmodeled postcondition for spec: " + spec);
							TestGeneratorSummaryData._I().incUnmodeledPostConditions();
							// TODO: how do we deal with specs for which there exist a postcondition, but
							// Toradocu failed to map the corresponding condition?
						}
						postCond = "";
					}
				} else if (spec instanceof ThrowsSpecification) {
					postCond = ((ThrowsSpecification) spec).getExceptionTypeName();
					if (postCond == null || postCond.isEmpty()) {
						if (((ThrowsSpecification) spec).getDescription().isEmpty()) {
							log.error("* Method " + configuration.getTargetClass() + "." + method.getSignature()
									+ ": Throws spec has empty property for spec: " + spec);
							TestGeneratorSummaryData._I().incEmptyPostConditions();
						} else {
							log.info("* Method " + configuration.getTargetClass() + "." + method.getSignature()
									+ ": Unmodeled exeception for spec: " + spec);
							TestGeneratorSummaryData._I().incUnmodeledPostConditions();
							// TODO: how do we deal with specs for which there exist a postcondition, but
							// Toradocu failed to map the corresponding condition?
						}
						postCond = "";
					}
				} else {
					log.error("* Method " + configuration.getTargetClass() + "." + method.getSignature()
							+ ": This type of target spec is not handled yet: " + spec);
					TestGeneratorSummaryData._I().incTestGenerationErrors();
					throw new RuntimeException("Should never happen");
				}

				if (postCond.isEmpty() && (specGuardEmpty || unmodeledGuard)) {
					log.error("* Method " + configuration.getTargetClass() + "." + method.getSignature()
							+ ": Skipping spec with both empty/unmodeled guard and empty/unmodeled post condition: "
							+ spec);
					TestGeneratorSummaryData._I().incTestGenerationErrors();
					continue;
				}
				String[] postconds = new String[] { postCond };

				// We then generate the 2 evaluators that refer to the guards and the postconds
				if (evaluatorNumber % MAX_EVALUATORS_PER_EVOSUITE_CALL == 0) {
					evaluatorDefsForEvosuite.push(Pair.of("", new HashMap<>())); // starts a new group of
																					// <evaluatorDefs, test case
																					// assertions>
				}
				boolean unmodeled = unmodeledGuard || postCond.isEmpty();
				final String evaluatorBaseName = "EvoSuiteEvaluator_" + (evaluatorNumber + 1);
				final String testBaseName = configuration.getTargetClass() + "_" + (evaluatorNumber + 1);
				// ...an evaluator to search for a test case that satisfies the given
				// postcondition
				final String evaluatorName = evaluatorBaseName + (unmodeled ? "_unmodeled" : "");
				final String testName = testBaseName + (unmodeled ? "_unmodeled" : "") + "_Test";
				createEvaluator(method, guards.toArray(new String[0]), postconds, false, evaluatorName, evaluatorsDir,
						classpathTarget);
				TestGeneratorSummaryData._I().incGeneratedPositiveEvaluators();
				evaluatorDefsForEvosuite.peek().getRight().put(testName, Pair.of(method, spec)); // spec for defining
																									// the assertion,
																									// when (and if) we
																									// generate the test
																									// case later on
				String evaluatorDef = method.getDeclaringClass().getCanonicalName() + "," + formattedMethodSignature
						+ "," + (packageName.isEmpty() ? "" : packageName + ".") + evaluatorName;
				// ...and another evaluator to search for a test case that violates the given
				// postcondition
				if (!unmodeled) {
					final String evaluatorForViolationName = evaluatorBaseName + "_failure";
					final String testForViolationName = testBaseName + "_failure_Test";
					createEvaluator(method, guards.toArray(new String[0]), postconds, true, evaluatorForViolationName,
							evaluatorsDir, classpathTarget);
					TestGeneratorSummaryData._I().incGeneratedNegativeEvaluators();
					evaluatorDefsForEvosuite.peek().getRight().put(testForViolationName, Pair.of(method, spec)); // spec
																													// for
																													// defining
																													// the
																													// assertion,
																													// when
																													// (and
																													// if)
																													// we
																													// generate
																													// the
																													// test
																													// case
																													// later
																													// on
					evaluatorDef += ":" + method.getDeclaringClass().getCanonicalName() + "," + formattedMethodSignature
							+ "," + (packageName.isEmpty() ? "" : packageName + ".") + evaluatorForViolationName;
					/*
					 * RATIONALE: if postCond != empty - guardUnmodeled: possibly we may violate the
					 * postcond out of the guard (which we cannot automatically check for) --> it
					 * does not make much sense to generate tests for failures -->(and we must
					 * manually set the assumption later on in the positive test). - guardModeled,
					 * and given: basic case --> yes, we try to generate tests for failures -
					 * guardModeled, but empty: the postcond is unguarded, --> it makes still sense
					 * to generate tests for failures else, if postCond == empty --> it is
					 * impossible to generate tests for failures -->(and we must manually set the
					 * assertion later on in the positive test).
					 */
				}
				Pair<String, Map<String, Pair<DocumentedExecutable, Specification>>> evaluatorDefs = evaluatorDefsForEvosuite
						.pop();
				String newDef = evaluatorDefs.getLeft() + (evaluatorDefs.getLeft().isEmpty() ? "" : ":") + evaluatorDef;
				evaluatorDefsForEvosuite.push(Pair.of(newDef, evaluatorDefs.getRight()));
				evaluatorNumber += 1;
			}
		}

		// Step 2/3: Generate test cases by launching EvoSuite with the evaluators as
		// fitness function
		if (evaluatorNumber > 0) {
			log.info("Going to generate test cases for " + evaluatorNumber + " oracles");
		} else {
			log.info("Did not find any oracles for which test cases shall be generated");
		}
		for (int i = 0; i < evaluatorDefsForEvosuite.size(); ++i) {
			// Launch EvoSuite
			List<String> evosuiteCommand = buildEvoSuiteCommand(evaluatorDefsForEvosuite.get(i).getLeft(),
					evaluatorsDir, testsDir);
			final Path evosuiteLogFilePath = evaluatorsDir.resolve("evosuite-log-" + i + ".txt");
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

			// Step 3/3: Enrich the generated test cases with assumptions and assertions
			Map<String, Pair<DocumentedExecutable, Specification>> assertionsToAddInTestCases = evaluatorDefsForEvosuite
					.get(i).getRight();
			for (String testName : assertionsToAddInTestCases.keySet()) {
				Pair<DocumentedExecutable, Specification> specData = assertionsToAddInTestCases.get(testName);
				enrichTestWithOracle(testsDir, testName, specData.getLeft(), specData.getRight(), specifications);
			}
		}

	}

	private static void enrichTestWithOracle(Path testsDir, String testName, DocumentedExecutable targetMethod,
			Specification spec, Map<DocumentedExecutable, OperationSpecification> allSpecs) {
		final Path testCaseAbsPath = testsDir.resolve(testName.replace('.', File.separatorChar) + ".java");
		File currentTestCase = new File(testCaseAbsPath.toUri());
		if (!currentTestCase.exists()) {
			return; // nothing to do, since EvoSuite failed to generate this test case
		}
		CompilationUnit cu = null;
		try {
			cu = StaticJavaParser.parse(currentTestCase);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// In any case: throw an exception if a failure-driven test case completed
		// without pinpointing any failure.
		if (testName.endsWith("failure_Test")) {
			List<MethodDeclaration> methodDecls = cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class,
					c -> true);
			for (MethodDeclaration m : methodDecls) {
				if (m.getBody().isPresent()) {
					m.getBody().get().addStatement(StaticJavaParser.parseStatement(
							"if (true) throw new RuntimeException(\"Failure-driven test case completed without pinpointing any failure. This should not happen. Please check\");"));
				}
			}
		}

		String targetMethodName = targetMethod.getName().substring(targetMethod.getName().lastIndexOf('.') + 1);

		List<MethodDeclaration> methodDeclarations = cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class,
				c -> true);
		for (MethodDeclaration m : methodDeclarations) {
			String testMethodName = "test";
			testMethodName += capitalizeFirstChar(targetMethodName);
			testMethodName += "_";
			if (spec instanceof ThrowsSpecification) {
				ThrowsSpecification throwsSpec = (ThrowsSpecification) spec;
				String excName = throwsSpec.getExceptionTypeName();
				testMethodName += excName.substring(excName.lastIndexOf('.') + 1);
				String guardText = spec.getGuard().getDescription();
				testMethodName += "_";
				testMethodName += extractTextForTestName(guardText, targetMethod);
			} else if (spec instanceof PostSpecification) {
				PostSpecification postSpec = (PostSpecification) spec;
				String propertyText = postSpec.getProperty().getDescription();
				testMethodName += extractTextForTestName(propertyText, targetMethod);
				/*
				 * String resultText = propertyText.indexOf("if") >= 0 ?
				 * propertyText.substring(0, propertyText.indexOf("if")) : propertyText;
				 * testMethodName += extractTextForTestName(resultText, targetMethod); String
				 * guardText = propertyText.indexOf("if") >= 0 ?
				 * propertyText.substring(propertyText.indexOf("if")) : null; if (guardText !=
				 * null) { testMethodName += "_"; testMethodName +=
				 * extractTextForTestName(guardText, targetMethod); }
				 */
			}
			m.setName(testMethodName);
		}

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
			List<ResolvedType> targetMethodParameters;
			try {
				if (es.getExpression().isObjectCreationExpr()) {
					ResolvedConstructorDeclaration rcd = es.getExpression().asObjectCreationExpr().resolve();
					targetMethodParameters = rcd.formalParameterTypes();
				} else if (es.getExpression().isAssignExpr()
						&& es.getExpression().asAssignExpr().getValue().isObjectCreationExpr()) {
					ResolvedConstructorDeclaration rcd = es.getExpression().asAssignExpr().getValue()
							.asObjectCreationExpr().resolve();
					targetMethodParameters = rcd.formalParameterTypes();
				} else if (es.getExpression().isVariableDeclarationExpr() && es.getExpression()
						.asVariableDeclarationExpr().getVariable(0).getInitializer().get().isObjectCreationExpr()) {
					ResolvedConstructorDeclaration rcd = es.getExpression().asVariableDeclarationExpr().getVariable(0)
							.getInitializer().get().asObjectCreationExpr().resolve();
					targetMethodParameters = rcd.formalParameterTypes();
				} else if (es.getExpression().isMethodCallExpr()) {
					ResolvedMethodDeclaration rcd = es.getExpression().asMethodCallExpr().resolve();
					targetMethodParameters = rcd.formalParameterTypes();
				} else if (es.getExpression().isAssignExpr()
						&& es.getExpression().asAssignExpr().getValue().isMethodCallExpr()) {
					ResolvedMethodDeclaration rcd = es.getExpression().asAssignExpr().getValue().asMethodCallExpr()
							.resolve();
					targetMethodParameters = rcd.formalParameterTypes();
				} else if (es.getExpression().isVariableDeclarationExpr() && es.getExpression()
						.asVariableDeclarationExpr().getVariable(0).getInitializer().get().isMethodCallExpr()) {
					ResolvedMethodDeclaration rcd = es.getExpression().asVariableDeclarationExpr().getVariable(0)
							.getInitializer().get().asMethodCallExpr().resolve();
					targetMethodParameters = rcd.formalParameterTypes();
				} else {
					throw new RuntimeException("Unexpected call to target method: " + es);
				}
				List<DocumentedParameter> argsWanted = targetMethod.getParameters();

				boolean found = false;
				if (targetMethodParameters.size() == argsWanted.size()) {
					if (targetMethodParameters.size() == 0) {
						found = true;
					} else {
						for (int i = 0; i < targetMethodParameters.size(); i++) {
							found = false;
							ResolvedType argMethod = targetMethodParameters.get(i);
							DocumentedParameter argWanted = argsWanted.get(i);
							String argMethodString = argMethod.describe();
							String argWantedString = argWanted.toString().substring(0,
									argWanted.toString().indexOf(" " + argWanted.getName()));
							// Replace $ with . to allow comparison of inner classes
							argWantedString = argWantedString.replace("$", ".");
							found = argMethodString.equals(argWantedString);
							if (!found)
								break;
						}
					}
					if (found)
						callsToTargetMethod.add(es);
				}
			} catch (Exception e) {
				// TODO This is a temporary fix. We should analyze why the SymbolSolver
				// sometimes fails in detecting the constructor/method declaration
				log.warn("SymbolSolver failure. A check will be added but it may need to be manually removed.", e);
				callsToTargetMethod.add(es);
			}
		}

		if (callsToTargetMethod.isEmpty()) {
			TestGeneratorSummaryData._I().incTestCasesWithoutTargetMehtod();
			List<ExpressionStmt> check = cu.findAll(ExpressionStmt.class,
					c -> c.getExpression().isVariableDeclarationExpr()
							&& c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().isPresent()
							&& (c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get()
									.isObjectCreationExpr()
									|| c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer()
											.get().isMethodCallExpr()));
			log.error("Test " + testName.replace('.', File.separatorChar) + ".java does not contains the target method "
					+ targetMethod.getSignature() + ", which is needed for the spec: " + spec);
			log.error("* Methods found in var declarations: " + check.toString());
			for (ExpressionStmt c : check) {
				log.error("** " + c.getExpression().asVariableDeclarationExpr().getVariable(0).getInitializer().get());
			}
			check = cu.findAll(ExpressionStmt.class,
					c -> c.getExpression().isAssignExpr()
							&& (c.getExpression().asAssignExpr().getValue().isObjectCreationExpr()
									|| c.getExpression().asAssignExpr().getValue().isMethodCallExpr()));
			log.error("* Methods found in var assignments: " + check.toString());
			for (ExpressionStmt c : check) {
				log.error("** " + c.getExpression().asAssignExpr().getValue());
			}
			check = cu.findAll(ExpressionStmt.class,
					c -> c.getExpression().isObjectCreationExpr() || c.getExpression().isMethodCallExpr());
			log.error("* Methods found in other statements " + check.toString());
			for (ExpressionStmt c : check) {
				log.error("** " + c.getExpression());
			}

			List<MethodDeclaration> methodDecls = cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class,
					c -> true);
			for (MethodDeclaration m : methodDecls) {
				if (m.getBody().isPresent()) {
					m.getBody().get().addStatement(StaticJavaParser
							.parseStatement("boolean ___WANT_TO_HIGHLIGHT_NOTARGETMETHOD_TEST_CASES__ = false;"));
					m.getBody().get().addStatement(StaticJavaParser.parseStatement(
							"if (___WANT_TO_HIGHLIGHT_NOTARGETMETHOD_TEST_CASES__) throw new RuntimeException(\"Failure-driven test case completed without pinpointing any failure. This should not happen. Please check\");"));
				}
			}
		} else {
			ExpressionStmt targetCall = callsToTargetMethod.get(callsToTargetMethod.size() - 1); // Call for which we
																									// shall add the
																									// oracle

			targetCall.setLineComment("** Automatically generated test oracle is: " + spec.getDescription()
					+ ", with guard: " + spec.getGuard());
			NodeList<Statement> insertionPoint = ((BlockStmt) targetCall.getParentNode().get()).getStatements();

			// add assumptions: the guard of the oracle, plus all preconditions
			addAssumeClause(spec, targetCall, insertionPoint, targetMethodName);
			for (PreSpecification precond : allSpecs.get(targetMethod).getPreSpecifications()) {
				addAssumeClause(precond, targetCall, insertionPoint, targetMethodName);
			}

			// In any case: fail if the test cases result in catching any exception
			// (for throws-specs, other than the ones in the oracles that we add below)
			// Indeed the postcondition failed in these cases.
			List<CatchClause> originalCatches = cu.findAll(com.github.javaparser.ast.stmt.CatchClause.class, c -> true);
			for (CatchClause cc : originalCatches) {
				cc.getBody().addStatement(StaticJavaParser.parseStatement("org.junit.Assert.fail();"));
			}
			// add assert
			if (spec instanceof PostSpecification) { // the postCondition of the oracle, plus all postConditions with
														// empty guards
				addAssertClause((PostSpecification) spec, targetCall, insertionPoint, targetMethodName,
						targetMethod.getReturnType().getType(), false);
			} else if (spec instanceof ThrowsSpecification) { // the try-catch block to check for the expected exception
				String exceptionName = ((ThrowsSpecification) spec).getExceptionTypeName();
				TryStmt tryStmt = new TryStmt();
				BlockStmt blockStmt = new BlockStmt();
				blockStmt.addStatement(targetCall.clone());
				Statement failStmt = StaticJavaParser.parseStatement("org.junit.Assert.fail();");
				failStmt.setLineComment("** Oracle postcondition: " + spec.getDescription());
				blockStmt.addStatement(failStmt);
				tryStmt.setTryBlock(blockStmt);
				CatchClause catchClause = new CatchClause();
				catchClause.setParameter(new Parameter(StaticJavaParser.parseType(exceptionName),
						StaticJavaParser.parseSimpleName("_e__")));
				NodeList<CatchClause> catches = new NodeList<>();
				catches.add(catchClause);
				tryStmt.setCatchClauses(catches);
				insertionPoint.addBefore(tryStmt, targetCall);
				insertionPoint.remove(targetCall);
			} else {
				throw new RuntimeException(
						"Spec of unexpected type " + spec.getClass().getName() + ": " + spec.getDescription());
			}
		}

		// write out the enriched test case
		try (FileOutputStream output = new FileOutputStream(currentTestCase)) {
			output.write(cu.toString().getBytes());
		} catch (IOException e) {
			log.error("Error while writing the enriched test case to file: " + currentTestCase, e);
		}

	}

	private static String extractTextForTestName(String description, DocumentedExecutable method) {
		String text = "";
		List<PropositionSeries> props = Parser.parse(new Comment(description), method);
		for (PropositionSeries p : props) {
			SemanticGraph sg = p.getSemanticGraph();
			List<String> words = Matcher.relevantWords(sg.vertexListSorted(), WordType.NN, WordType.WP, WordType.JJ,
					WordType.PR, WordType.FW, WordType.SYM, WordType.VB, WordType.CC, WordType.IN, WordType.TO,
					WordType.EX, WordType.RB, WordType.WRB);
			for (String w : words) {
				text += capitalizeFirstChar(w);
			}
		}
		text = text.replaceAll("[^A-Za-z_$0-9]", "");
		return text;
	}

	private static String capitalizeFirstChar(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private static void addAssertClause(PostSpecification postCond, ExpressionStmt targetCall,
			NodeList<Statement> insertionPoint, String targetMethodName, Type targetMethodReturnType,
			boolean isGeneralPostCond) {
		Statement targetCallToConsider = targetCall;
		String postCondCondition = postCond.getProperty().getConditionText();
		String oracle = null;
		String comment = null;
		if (postCondCondition != null && !postCondCondition.isEmpty()) {
			oracle = replaceFormalParamsWithActualValues(postCondCondition, targetCall);
			comment = "** Postcondition on which " + (isGeneralPostCond ? "" : "the oracle of ") + "method "
					+ targetMethodName + " depends: " + postCondCondition;
		} else {
			/*
			 * This test case was searched even if the postCond was not explicitly modelled.
			 * Thus, we must manually set the post condition in the test case. We set a
			 * RuntimeException to pinpoint this test case to the analyst.
			 */
			Statement checkpointStmt = StaticJavaParser.parseStatement("if (true) throw new RuntimeException();");
			checkpointStmt.setLineComment("Checkpoint as memento of manually adding the assertion");
			insertionPoint.addBefore(checkpointStmt, targetCall);
			oracle = "\"PLEASE_ADD_RIGHT_CONDITIONS_HERE\".equals(\"\")";
			comment = "** Toradocu failed to model the postcondition of this oracle, add assertion manually below: "
					+ postCond.getProperty().getDescription();
		}
		if (oracle.contains("_methodResult__")) {
			// targetCall has a return value which is currently not assigned to any variable
			// in the test case
			String targetMethodReturnTypeName = targetMethodReturnType.getTypeName().replaceAll("<[A-Za-z_$]+>", "<?>");
			String assignStmt = targetMethodReturnTypeName + " _methodResult__ = " + targetCall;
			try {
				Statement targetCallWithAssignment = StaticJavaParser.parseStatement(assignStmt);
				insertionPoint.replace(targetCall, targetCallWithAssignment);
				targetCallToConsider = targetCallWithAssignment;
			} catch (Exception e) {
				log.error("Parse problem while adapting test case with statement: \n" + "\n postcond is: " + postCond
						+ "\n oracle is: " + oracle + "\n target call is: " + targetCall
						+ "\n adapted assignment statement is: " + assignStmt + "\n test case is: "
						+ insertionPoint.getParentNode());
			}
		}
		Statement assertStmt = StaticJavaParser.parseStatement("org.junit.Assert.assertTrue(" + oracle + ");");
		assertStmt.setLineComment(comment);
		insertionPoint.addAfter(assertStmt, targetCallToConsider);
	}

	private static void addAssumeClause(Specification spec, ExpressionStmt targetCall,
			NodeList<Statement> insertionPoint, String targetMethodName) {
		String guard = spec.getGuard().getConditionText();
		String condToAssume = null;
		String comment = null;
		if (guard != null && !guard.isEmpty()) {
			condToAssume = replaceFormalParamsWithActualValues(guard, targetCall);
			comment = "** "
					+ (spec instanceof PreSpecification ? "Precondition on which " : "Guard on which the oracle of ")
					+ "method " + targetMethodName + " depends: " + guard;
		} else if (!(spec instanceof PreSpecification)) {
			if (!spec.getGuard().getDescription().isEmpty()) {
				/*
				 * This test case was searched even if the guard was not explicitly modelled.
				 * Thus, we must manually validate that the guard actually holds. We set the
				 * assumeTrue(false) to be manually adapted, and assertTrue(false) to enforce
				 * the noticing this checkpoint
				 */
				Statement checkpointStmt1 = StaticJavaParser
						.parseStatement("boolean ___WANT_TO_HIGHLIGHT_UNMODELED_CONDITIONS__ = false;");
				checkpointStmt1
						.setLineComment("Checkpoint as memento of manually checking that the proper assumptions hold.");
				insertionPoint.addBefore(checkpointStmt1, targetCall);
				Statement checkpointStmt2 = StaticJavaParser.parseStatement(
						"if (___WANT_TO_HIGHLIGHT_UNMODELED_CONDITIONS__) throw new RuntimeException();");
				insertionPoint.addBefore(checkpointStmt2, targetCall);
				condToAssume = "\"PLEASE_ADD_RIGHT_CONDITION_HERE\".equals(\"PLEASE_ADD_RIGHT_CONDITION_HERE\")";
				comment = "** Toradocu failed to model the guard of this oracle, adapt this assumption manually: "
						+ spec.getGuard().getDescription();
			} else {
				condToAssume = "true";
				comment = "** According to Toradocu, this oracle has no guard (no condition/description were identified)";
			}
		}
		if (condToAssume != null) {
			Statement assumeStmt = StaticJavaParser
					.parseStatement("org.junit.Assume.assumeTrue(" + condToAssume + ");");
			assumeStmt.setLineComment(comment);
			insertionPoint.addBefore(assumeStmt, targetCall);
		}
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
			if (arg.isCastExpr()) {
				ret = ret.replace("args[" + index + "]", "(" + arg.toString() + ")");
			} else {
				ret = ret.replace("args[" + index + "]", /* "((" + type + ") */ arg.toString());
			}
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
	private static List<String> buildEvoSuiteCommand(String evaluatorDefsForEvoSuite, Path outputDir, Path testsDir) {
		final String targetClass = configuration.getTargetClass();
		final List<String> retVal = new ArrayList<String>();
		String classpathTarget = outputDir.toString();
		for (URL cp : configuration.classDirs) {
			classpathTarget += ":" + cp.getPath();
		}
		retVal.add("/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java");
		retVal.add("-Xmx4G");
		retVal.add("-jar");
		retVal.add(configuration.getEvoSuiteJar());
		retVal.add("-class");
		retVal.add(targetClass);
		retVal.add("-mem");
		retVal.add("2048");
		retVal.add("-DCP=" + classpathTarget);
		retVal.add("-Dassertions=false");
		// retVal.add("-Dglobal_timeout=" + configuration.getEvoSuiteBudget());
		retVal.add("-Dsearch_budget=" + configuration.getEvoSuiteBudget());
		retVal.add("-Dreport_dir=" + outputDir);
		retVal.add("-Dtest_dir=" + testsDir);
		retVal.add("-Dvirtual_fs=false");
		retVal.add("-Dselection_function=ROULETTEWHEEL");
		retVal.add("-Dcriterion=PATHCONDITION");
		retVal.add("-Dpath_condition_target=LAST_ONLY");
		retVal.add("-Dpost_condition_check=true");
		retVal.add("-Dsushi_statistics=true");
		retVal.add("-Dinline=true");
		retVal.add("-Dsushi_modifiers_local_search=true");
		retVal.add("-Duse_minimizer_during_crossover=false");
		retVal.add("-Davoid_replicas_of_individuals=true");
		retVal.add("-Dno_change_iterations_before_reset=30");
		// retVal.add("-Dno_runtime_dependency");
		retVal.add("-Dpath_condition_evaluators_dir=" + outputDir);
		retVal.add("-Demit_tests_incrementally=true");
		retVal.add("-Dcrossover_function=SUSHI_HYBRID");
		retVal.add("-Dalgorithm=DYNAMOSA");
		retVal.add("-generateMOSuite");
		retVal.add("-Dpath_condition=" + evaluatorDefsForEvoSuite);
		retVal.add("-Dcheck_path_conditions_only_for_direct_calls=true");

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

	/**
	 * Creates a new evaluator for the given {@code method}.
	 *
	 * @param method                   method for which an evaluator will be
	 *                                 created, must not be null
	 * @param guardString              the condition that the evaluator must
	 *                                 address, must not be null
	 * @param evaluatorName            name of the file where the newly created
	 *                                 evaluator is saved, must not be null
	 * @param outputDir
	 * @param classpathForCompilation
	 * @param lookForPostCondViolation
	 */
	private static void createEvaluator(DocumentedExecutable method, String guards[], String postConds[],
			boolean lookForPostCondViolation, String evaluatorName, Path outputDir, String classpathForCompilation) {
		Checks.nonNullParameter(method, "method");
		Checks.nonNullParameter(guards, "guardStrings");
		Checks.nonNullParameter(evaluatorName, "evaluatorName");

		final InputStream evaluatorTemplate = ClassLoader.getSystemResourceAsStream(EVALUATOR_TEMPLATE_NAME + ".java");
		CompilationUnit cu = StaticJavaParser.parse(evaluatorTemplate);

		// Set the correct package for the newly created evaluator
		String packageName = method.getDeclaringClass().getPackage().getName();
		cu.setPackageDeclaration(new PackageDeclaration(StaticJavaParser.parseName(packageName)));
		cu.findFirst(ClassOrInterfaceDeclaration.class, c -> c.getNameAsString().equals(EVALUATOR_TEMPLATE_NAME))
				.ifPresent(c -> c.setName(evaluatorName));

		// Set the correct name for the newly created evaluator
		cu.findFirst(ConstructorDeclaration.class, c -> c.getNameAsString().equals(EVALUATOR_TEMPLATE_NAME))
				.ifPresent(c -> c.setName(evaluatorName));

		// Customize and emit the evaluator
		new EvaluatorModifierVisitor().visit(cu,
				new EvaluatorModifierVisitor.InstrumentationData(method, guards, postConds, lookForPostCondViolation));
		final Path evaluatorFolder = outputDir.resolve(packageName.replace('.', '/'));
		final Path evaluatorPath = evaluatorFolder.resolve(evaluatorName + ".java");
		try (FileOutputStream output = new FileOutputStream(new File(evaluatorPath.toString()))) {
			output.write(cu.toString().getBytes());
		} catch (IOException e) {
			log.error("Error while writing the evaluator to file: " + evaluatorPath, e);
		}

		// compile the evaluator
		final Path javacLogFilePath = evaluatorFolder.resolve("javac-log-" + evaluatorName + ".txt");
		final String[] javacParameters = { "-cp", classpathForCompilation, "-d", outputDir.toString(),
				evaluatorPath.toString() };
		try (final OutputStream w = new BufferedOutputStream(Files.newOutputStream(javacLogFilePath))) {
			compiler.run(null, w, w, javacParameters);
		} catch (IOException e) {
			log.error("[Test generator] Unexpected I/O error while creating evaluator compilation log file "
					+ javacLogFilePath.toString() + ": " + e);
			throw new RuntimeException(e);
		}

	}

	private static String bytecodeStyleSignature(DocumentedExecutable method) {
		boolean isConstructor = method.isConstructor();
		String formatted = isConstructor ? "<init>" : method.getName();
		formatted += "(";
		for (DocumentedParameter p : method.getParameters()) {
			formatted += bytecodeStyleType(p.getType().getName());
		}
		formatted += ")";
		if (isConstructor) {
			formatted += "V";
		} else {
			String sig = method.getExecutable().toString();
			String rawReturnType = sig.substring(0, sig.indexOf(method.getName() + "("));
			rawReturnType = rawReturnType.substring(0, rawReturnType.lastIndexOf(' '));
			rawReturnType = rawReturnType.substring(rawReturnType.lastIndexOf(' ') + 1);
			// String clazz = method.getDeclaringClass().getName();
			// String rawReturnType = sig.substring(0, sig.indexOf(clazz) - 1);
			// rawReturnType = rawReturnType.substring(rawReturnType.lastIndexOf(' ') + 1);
			formatted += bytecodeStyleType(rawReturnType);
		}
		return formatted;
	}

	private static String bytecodeStyleType(String typeName) {
		if (typeName.equals("int") || typeName.equals("I")) {
			return "I";
		} else if (typeName.equals("byte") || typeName.equals("B")) {
			return "B";
		} else if (typeName.equals("short") || typeName.equals("S")) {
			return "S";
		} else if (typeName.equals("long") || typeName.equals("J")) {
			return "J";
		} else if (typeName.equals("char") || typeName.equals("C")) {
			return "C";
		} else if (typeName.equals("boolean") || typeName.equals("Z")) {
			return "Z";
		} else if (typeName.equals("double") || typeName.equals("D")) {
			return "D";
		} else if (typeName.equals("float") || typeName.equals("F")) {
			return "F";
		} else if (typeName.equals("void") || typeName.equals("V")) {
			return "V";
		} else if (typeName.charAt(0) == '[' || typeName.endsWith("[]")) {
			String arrayItemType = typeName.charAt(0) == '[' ? typeName.substring(1)
					: typeName.substring(0, typeName.length() - 2);
			return "[" + bytecodeStyleType(arrayItemType);
		} else {
			typeName = pruneTypeErasure(typeName);
			typeName = typeName.replace('.', '/');
			if (typeName.charAt(0) != 'L') {
				typeName = "L" + typeName + ";";
			}
			return typeName;
		}
	}

	private static String pruneTypeErasure(String typeOrStringThatContainsTypes) {
		String stringWithRawTypes = typeOrStringThatContainsTypes;
		ArrayList<Integer> beingAt_stack = new ArrayList<>();
		for (int i = 0; i < stringWithRawTypes.length(); ++i) {
			char c = stringWithRawTypes.charAt(i);
			if (c == '<') {
				beingAt_stack.add(i);
			} else if (c == '>') {
				if (beingAt_stack.isEmpty()) {
					throw new RuntimeException("Shuld never happen: Wrong generic-type nesting");
				}
				int beginAt = beingAt_stack.remove(beingAt_stack.size() - 1);
				int endAt = i;
				if (beingAt_stack.isEmpty()) {
					stringWithRawTypes = stringWithRawTypes.substring(0, beginAt)
							+ stringWithRawTypes.substring(endAt + 1);
					i = beginAt - 1;
				} // else: skip nested type erasure
			}
		}
		return stringWithRawTypes;
	}

}
