package org.toradocu.translator;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.spec.Specification;

public interface Translator<T extends BlockTag> {
  Specification translate(T tag, DocumentedExecutable excMember);
}
