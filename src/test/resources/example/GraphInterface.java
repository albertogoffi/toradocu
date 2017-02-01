package example;

public interface GraphInterface<V, E> {

  /**
   * @param sourceVertex source vertex of the edge.
   * @param targetVertex target vertex of the edge.
   */
  public boolean containsEdge(V sourceVertex, V targetVertex);
}
