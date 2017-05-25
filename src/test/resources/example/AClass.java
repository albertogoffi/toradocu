package example;

import example.annotation.NotNull;
import example.annotation.Nullable;
import example.exception.AnException;

public class AClass {

  /** @throws NullPointerException always */
  public AClass() {}

  /**
   * @param x must not be null nor empty
   * @throws NullPointerException if x is null
   * @throws AnException if x is empty
   */
  public AClass(String x) {}

  /**
   * This is foo.
   *
   * @param array an array of objects, must not be null
   * @return 0 always
   */
  public double foo(@NotNull Object[] array) {
    return 0;
  }

  /**
   * @param x an object
   * @throws IllegalArgumentException if x is null
   */
  public double baz(@Nullable Object x) {
    return 0;
  }

  /** A private method. */
  private void aPrivateMethod() {}
}
