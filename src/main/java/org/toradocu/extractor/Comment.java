package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Comment associated to a tag. A comment is represented by the plain text and the list of words
 * that are tagged as @code. The class is easy to extend for future purposes, such as the handling
 * of other tags in the comments (e.g. @link).
 */
public final class Comment {

  /** The text representing the comment. */
  private String text;

  /** List of words marked with @code tags in the comment. */
  private List<String> wordsMarkedAsCode;

  public Comment(String text) {
    this(text, new ArrayList<>());
  }

  public Comment(String text, List<String> wordsMarkedAsCode) {
    this.text = text.replaceAll("\\s+", " ");
    this.wordsMarkedAsCode = wordsMarkedAsCode;

    String codePattern = "<code>(.*)</code>";
    java.util.regex.Matcher matcher = Pattern.compile(codePattern).matcher(text);
    while (matcher.find()) {
      // Get words marked as code
      String taggedSubstring = matcher.group(1).trim();
      String[] tokens = taggedSubstring.split(" ");
      for (int i = 0; i < tokens.length; i++) {
        if (tokens[i] != "") wordsMarkedAsCode.add(tokens[i]);
      }
    }
    codePattern = "\\{@code ([^}]+)\\}";
    matcher = Pattern.compile(codePattern).matcher(text);
    while (matcher.find()) {
      // Get words marked as code
      String taggedSubstring = matcher.group(1).trim();
      String[] tokens = taggedSubstring.split(" ");
      for (int i = 0; i < tokens.length; i++) {
        if (tokens[i] != "") wordsMarkedAsCode.add(tokens[i]);
      }
      this.text = this.text.replace(matcher.group(0), matcher.group(1));
    }

    String htmlTagPattern = "(<.*>).*(</.*>)";
    matcher = Pattern.compile(htmlTagPattern).matcher(text);
    // Remove the tag from the original comment
    while (matcher.find()) {
      this.text = this.text.replace(matcher.group(1), "");
      this.text = this.text.replace(matcher.group(2), "");
    }
    String linkPattern = "\\{@link #?([^}]+)\\}";
    matcher = Pattern.compile(linkPattern).matcher(text);
    while (matcher.find()) {
      this.text = this.text.replace(matcher.group(0), matcher.group(1));
    }
  }

  public String getText() {
    return text;
  }

  public List<String> getWordsMarkedAsCode() {
    return wordsMarkedAsCode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, wordsMarkedAsCode);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Comment)) {
      return false;
    }
    Comment that = (Comment) obj;
    return text.equals(that.text) && wordsMarkedAsCode.equals(that.wordsMarkedAsCode);
  }
}
