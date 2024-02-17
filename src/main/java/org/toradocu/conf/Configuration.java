package org.toradocu.conf;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.PathConverter;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.toradocu.generator.TestGeneratorSummaryData;

/**
 * Holds the configuration options (particularly command-line options) for
 * Toradocu.
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum Configuration {
	INSTANCE;

	/** Keyword that identifies receiver object in generated specifications. */
	public static final String RECEIVER = "receiverObjectID";
	/** Keyword that identifies method result in generated specifications. */
	public static final String RETURN_VALUE = "methodResultID";

	// General options

	@Parameter(names = "--target-class", description = "Fully-qualified name of the class that Toradocu should analyze", required = true)
	private String targetClass;

	@Parameter(names = "--source-dir", description = "Path of the directory containing source files of the target class", converter = PathConverter.class, required = true)
	public Path sourceDir;

	@Parameter(names = "--class-dir", description = "Specifies JAR files or directories containing binaries of the target class and"
			+ " its dependencies. Use the standard classpath separator to separate different paths.", listConverter = ClassDirsConverter.class, required = true)
	public List<URL> classDirs;

	@Parameter(names = "--debug", description = "Enable fine-grained logging", hidden = true)
	private boolean debug = false;

	@Parameter(names = { "--help", "-h" }, description = "Print a list of available options", help = true)
	private boolean help;

	@Parameter(names = "--stats-file", description = "File path to export Toradocu statistics in CSV format", converter = FileConverter.class)
	private File statsFile;

	@Parameter(names = "--silent", description = "Do not generate any output if no translation has been made", hidden = true)
	private boolean silent = false;

	@Parameter(names = "--stop-on-error", description = "Toradocu stops when there is an error in the being-analyzed Javadoc")
	public boolean stopOnError = false;

	// Javadoc extractor options

	@Parameter(names = "--javadoc-extractor-output", description = "File path to export Javadoc extractor output in JSON format", converter = FileConverter.class, hidden = true)
	private File javadocExtractorOutput;

	// Condition translator options

	@Parameter(names = "--condition-translation", description = "Enable/disable the condition translator", arity = 1, hidden = true)
	private boolean conditionTranslation = true;

	@Parameter(names = "--condition-translator-input", description = "Input file to the condition translator (this option disables the Javadoc extractor)", converter = FileConverter.class, hidden = true)
	private File conditionTranslatorInput;

	@Parameter(names = "--condition-translator-output", description = "File path to export condition translator output in JSON format", converter = FileConverter.class)
	private File conditionTranslatorOutput;

	@Parameter(names = "--distance-threshold", description = "Distance threshold: only code elements with edit distance less than this threshold"
			+ " will be considered candidates for translation", hidden = true)
	private int distanceThreshold = 2;

	@Parameter(names = "--word-removal-cost", description = "Cost of a single word deletion in the edit distance algorithm", hidden = true)
	private int wordRemovalCost = 1;

	@Parameter(names = "--expected-output", description = "Condition extractor expected output file used to compute statistics about "
			+ "Toradocu's performance. Statistics are computed only if a valid expected output file"
			+ " is provided", converter = FileConverter.class, hidden = true)
	private File expectedOutput;

	@Parameter(names = "--remove-commas", arity = 1, description = "Remove commas before the input text is parsed", hidden = true)
	private boolean removeCommas = true;

	@Parameter(names = "--tcomment", description = "Use @tComment to translate comments", hidden = true)
	private boolean tcomment = false;

	@Parameter(names = "--randoop-specs", description = "Path to file where to export comment translations as Randoop specifications.", converter = FileConverter.class, hidden = true)
	private File randoopSpecs;

	@Parameter(names = "--disable-semantics", description = "Disable semantic-based matcher for comments translation.", arity = 1)
	private boolean disableSemantics = false;

	// Aspect creation options

	@Parameter(names = "--test-generation", description = "Enable/disable the generation of the test cases", arity = 1)
	private boolean testGeneration = false;

	@Parameter(names = "--test-output-dir", description = "Specifies a directory where Toradocu will store the generated test cases")
	private String testOutputDir = "tests";

	@Parameter(names = "--evosuite-jar", description = "Specifies the path to the jar of EvoSuite")
	private String evosuiteJar = "evosuite-shaded-1.1.1-SNAPSHOT.jar";

	@Parameter(names = "--evosuite-budget", description = "Specifies the maximum time (in seconds) allowed to EvoSuite for test case generation")
	private int evosuiteBudget = 180;

	// Aspect creation options

	@Parameter(names = "--oracle-generation", description = "Enable/disable the generation of the aspects", arity = 1)
	private boolean oracleGeneration = false;

	@Parameter(names = "--test-class", description = "Fully-qualified name of the class that will be instrumented with aspects")
	private String testClass;

	@Parameter(names = "--aspects-output-dir", description = "Specifies a directory where Toradocu will output aspects")
	private String aspectsOutputDir = "aspects";

	@Parameter(names = "--validate-tests", description = "Enable the generation of test cases for comparison against Evosuite's traditional search-based algorithm", arity = 1)
	private boolean validateTests = false;

	/** File used as template for generated aspects. */
	private static final String ASPECT_TEMPLATE = "AspectTemplate.java";

	/** Aspect to instrument JUnit test cases. */
	private static final String JUNIT_TC_ASPECT = "TestCaseAspect.java";

	/**
	 * Initializes the configuration based on the given command-line options. This
	 * method must be called before Javadoc options or the temporary Javadoc output
	 * directory are retrieved.
	 */
	public void initialize() {
		if (help) {
			return; // No initialization necessary.
		}

		if (statsFile == null) {
			statsFile = new File("stats.csv");
		}
	}

	/**
	 * Returns the name of the file used as a template for generated aspects.
	 *
	 * @return the name of the file used as a template for generated aspects
	 */
	public String getAspectTemplate() {
		return ASPECT_TEMPLATE;
	}

	/**
	 * Returns the name of the aspect used to instrument JUnit test cases.
	 *
	 * @return the name of the aspect used to instrument JUnit test cases
	 */
	public String getJUnitTestCaseAspect() {
		return JUNIT_TC_ASPECT;
	}

	/**
	 * Returns the fully-qualified name of the target class to analyze with
	 * Toradocu.
	 *
	 * @return the fully-qualified name of the target class to analyze with Toradocu
	 */
	public String getTargetClass() {
		return targetClass;
	}

	/**
	 * Returns true if fine-grained logging should be enabled.
	 *
	 * @return true if fine-grained logging should be enabled
	 */
	public boolean debug() {
		return debug;
	}

	/**
	 * Returns true if the user has passed in a command-line option to print help.
	 *
	 * @return true if the user has passed in a command-line option to print help
	 */
	public boolean help() {
		return help;
	}

	/**
	 * Returns the file in which to export Javadoc extractor output or null if this
	 * file is not specified.
	 *
	 * @return the file in which to export Javadoc extractor output or null if this
	 *         file is not specified
	 */
	public File getJavadocExtractorOutput() {
		return javadocExtractorOutput;
	}

	/**
	 * Returns the input file to the condition translator or null if this file is
	 * not specified.
	 *
	 * @return the input file to the condition translator or null if this file is
	 *         not specified
	 */
	public File getConditionTranslatorInput() {
		return conditionTranslatorInput;
	}

	/**
	 * Returns the output file for the condition translator or null if this file is
	 * not specified.
	 *
	 * @return the output file for the condition translator or null if this file is
	 *         not specified
	 */
	public File getConditionTranslatorOutput() {
		return conditionTranslatorOutput;
	}

	/**
	 * Returns true if condition translation is enabled.
	 *
	 * @return true if condition translation is enabled
	 */
	public boolean isConditionTranslationEnabled() {
		return conditionTranslation;
	}

	/**
	 * Returns true if oracles (i.e. aspects) should be generated after translation.
	 *
	 * @return true if oracles (i.e. aspects) should be generated after translation
	 */
	public boolean isOracleGenerationEnabled() {
		return oracleGeneration;
	}

	/**
	 * Returns true if tests for validation should be generated
	 *
	 * @return true if tests for validation should be generated
	 */
	public boolean isTestValidationEnabled() {
		return validateTests;
	}

	/**
	 * Returns the fully-qualified name of the class that will be instrumented with
	 * aspects or null if a test class is not specified.
	 *
	 * @return the fully-qualified name of the class that will be instrumented with
	 *         aspects or null if a test class is not specified
	 */
	public String getTestClass() {
		return testClass;
	}

	/**
	 * Returns the path to the directory in which generated aspects should be
	 * output.
	 *
	 * @return the path to the directory in which generated aspects should be output
	 */
	public String getAspectsOutputDir() {
		return aspectsOutputDir;
	}

	/**
	 * Returns true if test cases should be generated after translation.
	 *
	 * @return true if test cases should be generated after translation
	 */
	public boolean isTestGenerationEnabled() {
		return testGeneration;
	}

	/**
	 * Returns the path to the directory in which generated test cases should be
	 * output.
	 *
	 * @return the path to the directory in which generated test cases should be
	 *         output
	 */
	public String getTestOutputDir() {
		return testOutputDir;
	}

	/**
	 * Returns the path to the evosuite jar executable.
	 *
	 * @return the path to the evosuite jar executable
	 */
	public String getEvoSuiteJar() {
		return evosuiteJar;
	}

	/**
	 * Returns the time budget allowed for evosuite to generate test cases.
	 *
	 * @return the time budget allowed for evosuite to generate test cases
	 */
	public int getEvoSuiteBudget() {
		return evosuiteBudget;
	}

	/**
	 * Returns the distance threshold that has been set for code element matching.
	 *
	 * @return the distance threshold that has been set for code element matching
	 */
	public int getDistanceThreshold() {
		return distanceThreshold;
	}

	/**
	 * Returns the cost of removing a word. This is used in Toradocu's edit distance
	 * algorithm.
	 *
	 * @return the cost of removing a word
	 */
	public int getWordRemovalCost() {
		return wordRemovalCost;
	}

	/**
	 * Returns the expected output file to compare Toradocu's output against, or
	 * null if no such file is specified.
	 *
	 * @return the expected output file to compare Toradocu's output against, or
	 *         null if no such file is specified
	 */
	public File getExpectedOutput() {
		return expectedOutput;
	}

	/**
	 * Returns the file in which to export Toradocu statistics in CSV format.
	 *
	 * @return the file in which to export Toradocu statistics in CSV format
	 */
	public File getStatsFile() {
		return statsFile;
	}

	/**
	 * Returns whether to use @tComment translation algorithm instead of the
	 * standard Toradocu condition translator.
	 *
	 * @return true if @tComment has to be used, false otherwise
	 */
	public boolean useTComment() {
		return tcomment;
	}

	/**
	 * Returns the file where to export Toradocu generated specifications as Randoop
	 * specifications.
	 *
	 * @return the file where to export Toradocu generated specifications as Randoop
	 *         specifications
	 */
	public File randoopSpecsFile() {
		return randoopSpecs;
	}

	/**
	 * Returns whether Toradocu uses semantic matching when translating conditions.
	 * If false, classic syntactic matching is used.
	 *
	 * @return true if semantic matching is used during translation, false otherwise
	 */
	public boolean isSemanticMatcherEnabled() {
		return !disableSemantics;
	}

	/**
	 * Returns whether Toradocu generates or not output when it has not been able to
	 * translate any comment.
	 *
	 * @return true if no output has to be generated when there is no translation,
	 *         false otherwise
	 */
	public boolean isSilent() {
		return silent;
	}
}
