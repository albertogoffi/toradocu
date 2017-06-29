package org.toradocu.output.util;

/** Created by arianna on 28/06/17. */
public class ReturnTagOutput {
  String comment;
  String kind;
  String condition;

  public ReturnTagOutput(String comment, String kind, String condition) {
    this.comment = comment;
    this.kind = kind;
    this.condition = condition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReturnTagOutput that = (ReturnTagOutput) o;

    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (kind != null ? !kind.equals(that.kind) : that.kind != null) return false;
    return condition != null ? condition.equals(that.condition) : that.condition == null;
  }
}
