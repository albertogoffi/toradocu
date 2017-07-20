package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class RemoveCommas implements PreprocessingPhase {

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    return tag.getComment().getText().replaceAll(",", " ");
  }
}
