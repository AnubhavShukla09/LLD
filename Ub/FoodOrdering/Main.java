import java.util.*; // import utility classes
// ================= RATING STATS =================
class RatingStats{
    int totalRating; // sum of ratings
    int count; // number of ratings
    public RatingStats(){
        totalRating=0;
        count=0;
    }
    public void addRating(int rating){ // O(1)
        totalRating+=rating;
        count++;
    }
    public double getAverage(){ // O(1)
        if(count==0)return 0.0;
        double avg=(double)totalRating/count; // raw avg
        // round to 1 decimal as per problem
        return (double)((int)((avg+0.05)*10))/10.0;
    }
    public boolean isRated(){return count>0;} // check rated
}
// ================= ORDER =================
class Order{
    String orderId;
    String restaurantId;
    String foodItemId;
    boolean isRated; // prevent multiple ratings
    public Order(String orderId,String restaurantId,String foodItemId){
        this.orderId=orderId;
        this.restaurantId=restaurantId;
        this.foodItemId=foodItemId;
        this.isRated=false;
    }
}
// ================= RESTAURANT =================
class Restaurant{
    String restaurantId;
    RatingStats overallStats; // overall rating
    Map<String,RatingStats> foodStats; // food -> rating stats
    public Restaurant(String id){
        this.restaurantId=id;
        this.overallStats=new RatingStats();
        this.foodStats=new HashMap<>();
    }
    public void addFoodRating(String foodId,int rating){ // O(1)
        // update overall
        overallStats.addRating(rating);
        // update food specific rating
        foodStats.putIfAbsent(foodId,new RatingStats());
        foodStats.get(foodId).addRating(rating);
    }
    public double getOverallRating(){return overallStats.getAverage();}
    public double getFoodRating(String foodId){
        if(!foodStats.containsKey(foodId))return 0.0;
        return foodStats.get(foodId).getAverage();
    }
    public boolean isFoodRated(String foodId){
        return foodStats.containsKey(foodId)&&foodStats.get(foodId).isRated();
    }
}
// ================= MAIN SYSTEM =================
class FoodOrderingSystem{
    private Map<String,Order> orders; // orderId -> order
    private Map<String,Restaurant> restaurants; // restaurantId -> restaurant
    // ================= INIT =================
    public void init(){
        orders=new HashMap<>();
        restaurants=new HashMap<>();
    }
    // ================= ORDER FOOD =================
    // Time Complexity: O(1)
    public void orderFood(String orderId,String restaurantId,String foodItemId){
        orders.put(orderId,new Order(orderId,restaurantId,foodItemId)); // create order
        restaurants.putIfAbsent(restaurantId,new Restaurant(restaurantId)); // ensure restaurant exists
    }
    // ================= RATE ORDER =================
    // Time Complexity: O(1)
    public void rateOrder(String orderId,int rating){
        Order order=orders.get(orderId);
        if(order==null||order.isRated)return;
        Restaurant restaurant=restaurants.get(order.restaurantId);
        restaurant.addFoodRating(order.foodItemId,rating); // update ratings
        order.isRated=true;
    }
    // ================= TOP RESTAURANTS BY FOOD =================
    // Time Complexity: O(N log N)
    public List<String> getTopRestaurantsByFood(String foodItemId){
        List<Restaurant> list=new ArrayList<>(restaurants.values());
        Collections.sort(list,(a,b)->{
            double r1=a.getFoodRating(foodItemId);
            double r2=b.getFoodRating(foodItemId);
            // unrated at bottom
            if(!a.isFoodRated(foodItemId)&&!b.isFoodRated(foodItemId))
                return a.restaurantId.compareTo(b.restaurantId);
            if(!a.isFoodRated(foodItemId))return 1;
            if(!b.isFoodRated(foodItemId))return -1;
            if(r1!=r2)return Double.compare(r2,r1); // desc rating
            return a.restaurantId.compareTo(b.restaurantId); // lexicographic
        });
        List<String> result=new ArrayList<>();
        for(int i=0;i<Math.min(20,list.size());i++)
            result.add(list.get(i).restaurantId);
        return result;
    }
    // ================= TOP RATED RESTAURANTS =================
    // Time Complexity: O(N log N)
    public List<String> getTopRatedRestaurants(){
        List<Restaurant> list=new ArrayList<>(restaurants.values());
        Collections.sort(list,(a,b)->{
            double r1=a.getOverallRating();
            double r2=b.getOverallRating();
            if(r1!=r2)return Double.compare(r2,r1); // desc rating
            return a.restaurantId.compareTo(b.restaurantId); // lexicographic
        });
        List<String> result=new ArrayList<>();
        for(int i=0;i<Math.min(20,list.size());i++)
            result.add(list.get(i).restaurantId);
        return result;
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){
        FoodOrderingSystem system=new FoodOrderingSystem();
        system.init();
        system.orderFood("O1","restaurant-1","veg-burger");
        system.orderFood("O2","restaurant-2","veg-burger");
        system.orderFood("O3","restaurant-1","ice-cream");
        system.rateOrder("O1",5);
        system.rateOrder("O2",4);
        system.rateOrder("O3",3);
        System.out.println(system.getTopRestaurantsByFood("veg-burger"));
        System.out.println(system.getTopRatedRestaurants());
    }
}