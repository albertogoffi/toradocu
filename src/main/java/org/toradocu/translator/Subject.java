package org.toradocu.translator;

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
  private final List<IndexedWord> subject;
  /** Words composing the container */
  private final List<IndexedWord> container;

  /**
   * Creates a new subject with an empty predicate.
   *
   * @param subject the words composing the subject.
   */
  public Subject(List<IndexedWord> subject) {
    this(subject, new ArrayList<>());
  }

  /**
   * Creates a new subject with the given subject and container words.
   *
   * @param subject words composing the subject
   * @param container words composing the container
   * @throws NullPointerException if subject or container is null
   */
  public Subject(List<IndexedWord> subject, List<IndexedWord> container) {
    Checks.nonNullParameter(subject, "subject");
    Checks.nonNullParameter(container, "container");
    this.subject = subject;
    this.container = container;
  }

  /**
   * Returns the words composing the subject.
   *
   * @return the words composing the subject
   */
  public List<IndexedWord> getSubject() {
    return subject;
  }

  /**
   * Returns the words composing the container.
   *
   * @return the words composing the container
   */
  public List<IndexedWord> getContainer() {
    return container;
  }

  /**
   * Returns a string representation of the subject, where the subject words are concatenated using
   * whitespaces.
   *
   * @return a string representation of the subject, where the subject words are concatenated sing
   *     whitespaces.
   */
  public String getSubjectAsString() {
    return subject.stream().map(IndexedWord::word).collect(Collectors.joining(" "));
  }

  /**
   * Returns a string representation of the container, where the container words are concatenated
   * using whitespaces.
   *
   * @return a string representation of the container, where the container words are concatenated
   *     sing whitespaces.
   */
  public String getContainerAsString() {
    if (!container.isEmpty()) {
      return container.stream().map(IndexedWord::word).collect(Collectors.joining(" "));
    } else {
      return "";
    }
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
    return this.subject.equals(that.subject) && this.container.equals(that.container);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, container);
  }

  @Override
  public String toString() {
    String asString = getSubjectAsString() + " " + getContainerAsString();
    if (!container.isEmpty()) {
      asString += " " + getContainerAsString();
    }
    return asString;
  }
}
