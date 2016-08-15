package org.toradocu.translator;

public class DummyCodeElement extends CodeElement {

	String representation;
	
	public DummyCodeElement(String representation, String identifier) {
		super(identifier);
		this.representation = representation;
	}

	@Override
	protected String buildJavaExpression() {
		return representation;
	}

}
