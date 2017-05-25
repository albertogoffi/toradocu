package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Created by arianna on 25/05/17. */
public class Comment {

  private String text;
  private List<String> wordsMarkedAsCode;

  public Comment(String text) {
    this.text = text;
    this.wordsMarkedAsCode = new ArrayList<String>();
    String codePattern = "\\{@code ([^}]+)\\}";
    java.util.regex.Matcher codeMatcher = Pattern.compile(codePattern).matcher(text);
    while(codeMatcher.find()){
      wordsMarkedAsCode.add(codeMatcher.group(1));
    }

//    tokenize(text);


  }

  private void tokenize(String text) {
    // Split comment on punctuation and white spaces
    String[] tokens = text.split("\\s*(;|,|'|. |\\s)\\s*");
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].startsWith("{@code")) {
        String codeWord = tokens[i+1].replace("}", "");
        this.getWordsMarkedAsCode().add(codeWord);
      }
    }
  }

  public String getText() {
    return text;
  }

  public List<String> getWordsMarkedAsCode() {
    return wordsMarkedAsCode;
  }
}
