package dataStructures.tree.interfaces;

import java.util.Iterator;

import dataStructures.tree.models.TileNode;

public interface ITree<V, E> {


    public TileNode<V, E> root();
    public TileNode<V, E> parent(TileNode<V, E> v);
    public Iterable<TileNode<V, E>> children(TileNode<V, E> v);
    public int numChildren(TileNode<V, E> v);
    public boolean isExternal(TileNode<V, E> v);
    public int depth(TileNode<V, E> v);
    public int size();
    public boolean isEmpty();
    public void insert(TileNode<V, E> parent, TileNode<V, E> child, String direction);
       
}
