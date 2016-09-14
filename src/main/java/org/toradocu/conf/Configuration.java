package org.toradocu.conf;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.PathConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class holds the configuration options (particularly command-line options) for Toradocu.
 */
public class Configuration {

  // Constants

  private static final String ASPECT_TEMPLATE = "AspectTemplate.java";

  // General options

  @Parameter(
    names = "--target-class",
    description = "Fully-qualified name of the class that Toradocu should analyze",
    required = true
  )
  private String targetClass;

  @Parameter(
    names = "--source-dir",
    description = "Directory containing source files of the target class",
    converter = PathConverter.class,
    required = true
  )
  private Path sourceDir;

  @Parameter(
    names = "--class-dir",
    description = "Directory containing binaries of the target class",
    converter = PathConverter.class,
    required = true
  )
  private Path classDir;

  @Parameter(names = "--debug", description = "Enable fine-grained logging", hidden = true)
  private boolean debug = false;

  @Parameter(
    names = {"-h", "-help", "--help"},
    description = "Print a list of available options",
    help = true
  )
  private boolean help;

  // Javadoc extractor options

  @Parameter(
    names = "--javadoc-extractor-output",
    description = "File path to export the Javadoc extractor output in JSON format.",
    converter = FileConverter.class,
    hidden = true
  )
  private File javadocExtractorOutput;

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
        "Distance threshold: only code elements with distance less than this threshold"
            + " will be considered as candidate for the translation",
    hidden = true
  )
  private int distanceThreshold = 2;

  @Parameter(
    names = "--word-removal-cost",
    description = "Cost of a single word deletion in the distance algorithm",
    hidden = true
  )
  private int wordRemovalCost = 1;

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
    description = "Directory where Toradocu will output aspects"
  )
  private String aspectsOutputDir = "aspects";

  // Javadoc options

  @DynamicParameter(names = "-J", description = "Javadoc options")
  private Map<String, String> javadocOptions = new HashMap<>();

  private List<String> javadocOptionsList;

  private String tempJavadocOutputDir;

  /**
   * Initializes the configuration based on the given command-line options. This method must be
   * called before Javadoc options or the temporary Javadoc output directory are retrieved.
   */
  public void initialize() {
    if (help) {
      // No initialization necessary.
      return;
    }

    if (sourceDir == null) {
      sourceDir = Paths.get(".");
    }
    if (classDir == null) {
      classDir = Paths.get(".");
    }

    javadocOptionsList = new ArrayList<>();
    for (Entry<String, String> javadocOption : javadocOptions.entrySet()) {
      javadocOptionsList.add(javadocOption.getKey());
      if (!javadocOption.getValue().isEmpty()) {
        javadocOptionsList.add(javadocOption.getValue());
      }
    }
    // Suppress Javadoc console output.
    if (!javadocOptions.containsKey("-quiet")) {
      javadocOptionsList.add("-quiet");
    }
    // Process classes with protected and private modifiers.
    if (!javadocOptions.containsKey("-private")) {
      javadocOptionsList.add("-private");
    }
    // Set the Javadoc source files directory.
    if (!javadocOptions.containsKey("-sourcepath")) {
      javadocOptionsList.add("-sourcepath");
      javadocOptionsList.add(sourceDir.toString());
    }
    // Use a temporary Javadoc output directory if one is not set.
    if (!javadocOptions.containsKey("-d")) {
      try {
        tempJavadocOutputDir = Files.createTempDirectory(null).toString();
        javadocOptionsList.add("-d");
        javadocOptionsList.add(tempJavadocOutputDir);
      } catch (IOException e) {
        // Could not create temporary directory so output to working directory instead.
      }
    }

    javadocOptionsList.add(getTargetPackage());
  }

  // Getters

  private String getTargetPackage() {
    int packageStringEnd = targetClass.lastIndexOf(".");
    if (packageStringEnd == -1) {
      return "";
    }
    return targetClass.substring(0, packageStringEnd);
  }

  public String getAspectTemplate() {
    return ASPECT_TEMPLATE;
  }

  public String getTargetClass() {
    return targetClass;
  }

  public boolean debug() {
    return debug;
  }

  public boolean help() {
    return help;
  }

  public Path getSourceDir() {
    return sourceDir;
  }

  public Path getClassDir() {
    return classDir;
  }

  public File getJavadocExtractorOutput() {
    return javadocExtractorOutput;
  }

  public File getConditionTranslatorInput() {
    return conditionTranslatorInput;
  }

  public File getConditionTranslatorOutput() {
    return conditionTranslatorOutput;
  }

  public boolean isConditionTranslationEnabled() {
    return conditionTranslation;
  }

  public boolean isOracleGenerationEnabled() {
    return oracleGeneration;
  }

  public String getTestClass() {
    return testClass;
  }

  public String getAspectsOutputDir() {
    return aspectsOutputDir;
  }

  public String[] getJavadocOptions() {
    return javadocOptionsList.toArray(new String[0]);
  }

  /**
   * Returns a temporary directory for Javadoc output or null if a non-temporary directory is set
   * for Javadoc output.
   *
   * @return a temporary directory for Javadoc output or null if a non-temporary directory is set
   * for Javadoc output
   */
  public String getTempJavadocOutputDir() {
    return tempJavadocOutputDir;
  }

  public int getDistanceThreshold() {
    return distanceThreshold;
  }

  public int getWordRemovalCost() {
    return wordRemovalCost;
  }
}
