package org.toradocu.extractor;

import java.util.Objects;

public final class Parameter {
	
	private final String type;
	private final String name;
	private final transient String dimension;
	
	public Parameter(String type, String name) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(name);
		this.type = type;
		this.name = name;
		
		int dimensionIndex = type.indexOf('[');
		dimension = dimensionIndex > 0 ? type.substring(dimensionIndex, type.length()) : "";
	}
	
	public String getDimension() {
		return dimension;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getSimpleType() {
		int lastDotIndex = type.lastIndexOf('.');
		return lastDotIndex < 0 ? getType() : type.substring(lastDotIndex + 1);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Parameter)) return false;
		
		Parameter that = (Parameter) obj;
		return type.equals(that.type) && name.equals(that.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(type, name);
	}
	
	@Override
	public String toString() {
		return type + " " + name;
	}
}
