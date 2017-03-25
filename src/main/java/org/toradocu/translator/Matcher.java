package org.toradocu.translator;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.util.Reflection;

/**
 * The {@code Matcher} class translates subjects and predicates in Javadoc comments to Java
 * expressions containing Java code elements.
 */
class Matcher {

  /**
   * Represents the threshold for the edit distance above which {@code CodeElement}s are considered
   * to be not matching.
   */
  private static final int EDIT_DISTANCE_THRESHOLD = Toradocu.configuration.getDistanceThreshold();

  /**
   * Takes the subject of a proposition in a Javadoc comment and the {@code DocumentedMethod} that
   * subject was extracted from. Then returns all {@code CodeElement}s that match (i.e. have a
   * similar name to) the given subject string.
   *
   * @param subject the subject of a proposition from a Javadoc comment
   * @param method the {@code DocumentedMethod} that the subject was extracted from
   * @return a set of {@code CodeElement}s that have a similar name to the subject
   */
  static Set<CodeElement<?>> subjectMatch(String subject, DocumentedMethod method) {
    // Extract every CodeElement associated with the method and the containing class of the method.
    Set<CodeElement<?>> codeElements = JavaElementsCollector.collect(method);

    // Clean the subject string by removing words and characters not related to its identity so that
    // they do not influence string matching.
    List<String> wordsToRemove = Arrays.asList("either", "both", "any");
    for (String word : wordsToRemove) {
      String wordToReplace = word + " ";
      if (subject.startsWith(wordToReplace)) {
        subject = subject.replaceFirst(wordToReplace, "");
      }
    }
    subject = subject.trim();

    // Filter and return the CodeElements whose name is similar to subject.
    return filterMatchingCodeElements(subject, codeElements);
  }

  /**
   * Takes the container of a proposition in a Javadoc comment and the {@code DocumentedMethod} that
   * container was extracted from. Then returns the {@code CodeElement} that matches (i.e. has a
   * similar name to) the given container string.
   *
   * @param container the container of a proposition from a Javadoc comment
   * @param method the {@code DocumentedMethod} that the subject was extracted from
   * @return the {@code CodeElement} that has a similar name to the container
   */
  static CodeElement<?> containerMatch(String container, DocumentedMethod method) {
    final Set<CodeElement<?>> containers = subjectMatch(container, method);
    return !containers.isEmpty() ? containers.iterator().next() : null;
  }

  /**
   * Returns the set of {@code CodeElement}s that match the given filter string.
   *
   * @param filter the string to match {@code CodeElement}s against
   * @param codeElements the set of {@code CodeElement}s to filter
   * @return a set of {@code CodeElement}s that match the given string
   */
  private static Set<CodeElement<?>> filterMatchingCodeElements(
      String filter, Set<CodeElement<?>> codeElements) {
    Set<CodeElement<?>> minCodeElements = new LinkedHashSet<>();
    // Only consider elements with a minimum distance <= the threshold distance.
    int minDistance = EDIT_DISTANCE_THRESHOLD;
    // Returns the CodeElement(s) with the smallest distance.
    for (CodeElement<?> codeElement : codeElements) {
      int distance = codeElement.getEditDistanceFrom(filter);
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
   * Returns the translation (to a Java expression) of the given subject and predicate. Returns null
   * if a translation could not be found.
   *
   * @param method the method whose comment (and predicate) is being translated
   * @param subject the subject of the proposition to translate
   * @param predicate the predicate of the proposition to translate
   * @param negate true if the given predicate should be negated, false otherwise
   * @return the translation (to a Java expression) of the predicate with the given subject and
   *     predicate, or null if no translation found
   */
  static String predicateMatch(
      DocumentedMethod method, CodeElement<?> subject, String predicate, boolean negate) {

    // Special case to handle predicates about arrays' length. We need a more general solution.
    if (subject.getJavaCodeElement().toString().contains("[]")) {
      final java.util.regex.Matcher lengthPatter =
          Pattern.compile("has length ([0-9]+|zero)").matcher(predicate);
      if (lengthPatter.find()) {
        final String lengthString = lengthPatter.group(1);
        final int length = lengthString.equals("zero") ? 0 : Integer.parseInt(lengthString);
        return subject.getJavaExpression() + ".length==" + length;
      }

      // "zero-length" special case handling.
      java.util.regex.Matcher zeroLengthPattern =
          Pattern.compile("(is|are|has|have) zero-?length").matcher(predicate);
      if (zeroLengthPattern.find()) {
        return subject.getJavaExpression() + ".length==0";
      }
    }

    // General case
    String match = simpleMatch(predicate);
    if (match != null && subject.isCompatibleWith(match)) {
      if (subject instanceof ContainerElementsCodeElement) {
        ContainerElementsCodeElement containerCodeElement = (ContainerElementsCodeElement) subject;
        match = containerCodeElement.getJavaExpression(match);
      } else {
        match = subject.getJavaExpression() + match;
      }
    } else {
      Set<CodeElement<?>> codeElements;
      if (subject instanceof ParameterCodeElement) {
        ParameterCodeElement paramCodeElement = (ParameterCodeElement) subject;
        codeElements =
            extractBooleanCodeElements(
                paramCodeElement, paramCodeElement.getJavaCodeElement().getType());
        Class<?> targetClass = Reflection.getClass(method.getContainingClass().getQualifiedName());
        codeElements.addAll(extractStaticBooleanMethods(targetClass, paramCodeElement));
      } else if (subject instanceof ClassCodeElement) {
        ClassCodeElement classCodeElement = (ClassCodeElement) subject;
        codeElements =
            extractBooleanCodeElements(classCodeElement, classCodeElement.getJavaCodeElement());
      } else if (subject instanceof MethodCodeElement) {
        MethodCodeElement methodCodeElement = (MethodCodeElement) subject;
        codeElements =
            extractBooleanCodeElements(
                methodCodeElement, methodCodeElement.getJavaCodeElement().getReturnType());
      } else if (subject instanceof StaticMethodCodeElement) {
        StaticMethodCodeElement staticMethodCodeElement = (StaticMethodCodeElement) subject;
        codeElements =
            extractBooleanCodeElements(
                staticMethodCodeElement,
                staticMethodCodeElement.getJavaCodeElement().getReturnType());
      } else {
        return null;
      }
      Set<CodeElement<?>> matches = filterMatchingCodeElements(predicate, codeElements);
      if (matches.isEmpty()) {
        return null;
      } else {
        // Matches contains matches that are at the same distance from s. We simply return one of
        // those because we don't know which one is best.
        match = matches.stream().findFirst().get().getJavaExpression();
      }
    }

    // Condition "target==null" is indeed not correct.
    if (match.equals("target==null")) {
      return null;
    }

    if (negate) {
      match = "(" + match + ") == false";
    }
    return match;
  }

  /**
   * Extracts and returns all the boolean methods of {@code type}, including methods that take as
   * parameter {@code parameterType}.
   *
   * @param targetClass the class from which extract the methods
   * @param parameter the actual parameter that has to be used to invoke the extracted methods
   * @return the static boolean methods in the given class target class as a set of code elements
   */
  private static Set<CodeElement<?>> extractStaticBooleanMethods(
      Class<?> targetClass, ParameterCodeElement parameter) {
    Set<CodeElement<?>> collectedElements = new LinkedHashSet<>();

    // Add methods in containing class as code elements.
    methodCollection:
    for (Method classMethod : targetClass.getMethods()) {
      if (Modifier.isStatic(classMethod.getModifiers())
          && classMethod.getParameters().length < 2
          && (classMethod.getReturnType().equals(Boolean.class)
              || classMethod.getReturnType().equals(boolean.class))) {
        for (java.lang.reflect.Parameter par : classMethod.getParameters()) {
          if (!parameter.getJavaCodeElement().getType().equals(par.getType())) {
            continue methodCollection;
          }
        }
        collectedElements.add(
            new StaticMethodCodeElement(classMethod, parameter.getJavaExpression()));
      }
    }

    return collectedElements;
  }

  /**
   * Extracts and returns all fields and methods in the given class that have a boolean (return)
   * value. The returned code elements have the given code element integrated into their Java
   * expression representations as the receiver of the field or method call.
   *
   * @param receiver the code element that calls the field or method in the Java expression
   *     representation of the return code elements
   * @param type the class whose boolean-valued fields and methods to extract
   * @return the boolean-valued fields and methods in the given class as a set of code elements
   */
  private static Set<CodeElement<?>> extractBooleanCodeElements(
      CodeElement<?> receiver, Class<?> type) {
    Set<CodeElement<?>> result = new LinkedHashSet<>();

    if (type.isArray()) {
      result.add(new GeneralCodeElement(receiver.getJavaExpression() + ".length==0", "isEmpty"));
      return result;
    }

    // Important: Sort members to make result deterministic!
    Comparator<Member> byName = Comparator.comparing(Member::getName);

    for (Field field :
        Arrays.stream(type.getFields()).sorted(byName).collect(Collectors.toList())) {
      if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
        result.add(new FieldCodeElement(receiver.getJavaExpression(), field));
      }
    }

    for (Method method :
        Arrays.stream(type.getMethods()).sorted(byName).collect(Collectors.toList())) {
      if (method.getParameterCount() == 0
          && (method.getReturnType().equals(Boolean.class)
              || method.getReturnType().equals(boolean.class))) {
        result.add(new MethodCodeElement(receiver.getJavaExpression(), method));
      }
    }

    return result;
  }

  /**
   * Attempts to match the given predicate to a simple Java expression (i.e. one containing only
   * literals). The visibility of this method is {@code protected} for testing purposes.
   *
   * @param predicate the predicate to translate to a Java expression. Must not be {@code null}.
   * @return a Java expression translation of the given predicate or null if the predicate could not
   *     be matched
   */
  private static String simpleMatch(String predicate) {
    String verbs = "(is|are|be|is equal to|are equal to|equals to) ?";

    String predicates =
        "(true|false|null|this|zero|positive|strictly positive|negative|strictly negative|nonnegative|nonpositive)";

    java.util.regex.Matcher isPattern =
        Pattern.compile(verbs + "(==|=)? ?" + predicates).matcher(predicate);

    java.util.regex.Matcher isNotPattern =
        Pattern.compile(verbs + "(!=)? ?" + predicates).matcher(predicate);

    java.util.regex.Matcher inequalityNumber =
        Pattern.compile(
                verbs
                    + "(<=|>=|<|>|!=|==|=)? ?(-?([0-9]+|zero|one|two|three|four|five|six|seven|eight|nine))")
            .matcher(predicate);

    java.util.regex.Matcher inequalityVar =
        Pattern.compile(verbs + "(<=|>=|<|>|!=|==|=) ?([a-z]+)").matcher(predicate);

    java.util.regex.Matcher instanceOf = Pattern.compile("(instanceof) (.*)").matcher(predicate);

    String predicateTranslation;
    if (isPattern.find()) {
      // Get the last group in the regular expression.
      String lastWord = isPattern.group(isPattern.groupCount());
      switch (lastWord) {
        case "true":
        case "false":
        case "null":
          predicateTranslation = "==" + lastWord;
          break;
        case "this":
          predicateTranslation = "==target"; // The receiver object in the generated aspects.
          break;
        case "zero":
          predicateTranslation = "==0";
          break;
        case "positive":
        case "strictly positive":
          predicateTranslation = ">0";
          break;
        case "negative":
        case "strictly negative":
          predicateTranslation = "<0";
          break;
        case "nonnegative":
          predicateTranslation = ">=0";
          break;
        case "nonpositive":
          predicateTranslation = "<=0";
          break;
        default:
          predicateTranslation = null;
      }
    } else if (isNotPattern.find()) {
      String word = isNotPattern.group(isNotPattern.groupCount());
      switch (word) {
        case "true":
        case "false":
        case "null":
          predicateTranslation = "!=" + word;
          break;
        case "zero":
          predicateTranslation = "!=0";
          break;
        case "positive":
        case "strictly positive":
          predicateTranslation = ">0";
          break;
        case "negative":
        case "strictly negative":
          predicateTranslation = "<0";
          break;
        case "nonnegative":
          predicateTranslation = ">=0";
          break;
        case "nonpositive":
          predicateTranslation = "<=0";
          break;
        default:
          predicateTranslation = null;
      }
    } else if (inequalityNumber.find()) {
      // Get the number from the last group of the regular expression.
      String numberString = inequalityNumber.group(inequalityNumber.groupCount());
      // Get the symbol from the regular expression.
      String relation = inequalityNumber.group(2);
      String numberWord = numberWordToDigit(numberString);
      int number =
          (!numberWord.equals(""))
              ? number = Integer.parseInt(numberWord)
              : Integer.parseInt(numberString);
      // relation is null in predicates without inequalities. For example "is 0".
      if (relation == null || relation.equals("=")) {
        predicateTranslation = "==" + number;
      } else {
        predicateTranslation = relation + number;
      }
    } else if (inequalityVar.find()) {
      // Get the variable from the last group of the regular expression.
      String variable = inequalityVar.group(3);
      // Get the symbol from the regular expression.
      String relation = inequalityVar.group(2);
      // Now we have the variable name, but who is it in the code? We'll have to find it.
      predicateTranslation = relation + "{" + variable + "}";
      if (predicate.contains(variable + "."))
        predicateTranslation += predicate.substring(predicate.indexOf("."));
    } else if (predicate.equals("been set")) {
      predicateTranslation = "!=null";
    } else if (instanceOf.find()) {
      predicateTranslation = " instanceof " + instanceOf.group(2);
    } else {
      predicateTranslation = null;
    }
    return predicateTranslation;
  }

  private static String numberWordToDigit(String numberString) {
    switch (numberString) {
      case "zero":
        return "0";
      case "one":
        return "1";
      case "two":
        return "2";
      case "three":
        return "3";
      case "four":
        return "4";
      case "five":
        return "5";
      case "six":
        return "6";
      case "seven":
        return "7";
      case "eight":
        return "8";
      case "nine":
        return "9";
    }

    return "";
  }
}
