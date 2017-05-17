package org.toradocu.translator.preprocess;

public class RemoveCommas implements PreprocessingPhase {

  @Override
  public String run(String comment) {
    return comment.replaceAll(",", " ");
  }
}
