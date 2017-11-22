package org.toradocu.translator.preprocess;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;

public class RemoveInitialIf implements PreprocessingPhase {

  /**
   * Removes one or more occurrences of {@code wordToRemove} at the beginning of {@code text}. This
   * method ignores the case of wordToRemove, i.e., occurrences are searched and removed ignoring
   * the case.
   *
   * @param text string from which remove occurences of {@code wordToRemove}
   * @param wordToRemove word to remove from {@code text}
   * @return the given {@code text} with initial occurrences of {@code wordToRemove} deleted
   */
  private static String removeInitial(String text, String wordToRemove) {
    wordToRemove = wordToRemove.toLowerCase();
    String lowerCaseComment = text.toLowerCase();
    while (lowerCaseComment.startsWith(wordToRemove + " ")) {
      text = text.substring(wordToRemove.length() + 1);
      lowerCaseComment = text.toLowerCase();
    }
    return text;
  }

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    // TODO Auto-generated method stub
    return removeInitial(tag.getComment().getText(), "if");
  }
}
