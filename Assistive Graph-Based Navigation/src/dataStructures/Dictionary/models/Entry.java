package dataStructures.Dictionary.models;

/**
 * @author Thobela FF 223079625
 * @param <K>
 * @param <V>
 */
public class Entry<K, V> {
	
	private K key;
	private V Element;
	private Entry<K, V> next;
	
	
	public Entry(K key, V elem,Entry<K, V> next) {
		this.key=key;
		this.Element=elem;
		this.next=next;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getElement() {
		return Element;
	}
	
	public Entry<K,V> Next(){
		return this.next;
	}
	
	public void setNext(Entry<K,V> next) {
		this.next=next;
	}
}
