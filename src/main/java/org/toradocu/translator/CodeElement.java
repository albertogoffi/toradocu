package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.toradocu.util.Distance;

/**
 * This class represents a Java code element for use in translation. It String identifiers for the code element
 * and a Java expression representation of the code element to build Java conditions.
 *
 * @param <T> the type of code element that this class holds data on
 */
public abstract class CodeElement<T> {

	/** Strings that can be used to refer to this code element. */
	private List<String> identifiers;
	/** String used to build Java conditions. */
	private String javaExpression;
	/** The Java code element this object wraps. */
	private T javaCodeElement;

	/**
	 * Constructs a new {@code CodeElement} that represents the given element with the given
	 * identifiers.
	 * 
	 * @param javaCodeElement the element that this code element contains data on
	 * @param identifiers string identifiers for the code element
	 */
	protected CodeElement(T javaCodeElement, String... identifiers) {
		this.javaCodeElement = javaCodeElement;
		this.identifiers = new ArrayList<>(Arrays.asList(identifiers));
	}
	
	/**
	 * Constructs a new {@code CodeElement} that represents the given element.
	 * 
	 * @param javaCodeElement the element that this code element contains data on
	 */
	protected CodeElement(T javaCodeElement) {
		this(javaCodeElement, new String[0]);
	}

	/**
	 * Adds a string identifier for the code element that this object represents.
	 * 
	 * @param identifier a string that identifies this code element
	 */
	public void addIdentifier(String identifier) {
		identifiers.add(identifier);
	}

	/**
	 * Returns a list of strings that identify this code element.
	 * 
	 * @return a list of strings that identify this code element
	 */
	public List<String> getIdentifiers() {
		return identifiers;
	}

	/**
	 * Returns the Java expression representation of this code element for use in building
	 * Java conditions.
	 * 
	 * @return the Java expression representation of this code element
	 */
	public String getJavaExpression() {
		if (javaExpression == null) {
			javaExpression = buildJavaExpression();
		}
		return javaExpression;
	}

	/**
	 * Returns the Levenshtein distance between this code element and the given string. The returned
	 * distance is the minimum distance calculated for all the identifiers of this code element.
	 * Integer.MAX_VALUE is returned if this code element has no identifiers.
	 * 
	 * @param s the string to get the Levenshtein distance from
	 * @return the minimum Levenshtein distance between the given string and the identifiers of this code element,
	 *         or Integer.MAX_VALUE if this code element has no identifiers
	 */
	public int getLevenshteinDistanceFrom(String s) {
		return identifiers.stream().map(identifier -> Distance.levenshteinDistance(identifier, s))
								   .min(Comparator.naturalOrder()).orElse(Integer.MAX_VALUE);
	}

	/**
	 * Returns the backing code element that this object holds data on.
	 * 
	 * @return the backing code element that this object holds data on
	 */
	public T getJavaCodeElement() {
		return javaCodeElement;
	}

	/**
	 * Builds and returns the Java expression representation of this code element.
	 * 
	 * @return the Java expression representation of this code element after building it
	 */
	protected abstract String buildJavaExpression();

	/**
	 * Returns true if this {@code CodeElement} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CodeElement)) return false;
		
		if (this == obj) return true;
		
		CodeElement<?> that = (CodeElement<?>) obj;
		if (this.getIdentifiers().equals(that.getIdentifiers()) && 
			this.getJavaCodeElement().equals(that.getJavaCodeElement()) &&
			this.getJavaExpression().equals(that.getJavaExpression())) {
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
		return Objects.hash(getIdentifiers(), getJavaExpression());
	}

	/**
	 * Returns a string representation of this {@code CodeElement}. The returned string is
	 * formatted as "EXPRESSION: [IDENTIFIER_1, IDENTIFIER_2, ...]", where EXPRESSION is the
	 * Java expression representation of this code element and IDENTIFIER_i is an identifier
	 * for this code element.
	 * 
	 * @return a string representation of this {@code CodeElement}
	 */
	@Override
	public String toString() {
		return getJavaExpression() + ": " + getIdentifiers();
	}
}
