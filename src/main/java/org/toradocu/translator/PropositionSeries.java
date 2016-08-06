package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PropositionList {
		
	private final List<Proposition> nodes;
	private final List<Conjunction> edges;
	
	public PropositionList() {
		nodes = new ArrayList<>();
		edges = new ArrayList<>();
	}
	
	public void add(Proposition proposition) {
		if (!nodes.isEmpty()) {
			throw new IllegalStateException("List is not empty. Use add(Proposition, Conjunction)");
		}
		nodes.add(proposition);
	}

	public void add(Conjunction conjunction, Proposition proposition) {
		if (nodes.isEmpty()) {
			throw new IllegalStateException("List is empty. Use add(Proposition)");
		}
		edges.add(conjunction);
		nodes.add(proposition);
	}
	
	public boolean contains(Proposition proposition) {
		return nodes.contains(proposition);
	}
	
	public boolean isEmpty() {
		return nodes.isEmpty();
	}
	
	public int size() {
		return nodes.size();
	}
	
	public List<Proposition> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	public List<Conjunction> getEdges() {
		return Collections.unmodifiableList(edges);
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("");
		if (!nodes.isEmpty()) {
			output.append(nodes.get(0));
			for (int i = 1; i < nodes.size(); i++) {
				output.append(edges.get(i - 1));
				output.append(nodes.get(i));	
			}
		}
		return output.toString();
	}
}
