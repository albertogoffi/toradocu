package org.toradocu.output.util;

/** Created by arianna on 29/06/17. */
public class TagOutput {

  String comment;
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

    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (kind != null ? !kind.equals(that.kind) : that.kind != null) return false;
    return condition != null ? condition.equals(that.condition) : that.condition == null;
  }
}
