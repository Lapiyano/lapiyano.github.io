package dataStructures.graph.interfaces;


public interface IEdge<E>  {

	public E getElement();
	public IPosition<IEdge<E>> getPosition();
	
}
