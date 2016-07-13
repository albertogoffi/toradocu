package org.toradocu.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

public class Matcher {
	
	private static final int THRESHOLD = 9;

	public static List<CodeElement<?>> subjectMatch(String s, ExecutableMemberDoc member) {
		Set<CodeElement<?>> codeElements = collectPossibleMatchingElements(member);
		
		/* The presence of words like either, both, etc. can influence the distance. We need to
		 * remove them. 
		 */
		if (s.startsWith("either ")) {
			s = s.replaceFirst("either", "");
		} else if (s.startsWith("both ")) {
			s = s.replaceFirst("both", "");
		}
		s = s.trim();
		return getMatchingCodeElement(s, codeElements);
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
			/* match_ contains matches that are at the same distance from s. We simply return one of those
			   because we don't know which one is better */
			Optional<CodeElement<?>> foundMatch = match_.stream().findFirst();
			if (foundMatch.isPresent()) {
				match = foundMatch.get().getStringRepresentation();
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
	
	private static List<CodeElement<?>> getMatchingCodeElement(String s, Set<CodeElement<?>> codeElements) {
		List<CodeElement<?>> elements = new ArrayList<>(codeElements);
		elements.removeIf(e -> e.getLevenshteinDistanceFrom(s) >= THRESHOLD);
		Collections.sort(elements, (e1, e2) -> e1.getLevenshteinDistanceFrom(s) < e2.getLevenshteinDistanceFrom(s) ? -1 
											 : e1.getLevenshteinDistanceFrom(s) == e2.getLevenshteinDistanceFrom(s) ? 0 : 1);
//		elements.stream().forEach(e -> System.out.println(e.getCodeElement() + " " + e.getLevenshteinDistanceFrom(s)));
		for (int i = 0; i < elements.size() - 1; i++) {
			if (elements.get(i).getLevenshteinDistanceFrom(s) < elements.get(i + 1).getLevenshteinDistanceFrom(s)) {
				return elements.subList(0, i + 1);
			}
		}
		return elements;
	}
	
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
	
	/**
	 * This method collect Java code elements that a subject can possibly match.
	 */
	private static Set<CodeElement<?>> collectPossibleMatchingElements(ExecutableMemberDoc member) {
		Set<CodeElement<?>> elements = new LinkedHashSet<>();
		
		// Add parameters' names and types as code elements
		for (Parameter par : member.parameters()) {
			ParameterCodeElement parCodeElement = new ParameterCodeElement(par, member);
			// Name identifiers
			parCodeElement.addIdentifier(par.name());
			parCodeElement.addIdentifier(par.type().simpleTypeName() + " " + par.name());
			parCodeElement.addIdentifier(par.name() + " " + par.type().simpleTypeName());
			parCodeElement.addIdentifier("parameter");
			// Type identifiers
			if (par.type().dimension().contains("[]")) {
				parCodeElement.addIdentifier("array");
				parCodeElement.addIdentifier(par.type().simpleTypeName() + " array");
			} else {
				parCodeElement.addIdentifier(par.type().simpleTypeName());
				if (par.type().simpleTypeName().equals("java.lang.Iterable")) {
					parCodeElement.addIdentifier("collection");
				}
			}
			elements.add(parCodeElement);
		}
		
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
	
	private static String getCheck(String predicate) {
		if (predicate.contains("not") || predicate.contains("n't")) {
			return "==false";
		} else {
			return ""; // "==true";
		}
	}
}
