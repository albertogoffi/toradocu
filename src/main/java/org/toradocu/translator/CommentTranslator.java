package org.toradocu.translator;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.preprocess.PreprocessorFactory;

public class CommentTranslator {

  public static <T extends BlockTag> void translate(T tag, DocumentedExecutable excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    final Translator<T> translator = TranslatorFactory.create(tag);
    // TODO In translators, check the consistency of the generated specification(s).
    translator.translate(tag, excMember);
  }
}
