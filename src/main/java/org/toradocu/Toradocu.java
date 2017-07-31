package org.toradocu;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedType;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.output.util.JsonOutput;
import org.toradocu.translator.CommentTranslator;
import org.toradocu.util.GsonInstance;
import org.toradocu.util.Stats;
import randoop.condition.specification.OperationSpecification;

/**
 * Entry point of Toradocu. {@code Toradocu.main} is automatically executed running the command:
 * {@code java -jar toradocu.jar}.
 */
public class Toradocu {

  /** Command to run Toradocu. This string is used only in output messages. */
  private static final String TORADOCU_COMMAND = "java -jar toradocu.jar";
  /** Toradocu's configurations. */
  public static Configuration configuration = null;
  /** Logger of this class. */
  private static Logger log;

  /**
   * Entry point for Toradocu. Takes several command-line arguments that configure its behavior.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    configuration = Configuration.INSTANCE;
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

    List<DocumentedExecutable> members = null;
    final String targetClass = configuration.getTargetClass();
    if (configuration.getConditionTranslatorInput() == null) {
      final JavadocExtractor javadocExtractor = new JavadocExtractor();
      try {
        final DocumentedType documentedType =
            javadocExtractor.extract(targetClass, configuration.getSourceDir().toString());
        members = documentedType.getDocumentedExecutables();
      } catch (ClassNotFoundException e) {
        log.error( // TODO Refine this error message for the specific caught exception.
            e.getMessage()
                + "\nPossible reasons for the error are:"
                + "\n1. The Javadoc documentations is wrong"
                + "\n2. The path to the source code of your system is wrong: "
                + configuration.getSourceDir()
                + "\n3. The path to the binaries of your system is wrong: "
                + configuration.classDirs
                + "\nPlease, check the correctness of the command line arguments."
                + "\nIf the error persists, report the issue at "
                + "https://github.com/albertogoffi/toradocu/issues"
                + "\nError stack trace:");
        e.printStackTrace();
        System.exit(1);
      } catch (FileNotFoundException e) {
        e.printStackTrace(); // TODO Print a more meaningful message!
        System.exit(1);
      }
    }
    //    else {
    //      // List of methods to analyze are read from a file specified with a command line option.
    //      try (BufferedReader reader =
    //          Files.newBufferedReader(configuration.getConditionTranslatorInput().toPath())) {
    //        methods = new ArrayList<>();
    //        methods.addAll(
    //            GsonInstance.gson()
    //                .fromJson(reader, new TypeToken<List<DocumentedExecutable>>() {}.getType()));
    //      } catch (IOException e) {
    //        log.error("Unable to read the file: " + configuration.getConditionTranslatorInput(), e);
    //        System.exit(1);
    //      }
    //    }

    if (configuration.getJavadocExtractorOutput() != null) { // Print collection to the output file.
      try (BufferedWriter writer =
          Files.newBufferedWriter(
              configuration.getJavadocExtractorOutput().toPath(), StandardCharsets.UTF_8)) {
        writer.write(GsonInstance.gson().toJson(members));
      } catch (Exception e) {
        log.error(
            "Unable to write the output on file "
                + configuration.getJavadocExtractorOutput().getAbsolutePath(),
            e);
      }
    }
    if (configuration.debug()) {
      log.debug("Constructors/methods found in source code: " + members);
    }

    // === Condition Translator ===

    if (configuration.isConditionTranslationEnabled()) {
      Map<DocumentedExecutable, OperationSpecification> specifications;

      // Use @tComment or the standard condition translator to translate comments.
      if (configuration.useTComment()) {
        specifications = tcomment.TcommentKt.translate(members);
      } else {
        specifications = CommentTranslator.createSpecifications(members);
      }

      // Output the result on a file or on the standard output, if silent mode is disabled.
      List<JsonOutput> jsonOutputs = new ArrayList<>();
      if (!configuration.isSilent() || !specifications.isEmpty()) {
        if (configuration.getConditionTranslatorOutput() != null) {
          try (BufferedWriter writer =
              Files.newBufferedWriter(
                  configuration.getConditionTranslatorOutput().toPath(), StandardCharsets.UTF_8)) {

            for (DocumentedExecutable executable : specifications.keySet()) {
              jsonOutputs.add(new JsonOutput(executable, specifications.get(executable)));
            }
            String jsonOutput = GsonInstance.gson().toJson(jsonOutputs);
            writer.write(jsonOutput);
          } catch (Exception e) {
            log.error(
                "Unable to write the output on file "
                    + configuration.getConditionTranslatorOutput().getAbsolutePath(),
                e);
          }
        } else {
          for (DocumentedExecutable member : members) {
            jsonOutputs.add(new JsonOutput(member, specifications.get(member)));
          }
          String jsonOutput = GsonInstance.gson().toJson(jsonOutputs);
          System.out.println("Condition translator output:\n" + jsonOutput);
        }
      }

      // Create statistics.
      File expectedResultFile = configuration.getExpectedOutput();
      if (expectedResultFile != null) {
        Type collectionType = new TypeToken<List<JsonOutput>>() {}.getType();
        try (BufferedReader reader = Files.newBufferedReader(expectedResultFile.toPath());
            BufferedWriter resultsFile =
                Files.newBufferedWriter(
                    configuration.getStatsFile().toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
          List<JsonOutput> expectedResult = GsonInstance.gson().fromJson(reader, collectionType);
          List<Stats> targetClassResults = Stats.getStats(jsonOutputs, expectedResult);
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
      generateRandoopSpecs(specifications);
    }

    // === Oracle Generator ===
    //    OracleGenerator.createAspects(methods);
  }

  /**
   * Export the specifications in {@code specsMap} to {@code conf.Configuration#randoopSpecsFile()}
   * as Randoop specifications.
   *
   * @param specsMap the documented methods containing the specifications to export
   */
  private static void generateRandoopSpecs(
      Map<DocumentedExecutable, OperationSpecification> specsMap) {
    File randoopSpecsFile = configuration.randoopSpecsFile();
    if (!configuration.isSilent() && randoopSpecsFile != null) {
      if (!randoopSpecsFile.exists()) {
        try {
          File parentDir = randoopSpecsFile.getParentFile();
          if (parentDir != null) {
            Files.createDirectories(parentDir.toPath());
          }

        } catch (IOException e) {
          log.error("Error occurred during creation of the file " + randoopSpecsFile.getPath(), e);
        }
      }
      Collection<OperationSpecification> specs = specsMap.values();
      if (!specs.isEmpty()) {
        // TODO Filter empty specs? (specs for which the translation is empty string)
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
}
