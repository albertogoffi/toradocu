package example;

public interface Interface {

  /** 
   * @param array must not be null
   * @throws NullPointerException if <code>array</code> is null 
   * */
  public double foo(int[] array);

  /** 
   * @param x must not be null
   * @throws IllegalArgumentException if <code>x</code> is null 
   * */
  public double bar(Object x, Object y);

  public double baz(Object x);
}
