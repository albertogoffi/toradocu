package org.toradocu.extractor;

import java.util.Objects;
import org.toradocu.util.Checks;

/**
 * Represents a Javadoc @param comment. Each {@code ParamTag} consists of the name of the parameter,
 * a comment, and a specification (available after the translation of the comment). A specification
 * is the translation of the comment into a Java boolean condition. When the condition evaluates to
 * {@code true}, the precondition expressed by this tag is satisfied. When the condition evaluates
 * to {@code false} the precondition expressed by this tag is violated, and the behavior of the
 * method documented by this tag is unspecified.
 */
public final class ParamTag extends BlockTag {

  /** The parameter associated with the param tag */
  private final DocumentedParameter parameter;

  /**
   * Constructs a {@code ParamTag} with the given exception and comment.
   *
   * @param parameter the parameter that the tag refers to
   * @param comment the comment associated with the tag
   * @throws NullPointerException if parameter or comment is null
   */
  public ParamTag(DocumentedParameter parameter, Comment comment) {
    super(Kind.PARAM, comment);
    Checks.nonNullParameter(parameter, "parameter");
    this.parameter = parameter;
  }

  /**
   * Returns the parameter associated to the tag.
   *
   * @return the parameter associated to the tag.
   */
  public DocumentedParameter getParameter() {
    return parameter;
  }

  /**
   * Returns true if this {@code ParamTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ParamTag)) return false;

    ParamTag that = (ParamTag) obj;
    return this.parameter.equals(that.parameter) && super.equals(that);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parameter);
  }

  /**
   * Returns a string representation of this param tag. The returned string is in the format "@param
   * PARAMNAME COMMENT" where PARAMNAME is the fully qualified name of the parameter in the param
   * tag and COMMENT is the text of the comment in the param tag (without inline tags, as they are
   * removed during the instantiation of the Comment object).
   *
   * @return a string representation of this param tag
   */
  @Override
  public String toString() {
    return getKind() + " " + parameter.getName() + " " + getComment().getText();
  }
}
