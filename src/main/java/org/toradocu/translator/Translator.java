package org.toradocu.translator;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public interface Translator<T extends BlockTag> {
  void translate(T tag, DocumentedExecutable excMember);
}
