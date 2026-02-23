import java.util.*; // import utility classes
class Node{
    int key; // cache key
    int value; // cache value
    Node prev; // previous node
    Node next; // next node
    public Node(int key,int value){
        this.key=key; // assign key
        this.value=value; // assign value
    }
}
class LRUCache{
    private int capacity; // max capacity
    private Map<Integer,Node> cache; // key -> node lookup
    private Node head; // dummy head (most recent side)
    private Node tail; // dummy tail (least recent side)
    public LRUCache(int capacity){ // O(1)
        this.capacity=capacity; // assign capacity
        this.cache=new HashMap<>(); // initialize map
        head=new Node(0,0); // create dummy head
        tail=new Node(0,0); // create dummy tail
        head.next=tail; // connect head -> tail
        tail.prev=head; // connect tail -> head
    }
    private void removeNode(Node node){ // O(1)
        node.prev.next=node.next; // unlink node
        node.next.prev=node.prev; // unlink node
    }
    private void addToHead(Node node){ // O(1)
        node.next=head.next; // insert after head
        node.prev=head;
        head.next.prev=node;
        head.next=node;
    }
    private Node removeTail(){ // O(1)
        Node node=tail.prev; // least recently used
        removeNode(node); // remove it
        return node;
    }
    public synchronized int get(int key){ // O(1)
        Node node=cache.get(key); // lookup node
        if(node==null)return -1; // not found
        removeNode(node); // move to most recent
        addToHead(node);
        return node.value; // return value
    }
    public synchronized void put(int key,int value){ // O(1)
        Node node=cache.get(key); // check if exists
        if(node!=null){ // update existing
            node.value=value; // update value
            removeNode(node); // move to head
            addToHead(node);
        }else{
            Node newNode=new Node(key,value); // create new node
            cache.put(key,newNode); // add to map
            addToHead(newNode); // add to head
            if(cache.size()>capacity){ // capacity exceeded
                Node lru=removeTail(); // remove least used
                cache.remove(lru.key); // remove from map
            }
        }
    }
}
public class Main{
    public static void main(String[] args){
        LRUCache cache=new LRUCache(2); // capacity=2
        cache.put(1,10); // insert
        cache.put(2,20); // insert
        System.out.println(cache.get(1)); // 10 (moves to recent)
        cache.put(3,30); // evicts key=2
        System.out.println(cache.get(2)); // -1 (not found)
        System.out.println(cache.get(3)); // 30
    }
}