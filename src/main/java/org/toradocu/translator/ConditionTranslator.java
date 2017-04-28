package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.Tag;
import org.toradocu.extractor.ThrowsTag;

/**
 * ConditionTranslator translates exception comments in method documentation to Java expressions.
 * The entry point is {@link #translate(List)}.
 */
public class ConditionTranslator {

  private static final Logger log = LoggerFactory.getLogger(ConditionTranslator.class);

  /**
   * Translates throws an param tags in the given methods. This method sets the field {@code
   * AbstractTag.condition} for each tag in the given methods.
   *
   * @param methods a list of {@code DocumentedMethod}s whose throws tags to translate
   */
  public static void translate(List<DocumentedMethod> methods) {
    for (DocumentedMethod method : methods) {
      for (ThrowsTag tag : method.throwsTags()) processTag(tag, method);
      for (ParamTag tag : method.paramTags()) processTag(tag, method);
      if (method.returnTag() != null) processTag(method.returnTag(), method);
    }
  }

  /**
   * Takes a comment as a String and returns a list of {@code PropositionSeries} objects, one for
   * each sentence in the comment.
   *
   * @param comment the text of a Javadoc comment
   * @param method the DocumentedMethod under analysis
   * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
   */
  private static List<PropositionSeries> getPropositionSeries(
      String comment, DocumentedMethod method) {
    comment = addPlaceholders(comment);
    List<PropositionSeries> result = new ArrayList<>();

    for (SemanticGraph semanticGraph : StanfordParser.getSemanticGraphs(comment, method))
      result.add(new SentenceParser(semanticGraph).getPropositionSeries());

    return removePlaceholders(result);
  }

  /**
   * Replaces inequalities (e.g. "< 3", ">= 42") with placeholder text that can be more easily
   * parsed.
   *
   * @param text the text containing inequalities
   * @return text with inequalities replaced by placeholders
   */
  private static String addPlaceholders(String text) {
    // Replace written out inequalities with symbols.
    text =
        text.replace("greater than or equal to", ">=")
            .replace("≥", ">=")
            .replace("less than or equal to", "<=")
            .replace("lesser than or equal to", "<=")
            .replace("lesser or equal to", "<=")
            .replace("≤", "<=")
            .replace("greater than", ">")
            .replace("smaller than or equal to", "<=")
            .replace("smaller than", "<")
            .replace("less than", "<")
            .replace("lesser than", "<")
            .replace("equal to", "==");

    java.util.regex.Matcher matcher = Pattern.compile(INEQUALITY_NUMBER_REGEX).matcher(text);

    java.util.regex.Matcher matcherInstanceOf = Pattern.compile(INEQ_INSOF).matcher(text);

    java.util.regex.Matcher matcherThis = Pattern.compile(INEQ_THIS).matcher(text);

    java.util.regex.Matcher matcherVarComp = Pattern.compile(INEQUALITY_VAR_REGEX).matcher(text);

    while (matcherInstanceOf.find()) {
      // Instance of added to the comparator list
      // Replace "[an] instance of" with "instanceof"
      text = text.replaceFirst(INEQ_INSOF, " instanceof");
    }

    java.util.regex.Matcher matcherIOfProcessed =
        Pattern.compile(INEQ_INSOFPROCESSED).matcher(text);
    String placeholderText = text;
    int i = 0;

    while (matcherIOfProcessed.find()) {
      // Specific case for the instance of placeholder. We put into inequalities the instanceof and
      // the name of the class.
      inequalities.add(text.substring(matcherIOfProcessed.start(), matcherIOfProcessed.end()));
      placeholderText = placeholderText.replaceFirst(INEQ_INSOFPROCESSED, PLACEHOLDER_PREFIX + i++);
    }

    while (matcherThis.find()) {
      inequalities.add(text.substring(matcherThis.start(), matcherThis.end()));
      placeholderText = placeholderText.replaceFirst(INEQ_THIS, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    while (matcher.find()) {
      inequalities.add(text.substring(matcher.start(), matcher.end()));
      placeholderText =
          placeholderText.replaceFirst(INEQUALITY_NUMBER_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    while (matcherVarComp.find()) {
      inequalities.add(text.substring(matcherVarComp.start(), matcherVarComp.end()));
      placeholderText = placeholderText.replaceFirst(INEQUALITY_VAR_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    return placeholderText;
  }

  /**
   * Verifies if the comment contains a verb among the {@code possibleVerbs}. If it doesn't, the
   * verb is assumed to be "is" and is added to the comment.
   *
   * @param placeholderText the comment text containing placeholders
   * @param i counter of the placeholders in the text
   * @return the placeholderText, updated with the added verb or as it was if it already had one
   */
  private static String findVerb(String placeholderText, int i) {
    // Verbs that could appear before (the inequality, or the keyword this, etc.).
    //One of these most be present and will be added otherwise.
    String[] possibleVerbs = {"is", "is not", "isn't", "are", "are not", "aren't"};
    boolean containsVerb = false;
    for (String possibleVerb : possibleVerbs) {
      if (placeholderText.contains(possibleVerb + PLACEHOLDER_PREFIX + i)) {
        containsVerb = true;
        break;
      }
    }
    if (!containsVerb) {
      // The verb is assumed to be "is" and will be added to the text.
      placeholderText =
          placeholderText.replaceFirst(PLACEHOLDER_PREFIX + i, " is" + PLACEHOLDER_PREFIX + i);
    }
    return placeholderText;
  }

  private static final String INEQUALITY_NUMBER_REGEX =
      " *((([<>=]=?)|(!=)) ?)-?([0-9]+(?!/)(.[0-9]+)?|zero|one|two|three|four|five|six|seven|eight|nine)";
  private static final String INEQUALITY_VAR_REGEX =
      " *((([<>=]=?)|(!=)) ?)(?!this)((([a-zA-Z]+([0-9]?))+_?(?! ))+(.([a-zA-Z]+([0-9]?))+(\\(*\\))?)?)";
  private static final String ARITHMETIC_OP_REGEX =
      "(([a-zA-Z]+[0-9]?_?)+) ?([-+*/%]) ?(([a-zA-Z]+[0-9]?_?)+)";
  private static final String PLACEHOLDER_PREFIX = " INEQUALITY_";
  private static final String INEQ_INSOF = " *[an]* (instance of)"; // e.g "an instance of"
  private static final String INEQ_INSOFPROCESSED =
      " instanceof +[^ \\.]*"; // e.g. "instanceof BinaryMutation"
  private static final String INEQ_THIS = " this\\."; // e.g "<object> is this."

  /** Stores the inequalities that are replaced by placeholders when addPlaceholders is called. */
  private static List<String> inequalities = new ArrayList<>();

  /**
   * Returns a new list of {@code PropositionSeries} in which any placeholder text has been replaced
   * by the original inequalities. Original inequalities that were written out (e.g. "less than")
   * are replaced by their symbolic equivalent (e.g. "<").
   *
   * @param seriesList the list of {@code PropositionSeries} containing placeholder text
   * @return a new list of {@code PropositionSeries} with placeholders replaced by inequalities
   */
  private static List<PropositionSeries> removePlaceholders(List<PropositionSeries> seriesList) {
    List<PropositionSeries> result = new ArrayList<>();

    for (PropositionSeries series : seriesList) {
      List<Proposition> inequalityPropositions = new ArrayList<>();
      for (Proposition placeholderProposition : series.getPropositions()) {
        Subject subject = placeholderProposition.getSubject();
        String subjectAsString = subject.getSubject();
        String predicate = placeholderProposition.getPredicate();

        for (int i = 0; i < inequalities.size(); i++) {
          subjectAsString = subjectAsString.replaceAll(PLACEHOLDER_PREFIX + i, inequalities.get(i));
          predicate = predicate.replaceAll(PLACEHOLDER_PREFIX + i, inequalities.get(i));
        }
        subject.setSubject(subjectAsString); // Replace subject string representation.

        inequalityPropositions.add(
            new Proposition(subject, predicate, placeholderProposition.isNegative()));
      }
      result.add(new PropositionSeries(inequalityPropositions, series.getConjunctions()));
    }

    inequalities.clear();
    return result;
  }

  /**
   * Translates the {@code Proposition}s in the given {@code propositionSeries} to Java expressions.
   *
   * @param propositionSeries the {@code Proposition}s to translate into Java expressions
   * @param method the method the containing the Javadoc comment from which the {@code
   *     propositionSeries} was extracted
   */
  private static void translatePropositions(
      PropositionSeries propositionSeries, DocumentedMethod method) {
    for (Proposition p : propositionSeries.getPropositions()) {
      Set<CodeElement<?>> subjectMatches;
      subjectMatches = Matcher.subjectMatch(p.getSubject().getSubject(), method);
      if (subjectMatches.isEmpty()) {
        log.debug("Failed subject translation for: " + p);
        return;
      }
      final Set<CodeElement<?>> matchingCodeElements = new LinkedHashSet<>();
      String loop = findMatchingCodeElements(p, subjectMatches, method, matchingCodeElements);
      if (loop.equals(LOOP_RETURN)) return;
      if (loop.equals(LOOP_CONTINUE)) continue;

      // Maps each subject code element to the Java expression translation that uses that code
      // element.
      Map<CodeElement<?>, String> translations = new LinkedHashMap<>();
      for (CodeElement<?> subjectMatch : matchingCodeElements) {
        String currentTranslation =
            Matcher.predicateMatch(method, subjectMatch, p.getPredicate(), p.isNegative());
        if (currentTranslation == null) {
          log.trace("Failed predicate translation for: " + p);
          continue;
        }
        if (currentTranslation.contains("{") && currentTranslation.contains("}")) {
          String argument =
              currentTranslation.substring(
                  currentTranslation.indexOf("{") + 1, currentTranslation.indexOf("}"));

          Set<CodeElement<?>> argMatches;
          argMatches = Matcher.subjectMatch(argument, method);
          if (argMatches.isEmpty()) {
            log.trace("Failed predicate translation for: " + p + " due to variable not found.");
            continue;
          } else {
            Iterator<CodeElement<?>> it = argMatches.iterator();
            String replaceTarget = "{" + argument + "}";
            //Naive solution: picks the first match from the list.
            String replacement = it.next().getJavaExpression();
            currentTranslation = currentTranslation.replace(replaceTarget, replacement);
          }
        }
        translations.put(subjectMatch, currentTranslation);
      }

      if (translations.isEmpty()) {
        // Predicate match failed for every subject match.
        return;
      }

      // The final translation.
      String result = null;

      Conjunction conjunction = getConjunction(p.getSubject());
      if (conjunction != null) {
        // A single subject can refer to multiple elements (e.g., in "either value is null").
        // Therefore, translations for each subject code element should be merged using the
        // appropriate conjunction.
        for (String translation : translations.values()) {
          if (result == null) {
            result = translation;
          } else {
            result += conjunction + translation;
          }
        }
      } else { // Only one of the matching subjects should be used.
        CodeElement<?> match = null;

        // Sort matching subjects according to their priorities (defined in CodeElement#compareTo).
        List<CodeElement<?>> matchingSubjects = new ArrayList<>();
        matchingSubjects.addAll(translations.keySet());
        matchingSubjects.sort(Collections.reverseOrder());
        // Get all the matching subjects with the same priority (i.e., of the same type).
        final List<CodeElement<?>> samePriorityElements =
            matchingCodeElements
                .stream()
                .filter(c -> matchingSubjects.get(0).getClass().equals(c.getClass()))
                .collect(Collectors.toList());
        // Get the first matching subject tagged with {@code} or the first at all.
        for (CodeElement<?> matchingSubject : samePriorityElements) {
          // If the indecision is between two subject matches that are absolutely equal
          // candidates, then the priority goes to the one which is also a {@code} tag in the
          // method's Javadoc: isTaggedAsCode checks this property.
          boolean isTaggedAsCode = false;
          for (ThrowsTag throwTag : method.throwsTags()) {
            isTaggedAsCode = throwTag.intersect(new ArrayList<>(matchingSubject.getIdentifiers()));
          }
          if (isTaggedAsCode) {
            match = matchingSubject;
            break;
          }
        }
        if (match == null) {
          match = samePriorityElements.get(0);
        }
        result = translations.get(match);
      }

      if (result == null) {
        log.warn("Failed translation for proposition " + p);
        p.setTranslation("");
      } else {
        log.trace("Translated proposition " + p + " as: " + result);
        p.setTranslation(result);
      }
    }
  }

  /**
   * Find a set of {@code CodeElement}s that match the subject of the {@code Proposition} relative
   * to the {@code DocumentedMethod}, updating the set {@code matchingCodeElements}.
   *
   * @param p the proposition
   * @param subjectMatches CodeElements matches for subject
   * @param method the DocumentedMethod under analysis
   * @param matchingCodeElements the set of matching CodeElements to update
   * @return a String defining whether the loop in the method translatePropositions has to continue
   *     to the next iteration (LOOP_CONTINUE), to stop (LOOP_RETURN) or go on executing the rest of
   *     the body (LOOP_OK)
   */
  private static String findMatchingCodeElements(
      Proposition p,
      Set<CodeElement<?>> subjectMatches,
      DocumentedMethod method,
      Set<CodeElement<?>> matchingCodeElements) {
    final String container = p.getSubject().getContainer();
    if (container.isEmpty()) {
      // Subject match
      subjectMatches = Matcher.subjectMatch(p.getSubject().getSubject(), method);
      if (subjectMatches.isEmpty()) {
        log.debug("Failed subject translation for: " + p);
        return LOOP_RETURN;
      }
      matchingCodeElements.addAll(subjectMatches);
    } else {
      // Container match
      final CodeElement<?> containerMatch = Matcher.containerMatch(container, method);
      if (containerMatch == null) {
        log.trace("Failed container translation for: " + p);
        matchingCodeElements.clear();
        return LOOP_CONTINUE;
      }
      try {
        matchingCodeElements.add(
            new ContainerElementsCodeElement(
                containerMatch.getJavaCodeElement(), containerMatch.getJavaExpression()));
      } catch (IllegalArgumentException e) {
        // The containerMatch is not supported by the current implementation of
        // ContainerElementsCodeElement.
        matchingCodeElements.clear();
        return LOOP_CONTINUE;
      }
    }
    return LOOP_OK;
  }

  private static final String LOOP_OK = "OK";
  private static final String LOOP_CONTINUE = "continue";
  private static final String LOOP_RETURN = "return";
  /**
   * Returns a boolean Java expression that merges the conditions from the given set of conditions.
   * Each condition in the set is combined using an || conjunction.
   *
   * @param conditions the translated conditions for a throws tag (as Java boolean conditions)
   * @return a boolean Java expression that is true only if any of the given conditions is true
   */
  private static String mergeConditions(Set<String> conditions) {
    conditions.removeIf(String::isEmpty);
    if (conditions.size() == 0) {
      return "";
    } else if (conditions.size() == 1) {
      return conditions.iterator().next();
    } else {
      Iterator<String> it = conditions.iterator();
      StringBuilder conditionsBuilder = new StringBuilder("(" + it.next() + ")");
      while (it.hasNext()) {
        conditionsBuilder.append(Conjunction.OR + "(" + it.next() + ")");
      }
      return conditionsBuilder.toString();
    }
  }

  /**
   * Returns the conjunction that should be used to form the translation for a {@code Proposition}
   * with the given subject. Returns null if no conjunction should be used.
   *
   * @param subject the subject of the {@code Proposition}
   * @return the conjunction that should be used to form the translation for the {@code Proposition}
   *     with the given subject or null if no conjunction should be used
   */
  private static Conjunction getConjunction(Subject subject) {
    String subjectAsString = subject.getSubject().toLowerCase();
    if (subjectAsString.startsWith("either ") || subjectAsString.startsWith("any ")) {
      return Conjunction.OR;
    } else if (subjectAsString.startsWith("both ") || subjectAsString.startsWith("all ")) {
      return Conjunction.AND;
    } else if (!subject.isSingular()) {
      return Conjunction.OR;
    } else {
      return null;
    }
  }

  /**
   * Method that process the comment in the tag and extracts the propositions from it.
   *
   * @param tag the tag provided by the method. Must not be null.
   * @param method the method that contains the tag to analyze. Must not be null.
   */
  private static void processTag(Tag tag, DocumentedMethod method) {

    log.trace(
        "Identifying propositions from: \"" + tag.getComment() + "\" in " + method.getSignature());

    String comment = tag.getComment().trim();

    // Add end-of-sentence period, if missing.
    if (!comment.endsWith(".")) {
      comment += ".";
    }

    comment = normalizeComment(comment, method);

    // Remove commas from the comment if enabled. (Do not remove commas when dealing with @return.)
    if (Toradocu.configuration != null
        && Toradocu.configuration.removeCommas()
        && !tag.getKind().equals(Tag.Kind.RETURN)) {
      comment = comment.replace(",", " ");
    }

    // Comment preprocessing for @throws tags.
    if (tag.getKind() == Tag.Kind.THROWS) {
      comment = removeInitial(comment, "if");
    }

    // Comment preprocessing for @param tags.
    if (tag.getKind() == Tag.Kind.PARAM) {
      //We're not interested on "may be null" conditions.
      if (comment.contains("may be null")) {
        comment = comment.replaceAll("may be null", "");
      }

      String parameterName = ((ParamTag) tag).parameter().getName();
      String[] patterns = {
        "must be",
        "must not be",
        "will be",
        "will not be",
        "can't be",
        "cannot be",
        "should be",
        "should not be",
        "shouldn't be",
        "may not be",
        "Must be",
        "Must not be",
        "Will be",
        "Will not be",
        "Can't be",
        "Cannot be",
        "Should be",
        "Should not be",
        "Shouldn't be",
        "May not be"
      };
      java.util.regex.Matcher matcher = Pattern.compile("\\(.*").matcher(comment);
      String separator = matcher.find() ? " " : ".";
      boolean noReplacedYet = true; //Tells if there was already a replacement in the phrase
      for (String pattern : patterns) {
        if (comment.contains(pattern)) {
          String replacement = separator + parameterName + " " + pattern;
          comment = comment.replace(pattern, replacement);
          noReplacedYet = false;
        }
      }

      String[] patternsWithoutVerb = {"not null"};
      if (noReplacedYet) { //Looks for the other patterns.
        for (String pattern : patternsWithoutVerb) {
          String replacement = ". " + parameterName + " is " + pattern;
          comment = comment.replace(pattern, replacement);
        }
      }
    }

    if (tag.getKind() == Tag.Kind.PARAM || tag.getKind() == Tag.Kind.THROWS) {
      // Identify propositions in the comment. Each sentence in the comment is parsed into a
      // PropositionSeries.
      List<PropositionSeries> extractedPropositions = getPropositionSeries(comment, method);
      Set<String> conditions = new LinkedHashSet<>();
      // Identify Java code elements in propositions.
      for (PropositionSeries propositions : extractedPropositions) {
        translatePropositions(propositions, method);
        conditions.add(propositions.getTranslation());
      }
      tag.setCondition(mergeConditions(conditions));
    }

    // @return tag translation.
    if (tag.getKind() == Tag.Kind.RETURN) {
      // Assumption: the comment is composed of a single sentence. We should probably split multiple
      // sentence comments using the Stanford Parser, and then work on each single sentence.
      String translation = "";

      // Split the sentence in three parts: predicate + true case + false case.
      // TODO Naive splitting. Make the split more reliable.
      final int predicateSplitPoint = comment.indexOf(" if ");
      if (predicateSplitPoint != -1) {
        translation = returnStandardPattern(method, comment, predicateSplitPoint);
      } else {
        final String[] truePatterns = {"true", "true always"};
        final String[] falsePatterns = {"false", "false always"};
        final String commentToTranslate = comment;

        // Comments always end with a period (added by Toradocu where missing). Therefore, we
        // have to add period(s) here.
        final boolean truePatternsMatch =
            Arrays.stream(truePatterns)
                .map(p -> p.concat("."))
                .anyMatch(p -> p.equalsIgnoreCase(commentToTranslate));
        final boolean falsePatternsMatch =
            Arrays.stream(falsePatterns)
                .map(p -> p.concat("."))
                .anyMatch(p -> p.equalsIgnoreCase(commentToTranslate));

        if (truePatternsMatch) {
          translation = "true ? result==true";
        } else if (falsePatternsMatch) {
          translation = "true ? result==false";
        } else {
          translation = manageArithmeticOperation(method, commentToTranslate);
          if (translation.equals("")) {
            // All the previous attempts failed: try the last strategies (e.g. search for missing subjects)
            String match = lastAttemptMatch(method, comment);
            if (match != null) {
              if (match.contains("result")) translation = "true ?" + match;
              else translation = "true ? result.equals(" + match + ")";
            }
          }
        }
      }

      tag.setCondition(translation);
    }
  }

  /**
   * Translate arithemtic operations between arguments, if found.
   *
   * @param method the DocumentedMethod
   * @param commentToTranslate String comment to translate
   * @return the translation
   */
  private static String manageArithmeticOperation(
      DocumentedMethod method, String commentToTranslate) {
    String translation = "";
    java.util.regex.Matcher matcherOp =
        Pattern.compile(ARITHMETIC_OP_REGEX).matcher(commentToTranslate);
    if (matcherOp.find()) {
      String firstFactor = matcherOp.group(1);
      String secFactor = matcherOp.group(4);
      String op = matcherOp.group(3);
      int firstIndex = searchForCode(firstFactor, method);
      if (firstIndex != -1) {
        int secIndex = searchForCode(secFactor, method);
        if (secIndex != -1)
          translation = "true ? result==args[" + firstIndex + "]" + op + "args[" + secIndex + "]";
      }
    }
    return translation;
  }

  /**
   * This method attempts to translate the return tag according to the classical pattern.
   *
   * @param method the DocumentedMethod
   * @param comment the String comment to translate
   * @param predicateSplitPoint index of the "if"
   * @return the translation computed
   */
  private static String returnStandardPattern(
      DocumentedMethod method, String comment, int predicateSplitPoint) {
    String translation = "";
    if (comment.contains(";")) comment = comment.replace(";", ",");
    String predicate = comment.substring(0, predicateSplitPoint);
    final String[] tokens = comment.substring(predicateSplitPoint + 3).split(",", 2);
    String trueCase = tokens[0];
    String falseCase = tokens.length > 1 ? tokens[1] : "";

    if (!predicate.isEmpty() && !trueCase.isEmpty()) {
      //          try {
      String predicateTranslation = translateFirstPart(predicate, method);
      if (predicateTranslation != null) {
        String conditionTranslation = translateSecondPart(trueCase, method);

        if (!predicateTranslation.isEmpty() && !conditionTranslation.isEmpty()) {
          translation = conditionTranslation + " ? " + predicateTranslation;
          // Else case might not be present.
          String elsePredicate = translateLastPart(falseCase, method);
          if (elsePredicate != null) {
            translation = translation + " : " + elsePredicate;
          }
        }
      }
      /* To validate the return values of the method under test we will have a code similar
       * to this in the generated aspect:
       *
       * if ( condition ) {
       *   return check;
       * else {
       *   return anotherCheck;
       * }
       *
       * Example: @return true if the values are equal.
       * translation: args[0].equals(args[1]) ? true
       *
       * if (args[0].equals(args[1])) {
       *   return result.equals(true);
       * }
       *
       * We use the characters '?' and ':' to separate the different parts of the translation.
       * We need that because the translation is currently a plain a String.
       */
      //          }
      //          catch (IllegalArgumentException e) {
      //            //TODO: Change the exception with one more meaningful.
      //            // Pattern not supported.
      //            log.warn("Unable to translate: \"" + comment + "\"");
      //          }

      else {
        String match = lastAttemptMatch(method, comment);
        if (match != null) translation = match;
        else translation = "";
      }
    }
    return translation;
  }

  /**
   * Replace some common expressions in the comment with other standard easier to translate
   * correctly.
   *
   * @param comment the String comment to sanitize
   * @param method the DocumentedMethod
   * @return the normalized comment
   */
  private static String normalizeComment(String comment, DocumentedMethod method) {
    if (comment.contains("if and only if")) comment = comment.replace("if and only if", "if");

    if (comment.contains("iff")) comment = comment.replace("iff", "if");

    if (comment.contains("non-null")) comment = comment.replace("non-null", "!=null");

    if (comment.contains("non-empty")) comment = comment.replace("non-empty", "!=empty");

    // "it" would be translated as a standalone subject, but more probably it is referred to another more meaningful one:
    // probably a previous mentioned noun.
    if (comment.contains(" it ")) {
      for (SemanticGraph sg : StanfordParser.getSemanticGraphs(comment, method)) {
        List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
        if (!nouns.isEmpty()) {
          IndexedWord boh = nouns.stream().findFirst().get();
          comment = comment.replace(" it ", " " + boh.word() + " ");
        }
      }
    }

    return comment;
  }

  /**
   * Called if all the previous stantard tentatives of matching failed.
   *
   * @param method the DocumentedMethod under analysis
   * @param comment the comment to translate
   * @return a match if found, otherwise null
   */
  private static String lastAttemptMatch(DocumentedMethod method, String comment) {
    //Try a match looking at the semantic graph.
    String match = null;
    comment = comment.replace(";", "").replace(",", "");
    for (SemanticGraph sg : StanfordParser.getSemanticGraphs(comment, method)) {
      //First: search for a verb.
      List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
      if (!verbs.isEmpty()) {
        List<PropositionSeries> extractedPropositions = getPropositionSeries(comment, method);
        for (PropositionSeries prop : extractedPropositions) {
          Set<String> conditions = new LinkedHashSet<>();
          for (Proposition p : prop.getPropositions()) {
            match =
                Matcher.predicateMatch(
                    method, new GeneralCodeElement("result"), p.getPredicate(), p.isNegative());
            if (match != null) break;
          }
        }
      } else { // No verb found: process nouns and their adjectives
        List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
        List<IndexedWord> adj = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
        if (match == null) {
          String wordToMatch = "";
          for (IndexedWord n : nouns) {
            for (IndexedWord a : adj) wordToMatch += a.word();
            wordToMatch += n.word();
            Set<CodeElement<?>> subject = Matcher.subjectMatch(wordToMatch, method);
            if (!subject.isEmpty()) match = subject.stream().findFirst().get().getJavaExpression();
          }
        }
      }
    }
    return match;
  }

  /**
   * Translates the given {@code text} that is the second part of an @return Javadoc comment. Here
   * "second part" means every word of the conditional clause. Example: {@code @return true if the
   * parameter is positive, false otherwise}. In this comment the second part is "if the parameter
   * is positive, false otherwise". This method translates only the "false otherwise" part.
   *
   * @param text words representing the second part of an @return comment
   * @param method the method to which the @return tag belongs to
   * @return the translation of the given {@code text}
   */
  private static String translateLastPart(String text, DocumentedMethod method) {
    final String lowerCaseText = text.toLowerCase();
    if (lowerCaseText.contains("true")) {
      return "result == true";
    } else if (lowerCaseText.contains("false")) {
      return "result == false";
    } else {
      String[] splittedText = text.split(" ");
      for (int i = 0; i < splittedText.length; i++) {
        int index = searchForCode(splittedText[i], method);
        if (index != -1) return "result == args[" + index + "]";
        //the empty String was found
        else if (splittedText[i].equals("\"\"")) return "result.equals(\"\")";
      }
    }
    return null;
  }

  /**
   * Translates the given {@code text} that is the second part of an @return Javadoc comment. Here
   * "second part" means every word of the conditional clause. Example: {@code @return true if the
   * parameter is null}. In this comment the second part is "if the parameter is null".
   *
   * @param text words representing the second part of an @return comment
   * @param method the method to which the @return tag belongs to
   * @return the translation of the given {@code text}
   */
  private static String translateSecondPart(String text, DocumentedMethod method) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.

    text = removeInitial(text, "if");
    List<PropositionSeries> extractedPropositions = getPropositionSeries(text, method);
    Set<String> conditions = new LinkedHashSet<>();
    // Identify Java code elements in propositions.
    for (PropositionSeries propositions : extractedPropositions) {
      translatePropositions(propositions, method);
      conditions.add(propositions.getTranslation());
    }
    return mergeConditions(conditions);
  }

  /**
   * Translates the given {@code text} that is the first part of an @return Javadoc comment. Here
   * "first part" means every word before the start of the conditional clause. Example:
   * {@code @return true if the parameter is null}. In this comment the first part is "true".
   *
   * @param text words representing the first part of an @return comment
   * @param method the DocumentedMethod the @return comment belongs to
   * @return the translation of the given {@code text}
   * @throws IllegalArgumentException if the given {@code text} cannot be translated
   */
  private static String translateFirstPart(String text, DocumentedMethod method) {
    String lowerCaseText = text.trim().toLowerCase();
    String match = null;
    switch (lowerCaseText) {
      case "true":
      case "false":
        return "result == " + lowerCaseText;
      default:
        {
          int index = searchForCode(text, method);
          if (index != -1) return "result == args[" + index + "]";
          else {
            match = lastAttemptMatch(method, text);
            if (match != null) {
              if (!match.contains("result==")) return "result.equals(" + match + ")";
              else return match;
            }
          }
        }
    }
    //TODO: Change the exception with one more meaningful.
    //    throw new IllegalArgumentException(text + " cannot be translated: Pattern not supported");
    return match;
  }

  private static int searchForCode(String text, DocumentedMethod method) {
    List<String> arguments =
        method.getParameters().stream().map(Parameter::getName).collect(toList());
    for (int i = 0; i < arguments.size(); i++) if (arguments.get(i).equals(text)) return i;

    return -1;
  }

  /**
   * Removes one or more occurrences of {@code wordToRemove} at the beginning of {@code text}. This
   * method ignores the case of wordToRemove, i.e., occurrences are searched and removed ignoring
   * the case.
   *
   * @param text string from which remove occurences of {@code wordToRemove}
   * @param wordToRemove word to remove from {@code text}
   * @return the given {@code text} with initial occurrences of {@code wordToRemove} deleted
   */
  private static String removeInitial(String text, String wordToRemove) {
    wordToRemove = wordToRemove.toLowerCase();
    String lowerCaseComment = text.toLowerCase();
    while (lowerCaseComment.startsWith(wordToRemove + " ")) {
      text = text.substring(wordToRemove.length() + 1);
      lowerCaseComment = text.toLowerCase();
    }
    return text;
  }
}
