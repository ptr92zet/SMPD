
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Piotr
 */
public class Tuple<K, V> implements Map.Entry<K, V>{
    private final K key;
    private V value;
    
    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    public K getKey() {
        return key;
    }
    
    public V getValue() {
        return value;
    }

    public V setValue(V val) {
        V oldValue = this.value;
        this.value = val;
        return oldValue;
    }
}
