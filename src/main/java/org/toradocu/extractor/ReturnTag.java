package org.toradocu.extractor;

import java.util.Objects;

/**
 * This class represents a throws tag in a method. Each @throws tag consists of an exception, a
 * comment, and can have an optional condition. A condition is the translation of the comment into a
 * Java boolean condition. When the condition evaluates to {@code true}, an exception is expected.
 */
public class ReturnTag extends AbstractTag {

  /**
   * Constructs a {@code ThrowsTag} with the given exception, comment, and words tagged with @code
   *
   * @param comment the comment associated to the return tag
   */
  public ReturnTag(String comment) {
    super(Kind.RETURN, comment);
  }

  /**
   * Returns true if this {@code ReturnTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ReturnTag)) return false;

    ReturnTag that = (ReturnTag) obj;
    return this.getComment().equals(that.getComment()) && super.equals(that);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }

  /**
   * Returns a string representation of this return tag. The returned string is in the format
   * "@return COMMENT" where COMMENT is the text of the comment in the return tag. If translation
   * has been attempted on this tag, then the returned string is also appended with " ==&gt;
   * CONDITION" where CONDITION is the translated condition for the exception as a Java expression
   * or the empty string if translation failed.
   *
   * @return a string representation of this throws tag
   */
  @Override
  public String toString() {
    String result = super.getKind() + " " + this.getComment() + " " + super.getComment();
    if (super.getCondition() != null
        && super.getCondition().isPresent()
        && !super.getCondition().get().isEmpty()) {
      result += " ==> " + super.getCondition().get();
    }
    return result;
  }
}
