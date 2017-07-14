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

  /**
   * @param a1 first number
   * @param a2 second number
   * @return true if the numbers are equal
   */
  public boolean paramIsEqual(Integer a1, Integer a2) {
    return a1.equals(a2);
  }

  /**
   * @param n the number
   * @return true if the number is greater than five
   * @throws IllegalArgumentException if the number is less than zero
   */
  public boolean checkNumberInLetter(int n) {
    if (n < 0) {
      throw new IllegalArgumentException();
    }
    return n > 5;
  }

  /**
   * @param arrayInt the array
   * @return true if the array length is greater than 10
   * @throws IllegalArgumentException if the array lenght is 0
   */
  public boolean checkArrayLength(int[] arrayInt) {
    if (arrayInt.length == 0) {
      throw new IllegalArgumentException();
    }
    return arrayInt.length > 10;
  }

  /**
   * @param n1 first number
   * @param n2 second number
   * @return n1-n2
   * @throws IllegalArgumentException if n1 is not strictly positive
   */
  public int checkArithmeticOp(int n1, int n2) {
    if (n1 <= 0) {
      throw new IllegalArgumentException();
    }
    return n1 - n2;
  }

  /** @return true if first=second */
  public boolean returnEqOnlyLetters(int first, int second) {
    return first == second;
  }

  /** @return true if v1=v2 */
  public boolean returnEqLettersNumbers(int v1, int v2) {
    return v1 == v2;
  }

  /** @return true if v1>=v2 */
  public boolean returnGELettersNumbers(int v1, int v2) {
    return v1 >= v2;
  }

  /** @return true if v1 is smaller than v2 */
  public boolean returnLTLettersNumbers(int v1, int v2) {
    return v1 < v2;
  }

  /**
   * @param v1 must be >= v2
   * @param v2
   * @return always true
   */
  public boolean paramGELettersNumbers(int v1, int v2) {
    return true;
  }

  /**
   * @param first
   * @param second must be smaller than first
   * @return true always
   */
  public boolean paramLTLetters(int first, int second) {
    return true;
  }

  /**
   * @param v1
   * @param v2
   * @throws IllegalArgumentException if v2 > v1
   * @return true always
   */
  public boolean throwsGTLettersNumbers(int v1, int v2) {
    if (v2 > v1) {
      throw new IllegalArgumentException();
    }
    return true;
  }

  /**
   * @param first
   * @param second
   * @throws IllegalArgumentException if first is smaller than second
   * @return false always
   */
  public boolean throwsLTLetters(int first, int second) {
    if (first < second) {
      throw new IllegalArgumentException();
    }
    return false;
  }
}
