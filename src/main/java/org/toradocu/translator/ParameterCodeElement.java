package org.toradocu.translator;

import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Parameter;

public class ParameterCodeElement extends CodeElement<Parameter> {
	
	private ExecutableMemberDoc member;
	
	public ParameterCodeElement(Parameter parameter, ExecutableMemberDoc member, String... identifiers) {
		super(parameter, identifiers);
		this.member = member;
	}

	@Override
	protected String buildStringRepresentation() {
		Parameter[] parameters =  member.parameters();
		for (int i = 0; i < parameters.length; i++) {
			if (codeElement.name().equals(parameters[i].name())) {
				return "args[" + i + "]";
			}
		}
		return null;
	}
}
