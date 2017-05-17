package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.toradocu.util.Checks;

/**
 * This class represents a throws tag in a method. Each @throws tag consists of an exception, a
 * comment, and can have an optional condition. A condition is the translation of the comment into a
 * Java boolean condition. When the condition evaluates to {@code true}, an exception is expected.
 */
public class ThrowsTag extends AbstractTag {

  /** The exception described in this {@code ThrowsTag}. */
  private final Type exception;
  /** Code tags specified in the method's Javadoc. For now stored as simple Strings. */
  private final List<String> codeTags;

  /**
   * Constructs a {@code ThrowsTag} with the given exception, comment, and words tagged with @code
   *
   * @param exception the exception type
   * @param comment the comment associated with the exception
   * @param codeTags words tagged with @code
   * @throws NullPointerException if exception or comment is null
   */
  public ThrowsTag(Type exception, String comment, Collection<String> codeTags) {
    super(Kind.THROWS, comment);
    Checks.nonNullParameter(exception, "exception");
    this.exception = exception;
    this.codeTags = codeTags == null ? new ArrayList<>() : new ArrayList<>(codeTags);
  }

  /**
   * Constructs a {@code ThrowsTag} with the given exception and comment.
   *
   * @param exception the exception type
   * @param comment the comment associated with the exception
   * @throws NullPointerException if exception or comment is null
   */
  public ThrowsTag(Type exception, String comment) {
    this(exception, comment, null);
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
   * Checks if in the code tags of this ThrowsTag there is at least an element of {@code
   * wordsTaggedAsCode}.
   *
   * @param wordsTaggedAsCode words tagged with @code
   * @return {@code true} if the intersection between the words tagged with @code and {@code
   *     wordsTaggedAsCode} is note empty, {@code false} otherwise
   */
  public boolean intersect(List<String> wordsTaggedAsCode) {
    return codeTags.stream().filter(wordsTaggedAsCode::contains).count() != 0;
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
    return appendCondition(result);
  }
}
