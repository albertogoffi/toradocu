package org.toradocu.extractor;

import java.util.Objects;
import java.util.StringJoiner;
import org.toradocu.util.Checks;
import randoop.condition.specification.ThrowsSpecification;

/**
 * Represents Javadoc @throws and @exception comments. Each {@link ThrowsTag} consists of a comment,
 * an exception, and a specification (available after the translation of the comment). The
 * specification specifies when the method documented with this @throws (or @exception) comment is
 * expected to throw the exception.
 */
public final class ThrowsTag extends BlockTag {

  /** The exception described in this {@code ThrowsTag}. */
  private final Class<?> exception;

  /**
   * Specification generated from the comment of this {@code ThrowsTag}. {@code null} if Toradocu
   * failed to generate a specification or if comment translation not yet attempted.
   */
  private ThrowsSpecification specification;

  /**
   * Constructs a {@code ThrowsTag} with the given exception and comment.
   *
   * @param exception the exception type, must not be null
   * @param comment the comment associated with the exception, must not be null
   */
  ThrowsTag(Class<?> exception, Comment comment) {
    super(Kind.THROWS, comment);
    Checks.nonNullParameter(exception, "exception");
    this.exception = exception;
  }

  /**
   * Returns the type of the exception in this throws tag.
   *
   * @return the type of the exception in this throws tag
   */
  public Class<?> getException() {
    return exception;
  }

  @Override
  public ThrowsSpecification getSpecifications() {
    return specification;
  }

  /**
   * Sets the specification generated from this tag.
   *
   * @param specification the specification corresponding to the comment of this tag
   */
  public void setSpecification(ThrowsSpecification specification) {
    this.specification = specification;
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
   * "@throws EXCEPTION COMMENT" where EXCEPTION is the string representation of the fully qualified
   * type of the exception in this throws tag and COMMENT is the text of the comment in the throws
   * tag. If translation has been attempted on this tag, then the returned string is also appended
   * with " ==&gt; CONDITION" where CONDITION is the translated condition for the exception as a
   * Java expression or the empty string if translation failed.
   *
   * @return a string representation of this throws tag
   */
  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add(getKind().toString());
    joiner.add(getException().getName());
    joiner.add(getComment().getText());
    return appendSpecification(joiner.toString());
  }

  @Override
  String appendSpecification(String stringRepresentation) {
    if (specification != null) {
      return stringRepresentation + " ==> " + specification;
    }
    return stringRepresentation;
  }
}
