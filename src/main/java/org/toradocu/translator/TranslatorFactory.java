package org.toradocu.translator;

import org.toradocu.extractor.Tag;

class TranslatorFactory {

  private TranslatorFactory() {}

  static Translator<? extends Tag> create(Tag.Kind tagKind) {
    switch (tagKind) {
      case PARAM:
        return new ParamTranslator();
      case THROWS:
        return new ThrowsTranslator();
      case RETURN:
        return new ReturnTranslator();
      default:
        throw new UnsupportedOperationException("No translator known for tag kind " + tagKind);
    }
  }
}
