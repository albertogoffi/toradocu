package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.toradocu.extractor.Method;
import org.toradocu.extractor.Parameter;

public class Matcher {
	
	private static final int THRESHOLD = 9;

	public static List<CodeElement> subjectMatch(String subject, Method methodUnderTest) throws ClassNotFoundException {
		List<CodeElement> codeElements;
		codeElements = collectPossibleMatchingElements(methodUnderTest);
		
		/* The presence of words like either, both, etc. can influence the distance. We need to
		 * remove them. 
		 */
		subject = subject.replace("either", "").trim(); //TODO: Remove initial "stop words" only at the beginning of the subject s
		return getMatchingCodeElement(subject, codeElements);
	}
	
	public static String predicateMatch(String s, CodeElement subject) {
		String match = simpleMatch(s);
		if (match == null) {
//			Set<CodeElement<?>> codeElements = null;
//			if (subject instanceof ParameterCodeElement) {
//				ParameterCodeElement par = (ParameterCodeElement) subject;
//				codeElements = collectPossibleMatchingElements(par.getCodeElement().type());
//			} else if (subject instanceof ClassCodeElement) {
//				ClassCodeElement classCodeElement = (ClassCodeElement) subject;
//				codeElements = collectPossibleMatchingElements(classCodeElement.getCodeElement());
//			} else {
//				return null; // Any other subject type should have a predicate matching with simple matching strategy
//			}
//			List<CodeElement<?>> match_ = getMatchingCodeElement(s, codeElements);
//			/* match_ contains matches that are at the same distance from s. We simply return one of those
//			   because we don't know which one is better */
//			Optional<CodeElement<?>> foundMatch = match_.stream().findFirst();
//			if (foundMatch.isPresent()) {
//				match = foundMatch.get().getStringRepresentation();
//				match += getCheck(s);
//			}
		}
		return match;
	}
	
	private static String simpleMatch(String phrase) {
		switch (phrase) {
		case "is negative":
			return "<0";
		case "is positive":
			return ">0";
		case "is false":
			return "==false";
		case "is true":
			return "==true";
		case "is null":
		case "are null":
		case "been set":
			return "==null";
		case "is < 1": // TODO Generalize this case!
			return "<1";
		case "is <= 0": // TODO Generalize this case!
			return "<=0";
		default:
			return null;
		}
	}
	
	private static List<CodeElement> getMatchingCodeElement(String s, List<CodeElement> codeElements) {
		List<CodeElement> elements = new ArrayList<>(codeElements);
		elements.removeIf(e -> e.getLevenshteinDistanceFrom(s) >= THRESHOLD);
		// FIXME Remove the sort and just pick the element with minimun distance (ignoring elements above treshold
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
	
//	private static Set<CodeElement<?>> collectPossibleMatchingElements(Type type) {
//		// Given the subject type, possible matching elements for the predicates are subject's methods and fields
//		// Note that this method is also used in predicate matching
//		Set<CodeElement<?>> elements = new HashSet<>();
//		
//		if (!type.isPrimitive()) {
//			ClassDoc classDoc = type.asClassDoc();
//			for (FieldDoc field : classDoc.fields()) {
//				if (field.isPublic()) {
//					elements.add(new FieldCodeElement(field, field.name()));
//				}
//			}
//			for (MethodDoc method : classDoc.methods()) {
//				if (method.isPublic() && method.parameters().length == 0 && 
//						(method.returnType().simpleTypeName().equals("boolean") || method.returnType().simpleTypeName().equals("Boolean"))) {
//					elements.add(new MethodCodeElement(method, method.name()));
//				}
//			}
//		} else if (type.dimension().equals("[]")) {
//			elements.add(new DummyCodeElement("isEmpty", ".length==0"));
//			elements.add(new DummyCodeElement("length", ".length"));
//		}
//		return elements;
//	}
	
	/**
	 * This method collect Java code elements that a subject can possibly match.
	 * @throws ClassNotFoundException if the class declaring <code>method</code> cannot be loaded
	 */
	private static List<CodeElement> collectPossibleMatchingElements(Method method) throws ClassNotFoundException {
		List<CodeElement> elements = new ArrayList<>();
		
		// Add parameters' names and types as code elements
		List<Parameter> parameters = method.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			Parameter par = parameters.get(i);
			ParameterCodeElement parCodeElement = new ParameterCodeElement(i);
			// Name identifiers
			parCodeElement.addIdentifier(par.getName());
			parCodeElement.addIdentifier(par.getSimpleType() + " " + par.getName());
			parCodeElement.addIdentifier(par.getName() + " " + par.getSimpleType());
			parCodeElement.addIdentifier("parameter");
			// Type identifiers
			if (par.getDimension().contains("[]")) {
				parCodeElement.addIdentifier("array");
				parCodeElement.addIdentifier(par.getType() + " array");
			} else {
				parCodeElement.addIdentifier(par.getType());
				if (par.getType().equals("java.lang.Iterable")) {
					parCodeElement.addIdentifier("collection");
				}
			}
			elements.add(parCodeElement);
		}
		
		// Add target object as code element
		Class<?> containingClass = Class.forName(method.getContainingClass());
		ClassCodeElement classCodeElement = new ClassCodeElement();
		String className = containingClass.getSimpleName();
		classCodeElement.addIdentifier(className);
		
		char[] classNameArray = className.toCharArray(); 
		for (int i = classNameArray.length - 1; i > 0; i--) {
			if (Character.isUpperCase(classNameArray[i])) {
				classCodeElement.addIdentifier(className.substring(i));
				break;
			}
		}
		
		for (Class<?> implementedInterface : containingClass.getInterfaces()) {
			classCodeElement.addIdentifier(implementedInterface.getSimpleName());
		}
		elements.add(classCodeElement);
		
		// Add target object methods as code elements
		for (java.lang.reflect.Method classMethod : containingClass.getMethods()) { // TODO Consider also protected methods?
			if (classMethod.getParameters().length == 0) {
				String methodName = classMethod.getName();
				if (methodName.startsWith("get")) {
					methodName = methodName.replaceFirst("get", "");
				}
				MethodCodeElement m = new MethodCodeElement(getSignature(classMethod), "target", classMethod.getName());
				elements.add(m);
			}
		}
		
		return elements;
	}
	
	private static String getSignature(java.lang.reflect.Method method) {
		 Pattern pattern = Pattern.compile("[^.]*\\(.*\\)");
		 java.util.regex.Matcher matcher = pattern.matcher(method.toString());
		 if (matcher.find()) {
			return matcher.group(); 
		 }
		 return "";
	}
	
	private static String getCheck(String predicate) {
		if (predicate.contains("not") || predicate.contains("n't")) {
			return "==false";
		} else {
			return ""; // "==true";
		}
	}
}
