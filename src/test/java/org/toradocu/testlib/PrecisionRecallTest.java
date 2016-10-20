package org.toradocu.testlib;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
      String targetClass, String srcPath, String binPath, String expectedOutputDir) {
    return computePrecisionAndRecall(targetClass, srcPath, binPath, expectedOutputDir);
  }

  /**
   * Runs Toradocu on the given class and collects data on its precision and recall.
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @param srcPath the source path for the given targetClass
   * @param binPath the path to the binaries for the given targetClass
   * @param expectedOutputDir the path of the directory containing the expected output for the
   * targetClass.
   * @return statistics for the test
   */
  public static TestCaseStats computePrecisionAndRecall(
      String targetClass, String srcPath, String binPath, String expectedOutputDir) {
    String actualOutputFile =
        AbstractPrecisionRecallTestSuite.OUTPUT_DIR + File.separator + targetClass + "_out.json";
    String expectedOutputFile =
        Paths.get(expectedOutputDir, targetClass + "_expected.json").toString();
    String message = "=== Test " + targetClass + " ===";

    Toradocu.main(
        new String[] {
          "--target-class",
          targetClass,
          "--condition-translator-output",
          actualOutputFile,
          "--oracle-generation",
          "false",
          "--class-dir",
          binPath,
          "--source-dir",
          srcPath
        });
    return compare(targetClass, actualOutputFile, expectedOutputFile, message);
  }

  /**
   * Compares the output file and the expected output file. Calculates statistics on precision and
   * recall and prints the results.
   *
   * @param targetClass the qualified name of the class under test
   * @param outputFile the file containing the actual test output
   * @param expectedOutputFile the file containing the expected test output
   * @param message a message to print before all other output
   * @return statistics on precision and recall for the test
   */
  private static TestCaseStats compare(
      String targetClass, String outputFile, String expectedOutputFile, String message) {
    StringBuilder report = new StringBuilder(message + "\n");

    try (BufferedReader outFile = Files.newBufferedReader(Paths.get(outputFile));
        BufferedReader expFile = Files.newBufferedReader(Paths.get(expectedOutputFile));
        BufferedWriter resultsFile =
            Files.newBufferedWriter(
                Paths.get(AbstractPrecisionRecallTestSuite.OUTPUT_DIR + "/results.csv"),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

      Type collectionType = new TypeToken<Collection<DocumentedMethod>>() {}.getType();
      List<DocumentedMethod> actualResult = GsonInstance.gson().fromJson(outFile, collectionType);
      List<DocumentedMethod> expectedResult = GsonInstance.gson().fromJson(expFile, collectionType);

      assertThat(actualResult.size(), is(expectedResult.size()));

      TestCaseStats result = new TestCaseStats(targetClass);
      int numberOfComments = 0;
      for (int methodIndex = 0; methodIndex < expectedResult.size(); methodIndex++) {
        DocumentedMethod method = expectedResult.get(methodIndex);
        ThrowsTag[] expectedMethodTags = method.throwsTags().toArray(new ThrowsTag[0]);
        ThrowsTag[] actualMethodTags =
            actualResult.get(methodIndex).throwsTags().toArray(new ThrowsTag[0]);
        assertThat(expectedMethodTags.length, is(actualMethodTags.length));
        StringBuilder methodReport = new StringBuilder(method.getSignature() + ":\n");
        boolean errors = false;

        for (int tagIndex = 0; tagIndex < expectedMethodTags.length; tagIndex++) {
          ThrowsTag expectedTag = expectedMethodTags[tagIndex];
          ThrowsTag actualTag = actualMethodTags[tagIndex];

          String expectedCondition = expectedTag.getCondition().get();
          String actualCondition = actualTag.getCondition().get();
          numberOfComments++;

          if (expectedCondition.equals(actualCondition)) {
            result.incrementTP();
          } else {
            errors = true;
            if (actualCondition.isEmpty()) {
              /* We do not consider any empty condition as correct. Empty conditions in expected
               * output files mean that it was not possible to manually define a condition.
               * This should not impact precision and recall. */
              methodReport.append("Empty condition. Comment: " + expectedTag.exceptionComment());
            } else {
              result.incrementFP();
              methodReport.append("Wrong condition. Comment: " + expectedTag.exceptionComment());
            }
            methodReport.append(
                " | Expected condition: "
                    + expectedCondition
                    + ". Actual condition: "
                    + actualCondition
                    + "\n");
          }
        }

        if (errors) {
          report.append(methodReport.toString());
        }
      }

      result.setTotal(numberOfComments);
      report.append(result);
      System.out.println(report);

      resultsFile.append(result.asCSV());
      resultsFile.newLine();

      return result;
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }
}
