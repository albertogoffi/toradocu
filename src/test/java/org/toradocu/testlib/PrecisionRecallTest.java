package org.toradocu.testlib;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.util.GsonInstance;

/**
 * PrecisionRecallTest contains static methods to perform a precision recall test using Toradocu.
 */
public class PrecisionRecallTest {

  public static TestCaseStats test(
      String targetClass, String srcPath, String binPath, String goalOutputDir) {
    return computePrecisionAndRecall(targetClass, srcPath, binPath, goalOutputDir);
  }

  /**
   * Runs Toradocu on the given class and collects data on its precision and recall.
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @param srcPath the source path for the given targetClass
   * @param binPath the path to the binaries for the given targetClass
   * @param goalOutputDir the path of the directory containing the goal output for the targetClass.
   * @return statistics for the test
   */
  public static TestCaseStats computePrecisionAndRecall(
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
          "--oracle-generation",
          "false",
          "--expected-output",
          goalOutputFile,
          "--class-dir",
          binPath,
          "--source-dir",
          srcPath
        };
    final List<String> argsList = new ArrayList<>(Arrays.asList(toradocuArgs));

    final String translator = System.getProperty("org.toradocu.translator");
    if (translator != null && translator.equals("tcomment")) {
      argsList.add("--tcomment");
      argsList.add("--stats-file");
      argsList.add("tcomment_results.csv");
    } else {
      argsList.add("--stats-file");
      argsList.add("results.csv");
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
  private static TestCaseStats compare(
      String targetClass, String outputFile, String goalOutputFile, String message) {
    StringBuilder report = new StringBuilder(message + "\n");

    try (BufferedReader outFile = Files.newBufferedReader(Paths.get(outputFile));
        BufferedReader goalFile = Files.newBufferedReader(Paths.get(goalOutputFile))) {

      Type collectionType = new TypeToken<Collection<DocumentedMethod>>() {}.getType();
      List<DocumentedMethod> actualResult = GsonInstance.gson().fromJson(outFile, collectionType);
      List<DocumentedMethod> goalResult = GsonInstance.gson().fromJson(goalFile, collectionType);

      assertThat(actualResult.size(), is(goalResult.size()));

      TestCaseStats result = new TestCaseStats(targetClass);
      int numberOfComments = 0;
      for (int methodIndex = 0; methodIndex < goalResult.size(); methodIndex++) {
        DocumentedMethod method = goalResult.get(methodIndex);
        ThrowsTag[] goalMethodTags = method.throwsTags().toArray(new ThrowsTag[0]);
        ThrowsTag[] actualMethodTags =
            actualResult.get(methodIndex).throwsTags().toArray(new ThrowsTag[0]);
        assertThat(goalMethodTags.length, is(actualMethodTags.length));
        StringBuilder methodReport = new StringBuilder(method.getSignature() + ":\n");
        boolean errors = false;

        for (int tagIndex = 0; tagIndex < goalMethodTags.length; tagIndex++) {
          ThrowsTag goalTag = goalMethodTags[tagIndex];
          ThrowsTag actualTag = actualMethodTags[tagIndex];

          String goalCondition = goalTag.getCondition().get();
          String actualCondition = actualTag.getCondition().get();

          // Empty goal conditions are ignored and have effects on precision and recall.
          if (!goalCondition.isEmpty()) {
            numberOfComments++;
            if (goalCondition.equals(actualCondition)) {
              result.incrementTP();
            } else {
              errors = true;
              if (actualCondition.isEmpty()) {
                methodReport.append("Empty condition. Comment: " + goalTag.exceptionComment());
              } else {
                result.incrementFP();
                methodReport.append("Wrong condition. Comment: " + goalTag.exceptionComment());
              }
              methodReport.append(
                  " | Goal condition: "
                      + goalCondition
                      + ". Actual condition: "
                      + actualCondition
                      + "\n");
            }
          }
        }

        if (errors) {
          report.append(methodReport.toString());
        }
      }

      result.setNumConditions(numberOfComments);
      report.append(result);
      System.out.println(report);
      return result;
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }
}
