package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
/** Created by arianna on 25/05/17. */
public class CommentTest {

  @Test
  public void testComment() {
    String doubleTagged = "This comment contains a {@code codeElement} and another {@code one}";
    String tagged = "This comment contains a {@code codeElement}";
    String complexTagged = "This comment contains a {@code complex codeElement}";

    Comment commentObject = new Comment(tagged);
    assertThat(commentObject.getWordsMarkedAsCode().contains("codeElement"), is(true));
    assertThat(commentObject.getText().equals("This comment contains a codeElement"), is(true));

    commentObject = new Comment(doubleTagged);
    List<String> codeWords = new ArrayList<String>();
    codeWords.add("codeElement");
    codeWords.add("one");
    assertThat(commentObject.getWordsMarkedAsCode().equals(codeWords), is(true));
    assertThat(
        commentObject.getText().equals("This comment contains a codeElement and another one"),
        is(true));

    codeWords.clear();
    codeWords.add("complex");
    codeWords.add("codeElement");
    commentObject = new Comment(complexTagged);
    assertThat(commentObject.getWordsMarkedAsCode().equals(codeWords), is(true));
    assertThat(
        commentObject.getText().equals("This comment contains a complex codeElement"), is(true));
  }
}
