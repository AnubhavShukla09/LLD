import java.util.*; // import utility classes
// ================= DATE UTIL =================
class DateUtil{
    // extract day from yyyy-mm-dd
    public static int getDay(String date){ // Time Complexity: O(1)
        return Integer.parseInt(date.substring(8,10)); // return day part
    }
    // compute inclusive days
    public static int getInclusiveDays(int from,int to){ // O(1)
        return 1+(to-from); // inclusive difference
    }
    // check overlapping ranges
    public static boolean isOverlap(int A,int B,int C,int D){ // O(1)
        return A<=D && C<=B; // inclusive overlap
    }
}
// ================= TRIP CLASS =================
class Trip{
    Integer startOdometer; // start reading
    Integer endOdometer; // end reading
    Integer endDay; // end date day
    public void start(int odo){this.startOdometer=odo;} // store start
    public void end(int odo,int day){ // store end details
        this.endOdometer=odo;
        this.endDay=day;
    }
}
// ================= CAR CLASS =================
class Car{
    String licensePlate; // unique id
    int costPerDay; // daily cost
    int freeKmsPerDay; // free kms per day
    int costPerKm; // extra km cost
    List<Booking> bookings; // list of bookings
    public Car(String lp,int costPerDay,int freeKms,int costPerKm){
        this.licensePlate=lp; // assign license
        this.costPerDay=costPerDay; // assign cost
        this.freeKmsPerDay=freeKms; // assign free kms
        this.costPerKm=costPerKm; // assign extra cost
        this.bookings=new ArrayList<>(); // initialize bookings
    }
}
// ================= BOOKING CLASS =================
class Booking{
    String orderId; // booking id
    Car car; // booked car
    int fromDay; // booking start day
    int tillDay; // booking end day
    Trip trip; // trip details
    int releaseDay; // when car becomes available again

    public Booking(String orderId,Car car,int from,int till){
        this.orderId=orderId; // assign id
        this.car=car; // assign car
        this.fromDay=from; // assign start
        this.tillDay=till; // assign end
        this.trip=new Trip(); // create trip
        this.releaseDay=till; // initially release after booking end
    }
}

// ================= MAIN SERVICE =================
class CarRentalService{
    private Map<String,Car> cars; // license -> car
    private Map<String,Booking> bookings; // orderId -> booking
    public CarRentalService(){
        this.cars=new HashMap<>(); // initialize cars
        this.bookings=new HashMap<>(); // initialize bookings
    }
    // ================= ADD CAR =================
    // Time Complexity: O(1)
    public void addCar(String licensePlate,int costPerDay,int freeKmsPerDay,int costPerKm){
        if(licensePlate==null||licensePlate.isBlank())return; // invalid input
        if(cars.containsKey(licensePlate))return; // ignore duplicate
        cars.put(licensePlate,new Car(licensePlate,costPerDay,freeKmsPerDay,costPerKm)); // add car
    }
    // ================= BOOK CAR =================
    // Time Complexity: O(B) where B = bookings of that car
    public boolean bookCar(String orderId,String carLicensePlate,String fromDate,String tillDate){
        if(orderId==null||orderId.isBlank())return false; // invalid order
        if(bookings.containsKey(orderId))return false; // order exists
        if(!cars.containsKey(carLicensePlate))return false; // car not found
        Car car=cars.get(carLicensePlate); // fetch car
        int from=DateUtil.getDay(fromDate); // parse day
        int till=DateUtil.getDay(tillDate); // parse day
        // check overlap with existing bookings
        for(Booking b:car.bookings){
            if(DateUtil.isOverlap(from,till,b.fromDay,b.releaseDay))return false; // conflict
        }
        Booking booking=new Booking(orderId,car,from,till); // create booking
        car.bookings.add(booking); // store in car
        bookings.put(orderId,booking); // store globally
        return true; // success
    }
    // ================= START TRIP =================
    // Time Complexity: O(1)
    public void startTrip(String orderId,int odometerReading){
        Booking booking=bookings.get(orderId); // fetch booking
        if(booking==null)return; // invalid
        booking.trip.start(odometerReading); // store start odo
    }
    // ================= END TRIP =================
    // Time Complexity: O(1)
    public int endTrip(String orderId,int finalOdometerReading,String endDate){
        Booking booking=bookings.get(orderId); // fetch booking
        if(booking==null)return 0; // invalid
        int endDay=DateUtil.getDay(endDate); // parse day
        booking.trip.end(finalOdometerReading,endDay); // store end
        int effectiveEnd=Math.max(booking.tillDay,endDay); // effective end date
        int totalDays=DateUtil.getInclusiveDays(booking.fromDay,effectiveEnd); // days
        int distance=booking.trip.endOdometer-booking.trip.startOdometer; // total kms
        int freeKms=booking.car.freeKmsPerDay*totalDays; // total free kms
        int extraKms=Math.max(0,distance-freeKms); // extra kms
        int cost=(totalDays*booking.car.costPerDay)+(extraKms*booking.car.costPerKm); // total cost
        // update release day (early return allowed)
        if(endDay<booking.tillDay)booking.releaseDay=endDay; // early completion
        return cost; // return trip cost
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){ // Time Complexity: O(1)
        CarRentalService service=new CarRentalService(); // create service
        service.addCar("KA01",1000,100,10); // add car
        System.out.println(service.bookCar("O1","KA01","2025-08-06","2025-08-12")); // true
        service.startTrip("O1",1000); // start trip
        int cost=service.endTrip("O1",1500,"2025-08-09"); // end early
        System.out.println("Trip Cost: "+cost); // print cost
        // should allow booking from next day
        System.out.println(service.bookCar("O2","KA01","2025-08-10","2025-08-11")); // true
    }
}