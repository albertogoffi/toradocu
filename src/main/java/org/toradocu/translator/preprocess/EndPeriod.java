package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

public class EndPeriod implements PreprocessingPhase {

  @Override
  public String run(Tag tag, DocumentedMethod excMember) {
    String comment = tag.getComment();
    if (!comment.endsWith(".")) {
      comment += ".";
    }

    return comment;
  }
}
