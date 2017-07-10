package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;

public class Trim implements PreprocessingPhase {

  @Override
  public String run(Tag tag, DocumentedExecutable excMember) {

    return tag.getComment().getText().trim();
  }
}
