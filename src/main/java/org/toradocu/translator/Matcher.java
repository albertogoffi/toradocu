package org.toradocu.translator;

import java.io.IOException;
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
import org.toradocu.extractor.Parameter;

public class Matcher {
	
	/**
	 * Represents the threshold for the Levenshtein distance above which {@code CodeElement}s are
	 * considered to be not matching.
	 */
	private static final int LEVENSHTEIN_DISTANCE_THRESHOLD = 9;

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
		// Extract every CodeElement from the given method.
		Set<CodeElement<?>> codeElements = extractCodeElements(method);
		
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
	 * Extracts and returns all {@code CodeElement}s associated with the given method and its enclosing class.
	 * 
	 * @param method the method (and its enclosing class) to extract {@code CodeElement}s from
	 * @return all {@code CodeElement}s associated with the given method and its enclosing class
	 */
	private static Set<CodeElement<?>> extractCodeElements(DocumentedMethod method) {
		Set<CodeElement<?>> result = new LinkedHashSet<>();
		
		// Add parameter code elements.
		for (Parameter param : method.getParameters()) {
			ParameterCodeElement paramCodeElement = new ParameterCodeElement(param);
			result.add(paramCodeElement);
		}
		
		// Add the containing class as a code element.
		Class<?> containingClass = getClass(method.getContainingClass().getQualifiedName());
		result.add(new ClassCodeElement(containingClass));
		
		// Add methods in containing class as code elements.
		for (Method classMethod : containingClass.getMethods()) {
			result.add(new MethodCodeElement(classMethod.toString(), "")); //FIXME Create the MethodCodeElement correctly
		}
		
		return result;
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
		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { classDir })) {
			targetClass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			LOG.error(ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			LOG.warn("Could not close class loader.");
		}
		return targetClass;
	}

	
	
	
	/*
	private static Set<CodeElement<?>> collectPossibleMatchingElements(ExecutableMemberDoc member) {
		
		// Add target object as code element
		ClassDoc containingClass = member.containingClass();
		ClassCodeElement classCodeElement = new ClassCodeElement(containingClass);
		String className = containingClass.simpleTypeName();
		classCodeElement.addIdentifier(className);
		
		char[] classNameArray = className.toCharArray(); 
		for (int i = classNameArray.length - 1; i > 0; i--) {
			if (Character.isUpperCase(classNameArray[i])) {
				classCodeElement.addIdentifier(className.substring(i));
				break;
			}
		}
		
		
		for (ClassDoc implementedInterface : containingClass.interfaces()) {
			classCodeElement.addIdentifier(implementedInterface.simpleTypeName());
		}
		elements.add(classCodeElement);
		
		// Add target object methods as code elements
		for (MethodDoc method : containingClass.methods()) {
			if (method.isPublic() && method.parameters().length == 0) {
				String methodName = method.name();
				if (methodName.startsWith("get")) {
					methodName = methodName.replaceFirst("get", "");
				}
				MethodCodeElement m = new MethodCodeElement(method, "target", methodName);
				elements.add(m);
			}
		}
		
		return elements;
	}

	/*
	private static Set<CodeElement<?>> collectPossibleMatchingElements(Type type) {
		// Given the subject type, possible matching elements for the predicates are subject's methods and fields
		// Note that this method is also used in predicate matching
		Set<CodeElement<?>> elements = new HashSet<>();
		
		if (!type.isPrimitive()) {
			ClassDoc classDoc = type.asClassDoc();
			for (FieldDoc field : classDoc.fields()) {
				if (field.isPublic()) {
					elements.add(new FieldCodeElement(field, field.name()));
				}
			}
			for (MethodDoc method : classDoc.methods()) {
				if (method.isPublic() && method.parameters().length == 0 && 
						(method.returnType().simpleTypeName().equals("boolean") || method.returnType().simpleTypeName().equals("Boolean"))) {
					elements.add(new MethodCodeElement(method, method.name()));
				}
			}
		} else if (type.dimension().equals("[]")) {
			elements.add(new DummyCodeElement("isEmpty", ".length==0"));
			elements.add(new DummyCodeElement("length", ".length"));
		}
		return elements;
	}
	
	public static String predicateMatch(String s, CodeElement<?> subject) {
		String match = simpleMatch(s);
		if (match == null) {
			Set<CodeElement<?>> codeElements = null;
			if (subject instanceof ParameterCodeElement) {
				ParameterCodeElement par = (ParameterCodeElement) subject;
				codeElements = collectPossibleMatchingElements(par.getCodeElement().type());
			} else if (subject instanceof ClassCodeElement) {
				ClassCodeElement classCodeElement = (ClassCodeElement) subject;
				codeElements = collectPossibleMatchingElements(classCodeElement.getCodeElement());
			} else {
				return null; // Any other subject type should have a predicate matching with simple matching strategy
			}
			List<CodeElement<?>> match_ = getMatchingCodeElement(s, codeElements);
			// match_ contains matches that are at the same distance from s. We simply return one of those
			// because we don't know which one is better
			Optional<CodeElement<?>> foundMatch = match_.stream().findFirst();
			if (foundMatch.isPresent()) {
				match = foundMatch.get().getJavaExpression();
				match += getCheck(s);
			}
		}
		return match;
	}
	
	private static String simpleMatch(String phrase) {
		switch (phrase) {
		case "is negative":
			return "<0";
		case "is positive":
			return ">0";
		case "been set":
			return "==null";
		default:
			String symbol = null;
			String numberString = null;
			String[] phraseStarts = { "is ", "are ", "" };
			String[] potentialSymbols = { "<=", ">=", "==", "!=", "=", "<", ">", "" };
			for (String phraseStart : phraseStarts) {
				for (String potentialSymbol : potentialSymbols) {
					if (phrase.startsWith(phraseStart + potentialSymbol)) {
						// Set symbol to the appropriate Java expression symbol.
						if (potentialSymbol.equals("=")
								|| (potentialSymbol.equals("") && !phraseStart.equals(""))) {
							symbol = "==";
						} else {
							symbol = potentialSymbol;
						}
						numberString = phrase.substring(phraseStart.length() + potentialSymbol.length()).trim();
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
	
	private static String getCheck(String predicate) {
		if (predicate.contains("not") || predicate.contains("n't")) {
			return "==false";
		} else {
			return ""; // "==true";
		}
	}
	*/
	
	
	
	
	
	
}
