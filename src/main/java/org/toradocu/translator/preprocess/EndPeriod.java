package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;

public class EndPeriod implements PreprocessingPhase {

  @Override
  public String run(Tag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    if (!comment.endsWith(".")) {
      comment += ".";
    }

    return comment;
  }
}
