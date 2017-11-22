package org.toradocu.output.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    return Objects.equals(exceptionType, that.exceptionType)
        && Objects.equals(codeTags, that.codeTags)
        && super.equals(that);
  }

  @Override
  public String toString() {
    return "@throws " + exceptionType.name + " " + getComment();
  }
}
