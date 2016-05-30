package org.toradocu.extractor;

import java.util.Objects;

public final class ThrowsTag {
	private final String comment;
	private final String exception;
	
	public ThrowsTag(String exception, String comment) {
		Objects.requireNonNull(exception);
		Objects.requireNonNull(comment);
		this.comment = comment;
		this.exception = exception;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String getException() {
		return exception;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ThrowsTag)) return false;
		
		ThrowsTag that = (ThrowsTag) obj;
		return this.comment.equals(that.comment) && this.exception.equals(that.exception);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(comment, exception);
	}
	
	@Override
	public String toString() {
		return "@throws " + exception + " " + comment;
	}
}
