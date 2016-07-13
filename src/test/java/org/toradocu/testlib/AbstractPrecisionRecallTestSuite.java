package org.toradocu.testlib;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Represents an abstract test suite that uses precision and recall to measure
 * relevance.
 */
public abstract class AbstractPrecisionRecallTestSuite {

	protected static final double PRECISION = 0.01;
	protected static final String PRECISION_MESSAGE = "Precision is different than expected";
	protected static final String RECALL_MESSAGE = "Recall is different than expected";

	/** Keeps track of statistics on currently run tests. */
	private static TestSuiteStats testSuiteStats;
	/** The directory containing the source files on which to run tests. */
	private String sourceDirPath;
	/** The directory containing the expected output of the tests. */
	private String expectedOutputDirPath;

	/**
	 * Initializes the test suite.
	 */
	@BeforeClass
	public static void setUp() {
		testSuiteStats = new TestSuiteStats();
	}

	/**
	 * Prints the results (i.e. statistics) of the test suite.
	 */
	@AfterClass
	public static void tearDown() {
		StringBuilder report = new StringBuilder();
		testSuiteStats.computeResults();
		report.append("=== Test Suite ===").append("\n");
		report.append("Average precision: " + String.format("%.2f", testSuiteStats.getPrecision()));
		report.append(", Std deviation: " + String.format("%.2f", testSuiteStats.getPrecisionStdDeviation())).append("\n");
		report.append("Average recall: " + String.format("%.2f", testSuiteStats.getRecall()));
		report.append(", Std deviation: " + String.format("%.2f", testSuiteStats.getRecallStdDeviation())).append("\n");
		report.append("F-measure: " + String.format("%.2f", testSuiteStats.getFMeasure()));
		System.out.println(report);
	}

	/**
	 * Runs a precision recall test on the given target class.
	 *
	 * @param targetClass the fully qualified name of the class on which to
	 *        run the test
	 * @return the statistics for the test
	 */
	protected TestCaseStats test(String targetClass) {
		TestCaseStats stats = PrecisionRecallTest.test(targetClass, sourceDirPath, expectedOutputDirPath);
		testSuiteStats.addTest(stats);
		return stats;
	}

	/**
	 * Sets the path to the directory containing the source files on which to
	 * run tests.
	 *
	 * @param sourceDirPath the path to the directory containing the source
	 * files on which to run tests
	 */
	protected void setSourceDir(String sourceDirPath) {
		this.sourceDirPath = sourceDirPath;
	}

	/**
	 * Sets the path to the directory containing the expected output of the
	 * tests.
	 *
	 * @param expectedOutputDirPath the path to the directory containing the
	 * expected output of the tests
	 */
	protected void setExpectedOutputDir(String expectedOutputDirPath) {
		this.expectedOutputDirPath = expectedOutputDirPath;
	}
}
