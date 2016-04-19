package org.toradocu.nlp;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.toradocu.util.Distance;

public abstract class CodeElement<T> {
	
	private Set<String> identifiers; // Strings that can be used to refer to the codeElement
	private String stringRepresentation; // String used to build Java conditions
	protected T codeElement; // Java code element
	
	public CodeElement(T codeElement, String... identifiers) {
		this.codeElement = codeElement;
		this.identifiers = new HashSet<>(Arrays.asList(identifiers));
	}

	public void addIdentifier(String indentifier) {
		identifiers.add(indentifier);
	}
	
	public Set<String> getIdentifiers() {
		return identifiers;
	}
	
	public T getCodeElement() {
		return codeElement;
	}
	
	public String getStringRepresentation() {
		if (stringRepresentation == null) {
			stringRepresentation = buildStringRepresentation();
		}
		return stringRepresentation;
	}
	
	public int getLevenshteinDistanceFrom(String s) {
		return identifiers.stream().map(identifier -> Distance.levenshteinDistance(identifier, s))
								   .min(Comparator.naturalOrder()).orElse(Integer.MAX_VALUE);
	}

	protected abstract String buildStringRepresentation();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CodeElement) {
			CodeElement<?> that = (CodeElement<?>) obj;
			if (this.getCodeElement().equals(that.getCodeElement()) &&
				this.getIdentifiers().equals(that.getIdentifiers()) &&
				this.getStringRepresentation().equals(that.getStringRepresentation())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getCodeElement().hashCode() + getIdentifiers().hashCode() + getStringRepresentation().hashCode();
	}
	
	@Override
	public String toString() {
		return getCodeElement().toString() + ": " + getIdentifiers();
	}
}
