package example;

public class AChild extends AClass {

  /**
   * @param z must not be null
   * @throws IllegalArgumentException if z is null
   */
  @Override
  public double baz(Object z) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @param x must not be null
   * @throws IllegalArgumentException if x is null
   */
  public double vararg(Object... x) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double testParam(double x, double y) {
    return 0;
  }

  /** @see example.AClass#testParam2 */
  public double testParam2(double x, double y) {
    return 0;
  }
}
