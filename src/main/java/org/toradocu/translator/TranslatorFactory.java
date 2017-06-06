package org.toradocu.translator;

import org.toradocu.extractor.Tag;

class TranslatorFactory {

  private TranslatorFactory() {}

  static Translator create(Tag.Kind tagKind) {
    switch (tagKind) {
      case PARAM:
      case THROWS:
        return new ParamThrowsTranslator();
      case RETURN:
        return new ReturnTranslator();
      default:
        throw new UnsupportedOperationException("No translator known for tag kind " + tagKind);
    }
  }
}
