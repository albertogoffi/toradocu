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
}
