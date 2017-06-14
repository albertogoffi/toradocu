package org.toradocu.extractor;

import java.util.Objects;
import org.toradocu.util.Checks;

/** Represents a Javadoc tag. Supported tags are registered in {@code Tag.Kind}. */
public abstract class Tag {

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
  Tag(Kind kind, Comment comment) {
    Checks.nonNullParameter(kind, "kind");
    Checks.nonNullParameter(comment, "comment");
    this.kind = kind;
    this.comment = comment;
    condition = "";
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
   * Returns the translated Java boolean condition for this tag. Empty if the translation has not
   * been attempted yet or no translation has been generated.
   *
   * @return the translated conditions for this tag. Empty if the translation has not been attempted
   *     yet or no translation has been generated.
   */
  public String getCondition() {
    return condition;
  }

  /**
   * Sets the translated condition for this tag to the given condition.
   *
   * @param condition the translated condition for this tag (as a Java boolean condition)
   * @throws NullPointerException if condition is null
   */
  public void setCondition(String condition) {
    Checks.nonNullParameter(condition, "condition");
    this.condition = condition;
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
   * @param comment the comment for this tag
   * @throws NullPointerException if comment is null
   */
  public void setComment(Comment comment) {
    this.comment = comment;
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
    return comment.equals(that.comment)
        && kind.equals(that.kind)
        && condition.equals(that.condition);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, kind, condition);
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
