package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

public class RemoveCommas implements PreprocessingPhase {

  @Override
  public String run(Tag tag, DocumentedMethod excMember) {
    return tag.getComment().replaceAll(",", " ");
  }
}
