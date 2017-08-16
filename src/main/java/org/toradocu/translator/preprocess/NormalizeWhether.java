package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class NormalizeWhether implements PreprocessingPhase {

  private static String normalizeComment(String comment, DocumentedExecutable method) {
    if (comment.toLowerCase().startsWith("whether")) {
      String preComment = "True if";
      String postComment = ", false otherwise";
      comment = comment.replaceFirst("whether", preComment) + postComment;
    }

    return comment;
  }

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    return normalizeComment(tag.getComment().getText(), excMember);
  }
}
