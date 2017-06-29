package org.toradocu.output.util;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.Comment;

/** Created by arianna on 28/06/17. */
public class ThrowsTagOutput extends TagOutput {

  Type exceptionType;

  List<String> codeTags;

  public ThrowsTagOutput(Type exceptionType, Comment comment, String kind, String condition) {
    super(comment.getText(), kind, condition);
    this.exceptionType = exceptionType;
    this.codeTags = new ArrayList<String>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ThrowsTagOutput that = (ThrowsTagOutput) o;

    if (exceptionType != null
        ? !exceptionType.equals(that.exceptionType)
        : that.exceptionType != null) return false;
    if (codeTags != null ? !codeTags.equals(that.codeTags) : that.codeTags != null) return false;
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (kind != null ? !kind.equals(that.kind) : that.kind != null) return false;
    return condition != null ? condition.equals(that.condition) : that.condition == null;
  }
}
