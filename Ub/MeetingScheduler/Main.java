import java.util.*; // import utility classes
class ActiveMeeting{
    String meetingId; // meeting identifier
    String roomId; // assigned room
    int start; // start time
    int end; // end time
    public ActiveMeeting(String meetingId,String roomId,int start,int end)
    {this.meetingId=meetingId;this.roomId=roomId;this.start=start;this.end=end;} // constructor
}
class MeetingRoomSystem{
    private TreeSet<String> availableRooms; // sorted available rooms
    private PriorityQueue<ActiveMeeting> minHeap; // min heap ordered by end time
    private Map<String,ActiveMeeting> activeMeetings; // active meeting map
    public MeetingRoomSystem(List<String> roomIds){
        this.availableRooms=new TreeSet<>(roomIds); // initialize available rooms sorted
        this.minHeap=new PriorityQueue<>(Comparator.comparingInt(m->m.end)); // heap ordered by end time
        this.activeMeetings=new HashMap<>(); // initialize meeting map
    }
    public synchronized String bookMeeting(String meetingId,int start,int end){ // O(log R + log M)
        if(activeMeetings.containsKey(meetingId))return "MeetingId Already Exists"; // check duplicate id
        if(start>=end)return "Invalid Time Interval"; // validate interval
        while(!minHeap.isEmpty()&&minHeap.peek().end<=start){ // free all finished meetings
            ActiveMeeting finished=minHeap.poll(); // remove earliest finishing meeting
            availableRooms.add(finished.roomId); // mark its room as available
            activeMeetings.remove(finished.meetingId); // remove from active map
        }
        if(availableRooms.isEmpty())return "No Rooms Available"; // if no room free
        String roomId=availableRooms.pollFirst(); // get smallest available room
        ActiveMeeting meeting=new ActiveMeeting(meetingId,roomId,start,end); // create meeting object
        minHeap.offer(meeting); // add to heap
        activeMeetings.put(meetingId,meeting); // store in active map
        return roomId; // return assigned room
    }
    public synchronized void cancelMeeting(String meetingId){ // O(log M)
        ActiveMeeting meeting=activeMeetings.get(meetingId); // fetch meeting
        if(meeting==null){System.out.println("Invalid MeetingId");return;} // handle invalid id
        minHeap.remove(meeting); // remove from heap
        availableRooms.add(meeting.roomId); // mark room available
        activeMeetings.remove(meetingId); // remove from map
        System.out.println("Meeting Cancelled"); // confirmation
    }
}
public class Main{
    public static void main(String[] args){
        List<String> roomIds=Arrays.asList("RoomC","RoomA","RoomB"); // predefined rooms
        MeetingRoomSystem system=new MeetingRoomSystem(roomIds); // initialize system

        String r1=system.bookMeeting("M1",10,20); // book first meeting
        System.out.println("M1 assigned to:"+r1); // print room

        String r2=system.bookMeeting("M2",12,18); // overlapping
        System.out.println("M2 assigned to:"+r2); // next room

        String r3=system.bookMeeting("M3",15,25); // overlapping
        System.out.println("M3 assigned to:"+r3); // next room

        system.cancelMeeting("M2"); // cancel one meeting

        String r4=system.bookMeeting("M4",16,19); // should reuse freed room
        System.out.println("M4 assigned to:"+r4); // print room
    }
}

//==============================================//==============================================//

class Meeting{
    String meetingId; // meeting identifier
    int start; // start time
    int end; // end time
    public Meeting(String meetingId,int start,int end){
        this.meetingId=meetingId; // assign id
        this.start=start; // assign start
        this.end=end; // assign end
    }
}

class MeetingRoom{
    private String roomId; // room id
    private TreeMap<Integer,Meeting> calendar; // startTime -> meeting sorted by start

    public MeetingRoom(String roomId){
        this.roomId=roomId; // assign room id
        this.calendar=new TreeMap<>(); // initialize calendar
    }

    public String getRoomId(){return roomId;} // return room id

    public boolean isAvailable(int start,int end){ // O(log N) conflict check
        Map.Entry<Integer,Meeting> prev=calendar.floorEntry(start); // previous meeting
        if(prev!=null && prev.getValue().end>start)return false; // overlap with previous

        Map.Entry<Integer,Meeting> next=calendar.ceilingEntry(start); // next meeting
        if(next!=null && next.getKey()<end)return false; // overlap with next

        return true; // no conflict
    }

    public boolean book(String meetingId,int start,int end){ // O(log N)
        if(!isAvailable(start,end))return false; // check availability
        calendar.put(start,new Meeting(meetingId,start,end)); // add meeting
        return true; // success
    }

    public boolean cancel(String meetingId){ // O(N)
        Iterator<Map.Entry<Integer,Meeting>> it=calendar.entrySet().iterator(); // iterator
        while(it.hasNext()){ // iterate meetings
            Map.Entry<Integer,Meeting> entry=it.next(); // get entry
            if(entry.getValue().meetingId.equals(meetingId)){ // match id
                it.remove(); // remove meeting
                return true; // success
            }
        }
        return false; // not found
    }
}

class MeetingRoomSystem1{
    private Map<String,MeetingRoom> rooms; // roomId -> room
    private Map<String,String> meetingToRoom; // meetingId -> roomId

    public MeetingRoomSystem1(List<String> roomIds){
        this.rooms=new HashMap<>(); // initialize rooms map
        this.meetingToRoom=new HashMap<>(); // initialize mapping

        for(String id:roomIds)rooms.put(id,new MeetingRoom(id)); // create rooms
    }

    public synchronized String bookMeeting(String meetingId,int start,int end){ // O(R log M)
        if(meetingToRoom.containsKey(meetingId))return "MeetingId Already Exists"; // duplicate id check
        if(start>=end)return "Invalid Time Interval"; // validate time

        for(MeetingRoom room:rooms.values()){ // try booking in each room
            if(room.book(meetingId,start,end)){ // if room available
                meetingToRoom.put(meetingId,room.getRoomId()); // store mapping
                return room.getRoomId(); // return assigned room
            }
        }
        return "No Rooms Available"; // no room free
    }

    public synchronized void cancelMeeting(String meetingId){ // O(log M + N)
        String roomId=meetingToRoom.get(meetingId); // find assigned room
        if(roomId==null){System.out.println("Invalid MeetingId");return;} // validation

        MeetingRoom room=rooms.get(roomId); // fetch room
        room.cancel(meetingId); // cancel meeting in room calendar
        meetingToRoom.remove(meetingId); // remove mapping
        System.out.println("Meeting Cancelled"); // confirmation
    }
}
// public class Main{
//     public static void main(String[] args){
//         List<String> roomIds=Arrays.asList("RoomA","RoomB","RoomC"); // predefined rooms
//         MeetingRoomSystem system=new MeetingRoomSystem(roomIds); // initialize system

//         String r1=system.bookMeeting("M1",10,20); // book meeting
//         System.out.println("M1 assigned to:"+r1); // print room

//         String r2=system.bookMeeting("M2",12,18); // overlapping time
//         System.out.println("M2 assigned to:"+r2); // print room

//         String r3=system.bookMeeting("M3",15,25); // overlapping time
//         System.out.println("M3 assigned to:"+r3); // print room

//         system.cancelMeeting("M2"); // cancel meeting

//         String r4=system.bookMeeting("M4",16,19); // reuse freed room slot
//         System.out.println("M4 assigned to:"+r4); // print room
//     }
// }