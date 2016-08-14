package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DocumentedMethod represents the documentation for a method in a class. It identifies the method itself
 * and key Javadoc information associated with it, such as throws tags, parameters and return type.
 */
public final class DocumentedMethod {
	
	/** The fully qualified name of the method. */
	private final String name;
	/** A list of parameters in the method. */
	private final List<Parameter> parameters;
	/** A list of throws tags specified in the method's Javadoc. */
	private final List<ThrowsTag> throwsTags;
	/** The signature of the method (excluding return type). */
	private final String signature;
	/** The return type of the method, including its dimension if it's an array. */
	private final String returnType;
	/** The class in which the method is contained. */
	private final String containingClass;
	
	/**
	 * Constructs a {@code DocumentedMethod} using the information in the provided {@code Builder}.
	 * 
	 * @param builder the {@code Builder} containing information about this {@code DocumentedMethod}
	 */
	private DocumentedMethod(Builder builder) {
		name = builder.name;
		parameters = builder.parameters;
		throwsTags = builder.throwsTags;
		returnType = builder.returnType;
		// Create the method signature using the method name and parameters.
		StringBuilder signatureBuilder = new StringBuilder(name + "(");
		for (Parameter param : parameters) {
			signatureBuilder.append(param);
			signatureBuilder.append(",");
		}
		if (signatureBuilder.charAt(signatureBuilder.length() - 1) == ',') { // Remove last comma when needed
			signatureBuilder.deleteCharAt(signatureBuilder.length() - 1);
		}
		signatureBuilder.append(")");
		signature = signatureBuilder.toString();
		// Set the containingClass.
		if (returnType == "") {
			// DocumentedMethod is for a constructor.
			containingClass = name;
		} else {
			this.containingClass = name.substring(0, name.lastIndexOf("."));
		}
	}
	
	/**
	 * Returns an unmodifiable list view of the throws tags in this method.
	 * 
	 * @return an unmodifiable list view of the throws tags in this method
	 */
	public List<ThrowsTag> throwsTags() {
		return Collections.unmodifiableList(throwsTags);
	}

	/**
	 * Returns an unmodifiable list view of the parameters in this method.
	 * 
	 * @return an unmodifiable list view of the parameters in this method
	 */
	public List<Parameter> getParameters() {
		return Collections.unmodifiableList(parameters);
	}
	
	/**
	 * Returns the signature of this method.
	 * 
	 * @return the signature of this method
	 */
	public String getSignature() {
		return signature;
	}
	
	/**
	 * Returns the return type of this method, including its dimension if it's an array, or the
	 * empty string if this is a constructor.
	 * 
	 * @return the return type of this method, including its dimension if it's an array, or the
	 *         empty string if this is a constructor
	 */
	public String getReturnType() {
		return returnType;
	}
	
	/**
	 * Returns the fully qualified name of the class in which this method is contained.
	 * 
	 * @return the fully qualified name of the class in which this method is contained
	 */
	public String getContainingClass() {
		return containingClass;
	}
	
	/**
	 * Returns true if this {@code DocumentedMethod} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DocumentedMethod)) return false;
		if (this == obj) return true;
		
		DocumentedMethod that = (DocumentedMethod) obj;
		if (this.name.equals(that.name) &&
			this.parameters.equals(that.parameters) &&
			this.throwsTags.equals(that.throwsTags)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, parameters, throwsTags);
	}
	
	/**
	 * Returns the signature of this method.
	 * 
	 * @return the signature of this method
	 */
	@Override
	public String toString() {
		return this.signature;
	}
	
	/**
	 * Builds a {@code DocumentedMethod} using the provided information.
	 */
	public static class Builder implements org.apache.commons.lang3.builder.Builder<DocumentedMethod> {

		/** The fully qualified name of the {@code DocumentedMethod} to build. */
		private final String name;
		/** The parameters of the {@code DocumentedMethod} to build. */
		private final List<Parameter> parameters;
		/** The return type of the {@code DocumentedMethod} to build. */
		private final String returnType;
		/** The throws tags of the {@code DocumentedMethod} to build. */
		private final List<ThrowsTag> throwsTags;
		
		/** Constructs a builder for a {@code DocumentedMethod} with the given {@code name} and {@parameters}.
		 * 
		 * @param name the fully qualified name of the {@code DocumentedMethod} to build
		 * @param returnType the fully qualified return type of the method, including its dimension if it's an array,
		 *        or the empty string if the {@code DocumentedMethod} to build is a constructor
		 * @param parameters the parameters of the {@code DocumentedMethod} to build
		 */
		public Builder(String name, String returnType, Parameter... parameters) {
			Objects.requireNonNull(name);
			Objects.requireNonNull(returnType);
			Objects.requireNonNull(parameters);
			
			if (name.startsWith(".") || name.endsWith(".") || !name.contains(".")) {
			    throw new IllegalArgumentException("Name must be a valid qualified name of a method of the form"
			        + "<package>.<class>.<method name> where <package> is optional.");
			}
			
			this.name = name;
			this.returnType = returnType;
			this.parameters = Arrays.asList(parameters);
			this.throwsTags = new ArrayList<>();
		}
		
		/**
		 * Adds the specified throws tag to the {@code DocumentedMethod} to build.
		 * 
		 * @param tag the throws tag in the {@code DocumentedMethod} to build
		 * @return this {@code Builder}
		 */
		public Builder tag(ThrowsTag tag) {
			if (!throwsTags.contains(tag)) {
				throwsTags.add(tag);
			}
			return this;
		}
		
		/**
		 * Builds and returns a {@code DocumentedMethod} with the information given to this {@code Builder}.
		 * 
		 * @return a {@code DocumentedMethod} containing the information passed to this builder
		 */
		@Override
		public DocumentedMethod build() {
			return new DocumentedMethod(this);
		}
	}
	
}
