package org.toradocu.extractor;

import java.util.Objects;

/**
 * This class represents a method parameter.
 */
public final class Parameter {
	
	/** The fully qualified name of the type of the parameter including its dimension. */
	private final String type;
	/** The name of the parameter. */
	private final String name;
	/** The dimension of the parameter if it is an array. */
	private final transient String dimension;
	/** True if this parameter is nullable, false if nonnull, and null if unspecified. */
	private final Boolean nullable;
	
	/**
	 * Constructs a parameter with the given type and name.
	 * 
	 * @param type the type of the parameter including its dimension
	 * @param name the name of the parameter
	 * @param nullable true if the parameter is nullable, false if nonnull and null if unspecified
	 */
	public Parameter(String type, String name, Boolean nullable) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(name);
		this.type = type;
		this.name = name;
		this.nullable = nullable;
		// Extract the dimension from the type string if the type is an array.
		int dimensionIndex = type.indexOf('[');
		dimension = dimensionIndex > 0 ? type.substring(dimensionIndex, type.length()) : "";
	}
	
	/**
	 * Constructs a parameter with the given type and name.
	 * 
	 * @param type the type of the parameter including its dimension
	 * @param name the name of the parameter
	 */
	public Parameter(String type, String name) {
		this(type, name, null);
	}
	
	/**
	 * Returns the dimension of the parameter if it is an array. Otherwise returns null.
	 * 
	 * @return the dimension of the parameter if it is an array or null otherwise
	 */
	public String getDimension() {
		return dimension;
	}
	
	/**
	 * Returns the name of the parameter.
	 * 
	 * @return the name of the parameter
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the fully qualified name of the type of the parameter including its dimension.
	 * 
	 * @return the fully qualified name of the type of the parameter including its dimension
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the simple type of the parameter.
	 * 
	 * @return the simple type of the parameter
	 */
	public String getSimpleType() {
		int lastDotIndex = type.lastIndexOf('.');
		return lastDotIndex < 0 ? getType() : type.substring(lastDotIndex + 1);
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
		return type.equals(that.type) && name.equals(that.name);
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(type, name);
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
