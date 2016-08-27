package org.toradocu.testlib;

import org.junit.AfterClass;

/**
 * Represents an abstract test suite that uses precision and recall to measure
 * relevance.
 */
public abstract class AbstractPrecisionRecallTestSuite {

	protected static final double PRECISION = 0.01;
	protected static final String PRECISION_MESSAGE = "Precision is different than expected";
	protected static final String RECALL_MESSAGE = "Recall is different than expected";
	
	/** Keeps track of statistics on currently run tests. */
	private static final TestSuiteStats testSuiteStats = new TestSuiteStats();
	/** The directory containing the source files on which to run tests. */
	private final String sourceDirPath;
	/** The directory containing the binaries on which to run tests. */
    private final String binDirPath;
	/** The directory containing the expected output of the tests. */
	private final String expectedOutputDirPath;

	public AbstractPrecisionRecallTestSuite(String sourceDirPath, String binDirPath, String expectedOutputDirPath) {
	    this.sourceDirPath = sourceDirPath;
	    this.binDirPath = binDirPath;
	    this.expectedOutputDirPath = expectedOutputDirPath;
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
		TestCaseStats stats = PrecisionRecallTest.test(targetClass, sourceDirPath, binDirPath, expectedOutputDirPath);
		testSuiteStats.addTest(stats);
		return stats;
	}
}
