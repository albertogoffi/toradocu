package org.toradocu.output.util;

import java.util.Objects;
import org.toradocu.extractor.Comment;

/** Created by arianna on 28/06/17. */
public class ParamTagOutput extends TagOutput {

  /** The parameter associated with the param tag */
  private final Parameter parameter;

  ParamTagOutput(Parameter parameter, Comment comment, String kind, String condition) {
    super(comment.getText(), kind, condition);
    this.parameter = parameter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ParamTagOutput that = (ParamTagOutput) o;

    return Objects.equals(parameter, that.parameter) && super.equals(that);
  }

  @Override
  public String toString() {
    return "@param " + parameter.getName() + " " + super.getComment();
  }
}
