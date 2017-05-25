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
    while (codeMatcher.find()) {
      // get words marked as code
      String taggedSubstring = codeMatcher.group(1);
      String[] tokens = taggedSubstring.split(" ");
      for (int i = 0; i < tokens.length; i++) wordsMarkedAsCode.add(tokens[i]);

      // remove the code tag from the original comment
      this.text = this.text.replace(codeMatcher.group(0), codeMatcher.group(1));
    }
  }

  public String getText() {
    return text;
  }

  public List<String> getWordsMarkedAsCode() {
    return wordsMarkedAsCode;
  }
}
