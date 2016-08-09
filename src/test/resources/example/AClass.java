package example;

public class AClass implements Interface {

	@Override
	public double foo(int[] array) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** 
	 * {@inheritDoc}
	 * @see example.Interface#bar(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double bar(Object x, Object y) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @throws IllegalArgumentException if x is null
	 * @see example.Interface#baz(java.lang.Object)
	 */
	@Override
	public double baz(Object x) {
		// TODO Auto-generated method stub
		return 0;
	}

}
