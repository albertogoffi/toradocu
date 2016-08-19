package org.toradocu.util;

import java.util.Objects;

public class Checks {

	public static void nonNullParameter(Object obj, String parameterName) {
		Objects.requireNonNull(obj, "The parameter " + parameterName + "must not be null");
	}
	
}
