package dataStructures.Dictionary.Interface;

import java.util.Iterator;
import dataStructures.Dictionary.models.Entry;

public interface IDictionary<K,V> {

	public Entry<K,V> find(K key);
	public Iterator<Entry<K,V>> findAll(K key);
	public Entry<K,V> insert(K key, V element);
	public void remove(Entry<K,V> entry);
	public int size();
	public boolean isEmpty();
	
}
