package org.toradocu.translator;

import org.jgrapht.graph.DefaultEdge;

public class ConjunctionEdge<V> extends DefaultEdge {
	
	private static final long serialVersionUID = 1L;
	private V source, target;
	private Conjunction conjunction;
	
	public ConjunctionEdge(V source, V target, Conjunction conjunction) {
		this.source = source;
		this.target = target;
		this.conjunction = conjunction;
	}
	
	@Override
	public V getSource() {
		return source;
	}
	
	@Override
	public V getTarget() {
		return target;
	}
	
	public Conjunction getConjunction() {
		return conjunction;
	}
	
	@Override
	public String toString() {
		return source + " " + conjunction + " " + target;
	}
}
