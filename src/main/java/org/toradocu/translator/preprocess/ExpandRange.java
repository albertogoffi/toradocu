package org.toradocu.translator.preprocess;

import java.util.regex.Pattern;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class ExpandRange implements PreprocessingPhase {

  private static final String SQUARE_BRACKETS_RANGE =
      "(not )?(lie )?(in|inside|outside) the .*(interval|range) (of )?\\[([0-9]+) ?,? ?([0-9]+)\\]";

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    java.util.regex.Matcher squareBracketsRange =
        Pattern.compile(SQUARE_BRACKETS_RANGE).matcher(comment);

    while (squareBracketsRange.find()) {
      int min = Integer.valueOf(squareBracketsRange.group(6));
      int max = Integer.valueOf(squareBracketsRange.group(7));
      String predicateTranslation = "";
      String negation = squareBracketsRange.group(1);
      String insideRange = squareBracketsRange.group(2);
      if (negation == null && insideRange != null) {
        comment = comment.replaceFirst(SQUARE_BRACKETS_RANGE, ">" + min + " and <" + max);
      } else {
        comment = comment.replaceFirst(SQUARE_BRACKETS_RANGE, "<" + min + " or >" + max);
      }
    }
    return comment;
  }
}
