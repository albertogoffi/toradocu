package org.toradocu.testlib;

/**
 * TestCaseStats keeps track of the precision and recall of a test case based on the number of true
 * positives and false positives.
 */
public class TestCaseStats {

  /** Qualified name of the class under test. */
  private String className;
  /** tp: true positives, fp: false positives, numConditions: total number of conditions */
  private int tp = 0, fp = 0, numConditions = 0;

  public TestCaseStats(String className) {
    this.className = className;
  }

  /**
   * Returns the recall of the test case.
   *
   * @return the recall of the test case
   */
  public double getRecall() {
    return tp / (double) numConditions;
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
   * Returns the number of numConditions in the test case.
   *
   * @return the number of numConditions in the test case
   */
  public int getNumConditions() {
    return numConditions;
  }

  /**
   * Set the total number of numConditions in the test case.
   *
   * @param numConditions the number of numConditions in the test case
   */
  public void setNumConditions(int numConditions) {
    this.numConditions = numConditions;
  }

  /** Increments the number of true positives found. */
  public void incrementTP() {
    ++tp;
  }

  /** Increments the number of false positives found. */
  public void incrementFP() {
    ++fp;
  }

  @Override
  public String toString() {
    return className
        + " | # Conditions: "
        + numConditions
        + " | Precision: "
        + String.format("%.2f", getPrecision())
        + " | Recall: "
        + String.format("%.2f", getRecall());
  }

  /**
   * Returns the representation of this object as comma-separated values.
   *
   * @return the representation of this object as comma-separated values
   */
  public String asCSV() {
    final String SEPARATOR = ",";
    return className
        + SEPARATOR
        + numConditions
        + SEPARATOR
        + getPrecision()
        + SEPARATOR
        + getRecall();
  }
}
