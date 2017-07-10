package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;

public interface PreprocessingPhase {
  String run(Tag tag, DocumentedExecutable excMember);
}
