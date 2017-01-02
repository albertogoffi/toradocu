package org.toradocu.util;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;

/**
 * Represents Toradocu precision/recall for a given Java element (for example, it can be a class or
 * a method). The Java element is only referred by its name and is used only to output the collected
 * statistics.
 */
public class Stats {

  /** Java element (for example a class or method name) this statistics refer to. */
  private final String identifier;
  /** Number of conditions correctly translated by Toradocu (true positives). */
  private int correctTranslations = 0;
  /** Number of conditions wrongly translated by Toradocu (false positives). */
  private int wrongTranslation = 0;
  /** Number of conditions not translated at all by Toradocu (false negatives). */
  private int missingTranslations = 0;

  public Stats(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Returns the recall.
   *
   * @return the recall
   */
  public double getRecall() {
    final int CONDITIONS = getNumberOfConditions();
    return CONDITIONS == 0 ? 1 : correctTranslations / (double) CONDITIONS;
  }

  /**
   * Returns the precision.
   *
   * @return the precision
   */
  public double getPrecision() {
    final int TRANSLATED_CONDITIONS = correctTranslations + wrongTranslation;
    return TRANSLATED_CONDITIONS == 0 ? 1 : correctTranslations / (double) TRANSLATED_CONDITIONS;
  }

  /**
   * Return the total number of conditions.
   *
   * @return the number of conditions
   */
  public int getNumberOfConditions() {
    return correctTranslations + wrongTranslation + missingTranslations;
  }

  /** Increments the number of correct translations produced by Toradocu by 1. */
  public void addCorrectTranslation() {
    ++correctTranslations;
  }

  /** Increments the number of wrong translations produced by Toradocu by 1. */
  public void addWrongTranslation() {
    ++wrongTranslation;
  }

  /** Increments the number of missing translations produced by Toradocu by 1. */
  public void addMissingTranslation() {
    ++missingTranslations;
  }

  @Override
  public String toString() {
    return identifier
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
        + identifier
        + "\""
        + SEPARATOR
        + Toradocu.configuration.getDistanceThreshold()
        + SEPARATOR
        + Toradocu.configuration.getWordRemovalCost()
        + SEPARATOR
        + correctTranslations
        + SEPARATOR
        + wrongTranslation
        + SEPARATOR
        + missingTranslations
        + SEPARATOR
        + getPrecision()
        + SEPARATOR
        + getRecall();
  }

  /**
   * Compares the given {@code actualMethodList} with {@code expectedMethodList}. This method is
   * used to generate statistics (precision and recall) of Toradocu for each method in {@code
   * actualMethodList}.
   *
   * @param actualMethodList methods with tags translated by Toradocu
   * @param expectedMethodList methods with tags manually translated
   * @throws IllegalArgumentException if {@code actualMethodList} and {@code expectedMethodList} are
   *     not of the same size
   * @return statistics for each method of the given lists
   */
  public static List<Stats> getStats(
      List<DocumentedMethod> actualMethodList, List<DocumentedMethod> expectedMethodList) {
    if (actualMethodList.size() != expectedMethodList.size()) {
      throw new IllegalArgumentException(
          "Actual and expected method list should be of the same size.");
    }

    List<Stats> stats = new ArrayList<>();
    for (int methodIndex = 0; methodIndex < expectedMethodList.size(); methodIndex++) {
      DocumentedMethod expectedMethod = expectedMethodList.get(methodIndex);
      ThrowsTag[] expectedMethodTags = expectedMethod.throwsTags().toArray(new ThrowsTag[0]);
      DocumentedMethod actualMethod = actualMethodList.get(methodIndex);
      ThrowsTag[] actualMethodTags = actualMethod.throwsTags().toArray(new ThrowsTag[0]);
      if (expectedMethodTags.length != actualMethodTags.length) {
        throw new IllegalArgumentException(
            "The number of @throws ("
                + expectedMethodTags.length
                + ") of method "
                + actualMethod
                + " is different than expected ("
                + actualMethodTags.length
                + ")");
      }

      Stats methodStats =
          new Stats(actualMethod.getContainingClass() + "." + actualMethod.getSignature());
      for (int tagIndex = 0; tagIndex < expectedMethodTags.length; tagIndex++) {
        ThrowsTag expectedTag = expectedMethodTags[tagIndex];
        ThrowsTag actualTag = actualMethodTags[tagIndex];
        String expectedCondition = expectedTag.getCondition().get();
        String actualCondition = actualTag.getCondition().get();

        // Ignore conditions for which there is no known translation
        if (!expectedCondition.isEmpty()) {
          if (expectedCondition.equals(actualCondition)) {
            methodStats.addCorrectTranslation();
          } else {
            if (actualCondition.isEmpty()) {
              methodStats.addMissingTranslation();
            } else {
              methodStats.addWrongTranslation();
            }
          }
        }
      }
      stats.add(methodStats);
    }
    return stats;
  }
}
