package org.toradocu.translator;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class PropositionListTest {

	@Test
	public void toStringTest() {
		PropositionSeries list = new PropositionSeries();
		list.add(new Proposition("subject1", "predicate1"));
		list.add(Conjunction.OR, new Proposition("subject2", "predicate2"));
		list.add(Conjunction.AND, new Proposition("subject3", "predicate3"));
		
		assertThat(list.size(), is(3));
		assertThat(list.toString(), is("(subject1, predicate1)||(subject2, predicate2)&&(subject3, predicate3)"));
	}
}
