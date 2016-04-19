package org.toradocu.nlp;

import com.sun.javadoc.ClassDoc;

public class ClassCodeElement extends CodeElement<ClassDoc> {

	public ClassCodeElement(ClassDoc codeElement, String... identifiers) {
		super(codeElement, identifiers);
	}

	@Override
	protected String buildStringRepresentation() {
		return "target";
	}

}
