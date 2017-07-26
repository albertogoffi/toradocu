package org.toradocu.output.util;

/** Created by arianna on 28/06/17. */
public class ReturnTagOutput extends TagOutput {

  ReturnTagOutput(String comment, String kind, String condition) {
    super(comment, kind, condition);
  }

  @Override
  public String toString() {
    String output = "@return " + getComment();
    if (getCondition() != null) {
      output += " ==> " + getCondition();
    }
    return output;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReturnTagOutput that = (ReturnTagOutput) o;
    return super.equals(that);
  }
}
