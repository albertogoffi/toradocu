package org.toradocu.nlp;

public class DummyCodeElement extends CodeElement<Object> {

	String representation;
	
	public DummyCodeElement(String identifier, String representation) {
		super(new Object(), identifier);
		this.representation = representation;
	}
	
	@Override
	public int hashCode() {
		return getIdentifiers().hashCode() + getStringRepresentation().hashCode();
	}

	@Override
	protected String buildStringRepresentation() {
		return representation;
	}

}
