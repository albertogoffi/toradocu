package org.toradocu.testlib;

public class TestCaseStats {
	
	private int tp = 0, fp = 0, total = 0;
	
	public TestCaseStats(int total) {
		this.total = total;
	}
	
	public float getRecall() {
		return tp / (float) total;
	}

	public float getPrecision() {
		return tp / (float) (tp + fp);
	}

	public void incrementTP() {
		++tp;
	}
	
	public void incrementFP() {
		++fp;
	}
}
