package org.toradocu.translator;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.preprocess.PreprocessorFactory;

public class CommentTranslator {

  public static void translate(Tag tag, DocumentedMethod excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    Specification spec = TranslatorFactory.create(tag.getKind()).translate(tag, excMember);

    // TODO ...
  }
}
