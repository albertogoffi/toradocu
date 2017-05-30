package org.toradocu.translator.spec;

import java.util.Objects;
import org.toradocu.util.Checks;

public class Postcondition extends AbstractSpecification {

  private final Guard trueProperty;

  private final Guard falseProperty;

  public Postcondition(Guard guard, Guard trueProperty, Guard falseProperty) {
    Checks.nonNullParameter(guard, "guard");
    Checks.nonNullParameter(trueProperty, "trueProperty");
    Checks.nonNullParameter(falseProperty, "falseProperty");
    this.guard = guard;
    this.trueProperty = trueProperty;
    this.falseProperty = falseProperty;
  }

  public Guard getTrueProperty() {
    return trueProperty;
  }

  public Guard getFalseProperty() {
    return falseProperty;
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

    Postcondition that = (Postcondition) o;
    return trueProperty.equals(that.trueProperty) && falseProperty.equals(that.falseProperty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guard, trueProperty, falseProperty);
  }
}
