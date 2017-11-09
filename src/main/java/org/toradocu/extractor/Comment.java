package org.toradocu.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text of a Javadoc block tag ({@link BlockTag}), such as {@code @param}, {@code @return}, or
 * {@code @throws}. A {@link Comment} is composed of a text and a list of words that are tagged
 * as @code.
 */
public final class Comment {

  /**
   * Comment text. Does not include the tag (e.g., @return) and any additional information like the
   * commented parameter name in case of @param tags and the exception name in case of @throws tags.
   */
  private String text;

  /** List of words marked with @code tag in comment text. */
  private final Map<String, List<Integer>> wordsMarkedAsCode;

  /**
   * Builds a new Comment with the given {@code text}. Words marked with {@literal @code} and
   * {@literal <code></code>} in {@code text} are automatically added to the list of words marked as
   * code.
   *
   * @param text text of the comment.
   */
  public Comment(String text) {
    this.text = text.replaceAll("\\s+", " ");
    this.wordsMarkedAsCode = new HashMap<>();

    final String codePattern1 = "<code>([A-Za-z0-9_]+)</code>";
    identifyCodeWords(codePattern1, 6);
    removeTags(codePattern1);

    final String codePattern2 = "\\{@code ([^}]+)\\}";
    identifyCodeWords(codePattern2, 7);
    removeTags(codePattern2);

    removeTags("\\{@link #?([^}]+)\\}");
    removeHTMLTags();
    decodeHTML();
  }

  private void decodeHTML() {
    this.text =
        this.text
            .replaceAll("&ge;", ">=")
            .replaceAll("&le;", "<=")
            .replaceAll("&gt;", ">")
            .replaceAll("&lt;", "<")
            .replaceAll("&amp;", "&");
  }

  /**
   * Builds a new Comment with the given {@code text} and code blocks. This constructor does not
   * preserve the correct correspondence between the text and the code blocks.
   *
   * @param text text of the comment.
   * @param wordsMarkedAsCode blocks of text wrapped in {@literal @code} or {@literal <code></code>}
   */
  public Comment(String text, Map<String, List<Integer>> wordsMarkedAsCode) {
    this(text);
    this.wordsMarkedAsCode.putAll(wordsMarkedAsCode);
  }

  public String getText() {
    return text;
  }

  public Map<String, List<Integer>> getWordsMarkedAsCode() {
    return wordsMarkedAsCode;
  }

  /**
   * Adds to {@link #wordsMarkedAsCode} any words in {@code text} that are marked with the given
   * {@code codePattern}.
   *
   * @param codePattern regular expression used to identify the words marked as code
   */
  private void identifyCodeWords(String codePattern, int PATTERN_OFFSET) {
    String[] subSentences = text.split("\\.");
    for (String subSentence : subSentences) {
      Matcher codeMatcher = Pattern.compile(codePattern).matcher(subSentence);

      while (codeMatcher.find()) {
        String taggedSubstring = codeMatcher.group(1).trim();
        String[] words = taggedSubstring.split("\\s+");
        int indexOfMatch = codeMatcher.start() + PATTERN_OFFSET;
        for (String word : words) {
          if (!word.isEmpty() && !word.matches(".*[0-9+-/*(){}[<>=]=?|!=].*")) {
            //search this word before this index in original text
            List<Integer> occurrences = new ArrayList<Integer>();
            occurrences.add(countStringOccurrence(word, subSentence, indexOfMatch));
            if (wordsMarkedAsCode.get(word) != null) {
              wordsMarkedAsCode.get(word).addAll(occurrences);
            } else {
              wordsMarkedAsCode.put(word, occurrences);
            }
          }
          indexOfMatch += word.length() + 1;
        }
      }
    }
  }

  private int countStringOccurrence(String word, String subSentence, int indexOfMatch) {
    Matcher matcher = Pattern.compile("\\b" + word + "\\b").matcher(subSentence);
    int i = 0;
    while (matcher.find() && matcher.start() < indexOfMatch) {
      //Looping on method find preserves the order of matches,
      //while staying behind the desired index counts how
      //many matches are before the desired word
      i++;
    }

    if (!matcher.find(0)) {
      //TODO check: could this happen?
      return -1;
    }

    return i;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comment comment = (Comment) o;

    if (!text.equals(comment.text)) return false;
    return wordsMarkedAsCode.equals(comment.wordsMarkedAsCode);
  }

  @Override
  public int hashCode() {
    int result = text.hashCode();
    result = 31 * result + wordsMarkedAsCode.hashCode();
    return result;
  }
}
