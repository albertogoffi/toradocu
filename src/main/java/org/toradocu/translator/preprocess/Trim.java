package org.toradocu.translator.preprocess;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;

public class Trim implements PreprocessingPhase {

  @Override
  public String run(Tag tag, ExecutableMember excMember) {

    return tag.getComment().trim();
  }
}
