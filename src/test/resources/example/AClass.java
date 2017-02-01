package example;

import example.annotation.NonNull;
import example.annotation.NotNull;
import example.annotation.Nullable;
import example.exception.AnException;

public class AClass implements Interface {

  /** @throws NullPointerException always */
  public AClass() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param x must not be null nor empty
   * @throws NullPointerException if x is null
   * @throws AnException if x is empty
   */
  public AClass(String x) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public double foo(@Nullable int[] array) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * {@inheritDoc}
   *
   * @see example.Interface#bar(java.lang.Object, java.lang.Object)
   */
  @Override
  public double bar(@NotNull Object x, @NonNull Object y) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @param x must not be null
   * @throws IllegalArgumentException if x is null
   * @see example.Interface#baz(java.lang.Object)
   */
  @Override
  public double baz(Object x) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * Method to test @param inheritance
   *
   * @param x the first number, must be positive
   * @param y the second number, must be positive
   */
  public double testParam(double x, double y) {
    return 0;
  }

  /**
   * Another method to test @param inheritance
   *
   * @param x the first number, must be positive
   * @param y the second number, must be positive
   */
  public double testParam2(double x, double y) {
    return 0;
  }

  /** @see example.Interface#testParam3(double) */
  @Override
  public double testParam3(double x) {
    return 0;
  }
}
