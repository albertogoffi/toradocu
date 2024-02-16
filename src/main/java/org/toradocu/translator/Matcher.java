package org.toradocu.translator;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.semantic.SemanticMatcher;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * The {@code Matcher} class translates subjects and predicates in Javadoc comments to Java
 * expressions containing Java code elements.
 */
public class Matcher {

  /**
   * Represents the threshold for the edit distance above which {@code CodeElement}s are considered
   * to be not matching.
   */
  private final int editDistanceThreshold;

  public Matcher() {
    this.editDistanceThreshold = Configuration.INSTANCE.getDistanceThreshold();
  }

  public Matcher(int editDistanceThreshold) {
    this.editDistanceThreshold = editDistanceThreshold;
  }

  /**
   * Takes the subject of a proposition in a Javadoc comment and the {@code DocumentedExecutable}
   * that subject was extracted from. Then returns all {@code CodeElement}s that match (i.e. have a
   * similar name to) the given subject string.
   *
   * @param subject the subject of a proposition from a Javadoc comment
   * @param method the {@code DocumentedExecutable} that the subject was extracted from
   * @return a set of {@code CodeElement}s that have a similar name to the subject
   */
  Set<CodeElement<?>> subjectMatch(String subject, DocumentedExecutable method) {
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
    Set<CodeElement<?>> matchingCodeElements = filterMatchingCodeElements(subject, codeElements);
    // If some CodeElement is a method that requires parameters, 
    //    try to instantiate the parameters, or remove the CodeElement otherwise
    Set<CodeElement<?>> candidateMatchings = new HashSet<>(matchingCodeElements);
    for (CodeElement<?> codeElement: candidateMatchings) {
        String[] args = null;
		if (codeElement instanceof MethodCodeElement) {
            args = ((MethodCodeElement) codeElement).getArgs();
		} else if (codeElement instanceof StaticMethodCodeElement) {
            args = ((StaticMethodCodeElement) codeElement).getArgs();
		} else {
			continue; //only methods can require parameters, keep this CodeElement as is
		}
		if (args == null || args.length == 0) {
			continue; //no parameters needed, keep this CodeElement as is
		}
    	String[] instantiatedArgs = possiblyAssignArgsWithParameters(args, method);
    	if (instantiatedArgs != null) {
    		if (codeElement instanceof MethodCodeElement) {
                ((MethodCodeElement) codeElement).setParameters(Arrays.asList(instantiatedArgs));
    		} else if (codeElement instanceof StaticMethodCodeElement) {
                ((StaticMethodCodeElement) codeElement).setParameters(Arrays.asList(instantiatedArgs));
    		}
    	} else {//parameters could not be instantiated
    		matchingCodeElements.remove(codeElement);
    	}
    }
    return matchingCodeElements;
  }

  /**
   * Takes the container of a proposition in a Javadoc comment and the {@code DocumentedExecutable}
   * that container was extracted from. Then returns the {@code CodeElement} that matches (i.e. has
   * a similar name to) the given container string.
   *
   * @param container the container of a proposition from a Javadoc comment
   * @param method the {@code DocumentedExecutable} that the subject was extracted from
   * @return the {@code CodeElement} that has a similar name to the container
   */
  CodeElement<?> containerMatch(String container, DocumentedExecutable method) {
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
  private Set<CodeElement<?>> filterMatchingCodeElements(
      String filter, Set<CodeElement<?>> codeElements) {
    Set<CodeElement<?>> minCodeElements = new LinkedHashSet<>();
    // If the word to match is a one-letter word (or empty string), we look for an exact match.
    int minDistance = 0;
    // Only consider elements with a minimum distance <= the threshold distance.
    if (filter.length() > 1) {
      minDistance = editDistanceThreshold;
    }
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
   * @param proposition the proposition to translate
   * @param comment comment from which the proposition has been extracted
   * @return the translation (to a Java expression) of the predicate with the given subject and
   *     predicate, or null if no translation found
   */
  String predicateMatch(
      DocumentedExecutable method,
      CodeElement<?> subject,
      Proposition proposition,
      String comment, SemanticGraph sg) {

    String predicate = proposition.getPredicate();
    // Special case to handle predicates about arrays' length. We need a more general solution.
    if (subject.getJavaCodeElement().toString().contains("[]")) {
      final java.util.regex.Matcher lengthPattern =
          Pattern.compile("has length ([0-9]+|zero)").matcher(predicate);
      if (lengthPattern.find()) {
        final String lengthString = lengthPattern.group(1);
        final int length = lengthString.equals("zero") ? 0 : Integer.parseInt(lengthString);
        return subject.getJavaExpression()
            + "!=null && "
            + subject.getJavaExpression()
            + ".length=="
            + length;
      }
      final java.util.regex.Matcher numberPattern =
          Pattern.compile("([<>=]=?|(!=)|is) ?([0-9]+|zero)").matcher(predicate);
      if (numberPattern.find()) {
        final String lengthString = numberPattern.group(3);
        final int length = lengthString.equals("zero") ? 0 : Integer.parseInt(lengthString);
        String operator = numberPattern.group(1);
        if (operator.equals("is")) {
          operator = "==";
        }
        return subject.getJavaExpression()
            + "!=null && "
            + subject.getJavaExpression()
            + ".length"
            + operator
            + length;
      }

      // "zero-length" special case handling.
      java.util.regex.Matcher zeroLengthPattern =
          Pattern.compile("(is|are|has|have) zero-?length").matcher(predicate);
      if (zeroLengthPattern.find()) {
        return subject.getJavaExpression()
            + "!=null && "
            + subject.getJavaExpression()
            + ".length==0";
      }
    }

    // General case
    Match match = simpleMatch(predicate);
    if (match != null
        && subject.isCompatibleWith(method.getDeclaringClass(), match.getBaseExpression())) {
      if (subject instanceof ContainerElementsCodeElement) {
        ContainerElementsCodeElement containerCodeElement = (ContainerElementsCodeElement) subject;
        match.setBaseExpression(containerCodeElement.getJavaExpression(match.getBaseExpression()));
      } else {
        match.setBaseExpression(subject.getJavaExpression() + match.getBaseExpression());
      }
    } else {
      match = codeElementsMatch(method, subject, proposition, comment, sg);
      if (match == null
          || !subject.isCompatibleWith(method.getDeclaringClass(), match.getBaseExpression())) {
        return null;
      }
    }

    if (match
        .getBaseExpression()
        .equals(
            Configuration.RECEIVER
                + "==null")) { // Condition "receiverObjectID==null" is indeed not correct.
      return null;
    }
    String finalMatch = "";
    if (proposition.isNegative()) {
      finalMatch = "(" + match.getBaseExpression() + ") == false";
    } else {
      finalMatch = match.getBaseExpression();
    }

    if (match.getNullDereferenceCheck() != null) {
      finalMatch = (match.getNullDereferenceCheck() + " && ").concat(finalMatch);
    }

    return finalMatch;
  }

  private Match codeElementsMatch(
      DocumentedExecutable method,
      CodeElement<?> subject,
      Proposition proposition,
      String comment, SemanticGraph sg) {
    Set<CodeElement<?>> codeElements;
    String predicate = proposition.getPredicate();

    // TODO check the following calls to extractBooleanCodeElements(): are they necessary before
    // TODO calling JavaElementsCollector#collect()?
    if (subject instanceof ParameterCodeElement) {
      ParameterCodeElement paramCodeElement = (ParameterCodeElement) subject;
      codeElements =
          extractBooleanCodeElements(
              paramCodeElement, paramCodeElement.getJavaCodeElement().getType());
      Class<?> targetClass = method.getDeclaringClass();
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
    codeElements.addAll(JavaElementsCollector.collect(method));

    // Filter collected code elements that refer to the documented method under analysis.
    // This avoids to generate specifications mentioning the method whose behavior they specify.
    codeElements =
        codeElements
            .stream()
            .filter(
                e -> {
                  if (e.getJavaExpression().matches("(.*).set[A-Z](.*)")) {
                    // exclude setters
                    return false;
                  }
                  if (e instanceof MethodCodeElement) {
                    Method m = ((MethodCodeElement) e).getJavaCodeElement();
                    if (m.toGenericString().equals(method.getExecutable().toGenericString())
                        || (!m.getReturnType().equals(Boolean.class)
                            && !m.getReturnType().equals(boolean.class))) {
                      return false;
                    }
                  } else if (e instanceof StaticMethodCodeElement) {
                    Method m = ((StaticMethodCodeElement) e).getJavaCodeElement();
                    if (m.toGenericString().equals(method.getExecutable().toGenericString())
                        || (!m.getReturnType().equals(Boolean.class)
                            && !m.getReturnType().equals(boolean.class))) {
                      return false;
                    }
                  }
                  return true;
                })
            .collect(Collectors.toSet());

    List<CodeElement<?>> sortedMethodList = new ArrayList<CodeElement<?>>(codeElements);
    
    List<String> wordsForParameterMatching = relevantWords(sg.vertexListSorted(), 
    		WordType.NN, WordType.WP, WordType.JJ, 
    		WordType.PR, WordType.FW, WordType.SYM);
    List<String> wordsInSubject = relevantWords(proposition.getSubject().getSubjectWords(),
    		WordType.NN, WordType.WP, WordType.JJ, 
    		WordType.PR, WordType.FW, WordType.SYM);
    List<String> wordsForParameterMatching_noSubject = new ArrayList<>(wordsForParameterMatching);
    wordsForParameterMatching_noSubject.removeAll(wordsInSubject);
    // Try the classic syntactic match first of all
    Match match = syntacticMatch(predicate, codeElements, method, wordsForParameterMatching_noSubject);
    if (match == null && SemanticMatcher.isEnabled()) {
      // When the syntactic match fails, try semantic if enabled
      try {
        SemanticMatcher semanticMatcher = new SemanticMatcher(true, (float) 0.2, (float) 3.11);

        // it is important to provide a fixed order since this point, to prevent method with same
        // score
        // being put in map in a different order every execution
        Collections.sort(sortedMethodList, new JavaExpressionComparator());
        LinkedHashMap<CodeElement<?>, Double> semanticMethodMatches =
            semanticMatcher.runSemanticMatch(
                sortedMethodList, method, subject, proposition, comment);

        if (semanticMethodMatches != null && !semanticMethodMatches.isEmpty()) {
          List<CodeElement<?>> semanticMethodList =
              new ArrayList<CodeElement<?>>(semanticMethodMatches.keySet());

          if (semanticMethodList.size() > 5) {
            semanticMethodList = semanticMethodList.subList(0, 4);
          }
          // NB: As this is a semantic match, also the subject can be a parameter for the predicate 
          // in the call below. E.g., if the sentence is in passive form
          match = findBestMethodMatch(method, predicate, semanticMethodList, wordsForParameterMatching);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return match;
  }

  public static enum WordType {
	  NN, //NN* (NN, NNS, NNP, NNPS) names
	  WP, //WP* (WP, WP$) Wh-pronouns
	  PR, //PR* (PRP, PRP$) pronouns
	  FW, //foreign word
	  SYM, //symbol
	  JJ, //JJ* (JJ, JJR, JJS) adjectives
	  
	  IN, //Preposition or subordinating conjunction
	  RB, //RB* (RB, RBR, RBS) adverbs
	  TO, //to
	  VB, //VB* (VB, VBD, VBG, VBN, VBP, VBZ) verbs
	  CC, //Coordinating conjunction
	  CD, //Cardinal number
	  DT, //Determiner
	  EX, //Existential there
	  LS, //List item marker
	  MD, //Modal
	  PDT,//Predeterminer
	  POS,//Possessive ending
	  RP, //Particle
	  UH, //Interjection
	  WDT,//Wh-determiner
	  WRB //Wh-adverb
  }
  
  public static List<String> relevantWords(List<IndexedWord> wordList, WordType... tagSpecifier) {
	  ArrayList<String> ret = new ArrayList<>();
	  for (IndexedWord word: wordList) {
		  String tag = word.tag();
		  String w = word.word();
		  for (WordType ts: tagSpecifier) {
			  if (tag.startsWith(ts.name())) {
				  ret.add(w);
				  break; //added
			  }
		  }
	  }
	  return ret;
  }

 private Match OLD_findBestMethodMatch(
		  DocumentedExecutable method, String predicate, List<CodeElement<?>> sortedCodeElements) {
	  Match match = null;
	  CodeElement<?> firstCodeMatch = null;
	  boolean foundArgMatch = false;
	  List<String> paramForMatch = new ArrayList<String>();
	  List<String> paramMatch = new ArrayList<String>();
	  String[] args = null;
	  String receiver = "";
	  java.lang.reflect.Parameter[] myParams = method.getExecutable().getParameters();

	  for (CodeElement<?> currentMatch : sortedCodeElements) {
		  if (currentMatch instanceof MethodCodeElement) {
			  args = ((MethodCodeElement) currentMatch).getArgs();
			  receiver = ((MethodCodeElement) currentMatch).getReceiver();
		  } else if (currentMatch instanceof StaticMethodCodeElement) {
			  args = ((StaticMethodCodeElement) currentMatch).getArgs();
		  } else continue;
		  // Match is a String: before building it, check if the method has parameters,
		  // and fill the parenthesis () with the right ones
		  if (args != null) {
			  paramMatch = Arrays.asList(args);
			  int pcount = 0;
			  for (java.lang.reflect.Parameter p : myParams) {
				  Type pt = p.getParameterizedType();
				  if (paramMatch.contains(pt.getTypeName())) {
					  paramForMatch.add("args[" + pcount + "]");
					  if (!receiver.equals("args[" + pcount + "]")) {
						  firstCodeMatch = currentMatch;
						  foundArgMatch = true;
					  }
				  }
				  pcount++;
			  }
		  }
		  if (foundArgMatch) {
			  firstCodeMatch = currentMatch;
			  break;
		  }
	  }
	  if (foundArgMatch && paramForMatch.size() == args.length) {
		  String exp = firstCodeMatch.getJavaExpression();
		  if (firstCodeMatch instanceof MethodCodeElement) {
			  match =
					  new Match(
							  exp.substring(0, exp.indexOf("(") + 1),
							  ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
		  } else {
			  match = new Match(exp.substring(0, exp.indexOf("(") + 1), null);
		  }
		  for (int j = 0; j < paramForMatch.size() - 1; j++)
			  match.completeExpression(paramForMatch.get(j) + ",");
		  match.completeExpression(paramForMatch.get(paramForMatch.size() - 1) + ")");
	  } else if (args
			  != null) { // the method is supposed to take params but we haven't find a match: does it
		  // have to take null?
		  // TODO check method match number of arguments!
		  final java.util.regex.Matcher nullPattern =
				  Pattern.compile("(has|have|contains?) null").matcher(predicate);

		  final java.util.regex.Matcher equalPattern = // or is it the equals() method?
				  Pattern.compile("(is|are) equals?").matcher(predicate);

		  firstCodeMatch = sortedCodeElements.stream().findFirst().get();

		  if (nullPattern.find()) {
			  String exp = firstCodeMatch.getJavaExpression();
			  match =
					  new Match(
							  exp.substring(0, exp.indexOf("(") + 1) + "null" + ")",
							  ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
			  foundArgMatch = true;
		  } else if (equalPattern.find()) {
			  // the equal method can be invoked only from an Object to an Object of the same type
			  receiver = receiver.replace("[", "").replace("]", "").replace("s", "");
			  for (int i = 0; i < myParams.length && !foundArgMatch; i++) {
				  Parameter p = myParams[i];
				  if (p.getName().equals(receiver)) { // found the receiver, who is the Object of same type?
					  String type = p.getParameterizedType().getTypeName();
					  for (int j = 0; j < myParams.length; j++) {
						  if (j != i && myParams[j].getParameterizedType().getTypeName().equals(type)) {
							  String exp = firstCodeMatch.getJavaExpression();
							  match =
									  new Match(
											  exp.substring(0, exp.indexOf("(") + 1) + "args[" + j + "]" + ")",
											  ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
							  foundArgMatch = true;
							  break;
						  }
					  }
				  }
			  }
		  }
	  }
	  if (!foundArgMatch) {
		  // No match is the absolute best: just pick the first one, but only if it takes no arguments!
		  firstCodeMatch = sortedCodeElements.stream().findFirst().get();
		  if ((firstCodeMatch instanceof MethodCodeElement
				  && ((MethodCodeElement) firstCodeMatch).getArgs() == null)) {
			  match =
					  new Match(
							  firstCodeMatch.getJavaExpression(),
							  ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
		  } else if (firstCodeMatch instanceof GeneralCodeElement) {
			  match =
					  new Match(
							  firstCodeMatch.getJavaExpression(),
							  ((GeneralCodeElement) firstCodeMatch).getNullDereferenceCheck());
		  }
	  }
	  return match;
  }
  
  /**
   * Run classic syntactic match based on edit distance
   *
   * @param predicate predicate to match
   * @param codeElements list of candidate code elements to match
   * @param method the {@code DocumentedExecutable} the predicate belongs to
 * @param wordsForParameterMatching 
   * @return the best matching code element according to the edit distance, null if none found
   */
  private Match syntacticMatch(
      String predicate, Set<CodeElement<?>> codeElements, DocumentedExecutable method, List<String> wordsForParameterMatching) {
    List<CodeElement<?>> sortedMethodList;
    sortedMethodList = new ArrayList<>(filterMatchingCodeElements(predicate, codeElements));
    if (!sortedMethodList.isEmpty())
      Collections.sort(sortedMethodList, new JavaExpressionComparator());
    if (sortedMethodList.isEmpty()) {
      return null;
    } else {
      return findBestMethodMatch(method, predicate, sortedMethodList, wordsForParameterMatching);
    }
  }

  /**
   * Search the best match between the {@code predicate} and the list of possibly matching sorted
   * {@code CodeElement}s. This is especially to find the best method match in case of {@code
   * MethodCodeElement}, by comparing the arguments needed.
   *
   * @param method the {@code DocumentedExecutable} the predicate is referring to
   * @param predicate the String predicate to match
   * @param sortedCodeElements sorted list of matching method {@code CodeElement}s
 * @param wordsForParameterMatching 
   * @return object representation of the best match found
   */
  private Match findBestMethodMatch(
      DocumentedExecutable method, String predicate, List<CodeElement<?>> sortedCodeElements, List<String> wordsForParameterMatching) {
    Match match = null;
    CodeElement<?> firstCodeMatch = null;
    boolean foundArgMatch = false;
    List<String> paramForMatch = new ArrayList<String>();
    String[] args = null;
    String receiver = "";
    java.lang.reflect.Parameter[] myParams = method.getExecutable().getParameters();

    for (CodeElement<?> currentMatch : sortedCodeElements) {
    	foundArgMatch = false;
        paramForMatch.clear();
      if (currentMatch instanceof MethodCodeElement) {
        args = ((MethodCodeElement) currentMatch).getArgs();
        receiver = ((MethodCodeElement) currentMatch).getReceiver();
      } else if (currentMatch instanceof StaticMethodCodeElement) {
        args = ((StaticMethodCodeElement) currentMatch).getArgs();
      } else continue;
      // Match is a String: before building it, check if the method has parameters,
      // and fill the parenthesis () with the right ones
      foundArgMatch = true;
      if (args != null) {    	  
          foundArgMatch = false;
    	  //List<String> paramMatch = Arrays.asList(args);

          List<String> candidateKeywords = new ArrayList<>(wordsForParameterMatching);
    	  int numNullSeenAndNotUsed = 0;
    	  for (int i = 0; i < wordsForParameterMatching.size(); ++i) {
    		  String candidateName = wordsForParameterMatching.get(i);
    		  if (candidateName.contentEquals("null")) {
    			  candidateKeywords.remove(i - numNullSeenAndNotUsed);
    			  ++numNullSeenAndNotUsed;
    		  }
    	  }
          for (String argType: args) {
        	  // Search a match for the arg: matches with a keyword in the predicate and has same type as arg 
        	  boolean foundMatchingKeyword = false;
        	  for (String candidateName: candidateKeywords) { // check match for each keyword
        		  Set<CodeElement<?>> candidatesMatches = this.subjectMatch(candidateName, method);
        		  for (CodeElement<?> candidateM: candidatesMatches) { 
        			  if (candidateM.equals(currentMatch) || candidateM.equals(receiver)) {
        				  continue;
        			  }
        			  Type candidateRetType;
        			  if (candidateM instanceof MethodCodeElement) {
        				  candidateRetType = ((MethodCodeElement) candidateM).getJavaCodeElement().getGenericReturnType();
        			  } else if (candidateM instanceof StaticMethodCodeElement) {
        				  candidateRetType = ((StaticMethodCodeElement) candidateM).getJavaCodeElement().getGenericReturnType();
        			  } else if (candidateM instanceof ParameterCodeElement) {
        				  candidateRetType = ((ParameterCodeElement) candidateM).getJavaCodeElement().getParameterizedType();
        			  } else {
        				  continue;
        			  }
        			  if (argType.equals(candidateRetType.getTypeName())) {
        				  String expr = candidateM.buildJavaExpression();
        				  paramForMatch.add(expr);
        				  foundMatchingKeyword = true;
        				  break;
        			  }
        		  }
        		  if (foundMatchingKeyword) {
        			  break;
        		  }
        	  }
        	  if (!foundMatchingKeyword && numNullSeenAndNotUsed > 0) {
        		  --numNullSeenAndNotUsed;
        		  paramForMatch.add("null");
        	  }
          }
          
          foundArgMatch = (paramForMatch.size() == args.length);
          
        /*int pcount = 0;
        for (java.lang.reflect.Parameter p : myParams) {
          Type pt = p.getParameterizedType();
          if (paramMatch.contains(pt.getTypeName())) {
            paramForMatch.add("args[" + pcount + "]");
            if (!receiver.equals("args[" + pcount + "]")) {
              firstCodeMatch = currentMatch;
              foundArgMatch = true;
            }
          }
          pcount++;
        }*/
      }

      if (foundArgMatch) {
        firstCodeMatch = currentMatch;
        break;
      }
    }
        
    if (foundArgMatch) {
      String exp = firstCodeMatch.getJavaExpression();
      if (firstCodeMatch instanceof MethodCodeElement) {
        match =
            new Match(
                exp.substring(0, exp.lastIndexOf("(") + 1),
                ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
      } else {
        match = new Match(exp.substring(0, exp.lastIndexOf("(") + 1), null);
      }
      for (int j = 0; j < paramForMatch.size(); j++) {
    	  if (j == paramForMatch.size() - 1) {
    	      match.completeExpression(paramForMatch.get(paramForMatch.size() - 1));    		  
    	  } else {
    		  match.completeExpression(paramForMatch.get(j) + ",");
    	  }
      }
	  match.completeExpression(")");
    } else if (args
        != null) { // the method is supposed to take params but we haven't find a match: does it
      // have to take null?
      // TODO check method match number of arguments!
      final java.util.regex.Matcher nullPattern =
          Pattern.compile("(has|have|contains?) null").matcher(predicate);

      final java.util.regex.Matcher equalPattern = // or is it the equals() method?
          Pattern.compile("(is|are) equals?").matcher(predicate);

      firstCodeMatch = sortedCodeElements.stream().findFirst().get();

      if (nullPattern.find()) {
        String exp = firstCodeMatch.getJavaExpression();
        match =
            new Match(
                exp.substring(0, exp.indexOf("(") + 1) + "null" + ")",
                ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
        foundArgMatch = true;
      } else if (equalPattern.find()) {
        // the equal method can be invoked only from an Object to an Object of the same type
        receiver = receiver.replace("[", "").replace("]", "").replace("s", "");
        for (int i = 0; i < myParams.length && !foundArgMatch; i++) {
          Parameter p = myParams[i];
          if (p.getName().equals(receiver)) { // found the receiver, who is the Object of same type?
            String type = p.getParameterizedType().getTypeName();
            for (int j = 0; j < myParams.length; j++) {
              if (j != i && myParams[j].getParameterizedType().getTypeName().equals(type)) {
                String exp = firstCodeMatch.getJavaExpression();
                match =
                    new Match(
                        exp.substring(0, exp.indexOf("(") + 1) + "args[" + j + "]" + ")",
                        ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
                foundArgMatch = true;
                break;
              }
            }
          }
        }
      }
    }
    if (!foundArgMatch) {
      // No match is the absolute best: just pick the first one, but only if it takes no arguments!
      firstCodeMatch = sortedCodeElements.stream().findFirst().get();
      if ((firstCodeMatch instanceof MethodCodeElement
          && ((MethodCodeElement) firstCodeMatch).getArgs() == null)) {
        match =
            new Match(
                firstCodeMatch.getJavaExpression(),
                ((MethodCodeElement) firstCodeMatch).getNullDereferenceCheck());
      } else if (firstCodeMatch instanceof GeneralCodeElement) {
        match =
            new Match(
                firstCodeMatch.getJavaExpression(),
                ((GeneralCodeElement) firstCodeMatch).getNullDereferenceCheck());
      }
    }
    
    Match old_match = OLD_findBestMethodMatch(method, predicate, sortedCodeElements);
    //COMPARISON:
    if (old_match != null && match != null) {
    	if (!old_match.getBaseExpression().equals(match.getBaseExpression())) {
    	    System.out.println("[DEBUG: COMPARISON WARNING] " + method.getDeclaringClass() + "." + method.getSignature() + "::" + predicate + "...");
        	System.out.println("[DEBUG: COMPARISON WARNING] OLD DIFFERS. OLD: " + old_match.getBaseExpression() + " , NEW: " + match.getBaseExpression());
    	}
    } else if (old_match != null) {
        System.out.println("[DEBUG: COMPARISON WARNING] " + method.getSignature() + "::" + predicate + "...");
    	System.out.println("[DEBUG: COMPARISON WARNING] ONLY OLD: " + old_match.getBaseExpression());
    } else if (match != null) {
        System.out.println("[DEBUG: COMPARISON WARNING] " + method.getSignature() + "::" + predicate + "...");
    	System.out.println("[DEBUG: COMPARISON WARNING] ONLY NEW: " + match.getBaseExpression());    	
    }

    return match;
  }

  private String[] possiblyAssignArgsWithParameters(String[] candidateArgs, DocumentedExecutable method) {
	  java.lang.reflect.Parameter[] params = method.getExecutable().getParameters();
	  String[] assignedArgs = null;
	  if (candidateArgs != null) {
		  assignedArgs = new String[candidateArgs.length];
		  HashSet<Integer> usedParams = new HashSet<>();
		  for (int i = 0; i < candidateArgs.length; ++i) {	
			  String candidateA = candidateArgs[i];
			  boolean foundMatch = false;
			  for (int j = 0; j < params.length; ++j) {
				  if (usedParams.contains(j)) {
					  continue;
				  }
				  java.lang.reflect.Parameter p = params[j];
				  Type pt = p.getParameterizedType();
				  if (candidateA.equals(pt.getTypeName())) {
					  assignedArgs[i] = "args[" + j + "]";
					  foundMatch = true;
					  usedParams.add(j);
					  break;
				  }
			  }
			  if (!foundMatch) {
				  return null; //we need a match for all args, but did not found any for at least an arg
			  }
		  }
	  }
	  return assignedArgs;
}

/**
   * Extracts and returns all the boolean methods of {@code type}, including methods that take as
   * parameter {@code parameterType}.
   *
   * @param targetClass the class from which extract the methods
   * @param parameter the actual parameter that has to be used to invoke the extracted methods
   * @return the static boolean methods in the given class target class as a set of code elements
   */
  private Set<CodeElement<?>> extractStaticBooleanMethods(
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
  private Set<CodeElement<?>> extractBooleanCodeElements(CodeElement<?> receiver, Class<?> type) {
    Set<CodeElement<?>> result = new LinkedHashSet<>();

    if (type.isArray()) {
      result.add(
          new GeneralCodeElement(
              receiver.getJavaExpression() + ".length==0",
              receiver.getJavaExpression() + "!=null",
              "isEmpty"));
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
      if (method.getReturnType().equals(Boolean.class)
          || method.getReturnType().equals(boolean.class)) {
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
  private static Match simpleMatch(String predicate) {
    String verbs = "(is|are|be|is equal to|are equal to|equals to|return) ?";

    String predicates =
        "(true|false|null|this|empty|zero|positive|strictly positive|negative|strictly negative|nonnegative|nonpositive)";

    java.util.regex.Matcher isPattern =
        Pattern.compile(verbs + "(==|=)? ?" + predicates).matcher(predicate);

    java.util.regex.Matcher isNotPattern =
        Pattern.compile(verbs + "(!=)? ?" + predicates).matcher(predicate);

    java.util.regex.Matcher inequalityNumber =
        Pattern.compile(
                verbs
                    + "(<=|>=|<|>|!=|==|=)? ?(-?([0-9]+(.[0-9]+)?|zero|one|two|three|four\b|five|six\b|seven\b|eight\b|nine\b)(?! ))")
            .matcher(predicate);

    java.util.regex.Matcher inequalityVar =
        Pattern.compile(verbs + "(<=|>=|<|>|!=|==|=) ?((([a-zA-Z]+[0-9]?)+_?)+)")
            .matcher(predicate);

    java.util.regex.Matcher instanceOf = Pattern.compile("(instanceof) (.*)").matcher(predicate);

    Match match = null;
    String translation = null;
    if (isPattern.find()) {
      // Get the last group in the regular expression.
      translation = manageIsPattern(isPattern);
    } else if (isNotPattern.find()) {
      translation = manageIsNotPattern(isNotPattern);
    } else if (inequalityNumber.find()) {
      translation = manageInequalityNumber(inequalityNumber);
    } else if (inequalityVar.find()) {
      match = manageInequalityVar(predicate, inequalityVar);
    } else if (predicate.equals("been set")) {
      translation = "!=null";
    } else if (instanceOf.find()) {
      translation = " instanceof " + instanceOf.group(2);
    }

    if (translation != null && match == null) {
      match = new Match(translation, null);
    }

    return match;
  }

  /**
   * Returns the translation of predicate matching the inequalityVar regex
   *
   * @param inequalityVar the matching regex
   * @return the translation
   */
  private static Match manageInequalityVar(
      String predicate, java.util.regex.Matcher inequalityVar) {
    Match match;
    // Get the variable from the last group of the regular expression.
    String variable = inequalityVar.group(3);
    // Get the symbol from the regular expression.
    String relation = inequalityVar.group(2);
    // Now we have the variable name, but who is it in the code? We'll have to find it.
    if (relation == null || relation.equals("=")) {
      match = new Match("==" + "{" + variable + "}", null);
    } else {
      match = new Match(relation + "{" + variable + "}", null);
    }
    if (predicate.contains(variable + ".")) {
      match.completeExpression(predicate.substring(predicate.indexOf(".")));
      match.setNullDereferenceCheck("{" + variable + "}" + "!=null");
    }
    return match;
  }

  /**
   * Returns the translation of predicate matching the inequalityNumber regex
   *
   * @param inequalityNumber the matching regex
   * @return the translation
   */
  private static String manageInequalityNumber(java.util.regex.Matcher inequalityNumber) {
    String predicateTranslation;
    // Get the number from the last group of the regular expression.
    String numberString = inequalityNumber.group(3);
    // Get the symbol from the regular expression.
    String relation = inequalityNumber.group(2);
    String numberWord = numberWordToDigit(numberString);

    int intNumber = 0;
    float floatNumber = 0;
    boolean isIntNumber = false;

    if (!numberString.contains(".")) { // the number is an int
      intNumber =
          (!numberWord.equals(""))
              ? intNumber = Integer.parseInt(numberWord)
              : Integer.parseInt(numberString);

      isIntNumber = true;
    } else {
      floatNumber = Float.parseFloat(numberString);
    }
    // relation is null in predicates without inequalities. For example "is 0".
    if (relation == null || relation.equals("=")) {
      predicateTranslation = (isIntNumber) ? ("==" + intNumber) : ("==" + floatNumber);
    } else {
      predicateTranslation = (isIntNumber) ? (relation + intNumber) : (relation + floatNumber);
    }
    return predicateTranslation;
  }

  /**
   * Returns the translation of predicate matching the isNotPattern regex
   *
   * @param isNotPattern the matching regex
   * @return the translation
   */
  private static String manageIsNotPattern(java.util.regex.Matcher isNotPattern) {
    String predicateTranslation;
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
    return predicateTranslation;
  }

  /**
   * Returns the translation of predicate matching the isPattern regex
   *
   * @param isPattern the matching regex
   * @return the translation
   */
  private static String manageIsPattern(java.util.regex.Matcher isPattern) {
    String predicateTranslation;
    String lastWord = isPattern.group(isPattern.groupCount());
    switch (lastWord) {
      case "true":
      case "false":
      case "null":
        predicateTranslation = "==" + lastWord;
        break;
      case "this":
        predicateTranslation =
            "==" + Configuration.RECEIVER; // The receiver object in the generated aspects.
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
