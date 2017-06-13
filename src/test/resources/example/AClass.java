package example;

import example.annotation.NotNull;
import example.annotation.Nullable;
import example.exception.AnException;
import java.util.Collection;

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

  /**
   * @param name a String
   * @param type a Class
   */
  public <T extends Class> void callFriend(String name, Class<T> type) {}

  /**
   * @param a an array
   * @param c a Collection
   */
  public <T> void fromArrayToCollection(@NotNull T[] a, @NotNull Collection<T> c) {
    for (T o : a) {
      c.add(o); // Correct
    }
  }
}
