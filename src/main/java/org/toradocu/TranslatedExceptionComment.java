package org.toradocu;

import java.util.Set;

import com.sun.javadoc.ExecutableMemberDoc;

public class TranslatedExceptionComment extends JavadocExceptionComment {

	private Set<String> conditions;
	
	public TranslatedExceptionComment(JavadocExceptionComment javadocComment, Set<String> conditions) {
		this(javadocComment.getMember(), javadocComment.getOriginalMember(), javadocComment.getException(), javadocComment.getComment(), conditions);
	}

	public TranslatedExceptionComment(ExecutableMemberDoc member, ExecutableMemberDoc originalMember, String exception, String comment, Set<String> conditions) {
		super(member, originalMember, exception, comment);
		this.conditions = conditions;
	}

	public Set<String> getConditions() {
		return conditions;
	}
	
	@Override
	public String toString() {
		return super.toString() + " ==> " + conditions;
	}
	

	@Override
	public int hashCode() {
		return super.hashCode() + conditions.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TranslatedExceptionComment) {
			TranslatedExceptionComment that = (TranslatedExceptionComment) obj;
			if (this.equals(that) && conditions.equals(that.getConditions())) {
				return true;
			}
		}
		return false;
	}
}
