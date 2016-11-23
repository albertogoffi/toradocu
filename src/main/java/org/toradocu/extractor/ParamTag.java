package org.toradocu.extractor;

import java.util.Objects;
import java.util.Optional;
import org.toradocu.util.Checks;

/**
 * This class represents a param tag in a method. Each param tag consist of the name of the param
 * and a brief description, which may contain a condition. A condition is the translation of the
 * comment into a Java boolean condition.
 */
public class ParamTag {

  /** The parameter associated with the param tag */
  private final Parameter parameter;
  /** The comment associated to the parameter tag */
  private final String comment;
  /**
   * Java boolean condition translated from the comment for this {@code ParamTag}. Null if
   * translation not yet attempted. Empty string if no translations found.
   */
  private String condition;

  /**
   * Constructs a {@code ParamTag} with the given exception and comment.
   *
   * @param parameter the parameter that the tag refers to
   * @param comment the comment associated with the tag
   * @throws NullPointerException if parameter or comment is null
   */
  public ParamTag(Parameter parameter, String comment) {
    Checks.nonNullParameter(parameter, "parameter");
    Checks.nonNullParameter(comment, "comment");
    this.comment = comment;
    this.parameter = parameter;
  }

  /**
   * Returns the parameter associated to the tag.
   *
   * @return the parameter associated to the tag.
   */
  public Parameter parameter() {
    return parameter;
  }

  /**
   * Returns the comment associated with the parameter in this param tag.
   *
   * @return the comment associated with the exception in this param tag
   */
  public String parameterComment() {
    return comment;
  }

  /**
   * Returns the translated Java boolean condition for this param tag as an optional which is empty
   * if translation has not been attempted yet.
   *
   * @return the translated conditions for this param tag if translation attempted, else empty
   *     optional
   */
  public Optional<String> getCondition() {
    return Optional.ofNullable(condition);
  }

  /**
   * Sets the translated condition for this param tags to the given condition.
   *
   * @param condition the translated condition for this param tag (as a Java boolean condition)
   * @throws NullPointerException if condition is null
   */
  public void setCondition(String condition) {
    Checks.nonNullParameter(condition, "condition");
    this.condition = condition;
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
    return this.comment.equals(that.comment)
        && this.parameter.equals(that.parameter)
        && Objects.equals(this.condition, that.condition);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, parameter, condition);
  }

  /**
   * Returns a string representation of this param tag. The returned string is in the format "@param
   * PARAMNAME COMMENT" where PARAMNAME is the fully qualified name of the parameter in the param
   * tag and COMMENT is the text of the comment in the param tag. If translation has been attempted
   * on this tag, then the returned string is also appended with " ==&gt; CONDITION" where CONDITION
   * is the translated condition as a Java expression or the empty string if translation failed.
   *
   * @return a string representation of this param tag
   */
  @Override
  public String toString() {
    String result = "@param " + parameter.getName() + " " + comment;
    if (condition != null) {
      result += " ==> " + condition;
    }
    return result;
  }
}
