package dataStructures.Dictionary.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import dataStructures.Dictionary.Interface.IDictionary;
/**
 * @author Thobela FF 223079625
 * @param <K>
 * @param <V>
 */
public class HashDictionary<K, V> implements IDictionary<K,V> {

  
    private Entry<K,V>[] table;
    private int size = 0;
    
    @SuppressWarnings("unchecked")
    public HashDictionary(int capacity) {
        this.table = new Entry[capacity];
    }

    private int hash(K key) {
        return Math.abs(key.hashCode()) % table.length;
    }

    //returns the first entry matching the key
    @Override
    public Entry<K, V> find(K key) {
        int index = hash(key);
        Entry<K,V> current = table[index];
        
       
        while(current != null) {
            if(current.getKey().equals(key)) {
                return current;
            }
            current = current.Next();
        }
        return null;
    }

    //finds all entries matching the key
    @Override
    public Iterator<Entry<K, V>> findAll(K key) {
        List<Entry<K,V>> list = new ArrayList<>();
        int index = hash(key);
        Entry<K,V> current = table[index];
        
        while(current != null) {
            if(current.getKey().equals(key)) {
                list.add(current);
            }
            current = current.Next();
        }
        return list.iterator();
    }

    //inserts an entry
    @Override
    public Entry<K, V> insert(K key, V element) {
        int index = hash(key);
        
        Entry<K,V> newEntry = new Entry<>(key, element, table[index]);
        table[index] = newEntry;
        
        size++;
        return newEntry;
    }

    //removes an entry from dictionary
    @Override
    public void remove(Entry<K, V> entry) {
        int index = hash(entry.getKey());
        Entry<K,V> current = table[index];
        Entry<K,V> prev = null;

        while (current != null) {
            if (current == entry) { 
                if (prev == null) {
                    table[index] = current.Next(); 
                } else {
                    prev.setNext(current.Next()); 
                }
                size--;
                return;
            }
            prev = current;
            current = current.Next();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    //returns an iterator of entries
    public Iterator<Entry<K, V>> entries() {
        List<Entry<K, V>> allEntries = new ArrayList<>();
        for (Entry<K, V> entry : table) {
            Entry<K, V> current = entry;
            while (current != null) {
                allEntries.add(current);
                current = current.Next();
            }
        }
        return allEntries.iterator();
    }
    
    //returns an iterator of keys 
    public Iterator<K> Keys(){
    	//this was tailored for our solution as we ensured that each index has linked list of entries with exactly the same key
    	List<K> keys= new ArrayList<>();
    	
    	
    	for(Entry<K,V> e: table) {
    		keys.add(e.getKey());
    	}
    	
    	return keys.iterator();
    }
}