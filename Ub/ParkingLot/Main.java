import java.util.*; // import utility classes
enum VehicleType{CAR,BIKE,TRUCK} // define vehicle types
enum SpotSize{SMALL,MEDIUM,LARGE} // define spot sizes
enum SpotStatus{FREE,OCCUPIED} // define spot status
class Vehicle{
    private String vehicleNumber; // store vehicle number
    private VehicleType type; // store vehicle type
    public Vehicle(String vehicleNumber,VehicleType type){this.vehicleNumber=vehicleNumber;this.type=type;} // constructor
    public VehicleType getType(){return type;} // return vehicle type
    public String getVehicleNumber(){return vehicleNumber;} // return vehicle number
    public SpotSize getRequiredSize(){ // determine required spot size for this vehicle
        if(type==VehicleType.BIKE)return SpotSize.SMALL; // bike requires small
        if(type==VehicleType.CAR)return SpotSize.MEDIUM; // car requires medium
        if(type==VehicleType.TRUCK)return SpotSize.LARGE; // truck requires large
        return null; // fallback
    }
}
class ParkingSpot{
    private int spotId; // unique spot id
    private SpotSize size; // size of the spot
    private SpotStatus status; // spot status
    private Vehicle vehicle; // vehicle parked in this spot
    private int floorNumber; // floor number where spot exists
    public ParkingSpot(int spotId,SpotSize size,int floorNumber){
        this.spotId=spotId; // assign id
        this.size=size; // assign size
        this.floorNumber=floorNumber; // assign floor
        this.status=SpotStatus.FREE; // initially free
    }
    public boolean isFree(){return status==SpotStatus.FREE;} // check if spot is free
    public void park(Vehicle vehicle){
        this.vehicle=vehicle; // assign vehicle
        this.status=SpotStatus.OCCUPIED; // update status
    }
    public void unpark(){
        this.vehicle=null; // remove vehicle
        this.status=SpotStatus.FREE; // mark free
    }
    public SpotSize getSize(){return size;} // return size of spot
    public int getSpotId(){return spotId;} // return spot id
    public int getFloorNumber(){return floorNumber;} // return floor number
    public boolean canFitVehicle(Vehicle vehicle){ // check size compatibility
        return this.size.ordinal()>=vehicle.getRequiredSize().ordinal(); // allow bigger spots
    }
}
class ParkingFloor{
    private int floorNumber; // floor identifier
    private List<ParkingSpot> spots; // list of spots on floor
    public ParkingFloor(int floorNumber,List<ParkingSpot> spots){this.floorNumber=floorNumber;this.spots=spots;} // constructor
    public int getFloorNumber(){return floorNumber;} // return floor number
    public List<ParkingSpot> getSpots(){return spots;} // return spots list
}
class Ticket{
    private String ticketId; // unique ticket id
    private ParkingSpot spot; // allocated parking spot
    private Vehicle vehicle; // parked vehicle
    private long entryTime; // parking entry time
    public Ticket(String ticketId,ParkingSpot spot,Vehicle vehicle){
        this.ticketId=ticketId; // assign id
        this.spot=spot; // assign spot
        this.vehicle=vehicle; // assign vehicle
        this.entryTime=System.currentTimeMillis(); // store entry time
    }
    public String getTicketId(){return ticketId;} // return ticket id
    public ParkingSpot getSpot(){return spot;} // return allocated spot
    public Vehicle getVehicle(){return vehicle;} // return vehicle
    public long getEntryTime(){return entryTime;} // return entry time
}
interface SlotAllocationStrategy{
    ParkingSpot allocate(List<ParkingFloor> floors,Vehicle vehicle); // allocation method
}
class FirstFitStrategy implements SlotAllocationStrategy{
    public ParkingSpot allocate(List<ParkingFloor> floors,Vehicle vehicle){ // iterate floors and spots to find first compatible
        for(ParkingFloor floor:floors){ // iterate through each floor
            for(ParkingSpot spot:floor.getSpots()){ // iterate through each spot
                if(spot.isFree()&&spot.canFitVehicle(vehicle))return spot; // first compatible spot
            }
        }
        return null; // return null if no spot found
    }
}
interface PricingStrategy{
    double calculateFee(long durationMillis,Vehicle vehicle); // pricing contract
}
class SimplePricingStrategy implements PricingStrategy{
    private static final double BIKE_RATE_PER_HOUR=10; // bike rate per hour
    private static final double CAR_RATE_PER_HOUR=20; // car rate per hour
    private static final double TRUCK_RATE_PER_HOUR=50; // truck rate per hour
    public double calculateFee(long durationMillis,Vehicle vehicle){
        double hours=Math.ceil(durationMillis/(1000.0*60*60)); // convert to hours round up
        if(vehicle.getType()==VehicleType.BIKE)return hours*BIKE_RATE_PER_HOUR; // bike cost
        if(vehicle.getType()==VehicleType.CAR)return hours*CAR_RATE_PER_HOUR; // car cost
        if(vehicle.getType()==VehicleType.TRUCK)return hours*TRUCK_RATE_PER_HOUR; // truck cost
        return 0; // fallback
    }
}
class ParkingLot{
    private List<ParkingFloor> floors; // list of floors
    private Map<String,Ticket> activeTickets; // map of active tickets
    private SlotAllocationStrategy allocationStrategy; // allocation strategy
    private PricingStrategy pricingStrategy; // pricing strategy
    public ParkingLot(List<ParkingFloor> floors){
        this.floors=floors; // initialize floors
        this.activeTickets=new HashMap<>(); // initialize ticket map
        this.allocationStrategy=new FirstFitStrategy(); // set allocation strategy
        this.pricingStrategy=new SimplePricingStrategy(); // set pricing strategy
    }
    public synchronized String park(Vehicle vehicle){ // O(F × S)
        ParkingSpot spot=allocationStrategy.allocate(floors,vehicle); // allocate spot
        if(spot==null)return "Parking Full"; // if no spot available
        spot.park(vehicle); // assign vehicle to spot
        String ticketId=UUID.randomUUID().toString(); // generate ticket id
        Ticket ticket=new Ticket(ticketId,spot,vehicle); // create ticket
        activeTickets.put(ticketId,ticket); // store ticket
        return ticketId; // return id
    }
    public synchronized void unpark(String ticketId){ // O(1)
        Ticket ticket=activeTickets.get(ticketId); // fetch ticket
        if(ticket==null){System.out.println("Invalid Ticket");return;} // handle invalid case
        ticket.getSpot().unpark(); // free spot
        long duration=System.currentTimeMillis()-ticket.getEntryTime(); // calculate duration
        double cost=pricingStrategy.calculateFee(duration,ticket.getVehicle()); // calculate fee
        System.out.println("Parking duration(ms):"+duration); // print duration
        System.out.println("Total Cost: ₹"+cost); // print cost
        activeTickets.remove(ticketId); // remove ticket
        System.out.println("Unparked Successfully"); // confirmation
    }
}
public class Main{
    public static void main(String[] args){
        List<ParkingFloor> floors=new ArrayList<>(); // create floors list
        List<ParkingSpot> floor1Spots=new ArrayList<>(); // floor1 spots
        floor1Spots.add(new ParkingSpot(1,SpotSize.SMALL,1)); // add spot
        floor1Spots.add(new ParkingSpot(2,SpotSize.MEDIUM,1)); // add spot
        floor1Spots.add(new ParkingSpot(3,SpotSize.LARGE,1)); // add spot
        floors.add(new ParkingFloor(1,floor1Spots)); // add floor1
        List<ParkingSpot> floor2Spots=new ArrayList<>(); // floor2 spots
        floor2Spots.add(new ParkingSpot(1,SpotSize.SMALL,2)); // add spot
        floor2Spots.add(new ParkingSpot(2,SpotSize.MEDIUM,2)); // add spot
        floor2Spots.add(new ParkingSpot(3,SpotSize.LARGE,2)); // add spot
        floors.add(new ParkingFloor(2,floor2Spots)); // add floor2
        ParkingLot lot=new ParkingLot(floors); // initialize lot
        Vehicle bike=new Vehicle("B1",VehicleType.BIKE); // create bike
        Vehicle car=new Vehicle("C1",VehicleType.CAR); // create car
        Vehicle truck=new Vehicle("T1",VehicleType.TRUCK); // create truck
        String bikeTicket=lot.park(bike); // park bike
        System.out.println("Bike Ticket:"+bikeTicket);
        String carTicket=lot.park(car); // park car
        System.out.println("Car Ticket:"+carTicket);
        String truckTicket=lot.park(truck); // park truck
        System.out.println("Truck Ticket:"+truckTicket);
        lot.unpark(bikeTicket); // unpark bike
        lot.unpark(carTicket); // unpark car
        lot.unpark(truckTicket); // unpark truck
    }
}