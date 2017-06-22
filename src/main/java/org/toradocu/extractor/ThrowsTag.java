package org.toradocu.extractor;

import java.util.Objects;
import java.util.StringJoiner;
import org.toradocu.translator.spec.ExcPostcondition;
import org.toradocu.util.Checks;

/**
 * This class represents a throws tag in a method. Each @throws tag consists of an exception, a
 * comment, and can have an optional condition. A condition is the translation of the comment into a
 * Java boolean condition. When the condition evaluates to {@code true}, an exception is expected.
 */
public class ThrowsTag extends Tag<ExcPostcondition> {

  /** The exception described in this {@code ThrowsTag}. */
  private final Class<?> exception;

  /**
   * Constructs a {@code ThrowsTag} with the given exception and comment.
   *
   * @param exception the exception type
   * @param comment the comment associated with the exception
   * @throws NullPointerException if exception or comment is null
   */
  ThrowsTag(Class<?> exception, Comment comment) {
    super(Kind.THROWS, comment);
    Checks.nonNullParameter(exception, "exception");
    this.exception = exception;
  }

  /**
   * Returns the type of the exception in this throws tag.
   *
   * @return the type of the exception in this throws tag
   */
  public Class<?> getException() {
    return exception;
  }

  /**
   * Returns true if this {@code ThrowsTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ThrowsTag)) return false;

    ThrowsTag that = (ThrowsTag) obj;
    return this.exception.equals(that.exception) && super.equals(that);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exception);
  }

  /**
   * Returns a string representation of this throws tag. The returned string is in the format
   * "@throws EXCEPTION COMMENT" where EXCEPTION is the fully qualified name of the exception in
   * this throws tag and COMMENT is the text of the comment in the throws tag. If translation has
   * been attempted on this tag, then the returned string is also appended with " ==&gt; CONDITION"
   * where CONDITION is the translated condition for the exception as a Java expression or the empty
   * string if translation failed.
   *
   * @return a string representation of this throws tag
   */
  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add(super.getKind().toString());
    joiner.add(exception.getName());
    joiner.add(super.getComment().getText());
    return appendCondition(joiner.toString());
  }
}
