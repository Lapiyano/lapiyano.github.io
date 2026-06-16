package dataStructures.graph.models;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dataStructures.graph.interfaces.IAdjacencyList;
import dataStructures.graph.interfaces.IEdge;
import dataStructures.graph.interfaces.IPosition;
import dataStructures.graph.interfaces.IVertex;

/**
 * @author Thobela FF 223079625
 * @param <V>
 * @param <E>
 */
public class AdjacencyListGraph<V, E>  implements IAdjacencyList<V,E>{

	private class Vertex<V> implements IVertex<V>,IPosition<V> {
		private V element;
		private IPosition<IVertex<V>> pos;
		private List<IEdge<E>> incidenceSequence;

		public Vertex(V element) {
			this.element = element;
			setPosition(this);
			this.incidenceSequence = new LinkedList<>();
		}

	
		@SuppressWarnings("unchecked")
		private void setPosition(IVertex<V> p) {
			this.pos = (IPosition<IVertex<V>>) p;
		}

		@Override
		public IPosition<IVertex<V>> getPosition() {
			return pos;
		}

		@Override
		public V getElement() {
			return element;
		}
		
		
		public List<IEdge<E>> getIncidence() {
			return incidenceSequence; 
		}
		
		
		public void addIncidentEdge(IEdge<E> e) {
			incidenceSequence.add(e);
		}

		@Override
		public V Element() {
			
			return getElement();
		}
	}

	private class Edge<E> implements IEdge<E>,IPosition<E> {
		private E element;
		private IPosition<IEdge<E>> pos;
		private IVertex<V>[] endpoints;

		@SuppressWarnings("unchecked")
		public Edge(IVertex<V> u, IVertex<V> v, E element) {
			this.element = element;
			this.endpoints = (IVertex<V>[]) new IVertex[] { u, v };
			setPosition(this);
		}

		@Override
		public E getElement() {
			return element;
		}

		
		@SuppressWarnings("unchecked")
		private void setPosition(IEdge<E> p) {
			this.pos = (IPosition<IEdge<E>>) p;
		}

		@Override
		public IPosition<IEdge<E>> getPosition() {
			return pos;
		}

		public IVertex<V>[] getEndpoints() {
			return endpoints;
		}

		@Override
		public E Element() {
			
			return getElement();
		}
	}
	
	
	
	private List<Vertex<V>> vertexSequence = new LinkedList<>(); 
    private List<Edge<E>> edgeSequence = new LinkedList<>();
	
	

	@Override
	public IVertex<V>[] endVertices(IEdge<E> e) {
		
		return ((Edge<E>)e).getEndpoints();
	}

	@Override
	public IVertex<V> opposite(IVertex<V> v, IEdge<E> e) {
		IVertex<V>[] endpoints=endVertices(e);
		
		if(endpoints[0]==v) { 
			return endpoints[1];
		}else if(endpoints[1]==v) {
			return endpoints[0];
		}else {
			return null;
		}
		
	}

	@Override
	public boolean areAdjacent(IVertex<V> V, IVertex<V> W) {
		
		List<IEdge<E>> vert=((Vertex<V>)V).getIncidence();
		
		for(IEdge<E> e:vert) {
			if(opposite(V,e)==W) {
				return true;
			}
		}
		return false;
	}

	
	@Override
	public IVertex<V> insertVertex(V elem) {
		
		Vertex<V> v = new Vertex<>(elem);
		
		vertexSequence.addLast(v);
		
		return v;
		
	}

	@Override
	public void insertEdge(IVertex<V> V, IVertex<V> W, E elem) {
		Edge<E> e=new Edge<>(V,W,elem);
		edgeSequence.addLast(e);
		
		((Vertex<V>) V).addIncidentEdge(e);
	    ((Vertex<V>) W).addIncidentEdge(e);
		
	}

	@Override
	public void removeVertex(IVertex<V> V) {
		
		for (IEdge<E> edge : ((Vertex<V>) V).getIncidence()) {
	        edgeSequence.remove(edge);//removes all edges
	    }
	    vertexSequence.remove(V);
		
		
	}

	
	public int numVertices() {
		return vertexSequence.size();
	}

	public int numEdges() {
		return edgeSequence.size();
	}

	public int Degree(IVertex<V> V) {
		return ((Vertex<V>)V).getIncidence().size();
	}
	@Override
	public void removeEdeg(IEdge<E> E) {
		edgeSequence.remove(E);
		
	}

	@Override
	public Iterator<IEdge<E>> incidentEdges(IVertex<V> V) {
		
		return ((Vertex<V>)V).getIncidence().iterator();
	}

	@Override
	public Iterator<IEdge<E>> edges() {

		
		List<IEdge<E>> list = new ArrayList<>();
	    for (Edge<E> e : edgeSequence) {
	        list.add(e);
	    }
	    return list.iterator();
	}

	@Override
	public Iterator<IVertex<V>> Vertices() {
		
		List<IVertex<V>> list = new ArrayList<>();
	    for (Vertex<V> v : vertexSequence) {
	        list.add(v); 
	    }
		return list.iterator();
	}
}