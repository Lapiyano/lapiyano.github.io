package dataStructures.tree.models;

import dataStructures.graph.models.AdjacencyListGraph;

public class TileNode<V, E> {

	
	private V Lower_Left;
	private V high_right;
    
    private AdjacencyListGraph<V, E> Graph;
  
	private String patternType;
   
    private int depth;
    
    
    private TileNode<V,E> parent;    
    private TileNode<V,E> front ;     
    private TileNode<V,E> left;      
    private TileNode<V,E> right;     


    
    
    public TileNode(AdjacencyListGraph<V, E> graph,TileNode<V,E> parent,int depth, String type,V Lower_Left,V High_Left ) {
        this.Graph = graph;
        this.patternType = type;
        this.parent=parent;
        this.Lower_Left = Lower_Left;
        this.high_right=High_Left;
        this.depth=0;
        
    
    }
    
    

    public boolean hasChild(String child) {
        if ("Front".equalsIgnoreCase(child)) return front != null;
        if ("Left".equalsIgnoreCase(child)) return left != null;
        if ("Right".equalsIgnoreCase(child)) return right != null;
        return false;
    }
    
  		public AdjacencyListGraph<V, E> getMicroGraph() {
  			return Graph;
  		}

  		public void setMicroGraph(AdjacencyListGraph<V, E> microGraph) {
  			this.Graph = microGraph;
  		}

  		public String getPatternType() {
  			return patternType;
  		}


  		public void setPatternType(String patternType) {
  			this.patternType = patternType;
  		}


  		public int getDistanceToUser() {
  			return depth;
  		}


  		public void setDistanceToUser(int depth) {
  			this.depth = depth;
  		}

  		public TileNode<V, E> getParent() {
  			return parent;
  		}


  		public void setParent(TileNode<V, E> parent) {
  			this.parent = parent;
  		}


  		public TileNode<V, E> getFront() {
  			return front;
  		}


  		public void setFront(TileNode<V, E> front) {
  			this.front = front;
  		}


  		public TileNode<V, E> getLeft() {
  			return left;
  		}


  		public void setLeft(TileNode<V, E> left) {
  			this.left = left;
  		}


  		public TileNode<V, E> getRight() {
  			return right;
  		}


  		public void setRight(TileNode<V, E> right) {
  			this.right = right;
  		}


  		
  		
  		public V getLowerLeft() {
  		    return this.Lower_Left;
  		}
  		
  		public V getHighRight() {
  			return this.high_right;
  		}

    		

}

