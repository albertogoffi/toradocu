package org.toradocu.translator;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.preprocess.PreprocessorFactory;

public class CommentTranslator {

  public static <T extends Tag> void translate(T tag, ExecutableMember excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    final Translator<Tag> translator = TranslatorFactory.create(tag.getKind());
    translator.translate(tag, excMember);

    // TODO ...
  }
}
