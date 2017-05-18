package org.toradocu.translator.spec;

import java.util.Objects;
import org.toradocu.util.Checks;

public class Postcondition extends AbstractSpecification {

  private final Guard property;

  public Postcondition(Guard guard, Guard property) {
    Checks.nonNullParameter(guard, "guard");
    Checks.nonNullParameter(property, "property");
    this.guard = guard;
    this.property = property;
  }

  public Guard getProperty() {
    return property;
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
    return property.equals(that.property);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guard, property);
  }
}
