package org.toradocu.translator;

import java.lang.reflect.Parameter;

/**
 * This class represents a parameter code element for use in translation. It holds String identifiers for the
 * parameter and a Java expression representation of the parameter to build Java conditions.
 */
public class ParameterCodeElement extends CodeElement<Parameter> {

	/** The 0-based index of this parameter in its associated method's parameter list. */
	private int index;
	
	/**
	 * Constructs and initializes a {@code ParameterCodeElement} that identifies the given parameter.
	 * 
	 * @param parameter the backing parameter that this code element identifies
	 * @param index the 0-based index of the parameter in the parameter list of its associated method
	 */
	public ParameterCodeElement(Parameter parameter, int index) {
		super(parameter);
		this.index = index;
		
		// Add name identifiers.
		addIdentifier("parameter");
		addIdentifier(parameter.getName());
		addIdentifier(parameter.getType().getSimpleName() + " " + parameter.getName());
		addIdentifier(parameter.getName() + " " + parameter.getType().getSimpleName());
		
		// Add type identifiers
		if (!parameter.getType().isArray()) {
			addIdentifier(parameter.getType().getSimpleName());
		} else {
			addIdentifier("array");
			addIdentifier(parameter.getType().getSimpleName() + " array");
		}
		if (parameter.getType().getName().equals("java.lang.Iterable"))	{
			addIdentifier("collection");
		}
	}
	
	/**
	 * Builds and returns the Java expression representation of this parameter code element. The returned
	 * string is formatted as "args[i]" where i is the index of this parameter in a parameter list.
	 * 
	 * @return the Java expression representation of this parameter code element after building it
	 */
	@Override
	public String buildJavaExpression() {
		return "args[" + index + "]";
	}
	
}
