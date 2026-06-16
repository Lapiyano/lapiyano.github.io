package dataStructures.tree.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tree.interfaces.ITree;
import dataStructures.tree.models.TileNode;



public class BinaryTree<V,E> implements ITree<V,E> {

	
	private TileNode<V,E> root;
	private int size=0;
	
	
	public BinaryTree(TileNode<V,E> root) {
		this.root=root;
		if(root!=null) size=1;
		
	}
	
	public BinaryTree() {
		this(null);
	}
	
	
	@Override
	public TileNode<V,E> root() {
		
		return root;
	}

	@Override
	public TileNode<V, E> parent(TileNode<V, E> v) {
		
		return v.getParent();
	}
	
	
	public void preOrderTraversal(TileNode<V,E> node, List<TileNode<V, E>> list) {
		
	    if (node == null) return;
	    
	    list.add(node);

	    for (TileNode<V, E> child : children(node)) {
	        preOrderTraversal(child, list);
	    }
	}
	
	public Iterable<TileNode<V, E>> TileNodes(){
		
		List<TileNode<V, E>> list=new ArrayList<>();
		
		preOrderTraversal(root,list);
		
		
		return list;
	}


	@Override
	public Iterable<TileNode<V, E>> children(TileNode<V, E> v) {
	    List<TileNode<V, E>> list = new ArrayList<>();
	    if (v.getFront() != null) list.add(v.getFront());
	    if (v.getLeft() != null) list.add(v.getLeft());
	    if (v.getRight() != null) list.add(v.getRight());
	    return list;
	}

	@Override
	public int numChildren(TileNode<V, E> v) {
	    List<TileNode<V, E>> childList = (List<TileNode<V, E>>) children(v);
	    return childList.size();
	}
	@Override
	public boolean isExternal(TileNode<V, E> v) {

		
		return numChildren(v)==0 ;
	}

	@Override
	public int depth(TileNode<V, E> v) {
		if (v == root()) {
	        return 0;
	    }
	    
	    return 1 + depth(v.getParent());
	}

	@Override
	public int size() {
		
		return size;
	}

	@Override
	public boolean isEmpty() {
		
		return size==0;
	}
	
	
	public void insert(TileNode<V, E> parent, TileNode<V, E> child, String direction) {
	    switch (direction.toUpperCase()) {
	        case "FRONT":{
	            parent.setFront(child);
	            break;
	        }case "Left":{ 
	        	parent.setLeft(child);
	        	break;
	        }case "Right":{
	        	parent.setRight(child);
	        	break;
	        }
	    }
	    child.setParent(parent);
	    this.size++;
	    
	}

}
