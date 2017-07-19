package org.toradocu.translator.preprocess;

import java.util.regex.Pattern;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;

public class MustWillShouldCanPatterns implements PreprocessingPhase {

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    String parameterName = ((ParamTag) tag).getParameter().getName();
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
    return comment;
  }
}
