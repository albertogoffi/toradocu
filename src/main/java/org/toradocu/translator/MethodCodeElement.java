package org.toradocu.translator;

import com.sun.javadoc.MethodDoc;

public class MethodCodeElement extends CodeElement<MethodDoc>{

	private String receiver;
	
	public MethodCodeElement(MethodDoc method, String identifier) {
		super(method, identifier);
		receiver = "";
	}

	public MethodCodeElement(MethodDoc method, String receiver, String identifier) {
		super(method, identifier);
		this.receiver = receiver;
	}
	

	@Override
	protected String buildStringRepresentation() {
		return receiver + "." + codeElement.name() + codeElement.signature();
	}

}
