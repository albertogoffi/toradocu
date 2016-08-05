package org.toradocu.extractor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents a throws tag in a method.
 */
public class ThrowsTag {
	
	/** The fully qualified name of the exception. */
	private final String exception;
	/** The comment associated with the exception. */
	private final String comment;
	/**
	 * Conditions translated from the comment for this throws tag. Null if translation not attempted.
	 * Empty set if no translations found.
	 */
	private Set<String> conditions;
	
	/**
	 * Constructs a {@code ThrowsTag} with the given exception and comment.
	 * 
	 * @param exception the fully qualified name of the exception
	 * @param comment the comment associated with the exception
	 */
	public ThrowsTag(String exception, String comment) {
		Objects.requireNonNull(exception);
		Objects.requireNonNull(comment);
		this.comment = comment;
		this.exception = exception;
	}
	
	/**
	 * Returns the fully qualified name of the exception in this throws tag.
	 * 
	 * @return the fully qualified name of the exception in this throws tag
	 */
	public String getException() {
		return exception;
	}
	
	/**
	 * Returns the comment associated with the exception in this throws tag.
	 * 
	 * @return the comment associated with the exception in this throws tag
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Returns the translated conditions for this throws tag.
	 * 
	 * @return the translated conditions for this throws tag
	 */
	public Set<String> getConditions() {
		return conditions;
	}
	
	/**
	 * Sets the translated conditions for this throws tags to the given conditions.
	 * 
	 * @param conditions the translated conditions for this throws tag (as Java expressions)
	 */
	public void setConditions(Set<String> conditions) {
		this.conditions = conditions;
	}
	
	/**
	 * Returns true if this {@code ThrowsTag} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ThrowsTag)) return false;
		
		ThrowsTag that = (ThrowsTag) obj;
		boolean result = this.comment.equals(that.comment) && this.exception.equals(that.exception);
		if (this.conditions == null) {
			return result;
		} else {
			return result && this.conditions.equals(that.conditions);
		}
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(comment, exception, conditions);
	}
	
	/**
	 * Returns a string representation of this throws tag. The returned string is in the
	 * format "@throws EXCEPTION COMMENT" where EXCEPTION is the fully qualified name of
	 * the exception in this throws tag and COMMENT is the text of the comment in the throws tag.
	 * If translation has been attemped on this tag, then the returned string is also appended
	 * with " ==> [CONDITION_1, CONDITION_2, ...]" where CONDITION_i are the translated conditions
	 * for the exception as Java expressions.
	 * 
	 * @return a string representation of this throws tag
	 */
	@Override
	public String toString() {
		String result = "@throws " + exception + " " + comment;
		if (conditions == null) {
			return result;
		} else {
			return result + " ==> " + conditions;
		}
	}
}
