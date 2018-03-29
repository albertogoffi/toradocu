package org.toradocu.util;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.Toradocu;
import org.toradocu.extractor.BlockTag;
import org.toradocu.output.util.JsonOutput;
import org.toradocu.output.util.ReturnTagOutput;
import org.toradocu.output.util.TagOutput;

/**
 * Represents Toradocu precision/recall for a given Java element (for example, it can be a class or
 * a method). The Java element is only referred by its name and is used only to output the collected
 * statistics.
 */
public class Stats {

  /** Java element (for example a class or method name) this statistics refer to. */
  private final String identifier;

  /** Number of @throws conditions correctly translated by Toradocu. */
  private int correctThrowsTranslations = 0;
  /** Number of @throws conditions wrongly translated by Toradocu. */
  private int wrongThrowsTranslations = 0;
  /** Number of @throws conditions unexpectedly translated by Toradocu. */
  private int unexpectedThrowsTranslations = 0;
  /** Number of @throws conditions not translated at all by Toradocu. */
  private int missingThrowsTranslations = 0;

  /** Number of @param conditions correctly translated by Toradocu. */
  private int correctParamTranslations = 0;
  /** Number of @param conditions wrongly translated by Toradocu. */
  private int wrongParamTranslations = 0;
  /** Number of @param conditions unexpectedly translated by Toradocu. */
  private int unexpectedParamTranslations = 0;
  /** Number of @param conditions not translated at all by Toradocu. */
  private int missingParamTranslations = 0;

  /** Number of @return conditions correctly translated by Toradocu (true positives). */
  private int correctReturnTranslations = 0;
  /** Number of @return conditions wrongly translated by Toradocu (false positives). */
  private int wrongReturnTranslations = 0;
  /** Number of @return conditions unexpectedly translated by Toradocu. */
  private int unexpectedReturnTranslations = 0;
  /** Number of @return conditions not translated at all by Toradocu (false negatives). */
  private int missingReturnTranslations = 0;

  /**
   * Creates new stats for a given element with specified identifier. For example, identifier could
   * be a class name or a method name.
   *
   * @param identifier a string used in the output to identify of what these statistics are about
   */
  private Stats(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Returns the precision for the given kind of tag.
   *
   * @param kind the kind of the tag
   * @return the precision for the given kind of tag
   */
  public double getPrecision(BlockTag.Kind kind) {
    int translated, wrong;
    switch (kind) {
      case THROWS:
        wrong = unexpectedThrowsTranslations + wrongThrowsTranslations;
        translated = correctThrowsTranslations + wrong;
        return translated == 0 ? 1 : correctThrowsTranslations / (double) translated;
      case PARAM:
        wrong = unexpectedParamTranslations + wrongParamTranslations;
        translated = correctParamTranslations + wrong;
        return translated == 0 ? 1 : correctParamTranslations / (double) translated;
      case RETURN:
        wrong = unexpectedReturnTranslations + wrongReturnTranslations;
        translated = correctReturnTranslations + wrong;
        return translated == 0 ? 1 : correctReturnTranslations / (double) translated;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
  }

  /**
   * Returns the recall for the specified tag kind.
   *
   * @param kind the kind of the tag
   * @return the recall for the specified {@code BlockTag.Kind}
   */
  public double getRecall(BlockTag.Kind kind) {
    final int conditions = numberOfConditions(kind);
    switch (kind) {
      case THROWS:
        return conditions == 0 ? 1 : correctThrowsTranslations / (double) conditions;
      case PARAM:
        return conditions == 0 ? 1 : correctParamTranslations / (double) conditions;
      case RETURN:
        return conditions == 0 ? 1 : correctReturnTranslations / (double) conditions;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
  }

  /**
   * Return the total number of conditions for a given tag kind.
   *
   * @return the number of conditions for the given {@code BlockTag.Kind}
   */
  private int numberOfConditions(BlockTag.Kind kind) {
    switch (kind) {
      case THROWS:
        return correctThrowsTranslations + wrongThrowsTranslations + missingThrowsTranslations;
      case PARAM:
        return correctParamTranslations + wrongParamTranslations + missingParamTranslations;
      case RETURN:
        return correctReturnTranslations + wrongReturnTranslations + missingReturnTranslations;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
  }

  /**
   * Return the total number of conditions from @param, @return, and @throws tags. The return value
   * includes correct, wrong, missing, and unexpected specifications.
   *
   * @return the total number of conditions from @param, @return, and @throws tags
   */
  public int numberOfConditions() {
    return numberOfCorrectTranslations()
        + numberOfWrongTranslations()
        + numberOfMissingTranslations()
        + numberOfUnexpectedTranslations();
  }

  /**
   * Return the total number of correct translations from @param, @return, and @throws tags.
   *
   * @return the total number of correct translations
   */
  private int numberOfCorrectTranslations() {
    return correctParamTranslations + correctThrowsTranslations + correctReturnTranslations;
  }

  /**
   * Return the total number of wrong translations from @param, @return, and @throws tags.
   *
   * @return the total number of wrong translations
   */
  private int numberOfWrongTranslations() {
    return wrongParamTranslations + wrongThrowsTranslations + wrongReturnTranslations;
  }

  /**
   * Return the total number of missing translations from @param, @return, and @throws tags.
   *
   * @return the total number of missing translations
   */
  private int numberOfMissingTranslations() {
    return missingParamTranslations + missingThrowsTranslations + missingReturnTranslations;
  }

  /**
   * Return the total number of unexpected translations from @param, @return, and @throws tags.
   *
   * @return the number of unexpected conditions
   */
  private int numberOfUnexpectedTranslations() {
    return unexpectedParamTranslations
        + unexpectedThrowsTranslations
        + unexpectedReturnTranslations;
  }

  /**
   * Increments the number of correct translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of correct translations
   */
  private void addCorrectTranslation(BlockTag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++correctThrowsTranslations;
        break;
      case PARAM:
        ++correctParamTranslations;
        break;
      case RETURN:
        ++correctReturnTranslations;
        break;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
  }

  /**
   * Increments the number of wrong translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of wrong translations
   */
  private void addWrongTranslation(BlockTag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++wrongThrowsTranslations;
        break;
      case PARAM:
        ++wrongParamTranslations;
        break;
      case RETURN:
        ++wrongReturnTranslations;
        break;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
  }

  /**
   * Increments the number of missing translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of missing translations
   */
  private void addMissingTranslation(BlockTag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++missingThrowsTranslations;
        break;
      case PARAM:
        ++missingParamTranslations;
        break;
      case RETURN:
        ++missingReturnTranslations;
        break;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
  }

  /**
   * Increments the number of unexpected translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of missing translations
   */
  private void addUnexpectedTranslation(BlockTag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++unexpectedThrowsTranslations;
        break;
      case PARAM:
        ++unexpectedParamTranslations;
        break;
      case RETURN:
        ++unexpectedReturnTranslations;
        break;
      default:
        throw new IllegalStateException("Unsupported BlockTag.Kind " + kind);
    }
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
        + correctThrowsTranslations
        + SEPARATOR
        + wrongThrowsTranslations
        + SEPARATOR
        + unexpectedThrowsTranslations
        + SEPARATOR
        + missingThrowsTranslations
        + SEPARATOR
        + correctParamTranslations
        + SEPARATOR
        + wrongParamTranslations
        + SEPARATOR
        + unexpectedParamTranslations
        + SEPARATOR
        + missingParamTranslations
        + SEPARATOR
        + correctReturnTranslations
        + SEPARATOR
        + wrongReturnTranslations
        + SEPARATOR
        + unexpectedReturnTranslations
        + SEPARATOR
        + missingReturnTranslations;
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
      List<JsonOutput> actualMethodList, List<JsonOutput> expectedMethodList) {

    if (actualMethodList.size() != expectedMethodList.size()) {
      throw new IllegalArgumentException(
          "Actual and expected method list should be of the same size.");
    }

    List<Stats> stats = new ArrayList<>();
    for (int methodIndex = 0; methodIndex < expectedMethodList.size(); methodIndex++) {
      JsonOutput actualMethod = actualMethodList.get(methodIndex);
      JsonOutput expectedMethod = expectedMethodList.get(methodIndex);

      Stats methodStats =
          new Stats(actualMethod.containingClass.getQualifiedName() + "." + actualMethod.signature);
      collectStats(
          methodStats, actualMethod.throwsTags, expectedMethod.throwsTags, BlockTag.Kind.THROWS);
      collectStats(
          methodStats, actualMethod.paramTags, expectedMethod.paramTags, BlockTag.Kind.PARAM);

      List<ReturnTagOutput> actualMethodReturnTag = new ArrayList<>();
      List<ReturnTagOutput> expectedMethodReturnTag = new ArrayList<>();
      actualMethodReturnTag.add(actualMethod.returnTag);
      expectedMethodReturnTag.add(expectedMethod.returnTag);
      collectStats(
          methodStats, actualMethodReturnTag, expectedMethodReturnTag, BlockTag.Kind.RETURN);

      stats.add(methodStats);
    }
    return stats;
  }

  /**
   * Compares the given {@code actualMethodList} with {@code expectedMethodList}. This method is
   * used to generate statistics (precision and recall) of Toradocu for each method in {@code
   * actualMethodList}. The statistics are aggregated per class, we assume that the {@code
   * actualMethodList} contains methods belonging to one class.
   *
   * @param targetClass the class for which collect statistics
   * @param actualMethodList methods with tags translated by Toradocu
   * @param expectedMethodList methods with tags manually translated
   * @param output the output message to be populated
   * @throws IllegalArgumentException if {@code actualMethodList} and {@code expectedMethodList} are
   *     not of the same size
   * @return statistics for each method of the given lists, aggregated per class
   */
  public static Stats getStats(
      String targetClass,
      List<JsonOutput> actualMethodList,
      List<JsonOutput> expectedMethodList,
      StringBuilder output) {

    if (actualMethodList.size() != expectedMethodList.size()) {
      throw new IllegalArgumentException(
          "Actual and expected method list should be of the same size.");
    }

    Stats stats = new Stats(targetClass);
    for (int methodIndex = 0; methodIndex < expectedMethodList.size(); methodIndex++) {
      JsonOutput actualMethod = actualMethodList.get(methodIndex);
      JsonOutput expectedMethod = expectedMethodList.get(methodIndex);

      List<ReturnTagOutput> actualMethodReturnTag = new ArrayList<>();
      List<ReturnTagOutput> expectedMethodReturnTag = new ArrayList<>();
      actualMethodReturnTag.add(actualMethod.returnTag);
      expectedMethodReturnTag.add(expectedMethod.returnTag);

      output
          .append(
              collectStats(
                  stats, actualMethod.throwsTags, expectedMethod.throwsTags, BlockTag.Kind.THROWS))
          .append(
              collectStats(
                  stats, actualMethod.paramTags, expectedMethod.paramTags, BlockTag.Kind.PARAM))
          .append(
              collectStats(
                  stats, actualMethodReturnTag, expectedMethodReturnTag, BlockTag.Kind.RETURN));
    }
    return stats;
  }

  private static StringBuilder collectStats(
      Stats stats,
      List<? extends TagOutput> actualTags,
      List<? extends TagOutput> expectedTags,
      BlockTag.Kind kind) {

    final StringBuilder outputMessage = new StringBuilder();
    final TagOutput[] actualTagsArray = actualTags.toArray(new TagOutput[actualTags.size()]);
    final TagOutput[] expectedTagsArray = expectedTags.toArray(new TagOutput[expectedTags.size()]);
    for (int tagIndex = 0; tagIndex < actualTagsArray.length; tagIndex++) {
      TagOutput actualTag = actualTagsArray[tagIndex];
      TagOutput expectedTag = expectedTagsArray[tagIndex];

      if (actualTag != null && expectedTag != null) {
        String expectedCondition = expectedTag.getCondition().replace(" ", "");
        String actualCondition = actualTag.getCondition().replace(" ", "");

        if (actualCondition.equals(expectedCondition)) {
          if (!expectedCondition.isEmpty()) {
            stats.addCorrectTranslation(kind);
            outputMessage.append("Correct ");
          } else {
            continue; // No output message when Toradocu does not output anything as expected.
          }
        } else {
          if (expectedCondition.isEmpty()) {
            stats.addUnexpectedTranslation(kind);
            outputMessage.append("Unexpected ");
          } else if (actualCondition.isEmpty()) {
            stats.addMissingTranslation(kind);
            outputMessage.append("Missing ");
          } else {
            stats.addWrongTranslation(kind);
            outputMessage.append("Wrong ");
          }
        }
        outputMessage
            .append(kind)
            .append(" condition. Comment: ")
            .append(actualTag.getComment())
            .append("\n\tExpected condition: ")
            .append(expectedCondition)
            .append("\n\tActual condition: ")
            .append(actualCondition)
            .append("\n");
      }
    }
    return outputMessage;
  }
}
