package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;

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
    // No need to chekc the beggining of a phrase since "if and only if" cannot be a substring of a word.
    if (comment.contains("if and only if")) comment = comment.replace("if and only if", "if");

    //Checks if the comment starts with "iff "

    if (comment.startsWith("iff ")) comment = comment.replace("iff ", "if ");
    comment = comment.replaceAll(" iff ", " if ");

    return comment;
  }

  @Override
  public String run(Tag tag, DocumentedExecutable excMember) {
    return normalizeComment(tag.getComment().getText(), excMember);
  }
}
