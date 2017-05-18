package org.toradocu.translator.spec;

public class AbstractSpecification implements Specification {

  protected Guard guard;

  @Override
  public Guard getGuard() {
    return null;
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
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractSpecification that = (AbstractSpecification) o;
    return guard != null ? guard.equals(that.guard) : that.guard == null;
  }
}
