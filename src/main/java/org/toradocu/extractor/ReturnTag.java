package org.toradocu.extractor;

/**
 * Represents a Javadoc @return comment. Each {@link ReturnTag} consists of a comment and a
 * specification (available after the translation of the comment). The specification specifies the
 * postconditions of the method documented by this @return block comment.
 */
public final class ReturnTag extends BlockTag {

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
