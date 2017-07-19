package org.toradocu.extractor;

import java.util.Objects;
import org.toradocu.translator.spec.Specification;
import org.toradocu.util.Checks;

/**
 * Represents a Javadoc block tag (e.g., {@code @param}, {@code @return}, {@code @thraws}) and its
 * associated text. Each {@code BlockTag} consists of a {@link BlockTag.Kind} ({@code @param},
 * {@code @return}, or {@code @throws}), a comment which is the text introduced by the tag, and a
 * specification (available after the translation of the comment). A specification is the
 * translation of the comment into a Java boolean condition.
 */
public abstract class BlockTag<S extends Specification> {

  /** The Javadoc block tags currently supported by Toradocu. */
  public enum Kind {
    PARAM, // @param
    RETURN, // @return
    THROWS; // @throws and @exception

    @Override
    public String toString() {
      final String label = name();
      switch (label) {
        case "PARAM":
          return "@param";
        case "RETURN":
          return "@return";
        case "THROWS":
          return "@throws";
        default:
          throw new IllegalStateException("The value " + label + " has no string representation.");
      }
    }
  }

  /** The kind of this tag (e.g., @throws, @param). */
  private final Kind kind;

  /** The comment of this tag. */
  private Comment comment;

  /**
   * Specification generated from the comment of this {@code BlockTag}. {@code null} if Toradocu
   * failed to generate a specification or if comment translation not yet attempted.
   */
  private S specification;

  /**
   * Constructs a {@code BlockTag} of the specific kind, with the given comment.
   *
   * @param kind the comment kind, must not be null
   * @param comment the comment associated with the exception, must not be null
   */
  public BlockTag(Kind kind, Comment comment) {
    Checks.nonNullParameter(kind, "kind");
    Checks.nonNullParameter(comment, "comment");
    this.kind = kind;
    this.comment = comment;
  }

  /**
   * Returns the kind of this tag (e.g., @throws, @param).
   *
   * @return the kind of this tag
   */
  public Kind getKind() {
    return kind;
  }

  /**
   * Returns the comment associated with the exception in this tag.
   *
   * @return the comment associated with the exception in this tag
   */
  public Comment getComment() {
    return comment;
  }

  /**
   * Sets the comment for this tag.
   *
   * @param comment the comment for this tag, must not be null
   */
  public void setComment(Comment comment) {
    Checks.nonNullParameter(comment, "comment");
    this.comment = comment;
  }

  /**
   * Returns the specification that represents the translation for this tag. {@code null} if the
   * translation has not been attempted yet or no translation has been generated.
   *
   * @return the translation for this tag. {@code null} if the translation has not been attempted
   *     yet or no translation has been generated.
   */
  public S getSpecification() {
    return specification;
  }

  /**
   * Sets the specification (translation) for this tag to the given specification.
   *
   * @param specification the comment translation for this tag (a specification)
   */
  public void setSpecification(S specification) {
    this.specification = specification;
  }

  /**
   * Returns true if this {@code BlockTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof BlockTag)) return false;

    BlockTag that = (BlockTag) obj;
    return kind.equals(that.kind)
        && comment.equals(that.comment)
        && Objects.equals(specification, that.specification);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, kind, specification);
  }

  /**
   * Returns a string representation of this tag. The returned string is in the format "@throws
   * COMMENT" where COMMENT is the text of the comment in the tag. If translation has been attempted
   * on this tag, then the returned string is also appended with " ==&gt; SPECIFICATION" where
   * CONDITION is the translation of this tag.
   *
   * @return a string representation of this tag
   */
  @Override
  public String toString() {
    String result = kind + " " + comment.getText();
    return appendCondition(result);
  }

  /**
   * Appends the condition to the given string if the condition is not empty. The condition is added
   * at the end of the given string in the following form: " ==&gt; condition".
   *
   * @param stringRepresentation a string representing this BlockTag
   * @return the string representation of this tag with the condition appended
   */
  String appendCondition(String stringRepresentation) {
    if (specification != null) {
      stringRepresentation += " ==> " + specification;
    }
    return stringRepresentation;
  }
}
