package org.toradocu.testlib;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstracPrecisionRecallTestSuite {

	protected static TestSuiteStats testSuiteStats;
	protected static final float PRECISION = 0.01f;
	
	@BeforeClass
	public static void setUp() {
		testSuiteStats = new TestSuiteStats();
	}
	
	@AfterClass
	public static void tearDown() {
		StringBuilder report = new StringBuilder();
		testSuiteStats.computeResults();
		report.append("=== Test Suite ===").append("\n");
		report.append("Average precision: " + testSuiteStats.getPrecision());
		report.append(", Std deviation: " + testSuiteStats.getPrecisionStdDeviation()).append("\n");
		report.append("Average recall: " + testSuiteStats.getRecall());
		report.append(", Std deviation: " + testSuiteStats.getRecallStdDeviation()).append("\n");
		report.append("F-measure: " + testSuiteStats.getFMeasure());
		System.out.println(report);
	}
}
