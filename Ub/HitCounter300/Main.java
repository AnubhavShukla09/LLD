import java.util.concurrent.atomic.AtomicInteger; // import utility classes
// ================= DATA HOLDER =================
class TimeBucket{
    volatile int timestamp; // latest timestamp (visibility across threads)
    AtomicInteger count; // atomic click count
    public TimeBucket(){
        this.timestamp=0; // initialize timestamp
        this.count=new AtomicInteger(0); // initialize atomic count
    }
}
// ================= MAIN SERVICE =================
class ClickCounter{
    private TimeBucket[] buckets; // circular buffer
    private static final int WINDOW_SIZE=300; // 5 minute window
    public ClickCounter(){
        this.buckets=new TimeBucket[WINDOW_SIZE]; // create array
        for(int i=0;i<WINDOW_SIZE;i++)buckets[i]=new TimeBucket(); // initialize
    }
    // ================= RECORD CLICK =================
    public void recordClick(int timestamp){ // no synchronized needed
        int index=timestamp%WINDOW_SIZE; // circular index
        TimeBucket bucket=buckets[index]; // get bucket
        if(bucket.timestamp!=timestamp){ // stale bucket
            synchronized(bucket){ // lock only this bucket (fine grained lock)
                if(bucket.timestamp!=timestamp){ // double check
                    bucket.timestamp=timestamp; // reset timestamp
                    bucket.count.set(0); // reset count
                }
            }
        }
        bucket.count.incrementAndGet(); // atomic increment
    }
    // ================= GET RECENT CLICKS =================
    public int getRecentClicks(int timestamp){
        int total=0; // store result
        for(int i=0;i<WINDOW_SIZE;i++){ // iterate all buckets
            TimeBucket bucket=buckets[i];
            if(timestamp-bucket.timestamp<WINDOW_SIZE){ // valid window
                total+=bucket.count.get(); // atomic read
            }
        }
        return total; // return total clicks
    }
}
public class Main{
    public static void main(String[] args){

        ClickCounter counter=new ClickCounter(); // create counter

        // simulate clicks
        counter.recordClick(1); // click at t=1
        counter.recordClick(1); // another click at t=1
        counter.recordClick(2); // click at t=2
        counter.recordClick(300); // click at t=300

        // check counts
        System.out.println("Clicks at t=300: "+counter.getRecentClicks(300)); 
        // should count clicks from [1,300]

        // after 300 seconds window shifts
        System.out.println("Clicks at t=301: "+counter.getRecentClicks(301)); 
        // click at t=1 should expire

        // more clicks
        counter.recordClick(301);
        counter.recordClick(302);

        System.out.println("Clicks at t=302: "+counter.getRecentClicks(302));
    }
}