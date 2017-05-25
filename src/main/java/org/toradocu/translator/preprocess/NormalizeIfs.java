package org.toradocu.translator.preprocess;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;

public class NormalizeIfs implements PreprocessingPhase {

  /**
   * Replace some common expressions in the comment with other standard easier to translate
   * correctly.
   *
   * @param comment the String comment to sanitize
   * @param method the ExecutableMember
   * @return the normalized comment
   */
  private static String normalizeComment(String comment, ExecutableMember method) {
    if (comment.contains("if and only if")) comment = comment.replace("if and only if", "if");

    if (comment.contains("iff")) comment = comment.replace("iff", "if");

    if (comment.contains("non-null")) comment = comment.replace("non-null", "!=null");

    if (comment.contains("non-empty")) comment = comment.replace("non-empty", "!=empty");

    return comment;
  }

  @Override
  public String run(Tag tag, ExecutableMember excMember) {
    return normalizeComment(tag.getComment().getText(), excMember);
  }
}
