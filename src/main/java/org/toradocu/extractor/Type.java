package org.toradocu.extractor;

import java.util.Objects;

public class Type {

	private final String qualifiedName;
	
	// Derived fields
	private final String name;
	private final boolean isArray;
	
	public Type(String qualifiedName) {
		Objects.requireNonNull(qualifiedName);
		String SEPARATOR = ".";
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
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isArray() {
		 return isArray;
	}
	
	@Override
	public String toString() {
		return qualifiedName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Type)) return false;
		
		Type that = (Type) obj;
		return qualifiedName.equals(that.qualifiedName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(qualifiedName);
	}
}
