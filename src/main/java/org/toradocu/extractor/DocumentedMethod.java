package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.toradocu.util.Checks;

/**
 * DocumentedMethod represents the Javadoc documentation for a method in a class. It identifies the method itself
 * and key Javadoc information associated with it, such as throws tags, parameters, and return type.
 */
public final class DocumentedMethod {
	
	/** Class in which the method is contained. */
	private final Type containingClass;
	/** Name of the method. */
	private final String name;
	/** Return type of the method. Null if this DocumentedMethod represents a constructor. */
	private final Type returnType;
	/** Method's parameters. */
	private final List<Parameter> parameters;
	/** Flag indicating whether this method takes a variable number of arguments.  */
	private final boolean isVarArgs;
	/** Throws tags specified in the method's Javadoc. */
	private final List<ThrowsTag> throwsTags;
	
	/** Method signature in the format method_name(type1 arg1, type2 arg2). */
	private final String signature;
	
	/**
	 * Constructs a {@code DocumentedMethod} using the information in the provided {@code Builder}.
	 * 
	 * @param builder the {@code Builder} containing information about this {@code DocumentedMethod}
	 */
	private DocumentedMethod(Builder builder) {
		containingClass = builder.containingClass;
		name = builder.name;
		returnType = builder.returnType;
		parameters = builder.parameters;
		isVarArgs = builder.isVarArgs;
		throwsTags = builder.throwsTags;
		// Create the method signature using the method name and parameters.
		StringBuilder signatureBuilder = new StringBuilder(name + "(");
		for (Parameter param : parameters) {
			signatureBuilder.append(param);
			signatureBuilder.append(",");
		}
		if (signatureBuilder.charAt(signatureBuilder.length() - 1) == ',') { // Remove last comma when needed.
			signatureBuilder.deleteCharAt(signatureBuilder.length() - 1);
		}
		signatureBuilder.append(")");
		signature = signatureBuilder.toString();
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
	 * Returns true if this method takes a variable number of arguments, false otherwise
	 * 
	 * @return {@code true} if this method takes a variable number of arguments, {@code false} otherwise
	 */
	public boolean isVarArgs() {
		return isVarArgs;
	}
	
	/**
	 * Returns the name of this method.
	 * 
	 * @return the name of this method
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if this method is a constructor, false otherwise. 
	 * 
	 * @return {@code true} if this method is a constructor, {@code false} otherwise
	 */
	public boolean isConstructor() {
		return returnType == null;
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
	 * Returns the return type of this method or null if this is a constructor.
	 * 
	 * @return the return type of this method or {@code null} if this is a constructor
	 */
	public Type getReturnType() {
		return returnType;
	}
	
	/**
	 * Returns the class in which this method is contained.
	 * 
	 * @return the class in which this method is contained
	 */
	public Type getContainingClass() {
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
		if (this == obj) return true;
		if (!(obj instanceof DocumentedMethod)) return false;
		
		DocumentedMethod that = (DocumentedMethod) obj;
		if (this.containingClass.equals(that.containingClass) &&
			this.name.equals(that.name) &&
			Objects.equals(this.returnType, that.returnType) &&
			this.parameters.equals(that.parameters) &&
			this.isVarArgs == that.isVarArgs &&
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
		return Objects.hash(containingClass, name, returnType, parameters, throwsTags);
	}
	
	/**
	 * Returns return type (when present), fully qualified class, and signature of this method 
	 * in the format "<RETURN TYPE> <CLASS TYPE.METHOD SIGNATURE>"
	 * 
	 * @return return the string representation of this method
	 */
	@Override
	public String toString() {
		StringBuilder methodAsString = new StringBuilder();
		if (returnType != null) {
			methodAsString.append(returnType + " ");
		}
		methodAsString.append(containingClass + Type.SEPARATOR + signature);
		return methodAsString.toString();
	}
	
	/**
	 * Builds a {@code DocumentedMethod} using the provided information.
	 */
	public static class Builder implements org.toradocu.util.Builder<DocumentedMethod> {

		/** The class containing the {@code DocumentedMethod} to build. */
		private final Type containingClass;
		/** The name of the {@code DocumentedMethod} to build. */
		private final String name;
		/** The parameters of the {@code DocumentedMethod} to build. */
		private final List<Parameter> parameters;
		/** The return type of the {@code DocumentedMethod} to build. */
		private final Type returnType;
		/** Flag indicating whether the {@code DocumentedMethod} to build takes a variable number of arguments. */
		private final boolean isVarArgs;
		/** The throws tags of the {@code DocumentedMethod} to build. */
		private final List<ThrowsTag> throwsTags;
		
		/** 
		 * Constructs a builder for a {@code DocumentedMethod} contained in a given {@code containingClass}
		 * with the given {@code name}, {@code returnType}, and {@parameters}.
		 * 
		 * @param containingClass class containing the {@code DocumentedMethod} to build
		 * @param name name of the {@code DocumentedMethod} to build
		 * @param returnType return type of the method or null 
		 *        if the {@code DocumentedMethod} to build is a constructor
		 * @param parameters the parameters of the {@code DocumentedMethod} to build
		 * 
		 * @throws NullPointerException if {@code containingClass} or {@code name} or {@parameters} is null
		 */
		public Builder(Type containingClass, String name, Type returnType, Parameter... parameters) {
			this(containingClass, name, returnType, false, parameters);
		}
		
		/** 
		 * Constructs a builder for a {@code DocumentedMethod} contained in a given {@code containingClass}
		 * with the given {@code name}, {@code returnType}, and {@parameters}.
		 * 
		 * @param containingClass class containing the {@code DocumentedMethod} to build
		 * @param name the fully qualified name of the {@code DocumentedMethod} to build
		 * @param returnType the fully qualified return type of the method, including its dimension if it's an array,
		 *        or the empty string if the {@code DocumentedMethod} to build is a constructor
		 * @param isVarArgs true if the {@code DocumentedMethod} to build takes a variable number of arguments, false otherwise
		 * @param parameters the parameters of the {@code DocumentedMethod} to build
		 * 
		 * @throws NullPointerException if {@code containingClass} or {@code name} or {@parameters} is null
		 */
		public Builder(Type containingClass, String name, Type returnType, boolean isVarArgs, Parameter... parameters) {
			Checks.nonNullParameter(containingClass, "containingClass");
			Checks.nonNullParameter(name, "name");
			Checks.nonNullParameter(parameters, "parameters");
			
			if (name.contains(".")) {
			    throw new IllegalArgumentException("Invalid method name: " + name + ". Method's name must be a valid "
			    		+ "Java method name (i.e., method name must not contain '.'");
			}
			
			this.containingClass = containingClass;
			this.name = name;
			this.returnType = returnType;
			this.isVarArgs = isVarArgs;
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
