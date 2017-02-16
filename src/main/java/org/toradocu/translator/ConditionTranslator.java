package org.toradocu.translator;

import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.ArrayList;
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
   * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
   */
  private static List<PropositionSeries> getPropositionSeries(String comment) {
    comment = addPlaceholders(comment);
    List<PropositionSeries> result = new ArrayList<>();
    for (SemanticGraph semanticGraph : StanfordParser.getSemanticGraphs(comment)) {
      result.add(new SentenceParser(semanticGraph).getPropositionSeries());
    }

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
            .replace("less than or equal to", "<=")
            .replace("greater than", ">")
            .replace("smaller than or equal to", "<=")
            .replace("smaller than", "<")
            .replace("less than", "<")
            .replace("equal to", "==");

    java.util.regex.Matcher matcher = Pattern.compile(INEQUALITY_NUMBER_REGEX).matcher(text);

    java.util.regex.Matcher matcherInstanceOf = Pattern.compile(INEQ_INSOF).matcher(text);

    java.util.regex.Matcher matcherThis = Pattern.compile(INEQ_THIS).matcher(text);

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
      // Verbs that could appear before the keyword this. One of these most be present
      // and will be added otherwise.
      String[] possibleVerbs = {"is", "is not", "isn't"};
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
      i++;
    }

    while (matcher.find()) {
      inequalities.add(text.substring(matcher.start(), matcher.end()));
      placeholderText =
          placeholderText.replaceFirst(INEQUALITY_NUMBER_REGEX, PLACEHOLDER_PREFIX + i);
      // Verbs that could appear before the inequality. One of these most be present
      // and will be added otherwise.
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
      i++;
    }

    return placeholderText;
  }

  private static final String INEQUALITY_NUMBER_REGEX = " *(([<>=]=?)|(!=)) ?-?[0-9]+";
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
      final String container = p.getSubject().getContainer();
      if (container.isEmpty()) {
        // Subject match
        subjectMatches = Matcher.subjectMatch(p.getSubject().getSubject(), method);
        if (subjectMatches.isEmpty()) {
          log.debug("Failed subject translation for: " + p);
          return;
        }
        matchingCodeElements.addAll(subjectMatches);
      } else {
        // Container match
        final CodeElement<?> containerMatch = Matcher.containerMatch(container, method);
        if (containerMatch == null) {
          log.trace("Failed container translation for: " + p);
          continue;
        }
        try {
          matchingCodeElements.add(
              new ContainerElementsCodeElement(
                  containerMatch.getJavaCodeElement(), containerMatch.getJavaExpression()));
        } catch (IllegalArgumentException e) {
          // The containerMatch is not supported by the current implementation of
          // ContainerElementsCodeElement.
          continue;
        }
      }

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
            isTaggedAsCode = throwTag.intersect(matchingSubject.getIdentifiers());
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

      log.trace("Translated proposition " + p + " as: " + result);
      p.setTranslation(result);
    }
  }

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

    // Remove commas from the comment if enabled.
    if (Toradocu.configuration != null && Toradocu.configuration.removeCommas()) {
      comment = comment.replace(",", " ");
    }

    // Comment preprocessing for @throws tags.
    if (tag.getKind() == Tag.Kind.THROWS) {
      String lowerCaseComment = comment.toLowerCase();
      while (lowerCaseComment.startsWith("if ")) {
        comment = comment.substring(3); // Remove initial "if"s.
        lowerCaseComment = comment.toLowerCase();
      }
    }

    // Comment preprocessing for @param tags.
    if (tag.getKind() == Tag.Kind.PARAM) {
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
      for (String pattern : patterns) {
        String replacement = separator + parameterName + " " + pattern;
        comment = comment.replace(pattern, replacement);
      }
    }

    if (tag.getKind() == Tag.Kind.PARAM || tag.getKind() == Tag.Kind.THROWS) {
      // Identify propositions in the comment. Each sentence in the comment is parsed into a
      // PropositionSeries.
      List<PropositionSeries> extractedPropositions = getPropositionSeries(comment);
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

      // 1. Split the sentence in two parts: predicate + condition.
      // TODO Naive splitting. Make the split more reliable.
      final int splitPoint = comment.indexOf(" if ");
      if (splitPoint == -1) {
        return;
      }
      String predicate = comment.substring(0, splitPoint);
      String condition = comment.substring(splitPoint + 3);

      if (!predicate.isEmpty() && !condition.isEmpty()) {
        try {
          String predicateTranslation = translateFirstPart(predicate);
          String conditionTranslation = translateSecondPart(condition, method);
          tag.setCondition(predicateTranslation + " @ " + conditionTranslation);

          /* To validate the return values of the method under test we will have a code similar
           * to this in the generated aspect:
           *
           * if ( conditionTranslation ) {
           *   return predicate;
           * else {
           *   ??? <= how to express this? We can use c ? x : y.
           * }
           *
           * Example: @return true if the values are equal.
           * predicate: true
           * condition: args[0].equals(args[1])
           * translation: true @ args[0].equals(args[1])
           *
           * if (args[0].equals(args[1])) {
           *   return result.equals(true);
           * }
           *
           * We use the character '@' to separate the two parts of the translation. We need that
           * because the translation is currently a plain a String.
           */
        } catch (IllegalArgumentException e) {
          //TODO: Change the exception with one more meaningful.
          // Pattern not supported.
          log.warn("Unable to translate: \"" + comment + "\"");
        }
      }
      // Else: do nothing. How can we support different forms of @return tags?
    }
  }

  private static String translateSecondPart(String text, DocumentedMethod method) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.
    List<PropositionSeries> extractedPropositions = getPropositionSeries(text);
    Set<String> conditions = new LinkedHashSet<>();
    // Identify Java code elements in propositions.
    for (PropositionSeries propositions : extractedPropositions) {
      translatePropositions(propositions, method);
      conditions.add(propositions.getTranslation());
    }
    return mergeConditions(conditions);
  }

  private static String translateFirstPart(String text) {
    String lowerCaseText = text.trim().toLowerCase();
    switch (lowerCaseText) {
      case "true":
      case "false":
        return "result.equals(" + lowerCaseText + ")";
    }
    //TODO: Change the exception with one more meaningful.
    throw new IllegalArgumentException(text + " cannot be translated: Pattern not supported");
  }
}
