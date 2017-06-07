package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import java.util.*;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.Tag;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.translator.spec.ExcPostcondition;
import org.toradocu.translator.spec.Guard;
import org.toradocu.translator.spec.Precondition;

/**
 * The {@code BasicTranslator} class holds the {@code translate()} methods for {@code Tag} of kind
 * Param and Throws, since the translation process needed for this two tags is the same.
 */
public class BasicTranslator {

  public static ExcPostcondition translate(ThrowsTag tag, ExecutableMember excMember) {
    return new ExcPostcondition(
        new Guard(translateTag(tag, excMember)), tag.getException().toString());
  }

  public static Precondition translate(ParamTag tag, ExecutableMember excMember) {
    return new Precondition(new Guard(translateTag(tag, excMember)));
  }

  /**
   * Given a {@code Tag}, ask to {@code Parser} the list of {@code PropositionSeries} associated to
   * the tag comment, in order to compute a translation for each one.
   *
   * @param tag the {@code Tag} for which produce a translation
   * @param excMember the {@code ExecutableMember} the tag belongs to
   * @return a String representing the translation
   */
  private static String translateTag(Tag tag, ExecutableMember excMember) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.
    List<PropositionSeries> propositions = Parser.parse(tag.getComment(), excMember);
    Set<String> conditions = new LinkedHashSet<>();

    for (PropositionSeries props : propositions) {
      translate(props, excMember);
      conditions.add(props.getTranslation()); // TODO Add only when translation is non-empty?
    }
    return mergeConditions(conditions);
  }

  /**
   * Returns a boolean Java expression that merges the conditions from the given set of conditions.
   * Each condition in the set is combined using an || conjunction.
   *
   * @param conditions the translated conditions for a throws tag (as Java boolean conditions)
   * @return a boolean Java expression that is true only if any of the given conditions is true
   */
  private static String mergeConditions(Set<String> conditions) {
    conditions.removeIf(String::isEmpty); // TODO Why should we have empty conditions here?

    String delimiter = " " + Conjunction.OR + " ";
    StringJoiner joiner = new StringJoiner(delimiter);
    for (String condition : conditions) {
      joiner.add("(" + condition + ")");
    }
    return joiner.toString();
  }

  private static final String LOOP_OK = "OK";
  private static final String LOOP_CONTINUE = "continue";
  private static final String LOOP_RETURN = "return";

  /**
   * Translates the {@code Proposition}s in the given {@code propositionSeries} to Java expressions.
   *
   * @param propositionSeries the {@code Proposition}s to translate into Java expressions
   * @param method the method the containing the Javadoc comment from which the {@code
   *     propositionSeries} was extracted
   */
  public static void translate(PropositionSeries propositionSeries, ExecutableMember method) {
    Matcher matcher = new Matcher();
    for (Proposition p : propositionSeries.getPropositions()) {
      Set<CodeElement<?>> subjectMatches;
      subjectMatches = matcher.subjectMatch(p.getSubject().getSubject(), method);
      if (subjectMatches.isEmpty()) {
        //        ConditionTranslator.log.debug("Failed subject translation for: " + p);
        return;
      }
      final Set<CodeElement<?>> matchingCodeElements = new LinkedHashSet<>();
      String loop = findMatchingCodeElements(p, subjectMatches, method, matchingCodeElements);
      if (loop.equals(LOOP_RETURN)) {
        return;
      }
      if (loop.equals(LOOP_CONTINUE)) {
        continue;
      }

      // Maps each subject code element to the Java expression translation that uses that code
      // element.
      Map<CodeElement<?>, String> translations = new LinkedHashMap<>();
      for (CodeElement<?> subjectMatch : matchingCodeElements) {
        String currentTranslation =
            matcher.predicateMatch(method, subjectMatch, p.getPredicate(), p.isNegative());
        if (currentTranslation == null) {
          //          ConditionTranslator.log.trace("Failed predicate translation for: " + p);
          continue;
        }
        if (currentTranslation.contains("{") && currentTranslation.contains("}")) {
          String argument =
              currentTranslation.substring(
                  currentTranslation.indexOf("{") + 1, currentTranslation.indexOf("}"));

          Set<CodeElement<?>> argMatches;
          argMatches = matcher.subjectMatch(argument, method);
          if (argMatches.isEmpty()) {
            //            ConditionTranslator.log.trace("Failed predicate translation for: " + p + " due to variable not found.");
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
                .collect(toList());
        // Get the first matching subject tagged with {@code} or the first at all.
        for (CodeElement<?> matchingSubject : samePriorityElements) {
          // If the indecision is between two subject matches that are absolutely equal
          // candidates, then the priority goes to the one which is also a {@code} tag in the
          // method's Javadoc: isTaggedAsCode checks this property.
          boolean isTaggedAsCode = false;
          for (ThrowsTag throwTag : method.throwsTags()) {
            isTaggedAsCode =
                !throwTag
                    .getComment()
                    .getWordsMarkedAsCode()
                    .stream()
                    .filter(matchingSubject.getIdentifiers()::contains)
                    .collect(toList())
                    .isEmpty();
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
        //        ConditionTranslator.log.warn("Failed translation for proposition " + p);
        p.setTranslation("");
      } else {
        //        ConditionTranslator.log.trace("Translated proposition " + p + " as: " + result);
        p.setTranslation(result);
      }
    }
  }

  /**
   * Find a set of {@code CodeElement}s that match the subject of the {@code Proposition} relative
   * to the {@code ExecutableMember}, updating the set {@code matchingCodeElements}.
   *
   * @param p the proposition
   * @param subjectMatches CodeElements matches for subject
   * @param method the ExecutableMember under analysis
   * @param matchingCodeElements the set of matching CodeElements to update
   * @return a String defining whether the loop in the method translatePropositions has to continue
   *     to the next iteration (LOOP_CONTINUE), to stop (LOOP_RETURN) or go on executing the rest of
   *     the body (LOOP_OK)
   */
  private static String findMatchingCodeElements(
      Proposition p,
      Set<CodeElement<?>> subjectMatches,
      ExecutableMember method,
      Set<CodeElement<?>> matchingCodeElements) {
    Matcher matcher = new Matcher();
    final String container = p.getSubject().getContainer();
    if (container.isEmpty()) {
      // Subject match
      subjectMatches = matcher.subjectMatch(p.getSubject().getSubject(), method);
      if (subjectMatches.isEmpty()) {
        //        ConditionTranslator.log.debug("Failed subject translation for: " + p);
        return LOOP_RETURN;
      }
      matchingCodeElements.addAll(subjectMatches);
    } else {
      // Container match
      final CodeElement<?> containerMatch = matcher.containerMatch(container, method);
      if (containerMatch == null) {
        //        ConditionTranslator.log.trace("Failed container translation for: " + p);
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
}
