package org.toradocu.testlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSuiteStats {
	
	private final List<TestCaseStats> tests = Collections.synchronizedList(new ArrayList<>());
	private double precision = 0, recall = 0, precisionStdDeviation = 0, recallStdDeviation = 0;
	
	public void addTest(TestCaseStats test) {
		tests.add(test);
	}
	
	public void computeResults() {
		this.precision = computePrecision();
		this.precisionStdDeviation = computePrecisionStdDeviation();
		this.recall = computeRecall();
		this.recallStdDeviation = computeRecallStdDeviation();
	}

	public double getPrecision() {
		return precision;
	}
	
	public double getRecall() {
		return recall;
	}
	
	public double getPrecisionStdDeviation() {
		return precisionStdDeviation;
	}
	
	public double getRecallStdDeviation() {
		return recallStdDeviation;
	}
	
	public double getFMeasure() {
		return (2 * getPrecision() * getRecall()) / (getPrecision() + getRecall());
	}

	private double computePrecision() {
		float precision = 0;
		for (TestCaseStats test : tests) {
			precision += test.getPrecision();
		}
		return precision / tests.size();
	}

	private double computeRecall() {
		float recall = 0;
		for (TestCaseStats test : tests) {
			recall += test.getRecall();
		}
		return recall / tests.size();
	}
	
	private double computePrecisionStdDeviation() {
		double deviation = 0;	
		for (TestCaseStats test : tests) {
			deviation += Math.pow(test.getPrecision() - precision, 2);
		}
		return deviation / tests.size();
	}

	private double computeRecallStdDeviation() {
		double deviation = 0;
		for (TestCaseStats test : tests) {
			deviation += Math.pow(test.getRecall() - recall, 2);
		}
		return deviation / tests.size();
	}
}
