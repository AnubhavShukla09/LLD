import java.util.*; // import utility classes
// ================= MESSAGE =================
class Message{
    String eventType; // event type
    String content; // message content
    public Message(String eventType,String content){
        this.eventType=eventType; // assign type
        this.content=content; // assign message
    }
}
// ================= SUBSCRIBER (OBSERVER) =================
class Subscriber{
    String subscriberId; // unique id
    Set<String> eventTypes; // allowed event types
    int lastReadIndex; // index in global queue
    int processedCount; // total processed messages
    boolean isActive; // subscription state
    public Subscriber(String id,List<String> eventTypes,int startIndex){
        this.subscriberId=id;
        this.eventTypes=new HashSet<>(eventTypes); // store allowed events
        this.lastReadIndex=startIndex; // start from current queue size
        this.processedCount=0;
        this.isActive=true;
    }
    // ================= PROCESS NEW MESSAGES =================
    // Time Complexity: O(K) where K = new messages
    public void consume(List<Message> queue){
        while(lastReadIndex<queue.size()){ // consume pending messages
            Message msg=queue.get(lastReadIndex);
            if(eventTypes.contains(msg.eventType)) // process only matching
                processedCount++;
            lastReadIndex++; // move pointer
        }
    }
}
// ================= QUEUE MANAGER (SUBJECT) =================
class QueueManager{
    private List<Message> globalQueue; // global FIFO queue
    private Map<String,Subscriber> subscribers; // id -> subscriber
    public QueueManager(){
        globalQueue=new ArrayList<>(); // initialize queue
        subscribers=new HashMap<>(); // initialize subscriber map
    }
    // ================= ADD SUBSCRIBER =================
    // Time Complexity: O(1)
    public void addSubscriber(String subscriberId,List<String> eventTypesToProcess){
        Subscriber existing=subscribers.get(subscriberId);
        if(existing!=null){ // re-subscribe case
            existing.eventTypes=new HashSet<>(eventTypesToProcess); // replace types
            existing.lastReadIndex=globalQueue.size(); // start fresh
            existing.isActive=true;
            return;
        }
        // new subscriber
        Subscriber sub=new Subscriber(subscriberId,eventTypesToProcess,globalQueue.size());
        subscribers.put(subscriberId,sub);
    }
    // ================= REMOVE SUBSCRIBER =================
    // Time Complexity: O(1)
    public void removeSubscriber(String subscriberId){
        Subscriber sub=subscribers.get(subscriberId);
        if(sub!=null)sub.isActive=false; // deactivate
    }
    // ================= SEND MESSAGE =================
    // Time Complexity: O(S)
    public void sendMessage(String eventType,String message){
        Message msg=new Message(eventType,message);
        globalQueue.add(msg); // append to FIFO queue
        // notify all active subscribers
        for(Subscriber sub:subscribers.values()){
            if(sub.isActive)sub.consume(globalQueue); // each consumes at own pace
        }
    }
    // ================= COUNT PROCESSED =================
    // Time Complexity: O(1)
    public int countProcessedMessages(String subscriberId){
        Subscriber sub=subscribers.get(subscriberId);
        if(sub==null)return 0;
        return sub.processedCount;
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){

        QueueManager manager=new QueueManager();

        manager.addSubscriber("S1",Arrays.asList("ORDER","PAYMENT"));
        manager.addSubscriber("S2",Arrays.asList("PAYMENT"));

        manager.sendMessage("ORDER","order created");
        manager.sendMessage("PAYMENT","payment done");
        manager.sendMessage("SHIPMENT","item shipped");

        System.out.println(manager.countProcessedMessages("S1")); // 2
        System.out.println(manager.countProcessedMessages("S2")); // 1

        manager.removeSubscriber("S1");

        manager.sendMessage("ORDER","new order");

        System.out.println(manager.countProcessedMessages("S1")); // still 2

        manager.addSubscriber("S1",Arrays.asList("ORDER")); // resubscribe

        manager.sendMessage("ORDER","another order");

        System.out.println(manager.countProcessedMessages("S1")); // 3
    }
}