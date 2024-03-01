package org.toradocu.generator;

import static org.toradocu.Toradocu.configuration;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.VoidVisitor;
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
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedExecutable;
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
		String output = configuration.getTestOutputDir();
		if (output.contains(configuration.getTargetClass())) {
			output = output.substring(0, output.indexOf(configuration.getTargetClass()));
		}
		final Path outputDir = Paths.get(output);

		// Create output directory where test cases are saved.

		final Path testsDir = outputDir.resolve(TESTCASES_FOLDER);
		final boolean testsDirCreationSucceeded = createOutputDir(testsDir.toString(), false);
		if (!testsDirCreationSucceeded || specifications.isEmpty()) {
			log.error("Test generation failed, cannot create dir:" + testsDir);
			return;
		}

		// Step 1/2: Generate test cases for the target class by launching EvoSuite
		String targetClass = configuration.getTargetClass();
		log.info("Going to generate validation test cases for " + targetClass + " oracles");

		// Launch EvoSuite
		List<String> evosuiteCommand = buildEvoSuiteCommand(outputDir, testsDir);
		final Path evosuiteLogFilePath = testsDir.resolve("evosuite-log-" + targetClass + ".txt");
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
			// Create fake test case for reporting purposes
			String testClaxName = testName.substring(testName.lastIndexOf(".") + 1);
			String testClaxPackage = testName.substring(0, testName.lastIndexOf("."));
			CompilationUnit cunew = new CompilationUnit();
			cunew.addClass(testClaxName);
			cunew.setPackageDeclaration(testClaxPackage);
			// write out the enriched test case
			try (FileOutputStream output = new FileOutputStream(currentTestCase)) {
				output.write(cunew.toString().getBytes());
			} catch (IOException e) {
				log.error("Error while creating empty test case: " + currentTestCase, e);
			}
			// return; // nothing to do, since EvoSuite failed to generate this test case
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
			throw new RuntimeException("Unexpected test structure");
		else {
			ClassOrInterfaceDeclaration clax = claxes.get(0);
			ClassOrInterfaceType uniqueGuardIdsType = StaticJavaParser
					.parseClassOrInterfaceType("java.util.HashSet<String>");
			Expression uniqueGuardIdsIdsInit = StaticJavaParser.parseExpression("new java.util.HashSet<String>()");
			clax.addFieldWithInitializer(uniqueGuardIdsType, "uniqueGuardIds_lta", uniqueGuardIdsIdsInit,
					Keyword.PUBLIC);

			// Create and initialize violatedPreconds and failedConds
			clax.addFieldWithInitializer(PrimitiveType.intType(), "violatedPreconds",
					StaticJavaParser.parseExpression("0"), Keyword.PUBLIC, Keyword.STATIC);
			clax.addFieldWithInitializer(PrimitiveType.intType(), "failedConds", StaticJavaParser.parseExpression("0"),
					Keyword.PUBLIC, Keyword.STATIC);

			// Add contracts method
			MethodDeclaration mdContracts = clax.addMethod("contracts", Modifier.Keyword.PUBLIC,
					Modifier.Keyword.STATIC);
			mdContracts.setType("String []");
			BlockStmt contractsBlock = mdContracts.createBody();
			ReturnStmt returnContractsStmt = new ReturnStmt();
			contractsBlock.addStatement(returnContractsStmt);
			ArrayCreationExpr contractsArray = new ArrayCreationExpr();
			contractsArray.setElementType("String");
			ArrayInitializerExpr contractsArrayInit = new ArrayInitializerExpr();
			contractsArray.setInitializer(contractsArrayInit);
			returnContractsStmt.setExpression(contractsArray);
			NodeList<Expression> contracts = contractsArrayInit.getValues();

			// Add beforeClass method
			ClassOrInterfaceType globalGuardsIdsType = StaticJavaParser
					.parseClassOrInterfaceType("java.util.HashMap<String, String>");
			Expression globalGuardsIdsInit = StaticJavaParser
					.parseExpression("new java.util.HashMap<String, String>()");
			clax.addFieldWithInitializer(globalGuardsIdsType, "globalGuardsIds_lta", globalGuardsIdsInit,
					Keyword.PUBLIC, Keyword.STATIC);
			MethodDeclaration mdInit = clax.addMethod("init", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			mdInit.setType(new com.github.javaparser.ast.type.VoidType());
			mdInit.addAnnotation(new MarkerAnnotationExpr("org.junit.BeforeClass"));
			BlockStmt bs = mdInit.createBody();

			int identifier = 0;
			int specificationCounter = 0;
			for (DocumentedExecutable targetMethod : allSpecs.keySet()) {
				OperationSpecification os = allSpecs.get(targetMethod);
				if (os.getPostSpecifications().size() != 0 || os.getThrowsSpecifications().size() != 0) {
					List<ExpressionStmt> methodCallsToEnrich = identifyMethodCallsToEnrich(cu, targetMethod);
					ArrayList<Specification> targetSpecs = new ArrayList<>();
					targetSpecs.addAll(os.getThrowsSpecifications());
					targetSpecs.addAll(os.getPostSpecifications());
					for (Specification spec : targetSpecs) {
						contracts.add(new StringLiteralExpr(StringEscapeUtils.escapeJava(spec.toString())));
						if (methodCallsToEnrich.size() != 0) {
							bs.addStatement(
									"globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"not-executed\");");
							identifier = enrichTestWithOracle(spec, targetMethod, methodCallsToEnrich, allSpecs,
									identifier, specificationCounter);
						} else {
							bs.addStatement(
									"globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"not-present\");");
						}
						specificationCounter++;
					}
				}
			}

			// Add AfterClass
			mdInit = clax.addMethod("generateReport", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
			mdInit.setType(new com.github.javaparser.ast.type.VoidType());
			mdInit.addAnnotation(new MarkerAnnotationExpr("org.junit.AfterClass"));
			BlockStmt bs2 = mdInit.createBody();
			bs2.addStatement("lta.test.utils.TestUtils.report(globalGuardsIds_lta, \"" + targetClass
					+ "\", contracts(), violatedPreconds, failedConds);");

			// write out the enriched test case
			try (FileOutputStream output = new FileOutputStream(currentTestCase)) {
				output.write(cu.toString().getBytes());
			} catch (IOException e) {
				log.error("Error while writing the enriched test case to file: " + currentTestCase, e);
			}
		}
	}

	private static List<ExpressionStmt> identifyMethodCallsToEnrich(CompilationUnit cu,
			DocumentedExecutable targetMethod) {
		List<ExpressionStmt> callsToTargetMethodTest = new ArrayList<ExpressionStmt>();
		SupportStructure ss = new SupportStructure(targetMethod, callsToTargetMethodTest);
		VoidVisitor<SupportStructure> visitor = new IdentifyCallsToEnrichVisitor();
		visitor.visit(cu, ss);
		return ss.getTargetCallsList();
	}

	private static int enrichTestWithOracle(Specification spec, DocumentedExecutable targetMethod,
			List<ExpressionStmt> methodCallsToEnrich, Map<DocumentedExecutable, OperationSpecification> allSpecs,
			int identifier, int specificationCounter) {
		String targetMethodName = targetMethod.getName().substring(targetMethod.getName().lastIndexOf('.') + 1);
		for (int i = 0; i < methodCallsToEnrich.size(); i++) {
			ExpressionStmt targetCall = methodCallsToEnrich.get(i);
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
				addSkipClause(insertionPoint, targetCall, identifier, specificationCounter, "unmodeled", comment);
				identifier++;
				continue;
			}

			String guardsComment = "Automatically generated test oracle with guard:" + spec.getGuard().toString();
			for (PreSpecification precond : allSpecs.get(targetMethod).getPreSpecifications()) {
				clause = composeGuardClause(precond, targetCall, insertionPoint, targetMethodName, clause);
				// Skip unmodeled guard
				if (clause.contains("SKIP_UNMODELED")) {
					String comment = "Skipped check due to unmodeled guard precondition: " + precond.getGuard() + " "
							+ spec.getDescription();
					addSkipClause(insertionPoint, targetCall, identifier, specificationCounter, "unmodeled", comment);
					identifier++;
					continue;
				}
				guardsComment = guardsComment + precond.getDescription();
			}

			// Count number of violated preconditions
			String precondStr = "", precondComment = "";
			for (PreSpecification precond : allSpecs.get(targetMethod).getPreSpecifications()) {
				precondStr = composeGuardClause(precond, targetCall, insertionPoint, targetMethodName, precondStr);
				precondComment = precondComment + precond.getDescription();
			}
			if (!precondStr.isEmpty()) {
				precondStr = "!(" + precondStr + ")";
				IfStmt precondCheck = new IfStmt();
				precondCheck.setCondition(StaticJavaParser.parseExpression(precondStr));
				precondCheck.setThenStmt(StaticJavaParser.parseStatement("violatedPreconds++;"));
				precondCheck.setLineComment(precondComment);
				insertionPoint.addBefore(precondCheck, targetCall);
			}

			// Skip unmodeled guard
			if (clause.contains("SKIP_UNMODELED")) {
				String comment = "Skipped check due to unmodeled clause. THIS SHOULDN'T HAPPEN BECAUSE UNMODELED CHECKS HAVE LAREADY BEEN PERFORMED."
						+ spec.getDescription();
				addSkipClause(insertionPoint, targetCall, identifier, specificationCounter, "unmodeled", comment);
				identifier++;
				continue;
			}

			// Skip non satisfiable pre-conditions
			if (clause.contains("null.")) {
				String comment = "Skipped check due to non satisfiable pre-conditions: " + clause + " "
						+ spec.getDescription();
				addSkipClause(insertionPoint, targetCall, identifier, specificationCounter, "not-executable", comment);
				identifier++;
				continue;
			}
			addIfGuard(clause, targetCall, targetMethod.getReturnType().getType(), insertionPoint, identifier,
					guardsComment);

			// add assert
			if (spec instanceof PostSpecification) {
				// the postCondition of the oracle, plus all postConditions with empty guards
				ExpressionStmt newTargetCall = addAssertClause((PostSpecification) spec, clause, targetCall,
						insertionPoint, targetMethodName, targetMethod.getReturnType().getType(), false, identifier,
						specificationCounter);
				methodCallsToEnrich.set(i, newTargetCall);
				targetCall = newTargetCall;
			} else if (spec instanceof ThrowsSpecification) {
				// the try-catch block to check for the expected
				addFailClause((ThrowsSpecification) spec, clause, targetCall, insertionPoint, targetMethodName,
						targetMethod.getReturnType().getType(), false, identifier, specificationCounter);
			} else {
				// exception
				throw new RuntimeException(
						"Spec of unexpected type " + spec.getClass().getName() + ": " + spec.getDescription());
			}
			identifier++;
		}
		return identifier;
	}

	private static void addSkipClause(NodeList<Statement> insertionPoint, ExpressionStmt targetCall, int identifier,
			int specificationCounter, String type, String comment) {

		Statement uniqueStmt = StaticJavaParser.parseStatement("uniqueGuardIds_lta.add(\"" + identifier + "\");");
		uniqueStmt.setComment(new LineComment(comment));
		insertionPoint.addBefore(uniqueStmt, targetCall);

		IfStmt ifContractStatus = new IfStmt();
		ifContractStatus.setCondition(StaticJavaParser
				.parseExpression("globalGuardsIds_lta.get(\"" + specificationCounter + "\").equals(\"not-executed\")"));
		ifContractStatus.setThenStmt(new BlockStmt()
				.addStatement("globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"" + type + "\");"));
		insertionPoint.addBefore(ifContractStatus, targetCall);

	}

	private static void addIfGuard(String clause, ExpressionStmt targetCall, Type targetCallReturnType,
			NodeList<Statement> insertionPoint, int identifier, String guardsComment) {
		if (!clause.isEmpty()) {
			Expression clauseExp = StaticJavaParser.parseExpression(clause);
			IfStmt i = new IfStmt();
			i.setCondition(clauseExp);
			MethodCallExpr mce = new MethodCallExpr();
			mce.setScope(StaticJavaParser.parseExpression("uniqueGuardIds_lta"));
			mce.setName("add");
			mce.addArgument(new StringLiteralExpr(Integer.toString(identifier)));
			BlockStmt bs = new BlockStmt();
			bs.addStatement(mce);
			i.setThenStmt(bs);
			i.setLineComment(guardsComment);
			try {
				insertionPoint.addBefore(i, targetCall);
			} catch (Throwable e) {
				String targetCallReturnTypeName = targetCallReturnType.getTypeName().replaceAll("<[A-Za-z_$]+>", "<?>");
				String assignStmt = targetCallReturnTypeName + " _methodResult__ = " + targetCall;
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
			boolean isGeneralPostCond, int identifier, int specificationCounter) {
		Statement assertStmt = StaticJavaParser.parseStatement(
				"if(uniqueGuardIds_lta.contains(\"" + identifier + "\")) {failedConds++;globalGuardsIds_lta.put(\""
						+ specificationCounter + "\",\"fail\");org.junit.Assert.fail();}");
		String comment = "Automatically generated test oracle:" + postCond.getDescription();
		assertStmt.setLineComment(comment);
		insertionPoint.addAfter(assertStmt, targetCall);
		Optional<Node> n = insertionPoint.getParentNode();
		if (n.isPresent()) {
			n = n.get().getParentNode();
			if (n.isPresent() && (n.get() instanceof TryStmt)) {
				TryStmt ts = (TryStmt) n.get();
				for (CatchClause cc : ts.getCatchClauses()) {
					cc.getParameter().setType(Throwable.class);

					BlockStmt bs = cc.getBody();
					IfStmt ifUniqueGuard = new IfStmt();
					ifUniqueGuard.setCondition(
							StaticJavaParser.parseExpression("uniqueGuardIds_lta.contains(\"" + identifier + "\")"));
					IfStmt ifContractStatus = new IfStmt();
					ifContractStatus.setCondition(StaticJavaParser.parseExpression(
							cc.getParameter().getName() + " instanceof " + postCond.getExceptionTypeName()));
					IfStmt thenIfContractStatus = new IfStmt();
					thenIfContractStatus.setCondition(StaticJavaParser.parseExpression(
							"!globalGuardsIds_lta.get(\"" + specificationCounter + "\").equals(\"fail\")"));
					thenIfContractStatus.setThenStmt(StaticJavaParser
							.parseStatement("globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"pass\");"));
					ifContractStatus.setThenStmt(new BlockStmt().addStatement(thenIfContractStatus));
					BlockStmt elseIfContractStatus = new BlockStmt();
					elseIfContractStatus.addStatement(StaticJavaParser
							.parseStatement("globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"fail\");"));
					elseIfContractStatus.addStatement(StaticJavaParser.parseStatement("failedConds++;"));
					ifContractStatus.setElseStmt(elseIfContractStatus);
					BlockStmt thenIfUniqueGuard = new BlockStmt();
					thenIfUniqueGuard.addStatement(ifContractStatus);
					thenIfUniqueGuard.addStatement("org.junit.Assert.assertTrue(" + cc.getParameter().getName()
							+ " instanceof " + postCond.getExceptionTypeName() + ");");
					ifUniqueGuard.setThenStmt(thenIfUniqueGuard);

					String comment2 = "Automatically generated test oracle:" + postCond.getDescription();
					ifUniqueGuard.setLineComment(comment2);
					bs.addStatement(ifUniqueGuard);
					cc.setBody(bs);
				}
			}
		}
	}

	private static ExpressionStmt addAssertClause(PostSpecification postCond, String clause, ExpressionStmt targetCall,
			NodeList<Statement> insertionPoint, String targetMethodName, Type targetMethodReturnType,
			boolean isGeneralPostCond, int identifier, int specificationCounter) {
		ExpressionStmt targetCallToConsider = targetCall;
		String postCondCondition = postCond.getProperty().getConditionText();
		String oracle = null;
		if (postCondCondition != null && !postCondCondition.isEmpty()) {
			oracle = replaceFormalParamsWithActualValues(postCondCondition, targetCall);
		}
		if (oracle.contains("_methodResult__") && !targetCall.toString().contains("_methodResult__ =")) {
			// targetCall has a return value which is currently not assigned to any variable
			// in the test case
			String targetCallReturnTypeName = targetMethodReturnType.getTypeName().replaceAll("<[A-Za-z_$]+>", "<?>");
			String assignStmt = targetCallReturnTypeName + " _methodResult__ = " + targetCall;
			try {
				ExpressionStmt targetCallWithAssignment = StaticJavaParser.parseStatement(assignStmt)
						.asExpressionStmt();
				Optional<Node> n = targetCall.getParentNode();
				try {
					Node parent = n.get();
					targetCallWithAssignment.setParentNode(parent);
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
		IfStmt ifUniqueGuard = new IfStmt();
		ifUniqueGuard
				.setCondition(StaticJavaParser.parseExpression("uniqueGuardIds_lta.contains(\"" + identifier + "\")"));
		IfStmt ifContractStatus = new IfStmt();
		ifContractStatus.setCondition(StaticJavaParser.parseExpression(oracle));
		IfStmt thenIfContractStatus = new IfStmt();
		thenIfContractStatus.setCondition(StaticJavaParser
				.parseExpression("!globalGuardsIds_lta.get(\"" + specificationCounter + "\").equals(\"fail\")"));
		thenIfContractStatus.setThenStmt(
				StaticJavaParser.parseStatement("globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"pass\");"));
		ifContractStatus.setThenStmt(new BlockStmt().addStatement(thenIfContractStatus));
		BlockStmt elseIfContractStatus = new BlockStmt();
		elseIfContractStatus.addStatement(
				StaticJavaParser.parseStatement("globalGuardsIds_lta.put(\"" + specificationCounter + "\",\"fail\");"));
		elseIfContractStatus.addStatement(StaticJavaParser.parseStatement("failedConds++;"));
		ifContractStatus.setElseStmt(elseIfContractStatus);
		BlockStmt thenIfUniqueGuard = new BlockStmt();
		thenIfUniqueGuard.addStatement(ifContractStatus);
		thenIfUniqueGuard.addStatement("org.junit.Assert.assertTrue(" + oracle + ");");
		ifUniqueGuard.setThenStmt(thenIfUniqueGuard);

		String comment = "Automatically generated test oracle:" + postCond.getDescription();
		ifUniqueGuard.setLineComment(comment);
		insertionPoint.addAfter(ifUniqueGuard, targetCallToConsider);
		Optional<Node> n = insertionPoint.getParentNode();
		if (n.isPresent()) {
			n = n.get().getParentNode();
			if (n.isPresent() && (n.get() instanceof TryStmt)) {
				TryStmt ts = (TryStmt) n.get();
				for (CatchClause cc : ts.getCatchClauses()) {
					BlockStmt bs = cc.getBody();
					Statement assertStmt2 = StaticJavaParser.parseStatement(
							"if(uniqueGuardIds_lta.contains(\"" + identifier + "\")) {failedConds++;globalGuardsIds_lta.put(\""
									+ specificationCounter + "\",\"fail\");org.junit.Assert.fail();}");
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
	private static List<String> buildEvoSuiteCommand(Path outputDir, Path testsDir) {
		final String targetClass = configuration.getTargetClass();
		final List<String> retVal = new ArrayList<String>();
		String classpathTarget = outputDir.toString();
		for (URL cp : configuration.classDirs) {
			classpathTarget += ":" + cp.getPath();
		}
		retVal.add("/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java");
		retVal.add("-Xmx16G");
		// enabled assertions since evosuite is generating failing test cases for them
		// retVal.add("-ea");
		retVal.add("-jar");
		retVal.add(configuration.getEvoSuiteJar());
		// retVal.add("/mnt/Backup/toradocu/lib-evosuite/evosuite-1.2.0.jar");
		retVal.add("-class");
		retVal.add(targetClass);
		retVal.add("-mem");
		retVal.add("16384");
		retVal.add("-DCP=" + classpathTarget);
		retVal.add("-Dassertions=false");
		retVal.add("-Dsearch_budget=" + configuration.getEvoSuiteBudget()); // configuration.getEvoSuiteBudget()
		retVal.add("-Dreport_dir=" + outputDir);
		retVal.add("-Dtest_dir=" + testsDir);
		retVal.add("-Dvirtual_fs=false");
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
