package org.toradocu.translator.spec;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.toradocu.util.Checks;

public class Postcondition extends Specification {

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

  /**
   * Creates a new Postcondition from the given string representation of a postcondition. String
   * representation must be in the form "GUARD ? TRUE_PROPERTY : FALSE_PROPERTY" where the fragment
   * ": FALSE_PROPERTY" is optional.
   *
   * @param postcondition string representation of a postcondition
   * @return a new Postcondition corresponding to the given string postcondition. Null if
   *     postcondition is empty
   */
  public static Postcondition create(String postcondition) {
    String guard = "";
    String trueProp = "";
    String falseProp = "";
    if (postcondition != null && !postcondition.equals("")) {
      Pattern pattern = Pattern.compile("([^?]+)(?:\\?([^:]+)(?::(.+))?)?");
      Matcher matcher = pattern.matcher(postcondition);

      if (matcher.find()) {
        guard = matcher.group();
      }
      if (matcher.find()) {
        trueProp = matcher.group();
      }
      if (matcher.find()) {
        falseProp = matcher.group();
      }
    }
    return new Postcondition(new Guard(guard), new Guard(trueProp), new Guard(falseProp));
  }

  public Guard getTrueProperty() {
    return trueProperty;
  }

  public Guard getFalseProperty() {
    return falseProperty;
  }

  @Override
  public String toString() {
    String result = "";
    if (guard != null) {
      result = guard.toString() + " ? " + trueProperty;
      if (!falseProperty.toString().isEmpty()) {
        result += " : " + falseProperty;
      }
    }
    return result;
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
