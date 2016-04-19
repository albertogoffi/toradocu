package org.toradocu.testlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSuiteStats {
	
	private final List<TestCaseStats> tests = Collections.synchronizedList(new ArrayList<>());
	private float precision = 0, recall = 0, precisionStdDeviation = 0, recallStdDeviation = 0;
	
	public void addTest(TestCaseStats test) {
		tests.add(test);
	}
	
	public void computeResults() {
		this.precision = computePrecision();
		this.precisionStdDeviation = computePrecisionStdDeviation();
		this.recall = computeRecall();
		this.recallStdDeviation = computeRecallStdDeviation();
	}

	public float getPrecision() {
		return precision;
	}
	
	public float getRecall() {
		return recall;
	}
	
	public float getPrecisionStdDeviation() {
		return precisionStdDeviation;
	}
	
	public float getRecallStdDeviation() {
		return recallStdDeviation;
	}
	
	public float getFMeasure() {
		return (2 * getPrecision() * getRecall()) / (getPrecision() + getRecall());
	}

	private float computePrecision() {
		float precision = 0;
		for (TestCaseStats test : tests) {
			precision += test.getPrecision();
		}
		return precision / tests.size();
	}

	private float computeRecall() {
		float recall = 0;
		for (TestCaseStats test : tests) {
			recall += test.getRecall();
		}
		return recall / tests.size();
	}
	
	private float computePrecisionStdDeviation() {
		double deviation = 0;	
		for (TestCaseStats test : tests) {
			deviation += Math.pow(test.getPrecision() - precision, 2);
		}
		return (float) (deviation / tests.size());
	}

	private float computeRecallStdDeviation() {
		double deviation = 0;
		for (TestCaseStats test : tests) {
			deviation += Math.pow(test.getRecall() - recall, 2);
		}
		return (float) (deviation / tests.size());
	}
}
