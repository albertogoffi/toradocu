package org.toradocu.generator;

import static org.toradocu.Toradocu.configuration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.util.Checks;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.Specification;

/**
 * The test case generator. The method {@code createTests} of this class creates the test cases for a
 * list of {@code ExecutableMember}.
 */
public class TestGenerator {
	private static String EVALUATORS_FOLDER = "evaluators";
	private static String TESTCASES_FOLDER = "testcases";
	private static String EVALUATOR_TEMPLATE_NAME = "EvoSuiteEvaluator_Template";	
	private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

  /** {@code Logger} for this class. */
  private static final Logger log = LoggerFactory.getLogger(TestGenerator.class);

  /**
   * Creates evaluators to allow EvoSuite to check fitness wrt the given {@code specs}, and then launches evosuite 
   * with the evaluators as input to generate the test cases. This method creates one evaluator for each
   * method with specifications.
   *
   * <p>Created evaluators allow EvoSuite to check fitness wrt the given {@code specs}, and then launches evosuite 
   * with the evaluators as input to generate the test cases.
   *
   * @param specifications the specifications to be tested. Must not be null.
   */
  public static void createTests(Map<DocumentedExecutable, OperationSpecification> specifications)
		  throws IOException {
	  Checks.nonNullParameter(specifications, "specifications");

	  // Create output directory where evaluators are saved.
	  final Path evaluatorsDir = Paths.get(configuration.getTestOutputDir()).resolve(EVALUATORS_FOLDER);
	  final boolean evaluatorsDirCreationSucceeded = createOutputDir(evaluatorsDir.toString());
	  if (!evaluatorsDirCreationSucceeded || specifications.isEmpty()) {
		  log.error("Test generation failed, cannot create dir:" + evaluatorsDir);
		  return;
	  }

	  // Create output directory where test cases are saved.
	  final Path testsDir = Paths.get(configuration.getTestOutputDir()).resolve(TESTCASES_FOLDER);
	  final boolean testsDirCreationSucceeded = createOutputDir(testsDir.toString());
	  if (!testsDirCreationSucceeded || specifications.isEmpty()) {
		  log.error("Test generation failed, cannot create dir:" + testsDir);
		  return;
	  }

	  // Create evaluators
	  int evaluatorNumber = 1;
	  String classpathTarget = ".";
	  for (URL cp: configuration.classDirs) {
		  classpathTarget += ":" + cp.getPath();
	  }
	  String evaluatorDefsForEvoSuite = "";
	  for (DocumentedExecutable method : specifications.keySet()) {
		  String formattedMethodSignature = bytecodeStyleSignature(method);
		  OperationSpecification specification = specifications.get(method);
		  if (!specification.isEmpty()) {
			  ArrayList<Specification> allMethodSpecs = new ArrayList<>();
			  allMethodSpecs.addAll(specification.getThrowsSpecifications());
			  allMethodSpecs.addAll(specification.getPreSpecifications());
			  allMethodSpecs.addAll(specification.getPostSpecifications());
			  boolean packageDirCreated = false;
			  String packageName = method.getDeclaringClass().getPackage().getName(); 
			  String packageDir = packageName.replace('.', File.separator.charAt(0));
			  for (Specification spec : allMethodSpecs) {
				  String guardString = spec.getGuard().getConditionText();
				  if (guardString.isEmpty()) {
					  continue; 
				  }    		  
				  String evaluatorName = "EvoSuiteEvaluator_" + evaluatorNumber++;
				  if (!packageDirCreated) {
					  final boolean packageDirCreationSucceeded = createOutputDir(evaluatorsDir + File.separator + packageDir);
					  if (!packageDirCreationSucceeded) {
						  throw new RuntimeException("Problems with creating package dir: " + packageDir);
					  }
				  }
				  createEvaluator(method, guardString, evaluatorName, evaluatorsDir, classpathTarget);
				  
				  evaluatorDefsForEvoSuite += ":" + method.getDeclaringClass().getCanonicalName() + "," + formattedMethodSignature + "," + 
						  (packageName.isEmpty() ? "" : packageName + ".") + evaluatorName; 
			  }
		  }
	  }
	  if (!evaluatorDefsForEvoSuite.isEmpty()) {
		  evaluatorDefsForEvoSuite = evaluatorDefsForEvoSuite.substring(1); //remove leading ':'
	  }

      // Launch EvoSuite
	  List<String> evosuiteCommand = buildEvoSuiteCommand(evaluatorDefsForEvoSuite, evaluatorsDir, testsDir);
      final Path evosuiteLogFilePath = evaluatorsDir.resolve("evosuite-log.txt");
      try {
    	  Process processEvosuite = launchProcess(evosuiteCommand, evosuiteLogFilePath);
          System.out.println("[EVOSUITE] Launched EvoSuite process, command line: " + evosuiteCommand.stream().reduce("", (s1, s2) -> { return s1 + " " + s2; }));
          try {
              processEvosuite.waitFor();
          } catch (InterruptedException e) {
              //the performer was shut down: kill the EvoSuite job
              processEvosuite.destroy();
          }
      } catch (IOException e) {
          System.err.println("[EVOSUITE] Unexpected I/O error while running EvoSuite: " + e);
          throw new RuntimeException(e);
      }
  }


  /**
   * Creates and launches an external process.
   * 
   * @param commandLine a {@link List}{@code <}{@link String}{@code >}, the command line
   *        to launch the process in the format expected by {@link ProcessBuilder}.
   * @param logFilePath a {@link Path} to a log file where stdout and stderr of the
   *        process will be redirected.
   * @return the created {@link Process}.
   * @throws IOException if thrown by {@link ProcessBuilder#start()}.
   */
  private static Process launchProcess(List<String> commandLine, Path logFilePath) throws IOException {
      final ProcessBuilder pb = new ProcessBuilder(commandLine).redirectErrorStream(true).redirectOutput(logFilePath.toFile());
      final Process pr = pb.start();
      return pr;
  }

  /**
   * Builds the command line for invoking EvoSuite.
   * @param evaluatorDefsForEvoSuite 
   * @param outputDir 
   * @param testsDir 
   * 
   * @return a command line in the format of a {@link List}{@code <}{@link String}{@code >},
   *         suitable to be passed to a {@link ProcessBuilder}.
   */
  private static List<String> buildEvoSuiteCommand(String evaluatorDefsForEvoSuite, Path outputDir, Path testsDir) {
	  final String targetClass = configuration.getTargetClass();
	  final List<String> retVal = new ArrayList<String>();
	  String classpathTarget = outputDir.toString();
	  for (URL cp: configuration.classDirs) {
		  classpathTarget += ":" + cp.getPath();
	  }
	  retVal.add("java");
	  retVal.add("-Xmx4G");
	  retVal.add("-jar");
	  retVal.add(configuration.getEvoSuiteJar());
	  retVal.add("-class");
	  retVal.add(targetClass);
	  retVal.add("-mem");
	  retVal.add("2048");
	  retVal.add("-DCP=" + classpathTarget); 
	  retVal.add("-Dassertions=false");
	  retVal.add("-Dglobal_timeout=" + configuration.getEvoSuiteBudget());
	  retVal.add("-Dsearch_budget=" + configuration.getEvoSuiteBudget());
	  retVal.add("-Dreport_dir=" + outputDir);
	  retVal.add("-Dtest_dir=" + testsDir);
	  retVal.add("-Dvirtual_fs=false");
	  retVal.add("-Dselection_function=ROULETTEWHEEL");
	  retVal.add("-Dcriterion=PATHCONDITION");		
	  retVal.add("-Dsushi_statistics=true");
	  retVal.add("-Dinline=true");
	  retVal.add("-Dsushi_modifiers_local_search=true");
	  retVal.add("-Duse_minimizer_during_crossover=true");
	  retVal.add("-Davoid_replicas_of_individuals=true"); 
	  retVal.add("-Dno_change_iterations_before_reset=30");
	  retVal.add("-Dno_runtime_dependency");
	  retVal.add("-Dpath_condition_evaluators_dir=" + outputDir);
	  retVal.add("-Demit_tests_incrementally=true");
	  retVal.add("-Dcrossover_function=SUSHI_HYBRID");
	  retVal.add("-Dalgorithm=DYNAMOSA");
	  retVal.add("-generateMOSuite");
	  retVal.add("-Dpath_condition=" + evaluatorDefsForEvoSuite);
	  return retVal;
  }
  
  /**
   * Creates the directory specified by {@code outputDir}.
   *
   * @param outputDir the directory to be created
   * @return {@code true} if the creation succeeded, {@code false} otherwise
   */
  private static boolean createOutputDir(String outputDir) {
    boolean creationSucceeded;
    final File outDir = new File(outputDir);
    if (outDir.exists()) {
      log.info("Directory " + outputDir + " already exists: " + outputDir);
      creationSucceeded = true;
    } else {
      creationSucceeded = outDir.mkdirs();
      if (!creationSucceeded) {
        log.error("Error during creation of directory: " + outputDir);
      }
    }
    return creationSucceeded;
  }

  /**
   * Creates a new evaluator for the given {@code method}.
   *
   * @param method method for which an evaluator will be created, must not be null
   * @param guardString the condition that the evaluator must address, must not be null
   * @param evaluatorName name of the file where the newly created evaluator is saved, must not be null
   * @param outputDir 
   * @param classpathForCompilation 
   */
  private static void createEvaluator(
      DocumentedExecutable method, String guardString, String evaluatorName, Path outputDir, String classpathForCompilation) {
    Checks.nonNullParameter(method, "method");
    Checks.nonNullParameter(guardString, "guardString");
    Checks.nonNullParameter(evaluatorName, "evaluatorName");

    final InputStream evaluatorTemplate =
        Object.class.getResourceAsStream("/" + EVALUATOR_TEMPLATE_NAME + ".java");
    CompilationUnit cu = JavaParser.parse(evaluatorTemplate);

    // Set the correct package for the newly created evaluator
    String packageName = method.getDeclaringClass().getPackage().getName();
    cu.setPackageDeclaration(new PackageDeclaration(JavaParser.parseName(packageName)));
    cu.findFirst(
            ClassOrInterfaceDeclaration.class, c -> c.getNameAsString().equals(EVALUATOR_TEMPLATE_NAME))
        .ifPresent(c -> c.setName(evaluatorName));

    // Set the correct name for the newly created evaluator
    cu.findFirst(
            ConstructorDeclaration.class, c -> c.getNameAsString().equals(EVALUATOR_TEMPLATE_NAME))
        .ifPresent(c -> c.setName(evaluatorName));
    new EvaluatorModifierVisitor().visit(cu, Pair.of(method, guardString));
    
    // Set the correct input parameters for the newly created evaluator
    if (!Modifier.isStatic(method.getExecutable().getModifiers())) {
    	String type = method.getDeclaringClass().getName();
    	String name = "___receiver__object___";
		cu.findFirst(MethodDeclaration.class, c -> c.getNameAsString().equals("test0"))
		.ifPresent(c -> c.addParameter(type, name));    	    	
		
	    cu.findFirst(MethodDeclaration.class, c -> c.getNameAsString().equals("test0")).get().getBody()
	    .ifPresent(c -> c.addStatement(0, JavaParser.parseStatement("receiverObjectID = " + name + ";")));
    }
    String sig = method.getSignature();
    sig = sig.substring(sig.indexOf('(') + 1, sig.indexOf(')'));
    if (!sig.isEmpty()) {
    	String[] params = sig.split(", ");
    	String allNames = "";
    	for (String p: params) {
    		String type = p.substring(0, p.indexOf(' '));
    		String name = p.substring(p.indexOf(' ') + 1);
    		cu.findFirst(MethodDeclaration.class, c -> c.getNameAsString().equals("test0"))
    		.ifPresent(c -> c.addParameter(type, name));  
    		allNames += (allNames.isEmpty() ? "" : ", ") + name;
    	}
    	final String allNames0 = allNames;
	    cu.findFirst(MethodDeclaration.class, c -> c.getNameAsString().equals("test0")).get().getBody()
	    .ifPresent(c -> c.addStatement(0, JavaParser.parseStatement("args = new Object[] {" + allNames0 + "};")));
    }


    final Path evaluatorFolder = outputDir.resolve(packageName.replace('.', '/'));
    final Path evaluatorPath = evaluatorFolder.resolve(evaluatorName + ".java");
    try (FileOutputStream output = new FileOutputStream(new File(evaluatorPath.toString()))) {
      output.write(cu.toString().getBytes());
    } catch (IOException e) {
      log.error("Error while writing the evaluator to file: " + evaluatorPath, e);
    }
    
	//compile the evaluator
	final Path javacLogFilePath = evaluatorFolder.resolve("javac-log-" + evaluatorName + ".txt");
	final String[] javacParameters = { "-cp", classpathForCompilation, "-d", outputDir.toString(), evaluatorPath.toString()};
	try (final OutputStream w = new BufferedOutputStream(Files.newOutputStream(javacLogFilePath))) {
		compiler.run(null, w, w, javacParameters);
	} catch (IOException e) {
		System.err.println("[Test generator] Unexpected I/O error while creating evaluator compilation log file " + javacLogFilePath.toString() + ": " + e);
		throw new RuntimeException(e);
	}
    
  }
  
  private static String bytecodeStyleSignature(DocumentedExecutable method) {
	  String formattedMethodSignature =  method.getSignature() + method.getReturnType().getType();
	  formattedMethodSignature = formattedMethodSignature.replaceAll("( |\\(|\\))([^ |\\)]+)( )?", "$1L$2;$3");//wrap types as in L<nome>;
	  formattedMethodSignature = formattedMethodSignature.replaceAll(" [^ |\\)]+( |\\))", "$1");//remove parameter names
	  formattedMethodSignature = formattedMethodSignature.replaceAll("(L[^ |\\)]+)\\[\\];", "\\[$1;");//handle array types
	  formattedMethodSignature = formattedMethodSignature.replaceAll(" ", ""); //remove remaining blank spaces
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Lint;", "I"); //replace primitive types
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Lshort;", "S");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Llong;", "J");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Lchar;", "C");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Lboolean;", "Z");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Ldouble;", "D");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Lfloat;", "F");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("Lvoid;", "V");
	  formattedMethodSignature = formattedMethodSignature.replaceAll("\\.", "/");//replace dots
	  return formattedMethodSignature;
  }

}
