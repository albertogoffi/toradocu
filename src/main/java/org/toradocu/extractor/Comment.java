package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text of a Javadoc block {@link Tag}. A {@link Comment} is composed of a text and a list of words
 * that are tagged as @code.
 */
public final class Comment {

  /** Comment text. */
  private String text;

  /** List of words marked with @code tag in comment text. */
  private final List<String> wordsMarkedAsCode;

  /**
   * Builds a new Comment with the given {@code text}. Words marked with {@literal @code} and
   * {@literal <code></code>} in {@code text} are automatically added to the list of words marked as
   * code.
   *
   * @param text text of the comment.
   */
  public Comment(String text) {
    this.text = text.replaceAll("\\s+", " ");
    this.wordsMarkedAsCode = new ArrayList<>();

    final String codePattern1 = "<code>([A-Za-z0-9_]+)</code>";
    identifyMarkedWords(text, codePattern1);
    removeTags(codePattern1);

    final String codePattern2 = "\\{@code ([^}]+)\\}";
    identifyMarkedWords(text, codePattern2);
    removeTags(codePattern2);

    removeTags("\\{@link #?([^}]+)\\}");
    removeHTMLTags();
  }

  /**
   * Builds a new Comment with the given {@code text} and code blocks. This constructor does not
   * preserve the correct correspondence between the text and the code blocks.
   *
   * @param text text of the comment.
   * @param wordsMarkedAsCode blocks of text wrapped in {@literal @code} or {@literal <code></code>}
   */
  public Comment(String text, List<String> wordsMarkedAsCode) {
    this.text = text.replaceAll("\\s+", " ");
    this.wordsMarkedAsCode = wordsMarkedAsCode;
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

  /**
   * Identifies in {@code text} words marked with the given {@code codePattern}. Identified words
   * are added to {@code wordsMarkedAsCode}.
   *
   * @param text text in which look for words marked as code
   * @param codePattern regular expression used to identify the words marked as code
   */
  private void identifyMarkedWords(String text, String codePattern) {
    Matcher matcher = Pattern.compile(codePattern).matcher(text);
    while (matcher.find()) {
      String taggedSubstring = matcher.group(1).trim();
      String[] words = taggedSubstring.split("\\s+");
      for (String word : words) {
        if (!word.isEmpty()) {
          wordsMarkedAsCode.add(word);
        }
      }
    }
  }

  /**
   * Removes Javadoc inline tags from the comment text preserving the content of the tags.
   *
   * @param pattern a regular expression
   */
  private void removeTags(String pattern) {
    Matcher matcher = Pattern.compile(pattern).matcher(text);
    while (matcher.find()) {
      this.text = this.text.replace(matcher.group(0), matcher.group(1));
    }
  }

  /** Removes HTML tags from the comment text. */
  private void removeHTMLTags() {
    String htmlTagPattern = "(<.*>).*(</.*>)";
    Matcher matcher = Pattern.compile(htmlTagPattern).matcher(text);
    while (matcher.find()) {
      this.text = this.text.replace(matcher.group(1), "");
      this.text = this.text.replace(matcher.group(2), "");
    }
  }
}
