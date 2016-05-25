package org.toradocu.translator;

import com.sun.javadoc.FieldDoc;

public class FieldCodeElement extends CodeElement<FieldDoc> {

	public FieldCodeElement(FieldDoc codeElement, String identifier) {
		super(codeElement, identifier);
	}

	@Override
	protected String buildStringRepresentation() {
		return codeElement.name();
	}

}
