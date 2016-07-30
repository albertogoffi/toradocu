package org.toradocu.util;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * This class contains utility methods for Java Reflection classes.
 */
public class Reflection {
	
	/**
	 * Returns the signature of the given method (i.e. its name and parameter list).
	 * 
	 * @param method the method whose signature to return
	 * @return the signature of the method
	 */
	public static String getMethodSignature(Method method) {
		 Pattern pattern = Pattern.compile("[^.]*\\(.*\\)");
		 java.util.regex.Matcher matcher = pattern.matcher(method.toString());
		 if (matcher.find()) {
			return matcher.group(); 
		 }
		 return "";
	}
}
