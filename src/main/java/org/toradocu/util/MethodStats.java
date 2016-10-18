package org.toradocu.util;

import org.toradocu.Toradocu;

/**
 * This class produces the statistics about the behavior of Toradocu on a specific method.
 */
public class MethodStats {
  /** Method name. **/
  private final String methodName;
  /** Number of conditions correctly translated by Toradocu. **/
  private int correctTranslations;
  /** Number of conditions wrongly translated by Toradocu. **/
  private int wrongTranslation;
  /** Number of conditions not translated at all by Toradocu. **/
  private int missingTranslations;

  public MethodStats(String methodName) {
    this.methodName = methodName;
    correctTranslations = 0;
    wrongTranslation = 0;
    missingTranslations = 0;
  }

  /**
   * Returns the recall.
   *
   * @return the recall
   */
  public double getRecall() {
    if (getNumberOfConditions() == 0) {
      return 0; // Toradocu did not translated any condition
    }
    return correctTranslations / (double) getNumberOfConditions();
  }

  /**
   * Returns the precision.
   *
   * @return the precision of the test case
   */
  public double getPrecision() {
    if (correctTranslations + wrongTranslation == 0) {
      return 1; // Toradocu did not translated any condition
    }
    return correctTranslations / (double) (correctTranslations + wrongTranslation);
  }

  /**
   * Return the total number of conditions.
   *
   * @return the number of conditions
   */
  public int getNumberOfConditions() {
    return correctTranslations + wrongTranslation + missingTranslations;
  }

  /**
   * Increments the number of correct translations produced by Toradocu by 1.
   */
  public void addCorrectTranslation() {
    ++correctTranslations;
  }

  /**
   * Increments the number of wrong translations produced by Toradocu by 1.
   */
  public void addWrongTranslation() {
    ++wrongTranslation;
  }

  /**
   * Increments the number of missing translations produced by Toradocu by 1.
   */
  public void addMissingTranslation() {
    ++missingTranslations;
  }

  @Override
  public String toString() {
    return methodName
        + " | Conditions: "
        + getNumberOfConditions()
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
    return "\""
        + methodName
        + "\""
        + SEPARATOR
        + Toradocu.configuration.getDistanceThreshold()
        + SEPARATOR
        + Toradocu.configuration.getWordRemovalCost()
        + SEPARATOR
        + getNumberOfConditions()
        + SEPARATOR
        + getPrecision()
        + SEPARATOR
        + getRecall();
  }
}
