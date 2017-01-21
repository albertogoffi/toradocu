package org.toradocu.conf;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.PathConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class holds the configuration options (particularly command-line options) for Toradocu. */
public class Configuration {

  // General options

  @Parameter(
    names = "--target-class",
    description = "Fully-qualified name of the class that Toradocu should analyze",
    required = true
  )
  private String targetClass;

  @Parameter(
    names = "--source-dir",
    description = "Specifies a directory containing source files of the target class",
    converter = PathConverter.class,
    required = true
  )
  private Path sourceDir;

  @Parameter(
    names = "--class-dir",
    description = "Specifies a JAR file or a directory containing binaries of the target class",
    converter = PathConverter.class,
    required = true
  )
  private Path classDir;

  @Parameter(names = "--debug", description = "Enable fine-grained logging", hidden = true)
  private boolean debug = false;

  @Parameter(
    names = {"--help", "-h"},
    description = "Print a list of available options",
    help = true
  )
  private boolean help;

  @Parameter(
    names = "--stats-file",
    description = "File path to export Toradocu statistics in CSV format",
    converter = FileConverter.class
  )
  private File statsFile;

  // Javadoc extractor options

  @Parameter(
    names = "--javadoc-extractor-output",
    description = "File path to export Javadoc extractor output in JSON format",
    converter = FileConverter.class,
    hidden = true
  )
  private File javadocExtractorOutput;

  @DynamicParameter(names = "-J", description = "Javadoc options")
  private Map<String, String> javadocOptionsMap = new HashMap<>();

  // Condition translator options

  @Parameter(
    names = "--condition-translation",
    description = "Enable/disable the condition translator",
    arity = 1,
    hidden = true
  )
  private boolean conditionTranslation = true;

  @Parameter(
    names = "--condition-translator-input",
    description =
        "Input file to the condition translator (this option disables the Javadoc extractor)",
    converter = FileConverter.class,
    hidden = true
  )
  private File conditionTranslatorInput;

  @Parameter(
    names = "--condition-translator-output",
    description = "File path to export condition translator output in JSON format",
    converter = FileConverter.class
  )
  private File conditionTranslatorOutput;

  @Parameter(
    names = "--distance-threshold",
    description =
        "Distance threshold: only code elements with edit distance less than this threshold"
            + " will be considered candidates for translation",
    hidden = true
  )
  private int distanceThreshold = 2;

  @Parameter(
    names = "--word-removal-cost",
    description = "Cost of a single word deletion in the edit distance algorithm",
    hidden = true
  )
  private int wordRemovalCost = 1;

  @Parameter(
    names = "--expected-output",
    description =
        "Condition extractor expected output file used to compute statistics about "
            + "Toradocu's performance. Statistics are computed only if a valid expected output file"
            + " is provided",
    converter = FileConverter.class,
    hidden = true
  )
  private File expectedOutput;

  @Parameter(
    names = "--remove-commas",
    arity = 1,
    description = "Remove commas before the input text is parsed",
    hidden = true
  )
  private boolean removeCommas = true;

  @Parameter(
    names = "--tcomment",
    description = "Use @tComment to translate comments",
    hidden = true
  )
  private boolean tcomment = false;

  @Parameter(
    names = "--export-conditions",
    description =
        "Path to folder where to export comment translations as Java boolean " + "conditions.",
    converter = FileConverter.class,
    hidden = true
  )
  private File exportAsJava;

  // Aspect creation options

  @Parameter(
    names = "--oracle-generation",
    description = "Enable/disable the generation of the aspects",
    arity = 1
  )
  private boolean oracleGeneration = true;

  @Parameter(
    names = "--test-class",
    description = "Fully-qualified name of the class that will be instrumented with aspects"
  )
  private String testClass;

  @Parameter(
    names = "--aspects-output-dir",
    description = "Specifies a directory where Toradocu will output aspects"
  )
  private String aspectsOutputDir = "aspects";

  /** File used as template for generated aspects. */
  private static final String ASPECT_TEMPLATE = "AspectTemplate.java";

  /** Aspect to instrument JUnit test cases. */
  private static final String JUNIT_TC_ASPECT = "TestCaseAspect.java";

  /** Command-line options passed to the Javadoc tool. */
  private List<String> javadocOptions = new ArrayList<>();

  /**
   * Temporary directory for Javadoc output or null if a non-temporary directory (e.g. the working
   * directory) is set for Javadoc output.
   */
  private String javadocOutputDir;

  /**
   * Initializes the configuration based on the given command-line options. This method must be
   * called before Javadoc options or the temporary Javadoc output directory are retrieved.
   */
  public void initialize() {
    if (help) {
      // No initialization necessary.
      return;
    }

    if (statsFile == null) {
      statsFile = new File("stats.csv");
    }

    // Initialize command-line options passed to Javadoc:
    for (Map.Entry<String, String> javadocOption : javadocOptionsMap.entrySet()) {
      javadocOptions.add(javadocOption.getKey());
      if (!javadocOption.getValue().isEmpty()) {
        javadocOptions.add(javadocOption.getValue());
      }
    }

    // Suppress Javadoc console output.
    if (!javadocOptions.contains("-quiet")) {
      javadocOptions.add("-quiet");
    }
    // Process classes with protected and private modifiers.
    if (!javadocOptions.contains("-private")) {
      javadocOptions.add("-private");
    }
    // Set the Javadoc source files directory.
    if (!javadocOptions.contains("-sourcepath")) {
      javadocOptions.add("-sourcepath");
      javadocOptions.add(sourceDir.toString());
    }
    // Attempt to use a temporary Javadoc output directory.
    if (!javadocOptions.contains("-d")) {
      try {
        javadocOutputDir = Files.createTempDirectory(null).toString();
        javadocOptions.add("-d");
        javadocOptions.add(javadocOutputDir);
      } catch (IOException e) {
        // Could not create temporary directory so output to working directory instead.
      }
    }
    // Use UTF-8 as default encoding.
    if (!javadocOptions.contains("-encoding")) {
      javadocOptions.add("-encoding");
      javadocOptions.add("UTF-8");
    }
    // Specify the target package on which to run Javadoc.
    javadocOptions.add(getTargetPackage());
  }

  /**
   * Returns the package in which the target class is contained.
   *
   * @return the package in which the target class is contained
   */
  private String getTargetPackage() {
    // Note that this implementation is currently incorrect.
    // It does not correctly separate the package and class part for inner classes.
    // One heuristic to fix this would be to separate package and class parts based
    // on capitalization. This would not work in every case but would handle the
    // majority of inner classes.
    int packageStringEnd = targetClass.lastIndexOf(".");
    if (packageStringEnd == -1) {
      return "";
    }
    return targetClass.substring(0, packageStringEnd);
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
   * Returns the fully-qualified name of the target class to analyze with Toradocu.
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
   * Returns the path to a directory containing the source files of the target class.
   *
   * @return the path to a directory containing the source files of the target class
   */
  public Path getSourceDir() {
    return sourceDir;
  }

  /**
   * Returns the path to a JAR file or a directory containing binaries of the target class.
   *
   * @return the path to a JAR file or a directory containing binaries of the target class
   */
  public Path getClassDir() {
    return classDir;
  }

  /**
   * Returns the file in which to export Javadoc extractor output or null if this file is not
   * specified.
   *
   * @return the file in which to export Javadoc extractor output or null if this file is not
   *     specified
   */
  public File getJavadocExtractorOutput() {
    return javadocExtractorOutput;
  }

  /**
   * Returns the input file to the condition translator or null if this file is not specified.
   *
   * @return the input file to the condition translator or null if this file is not specified
   */
  public File getConditionTranslatorInput() {
    return conditionTranslatorInput;
  }

  /**
   * Returns the output file for the condition translator or null if this file is not specified.
   *
   * @return the output file for the condition translator or null if this file is not specified
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
   * Returns the fully-qualified name of the class that will be instrumented with aspects or null if
   * a test class is not specified.
   *
   * @return the fully-qualified name of the class that will be instrumented with aspects or null if
   *     a test class is not specified
   */
  public String getTestClass() {
    return testClass;
  }

  /**
   * Returns the path to the directory in which generated aspects should be output.
   *
   * @return the path to the directory in which generated aspects should be output
   */
  public String getAspectsOutputDir() {
    return aspectsOutputDir;
  }

  /**
   * Returns the command-line options passed to the Javadoc tool.
   *
   * @return the command-line options passed to the Javadoc tool
   */
  public String[] getJavadocOptions() {
    return javadocOptions.toArray(new String[0]);
  }

  /**
   * Returns a temporary directory for Javadoc output or null if a non-temporary directory is set
   * for Javadoc output.
   *
   * @return a temporary directory for Javadoc output or null if a non-temporary directory is set
   *     for Javadoc output
   */
  public String getJavadocOutputDir() {
    return javadocOutputDir;
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
   * Returns the cost of removing a word. This is used in Toradocu's edit distance algorithm.
   *
   * @return the cost of removing a word
   */
  public int getWordRemovalCost() {
    return wordRemovalCost;
  }

  /**
   * Returns the expected output file to compare Toradocu's output against, or null if no such file
   * is specified.
   *
   * @return the expected output file to compare Toradocu's output against, or null if no such file
   *     is specified
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
   * Returns whether commas characters will be removed from the Javadoc comments, before they are
   * parsed by the Stanford parser.
   *
   * @return true if the commas has to be removed, false otherwise
   */
  public boolean removeCommas() {
    return removeCommas;
  }

  /**
   * Returns whether to use @tComment translation algorithm instead of the standard Toradocu
   * condition translator.
   *
   * @return true if @tComment has to be used, false otherwise
   */
  public boolean useTComment() {
    return tcomment;
  }

  /**
   * Returns the folder in which to export Java boolean conditions.
   *
   * @return the folder in which to export Java boolean conditions.
   */
  public File getExportAsJava() {
    return exportAsJava;
  }
}
