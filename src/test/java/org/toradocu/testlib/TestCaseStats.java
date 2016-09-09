package org.toradocu.testlib;

/**
 * TestCaseStats keeps track of the precision and recall of a test case based on the number of true
 * positives and false positives.
 */
public class TestCaseStats {

  /** Qualified name of the class under test. */
  private String className;
  /** tp: true positives, fp: false positives, total: total number of conditions */
  private int tp = 0, fp = 0, conditions = 0;

  public TestCaseStats(String className) {
    this.className = className;
  }

  /**
   * Returns the recall of the test case.
   *
   * @return the recall of the test case
   */
  public double getRecall() {
    return tp / (double) conditions;
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
    this.conditions = total;
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

  @Override
  public String toString() {
    return className
        + " | # Conditions: "
        + conditions
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
        + conditions
        + SEPARATOR
        + getPrecision()
        + SEPARATOR
        + getRecall();
  }
}
