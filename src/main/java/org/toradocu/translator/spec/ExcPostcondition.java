package org.toradocu.translator.spec;

import java.util.Objects;
import org.toradocu.util.Checks;

public class ExcPostcondition extends Specification {

  private final String exceptionType;

  public ExcPostcondition(Guard guard, String exceptionType) {
    Checks.nonNullParameter(guard, "guard");
    Checks.nonNullParameter(exceptionType, "exceptionType");
    this.guard = guard;
    this.exceptionType = exceptionType;
  }

  public String getExceptionType() {
    return exceptionType;
  }

  @Override
  public String toString() {
    return guard.toString() + " ? " + exceptionType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ExcPostcondition that = (ExcPostcondition) o;
    return exceptionType.equals(that.exceptionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guard, exceptionType);
  }
}
