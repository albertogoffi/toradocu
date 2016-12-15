package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.toradocu.util.Checks;

/**
 * This class represents a throws tag in a method. Each @throws tag consists of an exception, a
 * comment, and can have an optional condition. A condition is the translation of the comment into a
 * Java boolean condition.
 */
public class ThrowsTag {

  /** The exception described in this {@code ThrowsTag}. */
  private final Type exception;
  /** The comment associated with the exception. */
  private final String comment;
  /**
   * Java boolean condition translated from the comment for this {@code ThrowsTag}. Null if
   * translation not yet attempted. Empty string if no translations found.
   */
  private String condition;
  /** Code tags specified in the method's Javadoc. For now stored as simple Strings. */
  private final List<String> codeTags;

  /**
   * Constructs a {@code ThrowsTag} with the given exception and comment.
   *
   * @param exception the exception type
   * @param comment the comment associated with the exception
   * @throws NullPointerException if exception or comment is null
   */
  public ThrowsTag(Type exception, String comment, Collection<String> codeTags) {
    Checks.nonNullParameter(exception, "exception");
    Checks.nonNullParameter(comment, "comment");
    this.comment = comment;
    this.exception = exception;
    this.codeTags = codeTags == null ? new ArrayList<>() : new ArrayList<>(codeTags);
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
   * Returns the comment associated with the exception in this throws tag.
   *
   * @return the comment associated with the exception in this throws tag
   */
  public String exceptionComment() {
    return comment;
  }

  /**
   * Returns the translated Java boolean condition for this throws tag as an optional which is empty
   * if translation has not been attempted yet.
   *
   * @return the translated conditions for this throws tag if translation attempted, else empty
   *     optional
   */
  public Optional<String> getCondition() {
    return Optional.ofNullable(condition);
  }

  /**
   * Sets the translated condition for this throws tags to the given condition.
   *
   * @param condition the translated condition for this throws tag (as a Java boolean condition)
   * @throws NullPointerException if condition is null
   */
  public void setCondition(String condition) {
    Checks.nonNullParameter(condition, "condition");
    this.condition = condition;
  }

  public List<String> getCodeTags() {
    return codeTags;
  }

  /* This method will be used to check if in the code tags of the method's Javadoc
   * there is at least an element of the list passed as argument
   * (e.g. the list can contain some parameter's identifiers) */
  public boolean findCodeTag(List<String> list) {
    for (String identifier : list) {
      if (this.codeTags.contains(identifier)) return true;
    }
    return false;
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
    return this.comment.equals(that.comment)
        && this.exception.equals(that.exception)
        && Objects.equals(this.condition, that.condition);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, exception, condition);
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
    String result = "@throws " + exception + " " + comment;
    if (condition != null) {
      result += " ==> " + condition;
    }
    return result;
  }
}
