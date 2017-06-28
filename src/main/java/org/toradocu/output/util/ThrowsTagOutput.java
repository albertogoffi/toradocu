package org.toradocu.output.util;

import java.util.List;
import org.toradocu.extractor.Comment;

/** Created by arianna on 28/06/17. */
public class ThrowsTagOutput {

  Type exceptionType;

  List<String> codeTags;

  String comment;

  String condition;

  String kind;

  public ThrowsTagOutput(Type exceptionType, Comment comment, String kind, String condition) {
    this.exceptionType = exceptionType;
    this.condition = condition;
    this.kind = kind;
    this.comment = comment.getText();
  }
}
