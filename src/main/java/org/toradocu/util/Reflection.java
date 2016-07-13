package org.toradocu.util;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class Reflection {
	
	public static String getMethodSignature(Method method) {
		 Pattern pattern = Pattern.compile("[^.]*\\(.*\\)");
		 java.util.regex.Matcher matcher = pattern.matcher(method.toString());
		 if (matcher.find()) {
			return matcher.group(); 
		 }
		 return "";
	}
}
