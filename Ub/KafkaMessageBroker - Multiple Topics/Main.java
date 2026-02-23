import java.util.*; // import utility classes
class Message{
    private long offset; // message offset
    private String data; // message payload
    public Message(long offset,String data){
        this.offset=offset; // assign offset
        this.data=data; // assign data
    }
    public long getOffset(){return offset;} // return offset
    public String getData(){return data;} // return data
}
class Topic{
    private String name; // topic name
    private List<Message> messages; // append-only log
    public Topic(String name){
        this.name=name; // assign name
        this.messages=new ArrayList<>(); // initialize list
    }
    public synchronized long publish(String data){ // O(1)
        long offset=messages.size(); // next offset
        messages.add(new Message(offset,data)); // append message
        return offset; // return offset
    }
    public synchronized Message getMessage(long offset){ // O(1)
        if(offset<0 || offset>=messages.size())return null; // invalid offset
        return messages.get((int)offset); // return message
    }
    public synchronized int size(){return messages.size();} // O(1)
}
class Producer{
    private String producerId; // producer identifier
    private MessageQueue queue; // broker reference
    public Producer(String producerId,MessageQueue queue){
        this.producerId=producerId; // assign id
        this.queue=queue; // assign queue
    }
    public long publish(String topicName,String data){ // O(1)
        return queue.publish(topicName,data); // delegate to broker
    }
    public String getProducerId(){return producerId;} // return id
}
class Consumer{
    private String consumerId; // consumer identifier
    private Map<String,Long> offsets; // topic -> current offset
    public Consumer(String consumerId){
        this.consumerId=consumerId; // assign id
        this.offsets=new HashMap<>(); // initialize offsets
    }
    public String getConsumerId(){return consumerId;} // return id
    public long getOffset(String topic){ // O(1)
        return offsets.getOrDefault(topic,0L); // return current offset
    }
    public void updateOffset(String topic,long offset){ // O(1)
        offsets.put(topic,offset); // update offset
    }
}
class MessageQueue{
    private Map<String,Topic> topics; // topic registry
    public MessageQueue(){
        this.topics=new HashMap<>(); // initialize map
    }
    public synchronized void createTopic(String topicName){ // O(1)
        topics.putIfAbsent(topicName,new Topic(topicName)); // create topic if absent
    }
    public synchronized long publish(String topicName,String data){ // O(1)
        Topic topic=topics.get(topicName); // fetch topic
        if(topic==null)throw new RuntimeException("Topic not found"); // validation
        return topic.publish(data); // append message
    }
    public synchronized void subscribe(String topicName,Consumer consumer){ // O(1)
        if(!topics.containsKey(topicName))throw new RuntimeException("Topic not found"); // validation
        consumer.updateOffset(topicName,0); // start from offset 0
    }
    public synchronized String poll(String topicName,Consumer consumer){ // O(1)
        Topic topic=topics.get(topicName); // fetch topic
        if(topic==null)throw new RuntimeException("Topic not found"); // validation
        long offset=consumer.getOffset(topicName); // fetch consumer offset
        Message message=topic.getMessage(offset); // get message
        if(message==null)return null; // no new message
        consumer.updateOffset(topicName,offset+1); // advance offset
        return message.getData(); // return data
    }
}
public class Main{
    public static void main(String[] args){
        MessageQueue queue=new MessageQueue(); // create broker
        queue.createTopic("orders"); // create topic
        Producer p1=new Producer("P1",queue); // create producer 1
        Producer p2=new Producer("P2",queue); // create producer 2
        Consumer c1=new Consumer("C1"); // create consumer 1
        Consumer c2=new Consumer("C2"); // create consumer 2
        queue.subscribe("orders",c1); // subscribe consumers
        queue.subscribe("orders",c2);
        p1.publish("orders","order-1"); // producer publishes
        p2.publish("orders","order-2");
        System.out.println(c1.getConsumerId()+" -> "+queue.poll("orders",c1)); // order-1
        System.out.println(c1.getConsumerId()+" -> "+queue.poll("orders",c1)); // order-2
        System.out.println(c2.getConsumerId()+" -> "+queue.poll("orders",c2)); // order-1 (independent offset)
    }
}