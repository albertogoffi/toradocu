package org.toradocu.translator.spec;

import org.toradocu.util.Checks;

public class Guard {

  private final String condition;

  public Guard(String condition) {
    Checks.nonNullParameter(condition, "condition");
    this.condition = condition;
  }

  public String getCondition() {
    return condition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Guard guard = (Guard) o;
    return condition.equals(guard.condition);
  }

  @Override
  public int hashCode() {
    return condition.hashCode();
  }

  @Override
  public String toString() {
    return condition;
  }
}
