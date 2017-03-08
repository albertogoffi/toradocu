package org.toradocu.testlib;

import org.toradocu.Toradocu;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class PrecisionRecallTest {

  public static Map<String, TestCaseStats> test(String targetClass, String srcPath, String expectedOutputDir) {
    String className = getClassName(targetClass);
    String actualOutputFile = "tmp" + File.separator + className + "_out.txt";
    String expectedOutputFile = expectedOutputDir + className + "_expected.txt";

    Toradocu.main(
        new String[] {
          "--targetClass",
          targetClass,
          "--saveConditionTranslatorOutput",
          actualOutputFile,
          "--oracleGeneration", "false",
          "--aspectTemplate",
          "src/main/resources/AspectTemplate.java",
          "--testClass",
          "foo",
          // "--debug",
          "-J-sourcepath=" + srcPath,
          "-J-docletpath=build/classes/main",
          "-J-d=tmp",
          "-J-quiet="
        });
    return compare(actualOutputFile, expectedOutputFile);
  }

  private static String getClassName(String qualifiedClassName) {
    return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
  }

  private static Map<String, TestCaseStats> compare(String outputFile, String expectedOutputFile) {

    try (BufferedReader outFile = Files.newBufferedReader(Paths.get(outputFile));
        BufferedReader expFile = Files.newBufferedReader(Paths.get(expectedOutputFile))) {
      List<String> output = outFile.lines().collect(Collectors.toList());
      List<String> expected = expFile.lines().collect(Collectors.toList());
      Map<String, String> expectedTranslations = new HashMap<>();
      for (String line : expected) {
        final String[] tokens = line.split("==>");
        expectedTranslations.put(tokens[0], tokens[1]);
      }

      Map<String, TestCaseStats> methodResults = new HashMap<>();
      for (String line : output) {
        final String[] tokens = line.split("==>");
        final String condition = tokens[0];
        final String translation = tokens[1];

        final String expectedTranslation = expectedTranslations.get(condition);
        assertNotNull("Method " + condition.substring(0, condition.indexOf(" throws "))
                + " not found in expected output file.", expectedTranslation);
        // Ignore results when expected translation is empty.
        if (!expectedTranslation.endsWith(" []") && !expectedTranslation.endsWith(" [???]")) {
          final String method = condition.substring(0, condition.indexOf(") throws ") + 1);
          TestCaseStats result = methodResults.computeIfAbsent(method, TestCaseStats::new);

          if (translation.equals(expectedTranslation)) {
            System.out.print("Correct ");
            result.incrementCorrect();
          } else {
            if (translation.equals(" []")) {
              System.out.print("Missing ");
              result.incrementMissig(); // Toradocu did not produce any translation.
            } else {
              System.out.print("Wrong ");
              result.incrementWrong(); // Translation produced by Toradocu is wrong.
            }
          }
          System.out.printf("condition.%nExpected: %s%nActual: %s%n%n", expectedTranslation,
              translation);
        }
      }
      return methodResults;
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }
}
