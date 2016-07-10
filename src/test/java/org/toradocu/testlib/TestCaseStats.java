package org.toradocu.testlib;

/**
 * TestCaseStats keeps track of the precision and recall of a test case based
 * on the number of true positives and false positives.
 */
public class TestCaseStats {

	private int tp = 0, fp = 0, total = 0;

	/**
	 * Constructs a test case with the given number of relevant elements.
	 *
	 * @param total the number of relevant elements in the test case
	 */
	public TestCaseStats(int total) {
		this.total = total;
	}

	/**
	 * Returns the recall of the test case.
	 *
	 * @return the recall of the test case
	 */
	public double getRecall() {
		return tp / (double) total;
	}

	/**
	 * Returns the precision of the test case.
	 *
	 * @return the precision of the test case
	 */
	public double getPrecision() {
		return tp / (double) (tp + fp);
	}

	/**
	 * Increments the number of true positives found.
	 */
	public void incrementTP() {
		++tp;
	}

	/**
	 * Increments the number of false positives found.
	 */
	public void incrementFP() {
		++fp;
	}
}
