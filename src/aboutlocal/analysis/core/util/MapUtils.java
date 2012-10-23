package aboutlocal.analysis.core.util;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Map;

public class MapUtils {
    
    @SuppressWarnings("unchecked")
    public static <K, V> void putIntoArrMap(K key, V value, Map<K, V[]> map) {
        V[] list = map.get(key);
        if (list == null) {
            list = (V[]) Array.newInstance(value.getClass(), 1);
            list[0] = value;
        } else {
            V[] newList = (V[]) Array.newInstance(value.getClass(), list.length + 1);
            System.arraycopy(list, 0, newList, 0, list.length);
            newList[newList.length - 1] = value;
            list = newList;
        }
        map.put(key, list);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> void putIntoListMap(K key, V value, Map<K, LinkedList<V>> map) {
        LinkedList<V> list = map.get(key);
        if (list == null) {
            list = new LinkedList<V>();
        }
        list.add(value);
        map.put(key, list);
    }

}
