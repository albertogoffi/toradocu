package org.toradocu.output.util;

import org.toradocu.extractor.Comment;

/** Created by arianna on 28/06/17. */
public class ParamTagOutput {

  /** The parameter associated with the param tag */
  private final Parameter parameter;

  String condition;

  String kind;

  String comment;

  public ParamTagOutput(Parameter parameter, Comment comment, String kind, String condition) {
    this.parameter = parameter;
    this.condition = condition;
    this.kind = kind;
    this.comment = comment.getText();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ParamTagOutput that = (ParamTagOutput) o;

    if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null)
      return false;
    if (condition != null ? !condition.equals(that.condition) : that.condition != null)
      return false;
    if (kind != null ? !kind.equals(that.kind) : that.kind != null) return false;
    return comment != null ? comment.equals(that.comment) : that.comment == null;
  }
}
