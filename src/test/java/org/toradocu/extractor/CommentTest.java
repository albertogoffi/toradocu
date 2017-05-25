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
    String javaTypeComment = "This comment contains a.Java.Type.";

    Comment commentObject = new Comment(tagged);
    assertThat(commentObject.getWordsMarkedAsCode().contains("codeElement"), is(true));

    commentObject = new Comment(doubleTagged);
    List<String> codeWords = new ArrayList<String>();
    codeWords.add("codeElement");
    codeWords.add("one");
    assertThat(commentObject.getWordsMarkedAsCode().equals(codeWords), is(true));

    commentObject = new Comment(javaTypeComment);
  }
}
