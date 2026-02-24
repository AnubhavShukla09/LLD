import java.util.*; // import utilities
// ================= SEAT STATUS =================
enum SeatStatus{AVAILABLE,RESERVED,BOOKED} // define seat states
// ================= SEAT =================
class Seat{
    private String seatId; // unique seat id
    private SeatStatus status; // current status
    public Seat(String seatId){this.seatId=seatId;this.status=SeatStatus.AVAILABLE;} // constructor
    public String getSeatId(){return seatId;} // return seat id
    public SeatStatus getStatus(){return status;} // return seat status\
    public void reserve(){this.status=SeatStatus.RESERVED;} // mark as reserved
    public void book(){this.status=SeatStatus.BOOKED;} // mark as booked
    public void makeAvailable(){this.status=SeatStatus.AVAILABLE;} // reset seat
}
// ================= SHOW =================
class Show{
    private String showId; // show identifier
    private long timestamp; // show start time epoch
    private List<Seat> seats; // seats for this show
    private Map<String,Seat> seatMap; // seatId -> Seat for O(1) lookup
    private Screen screen; // screen reference
    public Show(String showId,long timestamp,List<Seat> seats,Screen screen){
        this.showId=showId; // assign id
        this.timestamp=timestamp; // assign timestamp
        this.seats=seats; // assign seats
        this.screen=screen; // assign screen
        this.seatMap=new HashMap<>(); // initialize seat map
        for(Seat seat:seats)seatMap.put(seat.getSeatId(),seat); // populate seat map
    }
    public String getShowId(){return showId;} // return id
    public long getTimestamp(){return timestamp;} // return timestamp
    public List<Seat> getSeats(){return seats;} // return seats
    public Screen getScreen(){return screen;} // return screen
    public Seat getSeatById(String seatId){return seatMap.get(seatId);} // O(1) seat lookup
}
// ================= SCREEN =================
class Screen{
    private String screenId; // screen id
    private List<Show> shows; // list of shows
    public Screen(String screenId){this.screenId=screenId;this.shows=new ArrayList<>();} // constructor
    public void addShow(Show show){shows.add(show);} // add show to screen
    public String getScreenId(){return screenId;} // return screen id
    public List<Show> getShows(){return shows;} // return shows
}
// ================= RESERVATION =================
class Reservation{
    private String reservationId; // reservation id
    private String userId; // user id
    private List<Seat> seats; // reserved seats directly stored
    private long createdAt; // reservation creation time
    public Reservation(String reservationId,String userId,List<Seat> seats){
        this.reservationId=reservationId; // assign id
        this.userId=userId; // assign user
        this.seats=seats; // assign seats
        this.createdAt=System.currentTimeMillis(); // store creation time
    }
    public String getReservationId(){return reservationId;} // return id
    public List<Seat> getSeats(){return seats;} // return reserved seats
    public long getCreatedAt(){return createdAt;} // return creation time
}
// ================= TICKET =================
class Ticket{
    private String ticketId; // ticket id
    private String reservationId; // reservation reference
    private List<Seat> seats; // booked seats
    private double totalPrice; // total cost
    public Ticket(String ticketId,String reservationId,List<Seat> seats,double totalPrice){
        this.ticketId=ticketId; // assign id
        this.reservationId=reservationId; // assign reservation
        this.seats=seats; // assign seats
        this.totalPrice=totalPrice; // assign price
    }
    public String getTicketId(){return ticketId;} // return ticket id
    public double getTotalPrice(){return totalPrice;} // return price
}
// ================= SEAT ALLOCATION STRATEGY =================
interface SeatAllocationStrategy{
    List<Seat> allocate(List<Seat> seats,int count); // allocation contract
}
// ================= FIRST AVAILABLE STRATEGY =================
class FirstAvailableStrategy implements SeatAllocationStrategy{
    public List<Seat> allocate(List<Seat> seats,int count){ // O(N)
        List<Seat> allocated=new ArrayList<>(); // store allocated seats
        for(Seat seat:seats){ // iterate seats
            if(seat.getStatus()==SeatStatus.AVAILABLE){ // check availability
                allocated.add(seat); // add seat
                if(allocated.size()==count)break; // stop if enough
            }
        }
        if(allocated.size()!=count)return null; // return null if insufficient
        return allocated; // return allocated seats
    }
}
// ================= PRICING STRATEGY =================
interface PricingStrategy{
    double calculatePrice(int seatCount); // pricing contract
}
// ================= FLAT PRICING STRATEGY =================
class FlatPricingStrategy implements PricingStrategy{
    private double pricePerSeat; // fixed price per seat
    public FlatPricingStrategy(double pricePerSeat){
        this.pricePerSeat=pricePerSeat; // assign price
    }
    public double calculatePrice(int seatCount){ // O(1)
        return seatCount*pricePerSeat; // total price
    }
}
// ================= BOOK MY SHOW SERVICE =================
class BookMyShow{
    private Show show; // show reference
    private Map<String,Reservation> reservations; // active reservations
    private SeatAllocationStrategy strategy; // seat allocation strategy
    private PricingStrategy pricingStrategy; // pricing strategy
    private Map<String,Ticket> tickets; // generated tickets
    private static final long RESERVATION_TIMEOUT=5*60*1000; // 5 minutes expiry
    public BookMyShow(Show show){
        this.show=show; // assign show
        this.reservations=new HashMap<>(); // initialize map
        this.strategy=new FirstAvailableStrategy(); // set default seat strategy
        this.pricingStrategy=new FlatPricingStrategy(200); // fixed price per seat
        this.tickets=new HashMap<>(); // initialize ticket store
    }
    private synchronized void cleanupExpiredReservations(){ // lazy cleanup
        long now=System.currentTimeMillis(); // current time
        Iterator<Map.Entry<String,Reservation>> it=reservations.entrySet().iterator(); // iterator
        while(it.hasNext()){ // iterate reservations
            Reservation r=it.next().getValue(); // get reservation
            if(now-r.getCreatedAt()>RESERVATION_TIMEOUT){ // check expiry
                for(Seat seat:r.getSeats())seat.makeAvailable(); // release seats
                it.remove(); // remove reservation
                System.out.println("Reservation expired:"+r.getReservationId()); // log expiry
            }
        }
    }
    public synchronized String reserve(String userId,int seatCount){ // O(N)
        cleanupExpiredReservations(); // cleanup before allocation
        List<Seat> allocated=strategy.allocate(show.getSeats(),seatCount); // allocate seats
        if(allocated==null)return "Not enough seats"; // insufficient seats
        for(Seat seat:allocated)seat.reserve(); // mark seats reserved
        String reservationId=UUID.randomUUID().toString(); // generate id
        reservations.put(reservationId,new Reservation(reservationId,userId,allocated)); // store reservation
        return reservationId; // return id
    }
    public synchronized void confirm(String reservationId){ // O(K)
        cleanupExpiredReservations(); // cleanup before confirm
        Reservation reservation=reservations.get(reservationId); // fetch reservation
        if(reservation==null){System.out.println("Invalid reservation");return;} // validation
        for(Seat seat:reservation.getSeats())seat.book(); // mark booked
        double price=pricingStrategy.calculatePrice(reservation.getSeats().size()); // calculate price
        String ticketId=UUID.randomUUID().toString(); // generate ticket id
        Ticket ticket=new Ticket(ticketId,reservationId,reservation.getSeats(),price); // create ticket
        tickets.put(ticketId,ticket); // store ticket
        reservations.remove(reservationId); // remove reservation
        System.out.println("Booking confirmed. TicketId:"+ticketId+" Total Price:"+price); // confirmation
    }
    public synchronized void cancel(String reservationId){ // O(K)
        cleanupExpiredReservations(); // cleanup before cancel
        Reservation reservation=reservations.get(reservationId); // fetch reservation
        if(reservation==null){System.out.println("Invalid reservation");return;} // validation
        for(Seat seat:reservation.getSeats())seat.makeAvailable(); // reset seats
        reservations.remove(reservationId); // remove reservation
        System.out.println("Reservation cancelled"); // confirmation
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){
        Screen screen1=new Screen("Screen1"); // create screen
        List<Seat> seats=new ArrayList<>(); // create seat list
        for(int i=1;i<=8;i++){seats.add(new Seat("S"+i));} // create seats

        Show show=new Show("Show1",System.currentTimeMillis(),seats,screen1); // create show
        screen1.addShow(show); // attach show to screen

        BookMyShow service=new BookMyShow(show); // create service for show

        String r1=service.reserve("User1",3); // reserve seats
        System.out.println("ReservationId:"+r1); // print id

        service.confirm(r1); // confirm booking

        String r2=service.reserve("User2",2); // reserve more seats
        System.out.println("ReservationId:"+r2); // print id

        service.cancel(r2); // cancel booking
    }
}
