package aboutlocal.analysis.core.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scala.actors.threadpool.Arrays;

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
    
    public static String getMaxIntersectedValueFromMap(List<String> input, HashMap<String, String[]> map){
     // nonIndexed.put(companyName, file.getName().replace("_",
        // " "));
        ArrayList<ArrayList<String>> candidates = new ArrayList<>();
        for (String token : input) {
            String[] tokenResult = map.get(token);
            if (tokenResult != null) {
                candidates.add(new ArrayList<String>(Arrays.asList(tokenResult)));
            }
        }
        if (candidates.size() > 0) {
            ArrayList<String> intersection = candidates.get(0);
            for (ArrayList<String> set : candidates) {
                intersection.retainAll(set);
            }
            if (intersection.size() == 1)
                return intersection.get(0);
        }
        return null;
    }
    
    public static <T> T getMaxIntersectedValueFromMap(List<String> input, Map<String, LinkedList<T>> map, int minIntersection){
        HashMap<T, Integer> resultIntersectionCounts = new HashMap<>();
        for(String token:input){
            List<T> result = map.get(token);
            if(result!=null)
                for(T target:result){
                    Integer prevCount = resultIntersectionCounts.get(target);
                    resultIntersectionCounts.put(target, prevCount==null?1:prevCount+1);
                }
                    
        }
         Entry<T, Integer> highestIntersectionTargetEntry = new EmptyEntry<T>();
        for(Entry<T, Integer> target:resultIntersectionCounts.entrySet())
            if(target.getValue()>highestIntersectionTargetEntry.getValue())
                highestIntersectionTargetEntry = target;
        
        if(highestIntersectionTargetEntry.getKey() == null || highestIntersectionTargetEntry.getValue()<minIntersection)
            return null;
        else
            return highestIntersectionTargetEntry.getKey();
            
    }
    
    private static class EmptyEntry<T> implements Entry<T, Integer>{

        @Override
        public T getKey() {
            return null;
        }

        @Override
        public Integer getValue() {
            return -1;
        }

        @Override
        public Integer setValue(Integer value) {
            return null;
        }
        
    }

}
