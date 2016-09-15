package org.toradocu.util;

import org.toradocu.Toradocu;

/**
 * This class produces the statistics about the behavior of Toradocu on a specific method.
 */
public class MethodStats {
  /** Method name. **/
  private final String methodName;
  /** Number of conditions correctly translated by Toradocu. **/
  private int truePositives;
  /** Number of conditions wrongly translated by Toradocu. **/
  private int falsePositives;
  /** Number of conditions not translated at all by Toradocu. **/
  private int missingTranslations;

  public MethodStats(String methodName) {
    this.methodName = methodName;
    truePositives = 0;
    falsePositives = 0;
    missingTranslations = 0;
  }

  /**
   * Returns the recall.
   *
   * @return the recall
   */
  public double getRecall() {
    return truePositives / (double) (truePositives + falsePositives + missingTranslations);
  }

  /**
   * Returns the precision.
   *
   * @return the precision of the test case
   */
  public double getPrecision() {
    if (truePositives + falsePositives == 0) {
      return 0;
    }
    return truePositives / (double) (truePositives + falsePositives);
  }

  /**
   * Return the total number of conditions.
   *
   * @return the number of conditions
   */
  public int getNumberOfConditions() {
    return truePositives + falsePositives + missingTranslations;
  }

  /**
   * Increments the number of correct translations produced by Toradocu by 1.
   */
  public void addCorrectTranslation() {
    ++truePositives;
  }

  /**
   * Increments the number of wrong translations produced by Toradocu by 1.
   */
  public void addWrongTranslation() {
    ++falsePositives;
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
