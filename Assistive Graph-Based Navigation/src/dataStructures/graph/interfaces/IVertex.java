package dataStructures.graph.interfaces;


public interface IVertex<V> {
	
	public IPosition<IVertex<V>> getPosition();
	public V getElement();
}
