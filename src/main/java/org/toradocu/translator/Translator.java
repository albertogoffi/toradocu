package org.toradocu.translator;

import java.util.List;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;
import randoop.condition.specification.Specification;

public interface Translator<T extends BlockTag> {
  List<? extends Specification> translate(T tag, DocumentedExecutable excMember);
}
