package org.toradocu.translator.spec;

public class Specification {

  protected Guard guard;

  public Guard getGuard() {
    return guard;
  }

  @Override
  public int hashCode() {
    return guard != null ? guard.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Specification)) {
      return false;
    }

    Specification that = (Specification) o;
    return guard != null ? guard.equals(that.guard) : that.guard == null;
  }
}
