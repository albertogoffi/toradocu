package org.toradocu.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.toradocu.Toradocu;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.extractor.Tag;

/**
 * Represents Toradocu precision/recall for a given Java element (for example, it can be a class or
 * a method). The Java element is only referred by its name and is used only to output the collected
 * statistics.
 */
public class Stats {

  /** Java element (for example a class or method name) this statistics refer to. */
  private final String identifier;

  /** Number of @throws conditions correctly translated by Toradocu (true positives). */
  private int correctTranslationsThrows = 0;
  /** Number of @throws conditions wrongly translated by Toradocu (false positives). */
  private int wrongTranslationThrows = 0;
  /** Number of @throws conditions not translated at all by Toradocu (false negatives). */
  private int missingTranslationsThrows = 0;

  /** Number of @param conditions correctly translated by Toradocu (true positives). */
  private int correctTranslationsParam = 0;
  /** Number of @param conditions wrongly translated by Toradocu (false positives). */
  private int wrongTranslationParam = 0;
  /** Number of @param conditions not translated at all by Toradocu (false negatives). */
  private int missingTranslationsParam = 0;

  /** Number of @return conditions correctly translated by Toradocu (true positives). */
  private int correctTranslationsReturn = 0;
  /** Number of @return conditions wrongly translated by Toradocu (false positives). */
  private int wrongTranslationReturn = 0;
  /** Number of @return conditions not translated at all by Toradocu (false negatives). */
  private int missingTranslationsReturn = 0;

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
   * Returns the recall for the specified tag kind.
   *
   * @param kind the kind of the tag
   * @return the recall for the specified {@code Tag.Kind}
   */
  public double getRecall(Tag.Kind kind) {
    final int conditions = numberOfConditions(kind);
    switch (kind) {
      case THROWS:
        return conditions == 0 ? 1 : correctTranslationsThrows / (double) conditions;
      case PARAM:
        return conditions == 0 ? 1 : correctTranslationsParam / (double) conditions;
      case RETURN:
        return conditions == 0 ? 1 : correctTranslationsReturn / (double) conditions;
      default:
        throw new IllegalStateException("Unsupported Tag.Kind " + kind);
    }
  }

  /**
   * Returns the precision for the given kind of tag.
   *
   * @param kind the kind of the tag
   * @return the precision for the given kind of tag
   */
  public double getPrecision(Tag.Kind kind) {
    int translated;
    switch (kind) {
      case THROWS:
        translated = correctTranslationsThrows + wrongTranslationThrows;
        return translated == 0 ? 1 : correctTranslationsThrows / (double) translated;
      case PARAM:
        translated = correctTranslationsParam + wrongTranslationParam;
        return translated == 0 ? 1 : correctTranslationsParam / (double) translated;
      case RETURN:
        translated = correctTranslationsReturn + wrongTranslationReturn;
        return translated == 0 ? 1 : correctTranslationsReturn / (double) translated;
      default:
        throw new IllegalStateException("Unsupported Tag.Kind " + kind);
    }
  }

  /**
   * Returns the recall considering all the translations for both @param and @throws tags.
   *
   * @return the recall considering all the translations for both @param and @throws tags
   */
  public double getRecall() {
    final int conditions = numberOfConditions();
    return conditions == 0 ? 1 : numberOfCorrectTranslations() / (double) conditions;
  }

  /**
   * Returns the precision considering all the translations for both @param and @throws tags.
   *
   * @return the precision considering all the translations for both @param and @throws tags
   */
  public double getPrecision() {
    final int translated = numberOfCorrectTranslations() + numberOfWrongTranslations();
    return translated == 0 ? 1 : numberOfCorrectTranslations() / (double) translated;
  }

  /**
   * Return the total number of conditions for a given tag kind.
   *
   * @return the number of conditions for the given {@code Tag.Kind}
   */
  private int numberOfConditions(Tag.Kind kind) {
    switch (kind) {
      case THROWS:
        return correctTranslationsThrows + wrongTranslationThrows + missingTranslationsThrows;
      case PARAM:
        return correctTranslationsParam + wrongTranslationParam + missingTranslationsParam;
      case RETURN:
        return correctTranslationsReturn + wrongTranslationReturn + missingTranslationsReturn;
      default:
        throw new IllegalStateException("Unsupported Tag.Kind " + kind);
    }
  }

  /**
   * Return the total number of conditions. These numbers include conditions from both @param
   * and @throws tags.
   *
   * @return the total number of conditions
   */
  public int numberOfConditions() {
    return numberOfCorrectTranslations()
        + numberOfWrongTranslations()
        + numberOfMissingTranslations();
  }

  /**
   * Return the total number of correct translations. These numbers include conditions from
   * both @param and @throws tags.
   *
   * @return the total number of correct translations
   */
  private int numberOfCorrectTranslations() {
    return correctTranslationsParam + correctTranslationsThrows + correctTranslationsReturn;
  }

  /**
   * Return the total number of wrong translations. These numbers include conditions from
   * both @param and @throws tags.
   *
   * @return the total number of wrong translations
   */
  private int numberOfWrongTranslations() {
    return wrongTranslationParam + wrongTranslationThrows + wrongTranslationReturn;
  }

  /**
   * Return the total number of missing translations. These numbers include conditions from
   * both @param and @throws tags.
   *
   * @return the total number of missing translations
   */
  private int numberOfMissingTranslations() {
    return missingTranslationsParam + missingTranslationsThrows + missingTranslationsReturn;
  }

  /**
   * Increments the number of correct translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of correct translations
   */
  private void addCorrectTranslation(Tag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++correctTranslationsThrows;
        break;
      case PARAM:
        ++correctTranslationsParam;
        break;
      case RETURN:
        ++correctTranslationsReturn;
        break;
      default:
        throw new IllegalStateException("Unsupported Tag.Kind " + kind);
    }
  }

  /**
   * Increments the number of wrong translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of wrong translations
   */
  private void addWrongTranslation(Tag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++wrongTranslationThrows;
        break;
      case PARAM:
        ++wrongTranslationParam;
        break;
      case RETURN:
        ++wrongTranslationReturn;
        break;
      default:
        throw new IllegalStateException("Unsupported Tag.Kind " + kind);
    }
  }

  /**
   * Increments the number of missing translations produced by Toradocu by 1.
   *
   * @param kind the kind of tag for which increment the number of missing translations
   */
  private void addMissingTranslation(Tag.Kind kind) {
    switch (kind) {
      case THROWS:
        ++missingTranslationsThrows;
        break;
      case PARAM:
        ++missingTranslationsParam;
        break;
      case RETURN:
        ++missingTranslationsReturn;
        break;
      default:
        throw new IllegalStateException("Unsupported Tag.Kind " + kind);
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
        + correctTranslationsThrows
        + SEPARATOR
        + wrongTranslationThrows
        + SEPARATOR
        + missingTranslationsThrows
        + SEPARATOR
        + getPrecision(Tag.Kind.THROWS)
        + SEPARATOR
        + getRecall(Tag.Kind.THROWS)
        + SEPARATOR
        + correctTranslationsParam
        + SEPARATOR
        + wrongTranslationParam
        + SEPARATOR
        + missingTranslationsParam
        + SEPARATOR
        + getPrecision(Tag.Kind.PARAM)
        + SEPARATOR
        + getRecall(Tag.Kind.PARAM)
        + SEPARATOR
        + correctTranslationsReturn
        + SEPARATOR
        + wrongTranslationReturn
        + SEPARATOR
        + missingTranslationsReturn
        + SEPARATOR
        + getPrecision(Tag.Kind.RETURN)
        + SEPARATOR
        + getRecall(Tag.Kind.RETURN)
        + SEPARATOR
        + numberOfCorrectTranslations()
        + SEPARATOR
        + numberOfWrongTranslations()
        + SEPARATOR
        + numberOfMissingTranslations()
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
      List<ExecutableMember> actualMethodList, List<ExecutableMember> expectedMethodList) {

    // TODO Fix the goal files and remove the following line. Goal files include inherited methods!
    expectedMethodList.removeIf(m -> !m.getContainingClass().equals(m.getContainingClass()));
    expectedMethodList.removeIf(m -> Modifier.isPrivate(m.getExecutable().getModifiers()));

    if (actualMethodList.size() != expectedMethodList.size()) {
      throw new IllegalArgumentException(
          "Actual and expected method list should be of the same size.");
    }

    List<Stats> stats = new ArrayList<>();
    for (int methodIndex = 0; methodIndex < expectedMethodList.size(); methodIndex++) {
      ExecutableMember actualMethod = actualMethodList.get(methodIndex);
      ExecutableMember expectedMethod = expectedMethodList.get(methodIndex);

      Stats methodStats =
          new Stats(actualMethod.getContainingClass() + "." + actualMethod.getSignature());
      collectStats(
          methodStats, actualMethod.throwsTags(), expectedMethod.throwsTags(), Tag.Kind.THROWS);
      collectStats(
          methodStats, actualMethod.paramTags(), expectedMethod.paramTags(), Tag.Kind.PARAM);

      List<ReturnTag> actualMethodReturnTag = new ArrayList<>();
      List<ReturnTag> expectedMethodReturnTag = new ArrayList<>();
      actualMethodReturnTag.add(actualMethod.returnTag());
      expectedMethodReturnTag.add(expectedMethod.returnTag());
      collectStats(methodStats, actualMethodReturnTag, expectedMethodReturnTag, Tag.Kind.RETURN);

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
      List<ExecutableMember> actualMethodList,
      List<ExecutableMember> expectedMethodList,
      StringBuilder output) {

    // TODO Fix the goal files and remove the following line. Goal files include inherited methods!
    expectedMethodList.removeIf(m -> !m.getContainingClass().equals(m.getContainingClass()));
    expectedMethodList.removeIf(m -> Modifier.isPrivate(m.getExecutable().getModifiers()));

    if (actualMethodList.size() != expectedMethodList.size()) {
      throw new IllegalArgumentException(
          "Actual and expected method list should be of the same size.");
    }

    Stats stats = new Stats(targetClass);
    for (int methodIndex = 0; methodIndex < expectedMethodList.size(); methodIndex++) {
      ExecutableMember actualMethod = actualMethodList.get(methodIndex);
      ExecutableMember expectedMethod = expectedMethodList.get(methodIndex);

      List<ReturnTag> actualMethodReturnTag = new ArrayList<>();
      List<ReturnTag> expectedMethodReturnTag = new ArrayList<>();
      actualMethodReturnTag.add(actualMethod.returnTag());
      expectedMethodReturnTag.add(expectedMethod.returnTag());

      output
          .append(
              collectStats(
                  stats, actualMethod.throwsTags(), expectedMethod.throwsTags(), Tag.Kind.THROWS))
          .append(
              collectStats(
                  stats, actualMethod.paramTags(), expectedMethod.paramTags(), Tag.Kind.PARAM))
          .append(
              collectStats(stats, actualMethodReturnTag, expectedMethodReturnTag, Tag.Kind.RETURN));
    }
    return stats;
  }

  private static StringBuilder collectStats(
      Stats stats,
      List<? extends Tag> actualTags,
      List<? extends Tag> expectedTags,
      Tag.Kind kind) {

    // TODO Restore the following check, once all the goal files are fixed (now that we completely
    // TODO removed inheritance!
    //    if (actualTags.size() != expectedTags.size()) {
    //      throw new IllegalArgumentException(
    //          "The number of "
    //              + kind
    //              + " tags ("
    //              + actualTags.size()
    //              + ") of method "
    //              + stats.identifier
    //              + " is different than expected ("
    //              + expectedTags.size()
    //              + ")");
    //    }

    final StringBuilder outputMessage = new StringBuilder();
    final Tag[] actualTagsArray = actualTags.toArray(new Tag[actualTags.size()]);
    final Tag[] expectedTagsArray = expectedTags.toArray(new Tag[expectedTags.size()]);
    for (int tagIndex = 0; tagIndex < actualTagsArray.length; tagIndex++) {
      Tag actualTag = actualTagsArray[tagIndex];
      Tag expectedTag = expectedTagsArray[tagIndex];

      if (actualTag != null
          && actualTag.getCondition() != null
          && expectedTag != null
          && expectedTag.getCondition() != null) {

        String expectedCondition = expectedTag.getCondition().replace(" ", "");
        if (Toradocu.configuration.useTComment() && actualTag.getKind().equals(Tag.Kind.RETURN)) {
          continue; // Ignore not translated @return tags when using @tComment engine.
        }
        String actualCondition = actualTag.getCondition().replace(" ", "");

        // Ignore conditions for which there is no known translation.
        if (!expectedCondition.isEmpty()) {
          if (expectedCondition.equals(actualCondition)) {
            stats.addCorrectTranslation(kind);
            outputMessage.append("Correct ");
          } else {
            if (actualCondition.isEmpty()) {
              stats.addMissingTranslation(kind);
              outputMessage.append("Empty ");
            } else {
              stats.addWrongTranslation(kind);
              outputMessage.append("Wrong ");
            }
          }
          outputMessage
              .append(kind)
              .append(" condition. Comment: ")
              .append(expectedTag.getComment())
              .append("\n\tExpected condition: ")
              .append(expectedCondition)
              .append("\n\tActual condition: ")
              .append(actualCondition)
              .append("\n");
        }
      }
    }
    return outputMessage;
  }
}
