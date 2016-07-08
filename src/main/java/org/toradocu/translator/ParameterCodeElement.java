package org.toradocu.translator;

import org.toradocu.extractor.Parameter;

public class ParameterCodeElement extends CodeElement<Parameter> {
	
	private final int parameterNumber;
	
	public ParameterCodeElement(Parameter parameter, int parameterNumber, String... identifiers) {
		super(parameter, identifiers);
		this.parameterNumber = parameterNumber;
	}

	@Override
	protected String buildStringRepresentation() {
		return "args[" + parameterNumber + "]";
	}
}
