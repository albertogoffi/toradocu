package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;

public class RemoveCommas implements PreprocessingPhase {

  @Override
  public String run(Tag tag, DocumentedExecutable excMember) {
    return tag.getComment().getText().replaceAll(",", " ");
  }
}
