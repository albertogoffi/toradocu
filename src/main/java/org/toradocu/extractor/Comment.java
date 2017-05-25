package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.List;

/** Created by arianna on 25/05/17. */
public class Comment {

  private String text;
  private List<String> wordsMarkedAsCode;

  public Comment(String text) {
    this.text = text;
    this.wordsMarkedAsCode = new ArrayList<String>();
  }

  public String getText() {
    return text;
  }

  public List<String> getWordsMarkedAsCode() {
    return wordsMarkedAsCode;
  }
}
