package org.toradocu.translator;

import edu.stanford.nlp.ling.IndexedWord;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Subject {
  private final List<IndexedWord> subject;
  private final List<IndexedWord> container;

  public Subject(List<IndexedWord> subject) {
    this(subject, new ArrayList<>());
  }

  public Subject(List<IndexedWord> subject, List<IndexedWord> container) {
    this.subject = subject;
    this.container = container;
  }

  public List<IndexedWord> getSubject() {
    return subject;
  }

  public List<IndexedWord> getContainer() {
    return container;
  }

  public String getSubjectAsString() {
    return subject.stream().map(IndexedWord::word).collect(Collectors.joining(" "));
  }

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
