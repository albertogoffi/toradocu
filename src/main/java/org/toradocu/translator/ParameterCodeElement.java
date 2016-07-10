package org.toradocu.translator;

public class ParameterCodeElement extends CodeElement {
	
	private final int parameterNumber;
	
	public ParameterCodeElement(int parameterNumber, String... identifiers) {
		super(identifiers);
		this.parameterNumber = parameterNumber;
	}

	@Override
	protected String buildStringRepresentation() {
		return "args[" + parameterNumber + "]";
	}
}
