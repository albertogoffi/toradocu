package org.toradocu.output.util;

// TODO: Add documentation throughout this class.
/** Created by arianna on 29/06/17. */
import java.util.Objects;

public class TagOutput {

  String comment;
  // TODO: What format is this?
  String kind;

  public String getComment() {
    return comment;
  }

  public String getKind() {
    return kind;
  }

  public String getCondition() {

    return condition;
  }

  String condition;

  public TagOutput(String comment, String kind, String condition) {
    this.comment = comment;
    this.kind = kind;
    this.condition = condition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TagOutput that = (TagOutput) o;
    if (condition != null && that.condition != null) {
      String expectedCondition = condition.replaceAll("\\s", "");
      String actualCondition = that.condition.replaceAll("\\s", "");
      return Objects.equals(comment, that.comment)
          && Objects.equals(kind, that.kind)
          && expectedCondition.equals(actualCondition);
    } else {
      return Objects.equals(comment, that.comment)
          && Objects.equals(kind, that.kind)
          && Objects.equals(condition, that.condition);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(comment, kind, condition);
  }
}
