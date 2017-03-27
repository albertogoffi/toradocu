package org.toradocu;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.reflect.TypeToken;
import com.sun.javadoc.ClassDoc;
import com.sun.tools.javadoc.Main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.extractor.Tag;
import org.toradocu.generator.OracleGenerator;
import org.toradocu.translator.ConditionTranslator;
import org.toradocu.util.GsonInstance;
import org.toradocu.util.NullOutputStream;
import org.toradocu.util.RandoopSpecs;
import org.toradocu.util.Stats;
import randoop.condition.specification.OperationSpecification;

/**
 * Entry point of Toradocu. {@code Toradocu.main} is automatically executed running the command:
 * {@code java -jar toradocu.jar}.
 */
public class Toradocu {

  /** Doclet class used when javadoc command is run. */
  private static final String DOCLET = "org.toradocu.doclet.standard.Standard";
  /** Command to run Toradocu. This string is used only in output messages. */
  private static final String TORADOCU_COMMAND = "java -jar toradocu.jar";
  /** Toradocu's configurations. */
  public static Configuration configuration = null;
  /** Logger of this class. */
  private static Logger log;
  /** Documented methods that will be processed by the condition translator. */
  private static List<DocumentedMethod> methods;

  /**
   * Entry point for Toradocu. Takes several command-line arguments that configure its behavior.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    configuration = new Configuration();
    JCommander options = null;
    try {
      options = new JCommander(configuration, args);
    } catch (ParameterException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
    options.setProgramName(TORADOCU_COMMAND);
    configuration.initialize();

    if (configuration.help()) {
      options.usage();
      System.out.println("Options preceded by an asterisk are required.");
      System.exit(1);
    }

    if (configuration.debug()) {
      System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "trace");
    }

    // Suppress non-error messages from Stanford parser (some of the messages directly printed on
    // standard error are still visible).
    System.setProperty(org.slf4j.impl.SimpleLogger.LOG_KEY_PREFIX + "edu.stanford", "error");
    log = LoggerFactory.getLogger(Toradocu.class);

    // === Javadoc Extractor ===

    // Populate the methods field.
    if (configuration.getConditionTranslatorInput() == null) {
      // Obtain list of methods by running Javadoc. Our doclet invokes method Toradocu.process
      // which side-effects the methods field.

      // Suppress all the output of the Javadoc tool.
      PrintWriter nullPrintWriter = new PrintWriter(new NullOutputStream());

      Main.execute(
          TORADOCU_COMMAND + " - Javadoc Extractor",
          nullPrintWriter,
          nullPrintWriter,
          nullPrintWriter,
          DOCLET,
          configuration.getJavadocOptions());
    } else {
      // List of methods to analyze are read from a file specified with a command line option.
      try (BufferedReader reader =
          Files.newBufferedReader(configuration.getConditionTranslatorInput().toPath())) {
        methods = new ArrayList<>();
        methods.addAll(
            GsonInstance.gson()
                .fromJson(reader, new TypeToken<List<DocumentedMethod>>() {}.getType()));
      } catch (IOException e) {
        log.error("Unable to read the file: " + configuration.getConditionTranslatorInput(), e);
        System.exit(1);
      }
    }

    if (methods == null) {
      log.error(
          "Unable to find the target class: "
              + configuration.getTargetClass()
              + "\nPossible reasons for the error are:"
              + "\n1. The qualified name of the target class is wrong: "
              + configuration.getTargetClass()
              + "\n2. The path to the source code of your system is wrong: "
              + configuration.getSourceDir()
              + "\n3. The path to the binaries of your system is wrong: "
              + configuration.getClassDir()
              + "\nPlease, check the correctness of the command line arguments."
              + "\nIf the error persists, report the issue at "
              + "https://github.com/albertogoffi/toradocu/issues");
      System.exit(1);
    }

    if (configuration.getJavadocExtractorOutput() != null) { // Print collection to the output file.
      try (BufferedWriter writer =
          Files.newBufferedWriter(
              configuration.getJavadocExtractorOutput().toPath(), StandardCharsets.UTF_8)) {
        writer.write(GsonInstance.gson().toJson(methods));
      } catch (Exception e) {
        log.error(
            "Unable to write the output on file "
                + configuration.getJavadocExtractorOutput().getAbsolutePath(),
            e);
      }
    }
    if (configuration.debug()) {
      log.debug("Methods with Javadoc documentation found in source code: " + methods.toString());
    }

    // === Condition Translator ===

    if (configuration.isConditionTranslationEnabled()) {

      // Use @tComment or the standard condition translator to translate comments.
      if (configuration.useTComment()) {
        tcomment.TcommentKt.translate(methods);
      } else {
        ConditionTranslator.translate(methods);
      }

      // Output the result on a file or on the standard output, if silent mode is disabled.
      if (!configuration.isSilent() || configuration.isSilent() && translationsPresentIn(methods)) {
        if (configuration.getConditionTranslatorOutput() != null) {
          try (BufferedWriter writer =
              Files.newBufferedWriter(
                  configuration.getConditionTranslatorOutput().toPath(), StandardCharsets.UTF_8)) {
            String jsonOutput = GsonInstance.gson().toJson(methods);
            writer.write(jsonOutput);
            printConditionLines(jsonOutput);
          } catch (Exception e) {
            log.error(
                "Unable to write the output on file "
                    + configuration.getConditionTranslatorOutput().getAbsolutePath(),
                e);
          }
        } else {
          System.out.println(
              "Condition translator output:\n" + GsonInstance.gson().toJson(methods));
        }
      }

      // Create statistics.
      File expectedResultFile = configuration.getExpectedOutput();
      if (expectedResultFile != null) {
        Type collectionType = new TypeToken<List<DocumentedMethod>>() {}.getType();
        try (BufferedReader reader = Files.newBufferedReader(expectedResultFile.toPath());
            BufferedWriter resultsFile =
                Files.newBufferedWriter(
                    configuration.getStatsFile().toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
          List<DocumentedMethod> expectedResult =
              GsonInstance.gson().fromJson(reader, collectionType);
          List<Stats> targetClassResults = Stats.getStats(methods, expectedResult);
          for (Stats result : targetClassResults) {
            if (result.numberOfConditions() != 0) { // Ignore methods with no tags.
              resultsFile.write(result.asCSV());
              resultsFile.newLine();
            }
          }
        } catch (IOException e) {
          log.error("Unable to read the file: " + configuration.getConditionTranslatorInput(), e);
        }
      }

      // Export generated specifications as Randoop specifications if requested.
      generateRandoopSpecs(methods);
    }

    // === Oracle Generator ===
    OracleGenerator.createAspects(methods);

    deleteTemporaryFiles();

    // Needed for testing: multiple executions (tests) of Toradocu run in the same JVM.
    // This can be improved making {@code methods} non-static and changing the way Toradocu
    // interacts with the javadoc tool.
    clearMethods();
  }

  /**
   * Export the specifications in {@code methods} to {@code conf.Configuration#randoopSpecsFile()}
   * as Randoop specifications.
   *
   * @param methods the documented methods containing the specifications to export
   */
  private static void generateRandoopSpecs(List<DocumentedMethod> methods) {
    File randoopSpecsFile = configuration.randoopSpecsFile();
    if (!configuration.isSilent() && randoopSpecsFile != null) {
      List<OperationSpecification> specs = null;
      if (!randoopSpecsFile.exists()) {
        try {
          Files.createDirectories(randoopSpecsFile.getParentFile().toPath());
          specs = methods.stream().map(RandoopSpecs::translate).collect(Collectors.toList());
        } catch (IOException e) {
          log.error("Error occurred during creation of the file " + randoopSpecsFile.getPath(), e);
        }
      }

      if (specs != null) {
        try (BufferedWriter writer =
            Files.newBufferedWriter(
                randoopSpecsFile.toPath(),
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE)) {
          writer.write(GsonInstance.gson().toJson(specs));
        } catch (IOException e) {
          log.error(
              "Error occurred during the export of generated specifications to file "
                  + randoopSpecsFile.getPath(),
              e);
        }
      }
    }
  }

  /**
   * Prints (to standard output) line numbers for lines in the given JSON output string that contain
   * Java expression translations of conditions. These lines can be altered to generate expected
   * output files for the precision recall test suite.
   *
   * @param jsonOutput string containing output of condition translator in JSON format
   */
  private static void printConditionLines(String jsonOutput) {
    BufferedReader reader = new BufferedReader(new StringReader(jsonOutput));
    String fileName = configuration.getConditionTranslatorOutput().toString();
    int lineNumber = 1;
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("\"comment\"") || line.startsWith("\"condition\"")) {
          System.out.println(fileName + ":" + lineNumber + ": " + line);
        }
        if (line.startsWith("\"condition\"")) {
          // Put a blank line between comment/condition pairs.
          System.out.println();
        }
        lineNumber++;
      }
    } catch (IOException e) {
      // An IOException should never occur when using a StringReader.
      e.printStackTrace();
    }
  }

  /**
   * This method populates the static field {@code methods} using {@code JavadocExtractor} when the
   * given {@code classDoc} is the target class specified in {@code configuration}. This method is
   * intended to be invoked by the Javadoc doclet.
   *
   * @param classDoc the class from which methods are extracted, but only if it is the target class
   *     specified in {@code configuration}
   * @param docletConfiguration configuration options for the Javadoc doclet
   * @throws IOException if there is an error while reading/generating class documentation
   */
  public static void process(ClassDoc classDoc, ConfigurationImpl docletConfiguration)
      throws IOException {
    if (!classDoc.qualifiedName().equals(configuration.getTargetClass())) {
      return;
    }
    methods = new ArrayList<>();
    JavadocExtractor extractor = new JavadocExtractor(docletConfiguration);
    methods.addAll(extractor.extract(classDoc));
  }

  /**
   * Clears the list of methods that are extracted and processed by Toradocu. This method is used
   * when multiple sequential Toradocu executions are performed (e.g., during the precision/recall
   * test suite execution).
   */
  public static void clearMethods() {
    methods.clear();
  }

  /** Deletes any temporary files created by Toradocu to store Javadoc output. */
  private static void deleteTemporaryFiles() {
    if (configuration.getJavadocOutputDir() != null) {
      try {
        FileUtils.deleteDirectory(new File(configuration.getJavadocOutputDir()));
      } catch (IOException e) {
        log.warn("Unable to delete temporary Javadoc output", e);
      }
    }
  }

  /**
   * Checks whether there is at least one nonempty translation for the comments of {@code methods}.
   *
   * @param methods the list of methods to inspect
   * @return true if there is at least one nonempty translation, false otherwise
   */
  private static boolean translationsPresentIn(List<DocumentedMethod> methods) {
    return methods
        .stream()
        .map(
            m -> {
              List<Tag> tags = new ArrayList<>(m.paramTags());
              tags.addAll(m.throwsTags());
              return tags;
            })
        .flatMap(List::stream)
        .anyMatch(t -> t.getCondition().isPresent() && !t.getCondition().get().isEmpty());
  }
}
