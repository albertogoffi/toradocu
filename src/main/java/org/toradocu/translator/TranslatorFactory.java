package org.toradocu.translator;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.BlockTag.Kind;

class TranslatorFactory {

  private TranslatorFactory() {}

  static <T extends BlockTag> Translator<T> create(T tag) {
    Translator translator;
    final Kind tagKind = tag.getKind();
    switch (tagKind) {
      case PARAM:
        translator = new ParamTranslator();
        break;
      case THROWS:
        translator = new ThrowsTranslator();
        break;
      case RETURN:
        translator = new ReturnTranslator();
        break;
      default:
        throw new UnsupportedOperationException("No translator known for tag kind " + tagKind);
    }
    @SuppressWarnings("unchecked") // This should be a safe cast.
    Translator<T> t = (Translator<T>) translator;
    return t;
  }
}
