package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public interface PreprocessingPhase {
  String run(BlockTag tag, DocumentedExecutable excMember);
}
