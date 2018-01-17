package org.toradocu.translator.preprocess;

import java.util.regex.Pattern;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class ExpandRange implements PreprocessingPhase {

  private static final String SQUARE_BRACKETS_RANGE =
      "(not )?(lie )?(in|inside|out|outside) the ([\\w\\s]+)?(interval|range) (of )?\\[([0-9]+) ?,? ?([0-9]+)\\]";

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    java.util.regex.Matcher squareBracketsRange =
        Pattern.compile(SQUARE_BRACKETS_RANGE).matcher(comment);

    while (squareBracketsRange.find()) {
      int min = Integer.valueOf(squareBracketsRange.group(7));
      int max = Integer.valueOf(squareBracketsRange.group(8));
      boolean negation = squareBracketsRange.group(1) != null;
      boolean insideRange =
          squareBracketsRange.group(3).equals("inside")
              || squareBracketsRange.group(3).equals("in");
      boolean outsideRange =
          squareBracketsRange.group(3).equals("outside")
              || squareBracketsRange.group(3).equals("out");
      if (!negation && insideRange || negation && outsideRange) {
        //covers "inside/in range" and "not out/outside range"
        comment = comment.replaceFirst(SQUARE_BRACKETS_RANGE, ">" + min + " and <" + max);
      } else {
        //covers "not in/inside range" and "out/outside range"
        comment = comment.replaceFirst(SQUARE_BRACKETS_RANGE, "<" + min + " or >" + max);
      }
    }
    return comment;
  }
}
