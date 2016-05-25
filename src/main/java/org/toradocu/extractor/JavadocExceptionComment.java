package org.toradocu.extractor;

import com.sun.javadoc.ExecutableMemberDoc;

@Deprecated
public class JavadocExceptionComment {
	
	private ExecutableMemberDoc member, originalMember;
	private String comment, exception;
	
	/**
	 * @param member the method that this comment refers to
	 * @param originalMember the method where the comment is defined 
	 * (can differ from <code>member</code> if, for example, the comment is defined in an interface
	 * @param exception the exception that is commented
	 * @param comment the comment
	 */
	public JavadocExceptionComment(ExecutableMemberDoc member, ExecutableMemberDoc originalMember, String exception, String comment) {
		this.member = member;
		this.originalMember = originalMember;
		this.exception = exception;
		this.comment = comment;
	}
	
	public ExecutableMemberDoc getMember() {
		return member;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String getException() {
		return exception;
	}
	
	public ExecutableMemberDoc getOriginalMember() {
		return originalMember;
	}
	
	@Override
	public String toString() {
		return member + " throws " + exception + " " + comment;
	}
	
	@Override
	public int hashCode() {
		return member.hashCode() + exception.hashCode() + comment.hashCode(); 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JavadocExceptionComment) {
			JavadocExceptionComment that = (JavadocExceptionComment) obj;
			if (member.equals(that.getMember()) && exception.equals(that.getException())
					&& comment.equals(that.getComment())) {
				return true;
			}
		}
		return false;
	}
}
