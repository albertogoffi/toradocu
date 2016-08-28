package org.toradocu.translator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;

/**
 * The {@code Matcher} class translates subjects and predicates in Javadoc comments to Java expressions containing
 * Java code elements.
 */
public class Matcher {
	
	/**
	 * Represents the threshold for the Levenshtein distance above which {@code CodeElement}s are
	 * considered to be not matching.
	 */
	private static final int LEVENSHTEIN_DISTANCE_THRESHOLD = 1;

	private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);
	
	/**
	 * Takes the subject of a proposition in a Javadoc comment and the {@code DocumentedMethod} that
	 * subject was extracted from. Then returns all {@CodeElement}s that match (i.e. have a similar name to)
	 * the given subject string.
	 * 
	 * @param subject the subject of a proposition from a Javadoc comment
	 * @param method the {@code DocumentedMethod} that the subject was extracted from
	 * @return a set of {@CodeElement}s that have a similar name to the subject
	 */
	public static Set<CodeElement<?>> subjectMatch(String subject, DocumentedMethod method) {
		// Extract every CodeElement associated with the method and the containing class of the method.
		Class<?> containingClass = getClass(method.getContainingClass().getQualifiedName());
		Set<CodeElement<?>> codeElements = extractCodeElements(containingClass, method);
		
		// Clean the subject string by removing words and characters not related to its identity so that
		// they do not influence string matching.
		if (subject.startsWith("either ")) {
			subject = subject.replaceFirst("either ", "");
		} else if (subject.startsWith("both ")) {
			subject = subject.replaceFirst("both ", "");
		}
		subject = subject.trim();
		
		// Filter and return the CodeElements whose name is similar to subject.
		return filterMatchingCodeElements(subject, codeElements);
	}
	
	/**
	 * Returns the set of {@code CodeElement}s that match the given filter string.
	 * 
	 * @param filter the string to match {@CodeElement}s against
	 * @param codeElements the set of {@CodeElement}s to filter
	 * @return a set of {@code CodeElement}s that match the given string
	 */
	private static Set<CodeElement<?>> filterMatchingCodeElements(String filter, Set<CodeElement<?>> codeElements) {
		Set<CodeElement<?>> minCodeElements = new LinkedHashSet<>();
		// Only consider elements with a minimum distance <= the threshold distance.
		int minDistance = LEVENSHTEIN_DISTANCE_THRESHOLD;
		// Returns the CodeElement(s) with the smallest Levenshtein distance.
		for (CodeElement<?> codeElement : codeElements) {
			int distance = codeElement.getLevenshteinDistanceFrom(filter);
			if (distance < minDistance) {
				minDistance = distance;
				minCodeElements.clear();
				minCodeElements.add(codeElement);
			} else if (distance == minDistance) {
				minCodeElements.add(codeElement);
			}
		}
		return minCodeElements;
	}
	
	/**
	 * Returns the {@code Class} object for the class with the given name or null if the class could not
	 * be retrieved.
	 * 
	 * @param className the fully qualified name of a class
	 * @return the {@code Class} object for the given class
	 */
	private static Class<?> getClass(String className) {
		Class<?> targetClass = null;
		URL classDir = null;
		final String ERROR_MESSAGE = "Unable to load class. Check the classpath.";
		try {
			classDir = Toradocu.CONFIGURATION.getClassDir().toUri().toURL();
		} catch (MalformedURLException e) {
			LOG.error(ERROR_MESSAGE);
			return null;
		}
		try {
			URLClassLoader classLoader = new URLClassLoader(new URL[] { classDir });
			targetClass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			LOG.error(ERROR_MESSAGE);
			return null;
		}
		return targetClass;
	}
	
	/**
	 * Returns the translation (to a Java expression) of the given subject and predicate. Returns null
	 * if a translation could not be found.
	 * 
	 * @param subject the subject of the proposition to translate
	 * @param predicate the predicate of the proposition to translate
	 * @param negate true if the given predicate should be negated, false otherwise
	 * @return the translation (to a Java expression) of the predicate with the given subject and
	 *         predicate, or null if no translation found
	 */
	public static String predicateMatch(CodeElement<?> subject, String predicate, boolean negate) {
		String match = simpleMatch(predicate);
		if (match != null) {
			match = subject.getJavaExpression() + match;
		} else {
			Set<CodeElement<?>> codeElements = null;
			if (subject instanceof ParameterCodeElement) {
				ParameterCodeElement paramCodeElement = (ParameterCodeElement) subject;
				codeElements = extractBooleanCodeElements(paramCodeElement,
														  paramCodeElement.getJavaCodeElement().getType());
			} else if (subject instanceof ClassCodeElement) {
				ClassCodeElement classCodeElement = (ClassCodeElement) subject;
				codeElements = extractBooleanCodeElements(classCodeElement,
														  classCodeElement.getJavaCodeElement());
			} else if (subject instanceof MethodCodeElement) {
				MethodCodeElement methodCodeElement = (MethodCodeElement) subject;
				codeElements = extractBooleanCodeElements(methodCodeElement,
														  methodCodeElement.getJavaCodeElement().getReturnType());
			} else {
				return null;
			}
			Set<CodeElement<?>> matches = filterMatchingCodeElements(predicate, codeElements);
			if (matches.isEmpty()) {
				return null;
			} else {
				// Matches contains matches that are at the same distance from s. We simply return one of those
				// because we don't know which one is best.
				match = matches.stream().findFirst().get().getJavaExpression();
			}
		}
		if (negate) {
			match = "(" + match + ") == false";
		}
		return match;
	}
	
	/**
	 * Extracts and returns all fields and methods in the given class that have a boolean (return) value.
	 * The returned code elements have the given code element integrated into their Java expression
	 * representations as the receiver of the field or method call.
	 * 
	 * @param receiver the code element that calls the field or method in the Java expression
	 *        representation of the return code elements
	 * @param type the class whose boolean-valued fields and methods to extract
	 * @return the boolean-valued fields and methods in the given class as a set of code elements
	 */
	private static Set<CodeElement<?>> extractBooleanCodeElements(CodeElement<?> receiver, Class<?> type) {
		Set<CodeElement<?>> result = new LinkedHashSet<>();
		
		if (type.isArray()) {
			result.add(new GeneralCodeElement(receiver.getJavaExpression() + ".length==0", "isEmpty"));
			result.add(new GeneralCodeElement(receiver.getJavaExpression() + ".length", "length"));
			return result;
		}
		
		for (Field field : type.getFields()) {
			if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
				result.add(new FieldCodeElement(receiver.getJavaExpression(), field));
			}
		}
		
		for (Method method : type.getMethods()) {
			if (method.getParameterCount() == 0
				&& (method.getReturnType().equals(Boolean.class) || method.getReturnType().equals(boolean.class))) {
				result.add(new MethodCodeElement(receiver.getJavaExpression(), method));
			}
		}
		
		return result;
	}

	/**
	 * Extracts and returns all {@code CodeElement}s associated with the given class and method.
	 * 
	 * @param class the class  to extract {@code CodeElement}s from
	 * @param documentedMethod the method to extract {@code ParameterCodeElement}s from
	 * @return all {@code CodeElement}s associated with the given class and method
	 */
	private static Set<CodeElement<?>> extractCodeElements(Class<?> type, DocumentedMethod documentedMethod) {
		Set<CodeElement<?>> result = new LinkedHashSet<>();
		
		Executable methodOrConstructor = null;
		// Load the DocumentedMethod as a reflection Method or Constructor.
		if (documentedMethod.isConstructor()) {
			for (Constructor<?> constructor : type.getDeclaredConstructors()) {
				if (constructor.getParameterCount() == documentedMethod.getParameters().size()) {
					methodOrConstructor = constructor;
					break;
				}
			}
		} else {
			for (Method method : type.getDeclaredMethods()) {
				if (method.getName().equals(documentedMethod.getName())
					&& method.getParameterCount() == documentedMethod.getParameters().size()) {
					methodOrConstructor = method;
					break;
				}
			}
		}
		if (methodOrConstructor == null) {
			LOG.error("Could not load method/constructor from DocumentedMethod " + documentedMethod);
		}
		
		// Add method parameters as code elements.
		for (int i = 0 ; i < methodOrConstructor.getParameters().length; i++) {
			result.add(new ParameterCodeElement(methodOrConstructor.getParameters()[i], i));
		}
		
		// Add the class itself as a code element.
		result.add(new ClassCodeElement(type));
		
		// Add no-arg methods in containing class as code elements.
		for (Method classMethod : type.getMethods()) {
			if (classMethod.getParameterCount() == 0) {
				result.add(new MethodCodeElement("target", classMethod));
			}
		}
		
		return result;
	}
	
	/**
	 * Attempts to match the given predicate to a simple Java expression (i.e. one containing only
	 * literals).
	 * 
	 * @param predicate the predicate to translate to a Java expression
	 * @return a Java expression translation of the given predicate or null if the predicate could
	 *         not be matched
	 */
	private static String simpleMatch(String predicate) {
		switch (predicate) {
		case "is negative":
			return "<0";
		case "is positive":
			return ">0";
		case "is zero":
			return "==0";
		case "been set":
			return "==null";
		default:
			String symbol = null;
			String numberString = null;
			String[] phraseStarts = { "is ", "are ", "" };
			String[] potentialSymbols = { "<=", ">=", "==", "!=", "=", "<", ">", "" };
			for (String phraseStart : phraseStarts) {
				for (String potentialSymbol : potentialSymbols) {
					if (predicate.startsWith(phraseStart + potentialSymbol)) {
						// Set symbol to the appropriate Java expression symbol.
						if (potentialSymbol.equals("=")
								|| (potentialSymbol.equals("") && !phraseStart.equals(""))) {
							symbol = "==";
						} else {
							symbol = potentialSymbol;
						}
						numberString = predicate.substring(phraseStart.length() + potentialSymbol.length()).trim();
						break;
					}
				}
				if (symbol != null) break;
			}
			if (symbol == null || numberString == "") {
				// The phrase did not match a simple pattern.
				return null;
			}
			try {
				if ((numberString.equals("null") || numberString.equals("true")
						|| numberString.equals("false")) && (symbol.equals("==") || symbol.equals("!="))) {
					return symbol + numberString;
				}
				int number = Integer.parseInt(numberString);
				return symbol + String.valueOf(number);
			} catch (NumberFormatException e) {
				// Text following symbol is not a number.
				return null;
			}
		}
	}
	
}
