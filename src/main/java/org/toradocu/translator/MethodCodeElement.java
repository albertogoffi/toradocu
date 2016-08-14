package org.toradocu.translator;

public class MethodCodeElement extends CodeElement {

	private final String receiver, signature;
	
	public MethodCodeElement(String signature, String identifier) {
		this(signature, "", identifier);
	}

	public MethodCodeElement(String signature, String receiver, String identifier) {
		super(identifier);
		this.signature = signature;
		this.receiver = receiver;
	}

	@Override
	protected String buildJavaExpression() {
		return receiver + "." + signature;
	}
}
