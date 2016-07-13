package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DocumentedMethod {
	private final String name;
	private final List<Parameter> parameters;
	private final List<ThrowsTag> tags;
	private final transient String signature;
	private final transient String containingClass;
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DocumentedMethod)) return false;
		
		DocumentedMethod that = (DocumentedMethod) obj;
		if (this.name.equals(that.name) &&
			this.parameters.equals(that.parameters) &&
			this.tags.equals(that.tags)) {
			return true;
		}
		return false;
	}
	
	public List<ThrowsTag> tags() {
		return Collections.unmodifiableList(tags);
	}

	public List<Parameter> getParameters() {
		return parameters;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public String getContainingClass() {
		return containingClass;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, parameters, tags);
	}
	
	@Override
	public String toString() {
		return this.signature;
	}
	
	public static class Builder implements org.toradocu.util.Builder<DocumentedMethod> {

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
		public DocumentedMethod build() {
			return new DocumentedMethod(this);
		}
	}
	
	private DocumentedMethod(Builder builder) {
		name = builder.name;
		parameters = builder.parameters;
		tags = builder.tags;
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
		this.containingClass = signature.substring(0, signature.lastIndexOf("."));
	}
}
