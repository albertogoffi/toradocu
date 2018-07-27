package org.toradocu.translator.preprocess;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

/**
 * Preprocessing phase in which value ranges are replaced with standard inequalities using &lt; and
 * &gt; math operators.
 */
public class ExpandRange implements PreprocessingPhase {

  private static final List<String> inRangeWords = Arrays.asList("in", "inside");
  private static final List<String> outRangeWords = Arrays.asList("out", "outside");
  private static final String inOutGroup;

  static {
    StringJoiner joiner = new StringJoiner("|", "(", ")");
    for (String word : inRangeWords) {
      joiner.add(word);
    }
    for (String word : outRangeWords) {
      joiner.add(word);
    }
    inOutGroup = joiner.toString();
  }

  private static final String SQUARE_BRACKETS_RANGE =
      "(not )?(lie )?"
          + inOutGroup
          + " the ([\\w\\s]+)?(interval|range) (of )?\\[([0-9]+) ?,? ?([0-9]+)\\]";

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    java.util.regex.Matcher squareBracketsRange =
        Pattern.compile(SQUARE_BRACKETS_RANGE).matcher(comment);

    while (squareBracketsRange.find()) {
      int min = Integer.valueOf(squareBracketsRange.group(7));
      int max = Integer.valueOf(squareBracketsRange.group(8));
      boolean negation = squareBracketsRange.group(1) != null;
      final String insideOrOutside = squareBracketsRange.group(3);
      boolean insideRange = inRangeWords.contains(insideOrOutside);
      boolean outsideRange = outRangeWords.contains(insideOrOutside);
      if (!negation && insideRange || negation && outsideRange) {
        // covers "inside/in range" and "not out/outside range"
        comment = comment.replaceFirst(SQUARE_BRACKETS_RANGE, ">" + min + " and <" + max);
      } else {
        // covers "not in/inside range" and "out/outside range"
        comment = comment.replaceFirst(SQUARE_BRACKETS_RANGE, "<" + min + " or >" + max);
      }
    }
    return comment;
  }
}
