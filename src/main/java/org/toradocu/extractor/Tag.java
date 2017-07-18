package org.toradocu.extractor;

import java.util.List;
import java.util.Objects;
import org.toradocu.util.Checks;
import randoop.condition.specification.Specification;

/**
 * Represents a Javadoc block tag (e.g. @param, @return). A {@link Tag} has a {@link Tag.Kind} that
 * specifies the kind of tag and a comment, that is the text introduced by the tag. From {@link
 * Tag}'s comment specifications can be derived.
 */
public abstract class Tag<S extends Specification> {

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
   * Specifications generated from the comment of this {@code Tag}. {@code null} if Toradocu failed
   * to generate a specification or if comment translation not yet attempted.
   */
  private List<S> specifications;

  /**
   * Constructs a {@code Tag} of the specific kind, with the given comment.
   *
   * @param kind the comment kind, must not be null. See {@code Tag.Kind} for the available kinds.
   * @param comment the comment associated with the exception, must not be null
   */
  public Tag(Kind kind, Comment comment) {
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
  public List<S> getSpecifications() {
    return specifications;
  }

  /**
   * Sets the specification (translation) for this tag to the given specification.
   *
   * @param specifications the comment translation for this tag (a specification)
   */
  public void setSpecification(List<S> specifications) {
    this.specifications = specifications;
  }

  /**
   * Returns true if this {@code Tag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Tag)) return false;

    Tag that = (Tag) obj;
    return kind.equals(that.kind)
        && comment.equals(that.comment)
        && Objects.equals(specifications, that.specifications);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, kind, specifications);
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
   * @param stringRepresentation a string representing this Tag
   * @return the string representation of this tag with the condition appended
   */
  String appendCondition(String stringRepresentation) {
    if (specifications != null) {
      stringRepresentation += " ==> " + specifications;
    }
    return stringRepresentation;
  }
}
