package org.toradocu.extractor;

import java.util.Objects;

/**
 * This class represents a throws tag in a method.
 */
public class ThrowsTag {
	
	/** The fully qualified name of the exception. */
	private final String exception;
	/** The comment associated with the exception. */
	private final String comment;
	
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
	 * Returns the comment associated with the exception in this throws tag.
	 * 
	 * @return the comment associated with the exception in this throws tag
	 */
	public String getComment() {
		return comment;
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
	 * Returns true if this {@code ThrowsTag} and the specified object are equal.
	 * 
	 * @param obj the object to test for equality
	 * @return true if this object and {@code obj} are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ThrowsTag)) return false;
		
		ThrowsTag that = (ThrowsTag) obj;
		return this.comment.equals(that.comment) && this.exception.equals(that.exception);
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(comment, exception);
	}
	
	/**
	 * Returns a string representation of this throws tag. The returned string is in the
	 * format "@throws EXCEPTION COMMENT" where EXCEPTION is the fully qualified name of
	 * the exception in this throws tag and COMMENT is the text of the comment in the throws tag.
	 * 
	 * @return a string representation of this throws tag
	 */
	@Override
	public String toString() {
		return "@throws " + exception + " " + comment;
	}
}
