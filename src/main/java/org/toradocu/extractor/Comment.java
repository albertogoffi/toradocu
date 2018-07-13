package org.toradocu.extractor;

import java.util.*;
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

  /**
   * Words marked with {@literal @code} tag in comment text. With "word" we mean a single String (in
   * case of a whole sentence tagged as code, each word is stored separately). We do not retain
   * mathematical signs and numbers in case of expressions such as {@code i<0} (only "i" is stored).
   * Each word retained (as a String key) is mapped with a list of integers that stores the
   * occurrences which are tagged as code in the original text. For example: "{@code a} is negative
   * and is a real number. {@code a} cannot be null" will be stored as a-> [0, 2] since the first
   * and third occurrences are tagged as code, but not the second one.
   */
  private final Map<String, List<Integer>> wordsMarkedAsCode;

  /**
   * Builds a new Comment with the given {@code text}. Words marked with {@literal @code} and
   * {@literal <code></code>} in {@code text} are added to the map of words marked as code. Than,
   * the text is cleaned from any tag.
   *
   * @param text text of the comment.
   */
  public Comment(String text) {
    this.text = text.replaceAll("\\s+", " ");
    this.wordsMarkedAsCode = new HashMap<>();

    final String codePattern1 = "<code>([A-Za-z0-9_]+)</code>";
    identifyCodeWords(codePattern1);
    removeTags(codePattern1);

    final String codePattern2 = "\\{@code ([^}]+)\\}";
    identifyCodeWords(codePattern2);
    removeTags(codePattern2);

    removeTags("\\{@link #?([^}]+)\\}");
    removeHTMLTags();
    decodeHTML();
    this.text = this.text.trim();
  }

  /** Decodes HTML character entities found in comment text with corresponding characters. */
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
   * Builds a new Comment with the given {@code text} and code blocks.
   *
   * @param text text of the comment.
   * @param wordsMarkedAsCode blocks of text wrapped in {@literal @code} or {@literal <code></code>}
   */
  public Comment(String text, Map<String, List<Integer>> wordsMarkedAsCode) {
    this(text);
    this.wordsMarkedAsCode.putAll(wordsMarkedAsCode);
  }

  /**
   * Returns the comment text as {@code String}. Notice that the text does not contain inline tags
   * because they are removed in the constructor of {@code Comment}.
   *
   * @return the {@code String} comment text
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the {@code Map} of words marked with {@literal @code} tag in comment text.
   *
   * @return the {@code Map} of words marked as code
   */
  public Map<String, List<Integer>> getWordsMarkedAsCode() {
    return wordsMarkedAsCode;
  }

  /**
   * Adds to {@link #wordsMarkedAsCode} any words in {@code text} that are marked with the given
   * {@code codePattern}.
   *
   * @param codePattern regular expression used to identify the words marked as code
   */
  private void identifyCodeWords(String codePattern) {
    String[] subSentences = text.split("\\. ");
    for (String subSentence : subSentences) {
      Matcher codeMatcher = Pattern.compile(codePattern).matcher(subSentence);

      while (codeMatcher.find()) {
        String taggedSubstring = codeMatcher.group(1).trim();
        String[] words = null;
        words = taggedSubstring.split("\\s+");
        if (words.length == 1 && words[0].matches(".[[<>=]=?|!=].")) {
          words = taggedSubstring.split("[<>=]=?|!=]");
        }

        int indexOfMatch = codeMatcher.start();
        for (String word : words) {
          if (!word.isEmpty() && !word.matches(".*[0-9+-/*(){}[<>=]=?|!=].*")) {
            // search this word before this index in original text
            List<Integer> occurrences = new ArrayList<>();
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

  /**
   * Counts how many occurrences of the String {@code word} there are before the given {@code
   * limitIndex} inside the {@code subSentence}. By doing this we know which occurrence of the word
   * we are examining in case of multiple occurrences of the same word in the sentence.
   *
   * @param word the word which occurrences must be count
   * @param subSentence the {@code String} in which to find the word
   * @param limitIndex limit index in {@code subSentence} where to count the occurrences
   * @return the computed occurrence
   */
  private int countStringOccurrence(String word, String subSentence, int limitIndex) {
    if (word.matches(".*[\\[\\]\\(\\)].*")) {
      // Escape special characters to prevent errors in subsequent pattern compiling
      word =
          word.replaceAll("\\]", "\\\\]")
              .replaceAll("\\[", "\\\\[")
              .replaceAll("\\)", "\\)")
              .replaceAll("\\(", "\\\\(")
              .replaceAll("\\.", "\\\\.");

      // Word boundaries do not work in case of special characters, thus use look ahead and look
      // behind
      word = "(?<!" + word + ")" + word + "(?!" + word + ")";
    } else {
      word = "\\b" + word + "\\b";
    }
    Matcher matcher = Pattern.compile(word).matcher(subSentence);
    int i = 0;
    while (matcher.find() && matcher.start() < limitIndex) {
      // Looping on method find preserves the order of matches,
      // while staying behind the desired limitIndex counts how
      // many matches are before the desired word
      i++;
    }

    if (!matcher.find(0)) {
      // TODO check: could this happen?
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
    String htmlTagPattern = "<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>(.*?)</\\1>|(<(.*)/>)";
    Matcher matcher = Pattern.compile(htmlTagPattern).matcher(text);
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        this.text = this.text.replace(matcher.group(0), matcher.group(2));
      } else {
        // Match contains self-closing tag
        this.text = this.text.replace(matcher.group(0), "");
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comment comment = (Comment) o;

    return text.equals(comment.text) && wordsMarkedAsCode.equals(comment.wordsMarkedAsCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, wordsMarkedAsCode);
  }
}
