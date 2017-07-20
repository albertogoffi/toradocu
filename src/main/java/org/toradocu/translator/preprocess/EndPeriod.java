package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class EndPeriod implements PreprocessingPhase {

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    if (!comment.endsWith(".")) {
      comment += ".";
    }

    return comment;
  }
}
