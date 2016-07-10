package org.toradocu.testlib;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractPrecisionRecallTestSuite {

	protected static final double PRECISION = 0.01;
	protected static final String PRECISION_MESSAGE = "Precision is different than expected";
	protected static final String RECALL_MESSAGE = "Recall is different than expected";
	
	private static TestSuiteStats testSuiteStats;
	private String sourceDirPath, expectedOutputDirPath;
	
	@BeforeClass
	public static void setUp() {
		testSuiteStats = new TestSuiteStats();
	}
	
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
	
	protected TestCaseStats test(String targetClass) {
		TestCaseStats stats = PrecisionRecallTest.test(targetClass, sourceDirPath, expectedOutputDirPath);
		testSuiteStats.addTest(stats);
		return stats;
	}
	
	protected void setSourceDir(String sourceDirPath) {
		this.sourceDirPath = sourceDirPath;
	}
	
	protected void setExpectedOutputDir(String expectedOutputDirPath) {
		this.expectedOutputDirPath = expectedOutputDirPath;
	}
}
