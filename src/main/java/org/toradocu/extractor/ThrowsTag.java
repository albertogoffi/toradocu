package org.toradocu.extractor;

import java.util.Objects;
import org.toradocu.util.Checks;

/**
 * This class represents a throws tag in a method. Each @throws tag consists of an exception, a
 * comment, and can have an optional condition. A condition is the translation of the comment into a
 * Java boolean condition.
 */
public class ThrowsTag extends AbstractTag {

  /** The exception described in this {@code ThrowsTag}. */
  private final Type exception;

  /**
   * Constructs a {@code ThrowsTag} with the given exception and comment.
   *
   * @param exception the exception type
   * @param comment the comment associated with the exception
   * @throws NullPointerException if exception or comment is null
   */
  public ThrowsTag(Type exception, String comment) {
    super("@throws", comment);
    Checks.nonNullParameter(exception, "exception");
    this.exception = exception;
  }

  /**
   * Returns the type of the exception in this throws tag.
   *
   * @return the type of the exception in this throws tag
   */
  public Type exception() {
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
    String result = super.getKind() + " " + exception + " " + super.getComment();
    if (super.getCondition() != null
        && super.getCondition().isPresent()
        && !super.getCondition().get().isEmpty()) {
      result += " ==> " + super.getCondition().get();
    }
    return result;
  }
}
