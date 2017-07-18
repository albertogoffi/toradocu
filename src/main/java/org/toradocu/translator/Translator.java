package org.toradocu.translator;

import java.util.List;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;
import randoop.condition.specification.Specification;

public interface Translator<T extends Tag> {
  List<? extends Specification> translate(T tag, DocumentedExecutable excMember);
}
