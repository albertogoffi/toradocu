package org.toradocu.translator;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.preprocess.PreprocessorFactory;

public class CommentTranslator {

  public static void translate(Tag tag, ExecutableMember excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    final Translator<Tag> translator = TranslatorFactory.create(tag);
    translator.translate(tag, excMember);

    // TODO ...
  }
}
