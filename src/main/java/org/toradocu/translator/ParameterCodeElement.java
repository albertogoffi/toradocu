package org.toradocu.translator;

import org.toradocu.extractor.Parameter;

/**
 * This class represents a parameter code element for use in translation. It holds String identifiers for the
 * parameter and a Java expression representation of the parameter to build Java conditions.
 */
public class ParameterCodeElement extends CodeElement<Parameter> {

	/**
	 * Constructs and initializes a {@code ParameterCodeElement} that identifies the given parameter.
	 * 
	 * @param parameter the backing parameter that this code element identifies
	 */
	public ParameterCodeElement(Parameter parameter) {
		super(parameter);
		
		// Add name identifiers.
		addIdentifier("parameter");
		addIdentifier(parameter.getName());
		addIdentifier(parameter.getType().getName() + " " + parameter.getName());
		addIdentifier(parameter.getName() + " " + parameter.getType().getName());
		
		// Add type identifiers
		if (!parameter.getType().isArray()) {
			addIdentifier(parameter.getType().getName());
			if (parameter.getType().getQualifiedName().equals("java.lang.Iterable"))	{
				addIdentifier("collection");
			}
		} else {
			addIdentifier("array");
			addIdentifier(parameter.getType().getName() + " array");
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
		return "args[" + getJavaCodeElement().getIndex() + "]";
	}
	
}
