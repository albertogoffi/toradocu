package org.toradocu.translator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ParamTag;
import org.toradocu.translator.spec.Specification;

public class ParamTranslator implements Translator<ParamTag> {

  @Override
  public Specification translate(ParamTag tag, DocumentedMethod excMember) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.
    List<PropositionSeries> extractedPropositions =
        PropositionSeries.create(tag.getComment(), excMember);
    Set<String> conditions = new LinkedHashSet<>();

    for (PropositionSeries propositions : extractedPropositions) {
      ConditionTranslator.translate(propositions, excMember);
      conditions.add(propositions.getTranslation()); // TODO Add only when translation is non-empty?
    }
    mergeConditions(conditions);

    // TODO Create the specification with the derived merged conditions.

    return null;
  }

  /**
   * Returns a boolean Java expression that merges the conditions from the given set of conditions.
   * Each condition in the set is combined using an || conjunction.
   *
   * @param conditions the translated conditions for a throws tag (as Java boolean conditions)
   * @return a boolean Java expression that is true only if any of the given conditions is true
   */
  private String mergeConditions(Set<String> conditions) {
    conditions.removeIf(String::isEmpty); // TODO Why should we have empty conditions here?

    String delimiter = " " + Conjunction.OR + " ";
    StringJoiner joiner = new StringJoiner(delimiter);
    for (String condition : conditions) {
      joiner.add("(" + condition + ")");
    }
    return joiner.toString();
  }
}
