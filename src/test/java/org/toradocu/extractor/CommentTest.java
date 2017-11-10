package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;

public class CommentTest {

  @Test
  public void testComment() {
    String multipleTags =
        "This comment contains a {@code codeElement}, another {@code one}, and a last {@code codeElement}";
    String simpleTag = "This comment contains a {@code codeElement}";
    String complexTag = "This comment contains a {@code complex codeElement}";
    String expressionTag = "This comment contains an expression: {@code i<0}";

    Comment simpleComment = new Comment(simpleTag);
    List<Integer> codeWordOccurrences = simpleComment.getWordsMarkedAsCode().get("codeElement");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    Comment multiTagComment = new Comment(multipleTags);
    codeWordOccurrences = multiTagComment.getWordsMarkedAsCode().get("codeElement");
    //codeElement is tagged as code twice (both the first and second occurrence) in multiTagComment.
    //Thus we expect to find the values 0 and 1 (first and second occurrence).
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(2));
    assertThat(codeWordOccurrences.get(0), is(0));
    assertThat(codeWordOccurrences.get(1), is(1));

    codeWordOccurrences = multiTagComment.getWordsMarkedAsCode().get("one");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    Comment complexComment = new Comment(complexTag);
    codeWordOccurrences = complexComment.getWordsMarkedAsCode().get("complex");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    codeWordOccurrences = complexComment.getWordsMarkedAsCode().get("codeElement");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    //From expression such as i<0, we do not retain signs and numbers
    Comment exprComment = new Comment(expressionTag);
    assertThat(exprComment.getWordsMarkedAsCode().size(), is(1));
    codeWordOccurrences = exprComment.getWordsMarkedAsCode().get("i");
    assertThat(codeWordOccurrences, not(is(nullValue())));
  }
}
