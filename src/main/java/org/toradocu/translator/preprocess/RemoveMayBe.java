package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class RemoveMayBe implements PreprocessingPhase {

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();

    if (comment.contains("may be")) {
      comment = comment.replaceAll("may be", "");
    }
    if (comment.contains("can be")) {
      comment = comment.replaceAll("can be", "");
    }
    return comment;
  }
}
