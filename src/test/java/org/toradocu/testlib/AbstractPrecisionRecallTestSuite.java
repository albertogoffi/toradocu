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

  @BeforeClass
  public static void setUp() {
    testSuiteStats = new TestSuiteStats();
  }

  @AfterClass
  public static void tearDown() {
    final String SEPARATOR = ",";
    StringBuilder report = new StringBuilder();
    final Map<String, TestCaseStats> tests = testSuiteStats.getTests();
    for (String method : tests.keySet()) {
      report.append("\"").append(method).append("\"").append(SEPARATOR);
      final TestCaseStats methodResults = tests.get(method);
      report.append(methodResults.getCorrect()).append(SEPARATOR)
          .append(methodResults.getWrong()).append(SEPARATOR)
          .append(methodResults.getMissing()).append(SEPARATOR)
          .append(methodResults.getUnexpected()).append("\n");
    }

    try (BufferedWriter resultsFile =
          Files.newBufferedWriter(
              Paths.get("results_toradocu-0.1.csv"),
              StandardOpenOption.CREATE,
              StandardOpenOption.APPEND)) {
      resultsFile.write(report.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
