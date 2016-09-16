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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.translator.ConditionTranslator;
import org.toradocu.util.GsonInstance;
import org.toradocu.util.MethodStats;
import org.toradocu.util.NullOutputStream;
import org.toradocu.util.Stats;

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
  private static final List<DocumentedMethod> methods = new ArrayList<>();

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
        methods.addAll(
            GsonInstance.gson()
                .fromJson(reader, new TypeToken<List<DocumentedMethod>>() {}.getType()));
      } catch (IOException e) {
        log.error("Unable to read the file: " + configuration.getConditionTranslatorInput(), e);
        System.exit(1);
      }
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
      ConditionTranslator.translate(methods);
      if (configuration.getConditionTranslatorOutput() != null) {
        try (BufferedWriter writer =
            Files.newBufferedWriter(
                configuration.getConditionTranslatorOutput().toPath(), StandardCharsets.UTF_8)) {
          writer.write(GsonInstance.gson().toJson(methods));
        } catch (Exception e) {
          log.error(
              "Unable to write the output on file "
                  + configuration.getConditionTranslatorOutput().getAbsolutePath(),
              e);
        }
      } else {
        System.out.println("Condition translator output:\n" + GsonInstance.gson().toJson(methods));
      }

      // Create statistics
      File expectedResultFile = configuration.getExpectedOutput();
      if (expectedResultFile != null) {
        Type collectionType = new TypeToken<List<DocumentedMethod>>() {}.getType();
        try (BufferedReader reader = Files.newBufferedReader(expectedResultFile.toPath());
            BufferedWriter resultsFile =
                Files.newBufferedWriter(
                    Paths.get("stats" + File.separator + "stats.csv"),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
          List<DocumentedMethod> expectedResult =
              GsonInstance.gson().fromJson(reader, collectionType);
          List<MethodStats> targetClassResults = Stats.getStats(methods, expectedResult);
          for (MethodStats result : targetClassResults) {
            if (result.getNumberOfConditions() != 0) { // Ignore methods with no @throws tag
              resultsFile.write(result.asCSV());
              resultsFile.newLine();
            }
          }
        } catch (IOException e) {
          log.error("Unable to read the file: " + configuration.getConditionTranslatorInput(), e);
        }
      }
    }

    // === Oracle Generator ===
    // OracleGenerator.generate(methods);

    deleteTemporaryFiles();
    /* Needed for testing: multiple executions of Toradocu run in the same JVM. This can be improved making {@code methods}
     * non-static and changing the way Toradocu interacts with the javadoc tool. */
    methods.clear();
  }

  /**
   * This method populates the static field {@code methods} using {@code JavadocExtractor} when the
   * given {@code classDoc} is the target class specified in {@code CONF}. This method is intended
   * to be invoked by the Javadoc doclet.
   *
   * @param classDoc the class from which methods are extracted, but only if it is the target class
   * specified in {@code CONF}
   * @param docletConfiguration configuration options for the Javadoc doclet
   * @throws IOException if there is an error while reading/generating class documentation
   */
  public static void process(ClassDoc classDoc, ConfigurationImpl docletConfiguration)
      throws IOException {
    if (!classDoc.qualifiedName().equals(configuration.getTargetClass())) {
      return;
    }
    JavadocExtractor extractor = new JavadocExtractor(docletConfiguration);
    methods.addAll(extractor.extract(classDoc));
  }

  /**
   * Deletes any temporary files created by Toradocu to store Javadoc output.
   */
  private static void deleteTemporaryFiles() {
    if (configuration.getTempJavadocOutputDir() != null) {
      try {
        FileUtils.deleteDirectory(new File(configuration.getTempJavadocOutputDir()));
      } catch (IOException e) {
        log.warn("Unable to delete temporary Javadoc output", e);
      }
    }
  }
}
