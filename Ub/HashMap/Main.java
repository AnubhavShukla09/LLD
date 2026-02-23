import java.util.*; // only for List in bucket keys
// ================= NODE CLASS =================
class Node{
    String key; // key
    String value; // value
    Node next; // next in chain
    public Node(String key,String value){
        this.key=key; // assign key
        this.value=value; // assign value
        this.next=null; // initialize next
    }
}
// ================= CUSTOM HASH MAP =================
class CustomHashMap{
    private Node[] buckets; // bucket array
    private int capacity; // total buckets
    private int size; // number of entries
    private final double loadFactor=0.75; // resize threshold
    // ================= CONSTRUCTOR =================
    // Time Complexity: O(1)
    public CustomHashMap(int capacity){
        this.capacity=capacity; // assign capacity
        this.buckets=new Node[capacity]; // create buckets
        this.size=0; // initialize size
    }
    // ================= HASH INDEX =================
    // Time Complexity: O(1)
    private int getIndex(String key){
        return Math.abs(key.hashCode())%capacity; // bucket index
    }
    // ================= PUT =================
    // Time Complexity: O(1) avg, O(N) worst
    public void put(String key,String value){
        int index=getIndex(key); // compute index
        Node curr=buckets[index];
        // check if key exists
        while(curr!=null){
            if(curr.key.equals(key)){ // update existing
                curr.value=value;
                return;
            }
            curr=curr.next;
        }
        // insert at head
        Node newNode=new Node(key,value);
        newNode.next=buckets[index];
        buckets[index]=newNode;
        size++; // increase size
        // check resize
        if((double)size/capacity>=loadFactor)
            resize();
    }
    // ================= GET =================
    // Time Complexity: O(1) avg
    public String get(String key){
        int index=getIndex(key);
        Node curr=buckets[index];
        while(curr!=null){
            if(curr.key.equals(key))return curr.value;
            curr=curr.next;
        }
        return ""; // not found
    }
    // ================= REMOVE =================
    // Time Complexity: O(1) avg
    public String remove(String key){
        int index=getIndex(key);
        Node curr=buckets[index];
        Node prev=null;
        while(curr!=null){
            if(curr.key.equals(key)){
                String removed=curr.value;
                if(prev==null)buckets[index]=curr.next; // remove head
                else prev.next=curr.next; // remove middle
                size--; // decrease size
                return removed;
            }
            prev=curr;
            curr=curr.next;
        }
        return ""; // not found
    }
    // ================= RESIZE (REHASH) =================
    // Time Complexity: O(N)
    private void resize(){
        int oldCapacity=capacity;
        capacity=capacity*2; // double capacity
        Node[] oldBuckets=buckets;
        buckets=new Node[capacity];
        int oldSize=size;
        size=0; // will reinsert
        // rehash all entries
        for(Node head:oldBuckets){
            Node curr=head;
            while(curr!=null){
                put(curr.key,curr.value);
                curr=curr.next;
            }
        }
        size=oldSize; // restore correct size
    }
    // ================= GET BUCKET KEYS =================
    // Time Complexity: O(k log k)
    public List<String> getBucketKeys(int bucketIndex){
        List<String> result=new ArrayList<>();
        if(bucketIndex<0||bucketIndex>=capacity)return result;
        Node curr=buckets[bucketIndex];
        while(curr!=null){
            result.add(curr.key);
            curr=curr.next;
        }
        Collections.sort(result); // lexicographic order
        return result;
    }
    // ================= SIZE =================
    public int size(){return size;} // O(1)
    // ================= CAPACITY =================
    public int capacity(){return capacity;} // O(1)
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){
        CustomHashMap map=new CustomHashMap(4); // initial capacity
        map.put("apple","red");
        map.put("banana","yellow");
        map.put("grape","green");
        System.out.println(map.get("apple")); // red
        System.out.println(map.get("banana")); // yellow
        System.out.println(map.remove("banana")); // yellow
        System.out.println(map.get("banana")); // ""
        System.out.println("Size: "+map.size());
        System.out.println("Capacity: "+map.capacity());
        System.out.println(map.getBucketKeys(0));
        System.out.println(map.getBucketKeys(1));
    }
}