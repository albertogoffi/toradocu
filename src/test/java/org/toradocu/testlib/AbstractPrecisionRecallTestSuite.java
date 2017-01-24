package org.toradocu.testlib;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public abstract class AbstractPrecisionRecallTestSuite {

  protected static TestSuiteStats testSuiteStats;
  protected static final float PRECISION = 0.01f;

  @BeforeClass
  public static void setUp() {
    testSuiteStats = new TestSuiteStats();
  }

  @AfterClass
  public static void tearDown() {
    final String SEPARATOR = ",";
    StringBuilder report = new StringBuilder();
    //		testSuiteStats.computeResults();
    //		report.append("=== Test Suite ===").append("\n");
    //		report.append("Average precision: " + testSuiteStats.getPrecision());
    //		report.append(", Std deviation: " + testSuiteStats.getPrecisionStdDeviation()).append("\n");
    //		report.append("Average recall: " + testSuiteStats.getRecall());
    //		report.append(", Std deviation: " + testSuiteStats.getRecallStdDeviation()).append("\n");
    //		report.append("F-measure: " + testSuiteStats.getFMeasure());

//    report.append("Method" + SEPARATOR + "Correct" + SEPARATOR + "Wrong" + SEPARATOR + "Missing" +
//        "\n");
    final Map<String, TestCaseStats> tests = testSuiteStats.getTests();
    for (String method : tests.keySet()) {
      report.append("\"").append(method).append("\"").append(SEPARATOR);
      final TestCaseStats methodResults = tests.get(method);
      report.append(methodResults.getCorrect()).append(SEPARATOR)
          .append(methodResults.getWrong()).append(SEPARATOR)
          .append(methodResults.getMissing()).append("\n");
    }

    try (BufferedWriter resultsFile =
          Files.newBufferedWriter(
              Paths.get("results.csv"),
              StandardOpenOption.CREATE,
              StandardOpenOption.APPEND)) {
      resultsFile.write(report.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
