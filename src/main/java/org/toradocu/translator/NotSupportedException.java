package org.toradocu.translator;

public class NotSupportedException extends Exception {
	
	private static final long serialVersionUID = 1335896013602703198L;
	private String message;
	private String sentence;

	public NotSupportedException(String message, String sentence) {
		this.message = message;
		this.sentence = sentence;
	}

	@Override
	public String getMessage() {
		return "Exception during the parsing of '" + sentence + "': " + message;
	}
}
