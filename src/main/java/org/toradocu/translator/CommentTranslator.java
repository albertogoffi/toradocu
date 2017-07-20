package org.toradocu.translator;

import java.util.List;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.preprocess.PreprocessorFactory;
import randoop.condition.specification.Specification;

public class CommentTranslator {

  public static <T extends BlockTag> void translate(T tag, DocumentedExecutable excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    final Translator<T> translator = TranslatorFactory.create(tag);
    final List<? extends Specification> specifications = translator.translate(tag, excMember);
    // TODO Check the consistency of the generated specification.

    tag.setSpecification(specifications);
  }
}
