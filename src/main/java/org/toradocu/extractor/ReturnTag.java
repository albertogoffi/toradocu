package org.toradocu.extractor;

/**
 * This class represents a return tag in a method. Each @return tag has a comment, and can have an
 * optional condition. A condition is the translation of the comment into a Java boolean condition.
 * When the condition evaluates to {@code true}, documented and actual behaviors are consistent.
 */
public class ReturnTag extends Tag {

  /**
   * Constructs a {@code ReturnTag} with the given comment
   *
   * @param comment the comment associated to the return tag
   */
  ReturnTag(Comment comment) {
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
    return obj instanceof ReturnTag && super.equals(obj);
  }
}
