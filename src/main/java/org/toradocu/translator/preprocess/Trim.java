package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

public class Trim implements PreprocessingPhase {

  @Override
  public String run(Tag tag, DocumentedMethod excMember) {

    return tag.getComment().trim();
  }
}
