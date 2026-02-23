import java.util.*; // import utility classes
class TrainAssignment{
    String trainId; // train identifier
    int platformNumber; // assigned platform
    int startTime; // arrival time inclusive
    int endTime; // departure time inclusive
    public TrainAssignment(String trainId,int platformNumber,int start,int end){
        this.trainId=trainId; // assign id
        this.platformNumber=platformNumber; // assign platform
        this.startTime=start; // assign start
        this.endTime=end; // assign end
    }
}
class Platform{
    private int platformNumber; // platform id
    private PriorityQueue<TrainAssignment> minHeap; // earliest ending assignment first
    private List<TrainAssignment> allAssignments; // store all for time queries
    public Platform(int platformNumber){
        this.platformNumber=platformNumber; // assign id
        this.minHeap=new PriorityQueue<>(Comparator.comparingInt(a->a.endTime)); // min heap by end time
        this.allAssignments=new ArrayList<>(); // store assignments
    }
    public int getPlatformNumber(){return platformNumber;} // return id
    public int getEarliestAvailableTime(int arrivalTime){ // O(N)
        int current=arrivalTime; // start from arrival
        for(TrainAssignment a:allAssignments){ // scan assignments
            if(current>=a.startTime && current<=a.endTime)
                current=a.endTime+1; // move after busy interval
        }
        return current; // return free time
    }
    public void addAssignment(TrainAssignment assignment){ // O(log N)
        minHeap.offer(assignment); // add to heap
        allAssignments.add(assignment); // store assignment
    }
    public String getTrainAt(int time){ // O(N)
        for(TrainAssignment a:allAssignments){ // scan assignments
            if(a.startTime<=time && time<=a.endTime)
                return a.trainId; // found train
        }
        return ""; // no train
    }
}
class TrainPlatformManager{
    private List<Platform> platforms; // all platforms
    private Map<String,List<TrainAssignment>> trainSchedule; // train -> assignments
    public TrainPlatformManager(int platformCount){ // O(P)
        platforms=new ArrayList<>(); // initialize list
        trainSchedule=new HashMap<>(); // initialize map
        for(int i=0;i<platformCount;i++) // create platforms
            platforms.add(new Platform(i));
    }
    public synchronized String assignPlatform(String trainId,int arrivalTime,int waitTime){ // O(P*N)
        int bestPlatform=-1; // chosen platform
        int minDelay=Integer.MAX_VALUE; // minimum delay
        int bestStart=0; // chosen start time
        for(Platform p:platforms){ // try each platform
            int freeTime=p.getEarliestAvailableTime(arrivalTime); // O(N)
            int delay=freeTime-arrivalTime; // compute delay
            if(delay<minDelay || (delay==minDelay && p.getPlatformNumber()<bestPlatform)){
                minDelay=delay; // update best delay
                bestPlatform=p.getPlatformNumber(); // update platform
                bestStart=freeTime; // update start
            }
        }
        int departure=bestStart+waitTime-1; // compute end time
        TrainAssignment assignment=new TrainAssignment(trainId,bestPlatform,bestStart,departure); // create assignment
        Platform platform=platforms.get(bestPlatform); // fetch platform
        platform.addAssignment(assignment); // O(log N)
        trainSchedule.putIfAbsent(trainId,new ArrayList<>()); // init train schedule
        trainSchedule.get(trainId).add(assignment); // store assignment
        return bestPlatform+","+minDelay; // return platform and delay
    }
    public synchronized String getTrainAtPlatform(int platformNumber,int timestamp){ // O(N)
        if(platformNumber<0 || platformNumber>=platforms.size())return ""; // invalid platform
        return platforms.get(platformNumber).getTrainAt(timestamp); // delegate
    }
    public synchronized int getPlatformOfTrain(String trainId,int timestamp){ // O(T)
        List<TrainAssignment> list=trainSchedule.get(trainId); // fetch train schedule
        if(list==null)return -1; // no such train
        for(TrainAssignment a:list){ // scan assignments
            if(a.startTime<=timestamp && timestamp<=a.endTime)
                return a.platformNumber; // found platform
        }
        return -1; // not present
    }
}
public class Main{
    public static void main(String[] args){
        TrainPlatformManager manager=new TrainPlatformManager(3); // create manager
        System.out.println(manager.assignPlatform("T1",10,5)); // assign train
        System.out.println(manager.assignPlatform("T2",10,5)); // assign train
        System.out.println(manager.assignPlatform("T3",10,5)); // assign train
        System.out.println(manager.assignPlatform("T4",10,5)); // delayed assignment
        System.out.println(manager.getTrainAtPlatform(0,12)); // query train
        System.out.println(manager.getPlatformOfTrain("T3",12)); // query platform
    }
}