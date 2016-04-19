package org.toradocu.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sun.javadoc.Parameter;

@Deprecated
public class ParametersMatcher {
	
	/**
	 * This method verifies that the subject we use to build a condition is actually a parameter.
	 * If not it tries to find the actual parameter the condition refers to.
	 * @param subject
	 * @param parameters
	 * @return
	 */
	public static List<Parameter> getMatchingParameters(String subject, Parameter[] parameters) {
		List<Parameter> matchingParameters = new ArrayList<>();
		
		Optional<Parameter> found = Arrays.stream(parameters).filter(p -> p.name().equalsIgnoreCase(subject)).findFirst();
		if (found.isPresent()) { // subjectText exactly matches one of the parameters
			matchingParameters.add(found.get());
			return matchingParameters;
		} else {
			// 0. Check whether there is "either" as determiner -> the subject is referring to more than one parameter
			if (subject.startsWith("either")) {
				String subjectText = subject.substring(subject.indexOf(" ") + 1);
				return Arrays.stream(parameters).filter(p -> p.name().toLowerCase().contains(subjectText.toLowerCase())).collect(Collectors.toList());
			}
			
			// 1. Check whether the subject matches the type of a parameter
			Optional<Parameter> parameter = Arrays.stream(parameters).filter(p -> p.type().simpleTypeName().equalsIgnoreCase(subject)).findFirst();
			if (parameter.isPresent()) {
				matchingParameters.add(parameter.get());
				return matchingParameters;
			}
			
			// 2. Check whether the subject matches the name of a parameter
			parameter = Arrays.stream(parameters).filter(p -> p.name().toLowerCase().contains(subject.toLowerCase())).findFirst();
			if (parameter.isPresent()) {
				matchingParameters.add(parameter.get());
				return matchingParameters;
			}
			
			// 3. Check whether a compound name subject matches the name of a parameter
			for (Parameter p : parameters) {
				boolean difference = false;
				for (String token : subject.split(" ")) {
					if (!p.name().toLowerCase().contains(token.toLowerCase())) {
						difference = true;
						break;
					}
				}
				if (!difference) {
					matchingParameters.add(p);
					return matchingParameters;
				}
			}

			// 4. Heuristics specifically design to work in particular corner cases
			
			// Check whether the word "specified" is used in the subject. In that case we can assume is referring to the parameter
			if (subject.startsWith("specified") && parameters.length == 1) {
				matchingParameters.add(parameters[0]);
				return matchingParameters;
			}
			
			// Check whether the subject is referring to an array and the only parameter is a monodimensional array
			if (subject.contains("array") && parameters.length == 1 && parameters[0].type().dimension().equals("[]")) {
				matchingParameters.add(parameters[0]);
				return matchingParameters;
			}
		}
		return matchingParameters;
	}

}
