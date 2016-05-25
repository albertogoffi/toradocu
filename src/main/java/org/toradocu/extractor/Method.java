package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Method {
	private final String name;
	private final List<Parameter> parameters;
	private final List<ThrowsTag> tags;
	private transient String signature;
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Method)) return false;
		
		Method that = (Method) obj;
		if (this.name.equals(that.name) &&
			this.parameters.equals(that.parameters) &&
			this.tags.equals(that.tags)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, parameters, tags);
	}
	
	@Override
	public String toString() {
		if (signature == null) {
			StringBuilder signature = new StringBuilder(name + "(");
			for (Parameter par : parameters) {
				signature.append(par);
				signature.append(",");
			}
			if (signature.charAt(signature.length() - 1) == ',') { // Remove last comma when needed
				signature.deleteCharAt(signature.length() - 1);
			}
			signature.append(")");
			this.signature = signature.toString(); 
		}
		return this.signature;
	}
	
	public static class Builder implements org.toradocu.util.Builder<Method> {

		private final String name;
		private final List<Parameter> parameters;
		private final List<ThrowsTag> tags;
		
		public Builder(String name, Parameter... parameters) {
			Objects.requireNonNull(name);
			Objects.requireNonNull(parameters);
			this.name = name;
			this.parameters = Arrays.asList(parameters);
			tags = new ArrayList<>();
		}
		
		public Builder tag(ThrowsTag tag) {
			if (!tags.contains(tag)) {
				tags.add(tag);
			}
			return this;
		}
		
		@Override
		public Method build() {
			return new Method(this);
		}
	}
	
	private Method(Builder builder) {
		name = builder.name;
		parameters = builder.parameters;
		tags = builder.tags;
	}
}
