package org.toradocu.testlib;

/**
 * TestCaseStats keeps track of the precision and recall of a test case based on the number of true
 * positives and false positives.
 */
public class TestCaseStats {

  private int tp = 0, fp = 0, total = 0;

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
    if (tp + fp == 0) {
      return 0;
    }
    return tp / (double) (tp + fp);
  }

  /**
   * Set the total number of relevant elements.
   *
   * @param total the number of relevant elements in the test case
   */
  public void setTotal(int total) {
    this.total = total;
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
