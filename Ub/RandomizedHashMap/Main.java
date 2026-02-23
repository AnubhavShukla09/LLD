import java.util.*; // import utility classes
class KeyValuePair<K,V>{
    private K key; // key
    private V value; // value
    public KeyValuePair(K key,V value){
        this.key=key; // assign key
        this.value=value; // assign value
    }
    public K getKey(){return key;} // return key
    public V getValue(){return value;} // return value
    public void setValue(V value){this.value=value;} // update value
}
class RandomizedMap<K,V>{
    private Map<K,Integer> indexMap; // key -> index in array
    private List<KeyValuePair<K,V>> entries; // store key,value pairs
    private Random random; // random generator
    public RandomizedMap(){
        this.indexMap=new HashMap<>(); // initialize map
        this.entries=new ArrayList<>(); // initialize array
        this.random=new Random(); // initialize random
    }
    public synchronized void set(K key,V value){ // O(1)
        if(indexMap.containsKey(key)){ // update existing key
            int index=indexMap.get(key); // fetch index
            entries.get(index).setValue(value); // update value
        }else{
            entries.add(new KeyValuePair<>(key,value)); // add new entry
            indexMap.put(key,entries.size()-1); // store index
        }
    }
    public synchronized V get(K key){ // O(1)
        Integer index=indexMap.get(key); // fetch index
        if(index==null)return null; // key not found
        return entries.get(index).getValue(); // return value
    }
    public synchronized void delete(K key){ // O(1)
        Integer index=indexMap.get(key); // fetch index
        if(index==null)return; // key not present
        int lastIndex=entries.size()-1; // last element index
        KeyValuePair<K,V> lastEntry=entries.get(lastIndex); // fetch last entry
        Collections.swap(entries,index,lastIndex); // swap with last element
        indexMap.put(lastEntry.getKey(),index); // update swapped index
        entries.remove(lastIndex); // remove last element
        indexMap.remove(key); // remove key mapping
    }
    public synchronized KeyValuePair<K,V> getRandom(){ // O(1)
        if(entries.isEmpty())return null; // empty structure
        int index=random.nextInt(entries.size()); // generate random index
        return entries.get(index); // return random pair
    }
}
public class Main{
    public static void main(String[] args){
        RandomizedMap<String,Integer> map=new RandomizedMap<>(); // create map
        map.set("A",10); // insert
        map.set("B",20); // insert
        map.set("C",30); // insert
        System.out.println(map.get("A")); // get value
        KeyValuePair<String,Integer> randomPair=map.getRandom(); // random entry
        if(randomPair!=null)
            System.out.println(randomPair.getKey()+" -> "+randomPair.getValue());
        map.delete("B"); // delete key
        System.out.println(map.get("B")); // should be null
    }
}