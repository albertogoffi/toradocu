package org.toradocu.testlib;

public class TestCaseStats {
	
	private int tp = 0, fp = 0, total = 0;
	
	public TestCaseStats(int total) {
		this.total = total;
	}
	
	public double getRecall() {
		return tp / (double) total;
	}

	public double getPrecision() {
		return tp / (double) (tp + fp);
	}

	public void incrementTP() {
		++tp;
	}
	
	public void incrementFP() {
		++fp;
	}
}
