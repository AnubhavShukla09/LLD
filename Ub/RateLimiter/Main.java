import java.util.*; // import utility classes

// ================= STRATEGY INTERFACE =================
interface RateLimitStrategy{
    boolean isAllowed(int timestamp); // check if request allowed
}
// ================= FIXED WINDOW COUNTER =================
class FixedWindowCounterStrategy implements RateLimitStrategy{
    private int maxRequests; // allowed requests
    private int timeWindow; // window size (seconds)
    private int currentWindowStart; // window start timestamp
    private int count; // request count in window
    public FixedWindowCounterStrategy(int maxRequests,int timeWindow){
        this.maxRequests=maxRequests; // assign limit
        this.timeWindow=timeWindow; // assign window
        this.currentWindowStart=-1; // initialize
        this.count=0; // initialize count
    }
    // Time Complexity: O(1)
    public boolean isAllowed(int timestamp){
        if(currentWindowStart==-1) // first request
            currentWindowStart=timestamp;
        // check if new window started
        if(timestamp-currentWindowStart>=timeWindow){
            currentWindowStart=timestamp; // reset window
            count=0; // reset count
        }
        if(count<maxRequests){ // allow request
            count++;
            return true;
        }
        return false; // reject
    }
}

// ================= SLIDING WINDOW COUNTER =================
class SlidingWindowCounterStrategy implements RateLimitStrategy{
    private int maxRequests; // allowed requests
    private int timeWindow; // window size
    private Deque<Integer> timestamps; // store request times
    public SlidingWindowCounterStrategy(int maxRequests,int timeWindow){
        this.maxRequests=maxRequests; // assign limit
        this.timeWindow=timeWindow; // assign window
        this.timestamps=new ArrayDeque<>(); // initialize queue
    }
    // Time Complexity: O(k) amortized
    public boolean isAllowed(int timestamp){
        // remove expired timestamps
        while(!timestamps.isEmpty() && timestamp-timestamps.peekFirst()>=timeWindow)
            timestamps.pollFirst(); // remove old
        if(timestamps.size()<maxRequests){ // allow request
            timestamps.offerLast(timestamp); // add timestamp
            return true;
        }
        return false; // reject
    }
}

// ================= RESOURCE CONFIG =================
class ResourceConfig{
    RateLimitStrategy strategy; // associated strategy
    public ResourceConfig(RateLimitStrategy strategy){
        this.strategy=strategy; // assign strategy
    }
}
// ================= MAIN RATE LIMITER =================
class RateLimiter{
    private Map<String,ResourceConfig> resources; // resourceId -> config
    public RateLimiter(){
        this.resources=new HashMap<>(); // initialize map
    }
    // ================= ADD RESOURCE =================
    // Time Complexity: O(1)
    public void addResource(String resourceId,String strategyName,String limits){
        String[] parts=limits.split(","); // parse limits
        int maxRequests=Integer.parseInt(parts[0]); // max count
        int timePeriod=Integer.parseInt(parts[1]); // window size
        RateLimitStrategy strategy=null; // strategy instance
        // create strategy
        if(strategyName.equals("fixed-window-counter"))
            strategy=new FixedWindowCounterStrategy(maxRequests,timePeriod);
        else if(strategyName.equals("sliding-window-counter"))
            strategy=new SlidingWindowCounterStrategy(maxRequests,timePeriod);
        else return; // invalid strategy
        resources.put(resourceId,new ResourceConfig(strategy)); // add/update resource
    }
    // ================= CHECK REQUEST =================
    // Time Complexity: depends on strategy
    public boolean isAllowed(String resourceId,int timestamp){
        ResourceConfig config=resources.get(resourceId); // fetch config
        if(config==null)return false; // invalid resource
        return config.strategy.isAllowed(timestamp); // delegate
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){ // Time Complexity: O(1)
        RateLimiter limiter=new RateLimiter(); // create limiter
        limiter.addResource("API1","fixed-window-counter","3,5"); // 3 requests per 5 sec
        limiter.addResource("API2","sliding-window-counter","2,3"); // 2 requests per 3 sec
        System.out.println(limiter.isAllowed("API1",1)); // true
        System.out.println(limiter.isAllowed("API1",2)); // true
        System.out.println(limiter.isAllowed("API1",3)); // true
        System.out.println(limiter.isAllowed("API1",4)); // false
        System.out.println("----");
        System.out.println(limiter.isAllowed("API2",1)); // true
        System.out.println(limiter.isAllowed("API2",2)); // true
        System.out.println(limiter.isAllowed("API2",3)); // false
        System.out.println(limiter.isAllowed("API2",5)); // true
    }
}