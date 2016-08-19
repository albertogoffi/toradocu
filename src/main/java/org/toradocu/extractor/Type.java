package org.toradocu.extractor;

import java.util.Objects;

/**
 * This class represents a Java type.
 */
public class Type {

	/** Separator used in Java to separate identifiers (e.g., Foo.toString() where "." separates class and method identifiers) */
	public static final String SEPARATOR = "."; 
	
	/** Fully qualified name of this {@code Type} */
	private final String qualifiedName;
	
	// The following fields are derived from qualifiedName
	/** Simple name of this {@code Type} */
	private final String name;
	/** Flag {@code true} when this {@code Type} is an array type (e.g., java.lang.String[]) */
	private final boolean isArray;
	
	/**
	 * Creates a new {@code Type} with a given fully-qualified name.
	 * 
	 * @param qualifiedName fully-qualified name of this {@code Type}
	 * @throws NullPointerException if {@code qualifiedName} is null
	 */
	public Type(String qualifiedName) {
		Objects.requireNonNull(qualifiedName);
		if (qualifiedName.startsWith(SEPARATOR) || qualifiedName.endsWith(SEPARATOR)) {
			throw new IllegalArgumentException(qualifiedName + " is not a valid fully-qualified type name.");
		}
		
		this.qualifiedName = qualifiedName;
		if (qualifiedName.contains(SEPARATOR)) {
			name = qualifiedName.substring(qualifiedName.lastIndexOf(SEPARATOR) + 1);
		} else {
			name = qualifiedName;
		}
		isArray = name.endsWith("]");
	}
	
	/**
	 * Returns the fully-qualified name of this {@code Type}
	 * 
	 * @return the fully-qualified name of this {@code Type}
	 */
	public String getQualifiedName() {
		return qualifiedName;
	}
	
	/**
	 * Returns the simple name of this {@code Type}.
	 * 
	 * @return the simple name of this {@code Type}
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if this Type represents an array type (e.g. int[], java.lang.String[][]).
	 * Returns false otherwise.
	 * 
	 * @return true if this Type is an array type, false otherwise 
	 */
	public boolean isArray() {
		 return isArray;
	}
	
	/** 
	 * Returns the fully qualified name of this {@code Type}.
	 * 
	 * @return the fully qualified name of this {@code Type}
	 */
	@Override
	public String toString() {
		return qualifiedName;
	}
	
	/**
	 * Returns true if this {@code Type} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Type)) return false;
		
		Type that = (Type) obj;
		return qualifiedName.equals(that.qualifiedName);
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(qualifiedName);
	}
}
