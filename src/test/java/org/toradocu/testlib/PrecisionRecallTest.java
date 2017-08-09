package org.toradocu.testlib;

import static org.junit.Assert.fail;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.toradocu.Toradocu;
import org.toradocu.output.util.JsonOutput;
import org.toradocu.util.GsonInstance;
import org.toradocu.util.Stats;

/**
 * PrecisionRecallTest contains static methods to perform a precision recall test using Toradocu.
 */
class PrecisionRecallTest {

  /**
   * Runs Toradocu on the given class and collects data on its precision and recall.
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @param srcPath the source path for the given targetClass
   * @param binPath the path to the binaries for the given targetClass
   * @param goalOutputDir the path of the directory containing the goal output for the targetClass.
   * @return statistics for the test
   */
  static Stats computePrecisionAndRecall(
      String targetClass, String srcPath, String binPath, String goalOutputDir) {
    String actualOutputFile =
        AbstractPrecisionRecallTestSuite.OUTPUT_DIR + File.separator + targetClass + "_out.json";
    String goalOutputFile = Paths.get(goalOutputDir, targetClass + "_goal.json").toString();
    String message = "=== Test " + targetClass + " ===";

    String[] toradocuArgs =
        new String[] {
          "--target-class",
          targetClass,
          "--condition-translator-output",
          actualOutputFile,
          "--expected-output",
          goalOutputFile,
          "--class-dir",
          binPath,
          "--source-dir",
          srcPath
        };
    final List<String> argsList = new ArrayList<>(Arrays.asList(toradocuArgs));

    final String oracleGeneration = System.getProperty("org.toradocu.generator");
    // Disable oracle generation unless the specific system property is set.
    if (oracleGeneration != null && oracleGeneration.equals("true")) {
      argsList.add("--aspects-output-dir");
      argsList.add("aspects" + File.separator + targetClass);
    } else {
      argsList.add("--oracle-generation");
      argsList.add("false");
    }

    final String translator = System.getProperty("org.toradocu.translator");
    if (translator != null && translator.equals("tcomment")) {
      argsList.add("--tcomment");
      argsList.add("--stats-file");
      argsList.add("results_tcomment_.csv");
    } else if (translator != null && translator.equals("nosemantics")) {
      argsList.add("--disable-semantics");
      argsList.add("true");
      argsList.add("--stats-file");
      argsList.add("results_.csv");
    } else {
      // Semantic-based translator enabled by default.
      argsList.add("--stats-file");
      argsList.add("results_semantics_.csv");
    }

    Toradocu.main(argsList.toArray(new String[0]));
    return compare(targetClass, actualOutputFile, goalOutputFile, message);
  }

  /**
   * Compares the output file and the goal output file. Calculates statistics on precision and
   * recall and prints the results.
   *
   * @param targetClass the qualified name of the class under test
   * @param outputFile the file containing the actual test output
   * @param goalOutputFile the file containing the goal test output
   * @param message a message to print before all other output
   * @return statistics on precision and recall for the test
   */
  private static Stats compare(
      String targetClass, String outputFile, String goalOutputFile, String message) {
    StringBuilder report = new StringBuilder(message + "\n");

    try (BufferedReader outFile = Files.newBufferedReader(Paths.get(outputFile));
        BufferedReader goalFile = Files.newBufferedReader(Paths.get(goalOutputFile))) {

      Type collectionType = new TypeToken<Collection<JsonOutput>>() {}.getType();
      List<JsonOutput> actualResult = GsonInstance.gson().fromJson(outFile, collectionType);
      List<JsonOutput> goalResult = GsonInstance.gson().fromJson(goalFile, collectionType);
      final Stats stats = Stats.getStats(targetClass, actualResult, goalResult, report);
      System.out.println(report);
      return stats;
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }
}
