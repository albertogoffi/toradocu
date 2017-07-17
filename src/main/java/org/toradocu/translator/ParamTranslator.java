package org.toradocu.translator;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import org.toradocu.translator.spec.Specification;

public class ParamTranslator implements Translator<ParamTag> {

  @Override
  public Specification translate(ParamTag tag, DocumentedExecutable excMember) {
    return BasicTranslator.translate(tag, excMember);
  }
}
