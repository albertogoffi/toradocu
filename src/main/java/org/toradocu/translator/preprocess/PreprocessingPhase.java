package org.toradocu.translator.preprocess;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

public interface PreprocessingPhase {
  String run(Tag tag, DocumentedMethod excMember);
}
