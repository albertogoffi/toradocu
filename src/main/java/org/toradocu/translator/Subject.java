package org.toradocu.translator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.toradocu.util.Checks;

/**
 * Represents a subject in a sentence. The subject contains also the information about the container
 * in the case the subject is actually referring to the elements contained in a container.
 */
public class Subject {
  /** Words composing the subject */
  private final List<IndexedWord> subjectWords;
  /** Words composing the container */
  private final List<IndexedWord> containerWords;

  private String subjectAsString;
  private final String containerAsString;

  /**
   * Creates a new subject with an empty container.
   *
   * @param subject the words composing the subject
   * @throws NullPointerException if {@code subject} is null
   */
  public Subject(List<IndexedWord> subject) {
    this(subject, new ArrayList<>());
  }

  /**
   * Creates a new subject with the given subject and container words.
   *
   * @param subjectWords words composing the subject
   * @param containerWords words composing the container
   * @throws NullPointerException if {@code subjectWords} or {@code containerWords} is null
   */
  public Subject(List<IndexedWord> subjectWords, List<IndexedWord> containerWords) {
    Checks.nonNullParameter(subjectWords, "subject");
    Checks.nonNullParameter(containerWords, "container");
    this.subjectWords = subjectWords;
    this.containerWords = containerWords;
    this.subjectAsString =
        subjectWords.stream().map(IndexedWord::word).collect(Collectors.joining(" "));
    this.containerAsString =
        containerWords.stream().map(IndexedWord::word).collect(Collectors.joining(" "));
  }

  /**
   * Returns the words composing the subject.
   *
   * @return the words composing the subject
   */
  public List<IndexedWord> getSubjectWords() {
    return subjectWords;
  }

  /**
   * Returns the words composing the container.
   *
   * @return the words composing the container
   */
  public List<IndexedWord> getContainerWords() {
    return containerWords;
  }

  /**
   * Returns a string representation of the subject, where the subject words are concatenated using
   * whitespaces.
   *
   * @return a string representation of the subject, where the subject words are concatenated sing
   *     whitespaces.
   */
  public String getSubject() {
    return subjectAsString;
  }

  /**
   * Returns a string representation of the container, where the container words are concatenated
   * using whitespaces.
   *
   * @return a string representation of the container, where the container words are concatenated
   *     sing whitespaces.
   */
  public String getContainer() {
    return containerAsString;
  }

  /**
   * Set the string representation for this subject. Note that setting the string representations
   * using this method will create a mismatch between the words composing this subject and its
   * string representation.
   *
   * @param subject the string representation of the subject
   */
  public void setSubject(String subject) {
    subjectAsString = subject;
  }

  /**
   * Returns true if this subject is singular, false otherwise.
   *
   * @return true if this subject is singular, false otherwise
   */
  public boolean isSingular() {
    final IndexedWord mainSubjectWord = subjectWords.get(subjectWords.size() - 1);
    return mainSubjectWord
        .backingLabel()
        .get(CoreAnnotations.PartOfSpeechAnnotation.class)
        .equals("NN");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Subject)) {
      return false;
    }
    Subject that = (Subject) obj;
    return this.subjectWords.equals(that.subjectWords)
        && this.containerWords.equals(that.containerWords)
        && this.subjectAsString.equals(that.subjectAsString)
        && this.containerAsString.equals(that.containerAsString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subjectWords, containerWords, subjectAsString, containerAsString);
  }

  @Override
  public String toString() {
    String result = subjectAsString;
    if (!containerAsString.isEmpty()) {
      result += " " + containerAsString;
    }
    return result;
  }
}
