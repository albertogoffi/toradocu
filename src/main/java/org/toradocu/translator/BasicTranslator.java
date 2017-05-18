package org.toradocu.translator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.Tag;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.translator.spec.ExcPostcondition;
import org.toradocu.translator.spec.Guard;
import org.toradocu.translator.spec.Precondition;

public class BasicTranslator {

  public static ExcPostcondition translate(ThrowsTag tag, DocumentedMethod excMember) {
    return new ExcPostcondition(
        new Guard(translateTag(tag, excMember)), tag.exception().toString());
  }

  public static Precondition translate(ParamTag tag, DocumentedMethod excMember) {
    return new Precondition(new Guard(translateTag(tag, excMember)));
  }

  private static String translateTag(Tag tag, DocumentedMethod excMember) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.
    List<PropositionSeries> extractedPropositions =
        PropositionSeries.create(tag.getComment(), excMember);
    Set<String> conditions = new LinkedHashSet<>();

    for (PropositionSeries propositions : extractedPropositions) {
      ConditionTranslator.translate(propositions, excMember);
      conditions.add(propositions.getTranslation()); // TODO Add only when translation is non-empty?
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
}
