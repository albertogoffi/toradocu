package org.toradocu.translator;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ParamTag;
import org.toradocu.translator.spec.Specification;

public class ParamTranslator implements Translator<ParamTag> {

  @Override
  public Specification translate(ParamTag tag, ExecutableMember excMember) {
    return BasicTranslator.translate(tag, excMember);

    //    // TODO Create the specification with the derived merged conditions.
    //
    //    return null;
  }
}
