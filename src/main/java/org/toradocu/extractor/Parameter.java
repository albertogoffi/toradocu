package org.toradocu.extractor;

import java.util.Objects;

/**
 * This class represents a method parameter.
 */
public final class Parameter {
	
	/** The type of the parameter. */
	private final Type type;
	/** The name of the parameter. */
	private final String name;
	/** The index of the parameter (i.e. its zero-based position in the parameter list). */
	private final int index;
	/** True if this parameter is nullable, false if nonnull, and null if unspecified. */
	private final Boolean nullable;
	
	/**
	 * Constructs a parameter with the given type and name.
	 * 
	 * @param type the type of the parameter including its dimension
	 * @param name the name of the parameter
	 * @param index the 0-based index of the parameter in the parameter list this parameter is added to
	 * @param nullable true if the parameter is nullable, false if nonnull and null if unspecified
	 * 
	 * @throws NullPointerException if type or name is null
	 */
	public Parameter(Type type, String name, int index, Boolean nullable) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(name);
		this.type = type;
		this.name = name;
		this.index = index;
		this.nullable = nullable;
	}
	
	/**
	 * Constructs a parameter with the given type and name.
	 * 
	 * @param type the type of the parameter including its dimension
	 * @param name the name of the parameter
	 */
	public Parameter(Type type, String name, int index) {
		this(type, name, index, null);
	}

	/**
	 * Returns the index of the parameter (i.e. its zero-based position in the parameter list).
	 * 
	 * @return the index of the parameter (i.e. its zero-based position in the parameter list)
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the simple name of the parameter.
	 * 
	 * @return the simple name of the parameter
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the fully qualified name of the type of the parameter including its dimension.
	 * 
	 * @return the fully qualified name of the type of the parameter including its dimension
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Returns true if the parameter is nullable, false if it is nonnull, or null if its
	 * nullability is unspecified.
	 * 
	 * @return true if the parameter is nullable, false if it is nonnull, or null if its
	 * nullability is unspecified
	 */
	public Boolean getNullability() {
		return nullable;
	}
	
	/**
	 * Returns true if this {@code Parameter} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Parameter)) return false;
		
		Parameter that = (Parameter) obj;
		return type.equals(that.type) && 
			   name.equals(that.name) &&
			   index == that.index &&
			   Objects.equals(nullable, that.nullable);
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(type, name, index, nullable);
	}
	
	/**
	 * Returns a string representation of this parameter. The returned string is in the
	 * format "TYPE NAME" where TYPE is the fully qualified parameter type and NAME is the
	 * name of the parameter.
	 * 
	 * @return a string representation of this parameter
	 */
	@Override
	public String toString() {
		return type + " " + name;
	}
}
