package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.preprocess.PreprocessingPhase;
import org.toradocu.translator.preprocess.Preprocessor;
import org.toradocu.translator.preprocess.RemoveCommas;

public class CommentTranslator {

  public static void translate(Tag tag, DocumentedMethod excMember) {

    // Preprocessing
    preprocessing(tag, excMember);
  }

  private static void preprocessing(Tag tag, DocumentedMethod excMember) {
    List<PreprocessingPhase> phases = new ArrayList<>();

    switch (tag.getKind()) {
      case PARAM:
        phases.add(new RemoveCommas()); // TODO Make RemoveCommas a singleton?
    }

    new Preprocessor(phases).run(tag, excMember);
  }
}
