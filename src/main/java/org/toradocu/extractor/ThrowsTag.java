package org.toradocu.extractor;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
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
	 * Java boolean condition translated from the comment for this throws tag.
	 * Null if translation not yet attempted.
	 * Empty string if no translations found.
	 */
	private String condition;
	
	/**
	 * Constructs a {@code ThrowsTag} with the given exception and comment.
	 * 
	 * @param exception the fully qualified name of the exception
	 * @param comment the comment associated with the exception
	 * @throws NullPointerException if exception or comment is null
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
	 * Returns the translated Java boolean condition for this throws tag as an optional which is empty if translation
	 * has not been attempted yet.
	 * 
	 * @return the translated conditions for this throws tag if translation attempted, else empty optional
	 */
	public Optional<String> getCondition() {
		return Optional.ofNullable(condition);
	}
	
	/**
	 * Sets the translated condition for this throws tags to the given conditions. Each element in
	 * the set is combined using an || conjunction.
	 * 
	 * @param conditions the translated conditions for this throws tag (as Java boolean conditions)
	 * @throws NullPointerException if conditions is null
	 */
	public void setCondition(Set<String> conditions) {
		Objects.requireNonNull(conditions, "conditions must not be null");
		
		if (conditions.size() == 0) {
			this.condition = "";
		} else {
			Iterator<String> it = conditions.iterator();
			StringBuilder conditionsBuilder = new StringBuilder("(" + it.next() + ")");
			while (it.hasNext()) {
				conditionsBuilder.append("||(" + it.next() + ")");
			}
			this.condition = conditionsBuilder.toString();
		}
	}
	
	/**
	 * Sets the translated conditions for this throws tags to the given conditions.
	 * 
	 * @param conditions the translated conditions for this throws tag (as Java boolean conditions)
	 * @throws NullPointerException if condition is null
	 */
	public void setCondition(String condition) {
		Objects.requireNonNull(condition, "conditions must not be null");
		this.condition = condition;
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
		return this.comment.equals(that.comment) && 
			   this.exception.equals(that.exception) &&
			   Objects.equals(this.condition, that.condition);
	}
	
	/**
	 * Returns the hash code of this object.
	 * 
	 * @return the hash code of this object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(comment, exception, condition);
	}
	
	/**
	 * Returns a string representation of this throws tag. The returned string is in the
	 * format "@throws EXCEPTION COMMENT" where EXCEPTION is the fully qualified name of
	 * the exception in this throws tag and COMMENT is the text of the comment in the throws tag.
	 * If translation has been attempted on this tag, then the returned string is also appended
	 * with " ==> CONDITION" where CONDITION is the translated condition for the exception 
	 * as Java expressions.
	 * 
	 * @return a string representation of this throws tag
	 */
	@Override
	public String toString() {
		String result = "@throws " + exception + " " + comment;
		if (condition != null) {
			result += " ==> " + condition;
		}
		return result;
	}
}
