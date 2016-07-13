package org.toradocu.testlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TestSuiteStats computes the mean and standard deviation of the precision
 * and recall of a collection of tests.
 */
public class TestSuiteStats {

	/** A list of statistics for individual test cases in a test suite. */
	private final List<TestCaseStats> tests = Collections.synchronizedList(new ArrayList<>());
	private double precision = 0, recall = 0, precisionStdDeviation = 0, recallStdDeviation = 0;

	/**
	 * Adds a test case to the list of test cases used to compute statistics
	 * for the test suite. Added test cases are not incorporated into the test
	 * suite statistics until computeResults is called.
	 *
	 * @param test the statistics for the test case
	 */
	public void addTest(TestCaseStats test) {
		tests.add(test);
	}

	/**
	 * Computes statistics for this test suite, incorporating statistics from
	 * any added test cases.
	 */
	public void computeResults() {
		this.precision = computePrecision();
		this.precisionStdDeviation = computePrecisionStdDeviation();
		this.recall = computeRecall();
		this.recallStdDeviation = computeRecallStdDeviation();
	}

	/**
	 * Returns the average precision of the tests.
	 *
	 * @return the average precision of the tests
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * Returns the average recall of the tests.
	 *
	 * @return the average recall of the tests
	 */
	public double getRecall() {
		return recall;
	}

	/**
	 * Returns the standard deviation of the precision of the tests.
	 *
	 * @return the standard deviation of the precision of the tests
	 */
	public double getPrecisionStdDeviation() {
		return precisionStdDeviation;
	}

	/**
	 * Returns the standard deviation of the recall of the tests.
	 *
	 * @return the standard deviation of the recall of the tests
	 */
	public double getRecallStdDeviation() {
		return recallStdDeviation;
	}

	/**
	 * Returns the F-measure of the tests.
	 *
	 * @return the F-measure of the tests
	 */
	public double getFMeasure() {
		return (2 * getPrecision() * getRecall()) / (getPrecision() + getRecall());
	}

	/**
	 * Computes and returns the average precision of the tests.
	 *
	 * @return the average precision of the tests
	 */
	private double computePrecision() {
		double precision = 0;
		for (TestCaseStats test : tests) {
			precision += test.getPrecision();
		}
		return precision / tests.size();
	}

	/**
	 * Computes and returns the average recall of the tests.
	 *
	 * @return the average recall of the tests
	 */
	private double computeRecall() {
		double recall = 0;
		for (TestCaseStats test : tests) {
			recall += test.getRecall();
		}
		return recall / tests.size();
	}

	/**
	 * Computes and returns the standard deviation of the precision of the tests.
	 *
	 * @return the standard deviation of the precision of the tests
	 */
	private double computePrecisionStdDeviation() {
		double deviation = 0;
		for (TestCaseStats test : tests) {
			deviation += Math.pow(test.getPrecision() - precision, 2);
		}
		return Math.sqrt(deviation / tests.size());
	}

	/**
	 * Computes and returns the standard deviation of the recall of the tests.
	 *
	 * @return the standard deviation of the recall of the tests
	 */
	private double computeRecallStdDeviation() {
		double deviation = 0;
		for (TestCaseStats test : tests) {
			deviation += Math.pow(test.getRecall() - recall, 2);
		}
		return Math.sqrt(deviation / tests.size());
	}
}
