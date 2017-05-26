package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Comment associated to a tag. A comment is represented by the plain text and the list of words
 * that are tagged as @code. The class is easy to extend for future purposes, such as the handling
 * of other tags in the comments (e.g. @link).
 */
public class Comment {

  /** The text representing the comment. */
  private String text;

  /** List of words marked with @code tags in the comment. */
  private List<String> wordsMarkedAsCode;

  public Comment(String text) {
    this(text, new ArrayList<>());
  }

  public Comment(String text, List<String> wordsMarkedAsCode) {
    this.text = text;
    this.wordsMarkedAsCode = wordsMarkedAsCode;

    String codePattern = "\\{@code ([^}]+)\\}";
    java.util.regex.Matcher codeMatcher = Pattern.compile(codePattern).matcher(text);
    while (codeMatcher.find()) {
      // Get words marked as code
      String taggedSubstring = codeMatcher.group(1);
      String[] tokens = taggedSubstring.split(" ");
      for (int i = 0; i < tokens.length; i++) wordsMarkedAsCode.add(tokens[i]);

      // Remove the code tag from the original comment
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
