package org.toradocu.translator;

import java.util.Objects;
import java.util.Optional;

/**
 * This class represents a proposition about Java code. It consists of a subject, predicate, and a
 * translation of the proposition into a Java expression.
 */
public class Proposition {
  private final Subject subject;
  private final String predicate;
  private String translation;
  private final boolean isNegative;

  /**
   * Constructs and initializes a {@code Proposition} with the given subject and predicate.
   *
   * @param subject the subject of the proposition
   * @param predicate the predicate associated with the subject
   */
  public Proposition(Subject subject, String predicate) {
    this(subject, predicate, false);
  }

  /**
   * Constructs and initializes a {@code Proposition} with the given subject, container, and
   * predicate.
   *
   * @param subject the subject of the proposition
   * @param predicate the predicate associated with the subject
   * @param isNegative true if this Proposition is the negation of the given predicate, false
   *     otherwise
   */
  public Proposition(Subject subject, String predicate, boolean isNegative) {
    this.subject = Objects.requireNonNull(subject);
    this.predicate = Objects.requireNonNull(predicate);
    this.isNegative = isNegative;
  }

  /**
   * Returns the subject of the proposition.
   *
   * @return the subject of the proposition
   */
  public Subject getSubject() {
    return subject;
  }

  /**
   * Returns the predicate of the proposition, not taking into account whether this proposition is
   * negated.
   *
   * @return the predicate of the proposition
   */
  public String getPredicate() {
    return predicate;
  }

  /**
   * Returns true if the negation of this proposition is true (i.e. the predicate should be negated
   * to obtain a true statement).
   *
   * @return whether this proposition should be negated
   */
  public boolean isNegative() {
    return isNegative;
  }

  /**
   * Returns the proposition as a Java expression if it has been translated. The returned expression
   * takes into account whether the proposition is negated.
   *
   * @return the proposition as a Java expression if it has been translated
   */
  public Optional<String> getTranslation() {
    return Optional.ofNullable(translation);
  }

  /**
   * Sets the translation of the proposition to the given Java expression, taking into account if
   * this proposition is negated.
   *
   * @param translation a Java expression representing the proposition
   * @throws NullPointerException if translation is null
   */
  public void setTranslation(String translation) {
    Objects.requireNonNull(translation);
    this.translation = translation;
  }

  /**
   * Returns true if this {@code Proposition} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Proposition)) return false;

    Proposition that = (Proposition) obj;
    boolean result =
        subject.equals(that.subject)
            && predicate.equals(that.predicate)
            && isNegative == that.isNegative;
    if (translation == null) {
      return result && that.translation == null;
    } else {
      return result && translation.equals(that.translation);
    }
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, predicate, translation, isNegative);
  }

  /**
   * Returns a String representation of this proposition. The returned String is formatted as
   * "(SUBJECT, PREDICATE) -&gt; TRANSLATION", or if no translation is present, "(SUBJECT,
   * PREDICATE)". PREDICATE is formatted as "not (PREDICATE)" if this proposition is negative.
   *
   * @return a string representation of this proposition
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("(" + getSubject() + ", ");
    result.append(isNegative ? "not(" + predicate + ")" : predicate);
    result.append(")");
    if (getTranslation().isPresent()) {
      result.append(" -> " + getTranslation());
    }
    return result.toString();
  }
}
