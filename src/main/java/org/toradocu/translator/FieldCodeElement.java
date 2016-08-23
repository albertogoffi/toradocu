package org.toradocu.translator;

import java.lang.reflect.Field;

/**
 * This class represents a field code element for use in translation. It holds String identifiers for the
 * field and a Java expression representation of the field to build Java conditions.
 */
public class FieldCodeElement extends CodeElement<Field> {

	/** The class/object in which this field is contained. */
	private final String receiver;
	
	/**
	 * Constructs and initializes a {@code MethodCodeElement} that identifies the given method. The given method
	 * must take no arguments.
	 * 
	 * @param receiver the class/object in which this field is contained
	 * @param field the boolean field that this code element identifies
	 * @throws IllegalArgumentException if field is not boolean/Boolean
	 */
	public FieldCodeElement(String receiver, Field field) {
		super(field);
		if (!field.getType().equals(Boolean.class) && !field.getType().equals(boolean.class)) {
			throw new IllegalArgumentException("Field " + field.getName() + " is of type "
											   + field.getType().getName() + ". Expected boolean/Boolean.");
		}
		this.receiver = receiver;
		
		// Add name identifier.
		addIdentifier(field.getName());
	}

	@Override
	protected String buildJavaExpression() {
		return receiver + "." + getJavaCodeElement().getName();
	}

}
