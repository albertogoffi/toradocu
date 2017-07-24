package org.toradocu.extractor;

import java.util.List;
import randoop.condition.specification.PostSpecification;

/**
 * Represents a Javadoc @return comment. Each {@link ReturnTag} consists of a comment and a
 * specification (available after the translation of the comment). The specification specifies the
 * postconditions of the method documented by this @return block comment.
 */
public final class ReturnTag extends BlockTag {

  /**
   * Specification generated from the comment of this {@code ThrowsTag}. {@code null} if Toradocu
   * failed to generate a specification or if comment translation not yet attempted.
   */
  private List<PostSpecification> specifications;

  /**
   * Constructs a {@code ReturnTag} with the given comment
   *
   * @param comment the comment associated to the return tag
   */
  ReturnTag(Comment comment) {
    super(Kind.RETURN, comment);
  }

  /**
   * Returns the specification that represents the translation for this tag. {@code null} if the
   * translation has not been attempted yet or no translation has been generated.
   *
   * @return the translation for this tag. {@code null} if the translation has not been attempted
   *     yet or no translation has been generated.
   */
  public List<PostSpecification> getSpecifications() {
    return specifications;
  }

  /**
   * Sets the specifications generated from this tag.
   *
   * @param specifications the specification corresponding to the comment of this tag
   */
  public void setSpecifications(List<PostSpecification> specifications) {
    this.specifications = specifications;
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

  @Override
  String appendSpecification(String stringRepresentation) {
    if (specifications != null) {
      return stringRepresentation + " ==> " + specifications;
    }
    return stringRepresentation;
  }
}
