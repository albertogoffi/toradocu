package org.toradocu.translator.preprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

// TODO This should be done right before the parsing not during preprocessing!
public class AddPlaceholders implements PreprocessingPhase {

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

  /** The regular expressions used to identify patterns in the comment */
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

  @Override
  public String run(Tag tag, DocumentedMethod excMember) {

    String text =
        tag.getComment()
            .replace("greater than or equal to", ">=")
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
}
