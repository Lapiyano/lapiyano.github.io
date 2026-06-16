package dataStructures.graph.interfaces;

import java.util.Iterator;



public interface IAdjacencyList<V,E> {

	//access methods i will need
	public IVertex<V>[] endVertices(IEdge<E> e);
	public IVertex<V> opposite(IVertex<V> v,IEdge<E> e);
	public boolean areAdjacent(IVertex<V> V, IVertex<V> W);
	
	
	//update methods i will need
	public IVertex<V> insertVertex(V elem );
	public void insertEdge(IVertex<V> V, IVertex<V> W,E elem);
	public void removeVertex(IVertex<V> V);
	public void removeEdeg(IEdge<E> E);
	
	//iterators
	public Iterator<IEdge<E>> incidentEdges(IVertex<V> V);
	public Iterator<IEdge<E>> edges();
	public Iterator<IVertex<V>> Vertices();
}
