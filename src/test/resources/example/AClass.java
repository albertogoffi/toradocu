package example;

import example.annotation.NonNull;
import example.annotation.NotNull;
import example.annotation.Nullable;

import example.exception.AnException;

public class AClass implements Interface {
	
	/**
	 * @throws NullPointerException always
	 */
	public AClass() {
		// TODO Auto-generated constructor stub
   	}
	
	/**
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
	 * @see example.Interface#bar(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double bar(@NotNull Object x, @NonNull Object y) {
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
