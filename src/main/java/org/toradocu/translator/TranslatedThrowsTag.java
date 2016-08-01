package org.toradocu.translator;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;

/**
 * This class represents a translated throws tag for a method.
 */
public final class TranslatedThrowsTag extends ThrowsTag {
	
	/** The method that this throws tag is for. */
	private final DocumentedMethod method;
	
	/** Conditions translated from the comment for this throws tag. */
	private final Set<String> conditions;
	
	/**
	 * Constructs a {@code TranslatedThrowsTag} contained in {@code method} that has the translated
	 * {@code conditions} for the given {@code ThrowsTag}.
	 * 
	 * @param tag the throws tag that this {@code TranslatedThrowsTag} has the translated conditions for
	 * @param method the method that this throws tag is for
	 * @param conditions conditions translated from the comment for this tag
	 */
	public TranslatedThrowsTag(ThrowsTag tag, DocumentedMethod method, Set<String> conditions) {
		super(tag.getException(), tag.getComment());
		Objects.requireNonNull(method);
		Objects.requireNonNull(conditions);
		this.method = method;
		this.conditions = conditions;
	}
	
	/**
	 * Constructs a {@code TranslatedThrowsTag} contained in {@code method} that specifies the throwing of
	 * {@code exception} when the {@code conditions} translated from {@code comment} are violated.
	 * 
	 * @param method the method that this throws tag is for
	 * @param exception the fully qualified name of the exception
	 * @param comment the comment associated with the exception
	 * @param conditions conditions translated from the comment for this tag
	 */
	public TranslatedThrowsTag(DocumentedMethod method, String exception, String comment, Set<String> conditions) {
		super(exception, comment);
		Objects.requireNonNull(method);
		Objects.requireNonNull(conditions);
		this.method = method;
		this.conditions = conditions;
	}
	
	/**
	 * Returns the method containing this throws tag.
	 * 
	 * @return the method containing this throws tag
	 */
	public DocumentedMethod getMethod() {
		return method;
	}
	
	/**
	 * Returns an unmodifiable set view of the translated conditions for this throws tag.
	 * 
	 * @return an unmodifiable set view of the translated conditions for this throws tag
	 */
	public Set<String> getConditions() {
		return Collections.unmodifiableSet(conditions);
	}
	
	/**
	 * Returns true if this {@code TranslatedThrowsTag} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TranslatedThrowsTag)) return false;
		if (this == obj) return true;
		
		TranslatedThrowsTag that = (TranslatedThrowsTag) obj;
		return this.method.equals(that.method) && this.getComment().equals(that.getComment())
				&& this.getException().equals(that.getException())
				&& this.conditions.equals(that.conditions);
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(method.getSignature(), getComment(), getException(), conditions);
	}
}
