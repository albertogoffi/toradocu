package org.toradocu.extractor;

import java.util.Objects;
import org.toradocu.Checks;

class AbstractTag implements Tag {

  /** The comment of this tag. */
  private Comment comment;

  /** The kind of this tag (e.g., @throws, @param). */
  private final Kind kind;

  /**
   * Java boolean condition translated from the comment for this {@code Tag}. Empty string if no
   * translations found or if translation not yet attempted.
   */
  private String condition;

  /**
   * Constructs a {@code Tag} of the specific kind, with the given comment.
   *
   * @param kind the comment kind. See {@code Tag.Kind} for the available kinds.
   * @param comment the comment associated with the exception
   * @throws NullPointerException if either kind or comment is null
   */
  AbstractTag(Kind kind, Comment comment) {
    Checks.nonNullParameter(kind, "kind");
    Checks.nonNullParameter(comment, "comment");
    this.kind = kind;
    this.comment = comment;
    condition = "";
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public String getCondition() {
    return condition;
  }

  @Override
  public void setCondition(String condition) {
    Checks.nonNullParameter(condition, "condition");
    this.condition = condition;
  }

  @Override
  public Comment getComment() {
    return comment;
  }

  @Override
  public void setComment(Comment comment) {
    this.comment = comment;
  }

  /**
   * Returns true if this {@code AbstractTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof AbstractTag)) return false;

    AbstractTag that = (AbstractTag) obj;
    return comment.equals(that.comment)
        && condition.equals(that.condition)
        && kind.equals(that.kind);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, condition, kind);
  }

  /**
   * Returns a string representation of this tag. The returned string is in the format "@throws
   * COMMENT" where COMMENT is the text of the comment in the tag. If translation has been attempted
   * on this tag, then the returned string is also appended with " ==&gt; CONDITION" where CONDITION
   * is the translated condition for the exception as a Java expression or the empty string if
   * translation failed.
   *
   * @return a string representation of this tag
   */
  @Override
  public String toString() {
    String result = kind + " " + comment;
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
    if (!condition.isEmpty()) {
      stringRepresentation += " ==> " + condition;
    }
    return stringRepresentation;
  }
}
