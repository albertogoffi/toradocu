package org.toradocu.output.util;

import org.toradocu.extractor.Comment;

/** Created by arianna on 28/06/17. */
public class ParamTagOutput {

  /** The parameter associated with the param tag */
  private final Parameter parameter;

  String condition;

  String kind;

  Comment comment;

  public ParamTagOutput(Parameter parameter, Comment comment, String kind, String condition) {
    this.parameter = parameter;
    this.condition = condition;
    this.kind = kind;
    this.comment = comment;
  }
}
