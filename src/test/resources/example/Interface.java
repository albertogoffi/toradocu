package example;

public interface Interface {
	
	/**
	 * @throws NullPointerException if <code>array</code> is null
	 */
	public double foo(int[] array);
	
	/**
	 * @throws IllegalArgumentException if <code>x</code> is null
	 */
	public double bar(Object x, Object y);
	
	public double baz(Object x);
}
