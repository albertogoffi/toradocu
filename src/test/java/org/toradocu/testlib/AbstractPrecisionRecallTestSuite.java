package org.toradocu.testlib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Represents an abstract test suite that uses precision and recall to measure relevance.
 *
 * To create a new precision/recall test suite, create a new test class extending
 * `org.toradocu.testlib.AbstractPrecisionRecallTestSuite`.
 */
public abstract class AbstractPrecisionRecallTestSuite {

  protected static final double PRECISION = 0.001;
  protected static final String PRECISION_MESSAGE = "Precision is different than expected";
  protected static final String RECALL_MESSAGE = "Recall is different than expected";
  public static final String OUTPUT_DIR = "tmp";

  /** Keeps track of statistics on currently run tests. */
  private static final TestSuiteStats testSuiteStats = new TestSuiteStats();
  /** The directory containing the source files on which to run tests. */
  private final String sourceDirPath;
  /** The directory containing the binaries on which to run tests. */
  private final String binDirPath;
  /** The directory containing the expected output of the tests. */
  private final String expectedOutputDirPath;

  /**
   * Constructs and initializes a precision recall test suite that will test
   * Toradocu using the files in the given directories.
   *
   * @param sourceDirPath the path to the sources of the library to test
   * @param binDirPath the path to the binaries of the library to test
   * @param expectedOutputDirPath the path to the directory containing the
   *                              expected output files
   */
  public AbstractPrecisionRecallTestSuite(
      String sourceDirPath, String binDirPath, String expectedOutputDirPath) {
    this.sourceDirPath = sourceDirPath;
    this.binDirPath = binDirPath;
    this.expectedOutputDirPath = expectedOutputDirPath;
  }

  /**
   * Creates the temporary directory if it does not exist.
   */
  @BeforeClass
  public static void setUp() throws IOException {
    Files.createDirectories(Paths.get(OUTPUT_DIR));
  }

  /**
   * Prints the results (i.e. statistics) of the test suite.
   */
  @AfterClass
  public static void tearDown() {
    testSuiteStats.computeResults();
    System.out.println(
        "=== Test Suite ==="
            + "\nAverage precision: "
            + String.format("%.2f", testSuiteStats.getPrecision())
            + ", Std deviation: "
            + String.format("%.2f", testSuiteStats.getPrecisionStdDeviation())
            + "\nAverage recall: "
            + String.format("%.2f", testSuiteStats.getRecall())
            + ", Std deviation: "
            + String.format("%.2f", testSuiteStats.getRecallStdDeviation())
            + "\nF-measure: "
            + String.format("%.2f", testSuiteStats.getFMeasure()));
  }

  /**
   * Runs a precision recall test on the given target class.
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @return the statistics for the test
   */
  protected TestCaseStats test(String targetClass) {
    TestCaseStats stats =
        PrecisionRecallTest.test(targetClass, sourceDirPath, binDirPath, expectedOutputDirPath);
    testSuiteStats.addTest(stats);
    return stats;
  }
}
