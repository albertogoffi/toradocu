package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
/** Created by arianna on 25/05/17. */
public class TokenizerTest {
  public void testTokenize() {
    Tokenizer tokenizer = new Tokenizer();
    String untaggedComment = "Ain't a real comment.";
    String taggedComment = "This comment contains a {@code codeElement}";

    Comment output = tokenizer.tokenize(untaggedComment);
    assertThat(output.getWordsMarkedAsCode().isEmpty(), is(true));

    output = tokenizer.tokenize(taggedComment);
    List<String> codeWords = new ArrayList<String>();
    codeWords.add("codeElement");
    assertThat(output.getWordsMarkedAsCode().equals(codeWords), is(true));
  }
}
