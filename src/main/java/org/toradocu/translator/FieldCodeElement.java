package org.toradocu.translator;

public class FieldCodeElement extends CodeElement {

	private final String name;
	
	public FieldCodeElement(String name, String identifier) {
		super(identifier);
		this.name = name;
	}

	@Override
	protected String buildStringRepresentation() {
		return name;
	}

}
