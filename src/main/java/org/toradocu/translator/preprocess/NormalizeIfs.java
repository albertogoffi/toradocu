package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class NormalizeIfs implements PreprocessingPhase {

  /**
   * Replace some common expressions in the comment with other standard easier to translate
   * correctly.
   *
   * @param comment the String comment to sanitize
   * @param method the DocumentedExecutable
   * @return the normalized comment
   */
  private static String normalizeComment(String comment, DocumentedExecutable method) {
    // Checks if comment contains "if and only if", " iff ", or starts with "iff".
    // No need to check the beginning of a phrase since "if and only if" cannot be a substring of a
    // word.
    if (comment.contains("if and only if")
        || comment.startsWith("iff ")
        || comment.contains(" iff ")) {
      comment = comment.replaceAll("if and only if", "if");
      comment = comment.replace("iff ", "if ");
      comment = comment.replaceAll(" iff ", " if ");
      if (comment.endsWith(".")) {
        comment = comment.substring(0, comment.length() - 1);
      }
      if (comment.contains("true if"))
        // By adding this, Toradocu is able to understand that also the false property must be
        // specified
        comment += ", false otherwise.";
      else if (comment.contains("null if")) comment += ", non-null otherwise.";
    }

    return comment;
  }

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    return normalizeComment(tag.getComment().getText(), excMember);
  }
}
