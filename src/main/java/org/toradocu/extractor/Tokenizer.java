package org.toradocu.extractor;

/** Created by arianna on 25/05/17. */
public class Tokenizer {
  public Comment tokenize(String textComment) {
    Comment comment = new Comment(textComment);

    // Split comment on non-alphanumeric characters, i.e. blanks, tabs,
    // punctuation, new line
    String[] tokens = textComment.split("\\W+");
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].startsWith("@code")) {
        String codeWord = tokens[i].replace("@code", "");
        comment.getWordsMarkedAsCode().add(codeWord);
      }
    }

    return comment;
  }
}
