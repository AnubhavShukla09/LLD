import java.util.*; // import utility classes
// ================= USER CLASS =================
class User{
    String userId; // unique id
    String displayName; // user name
    public User(String userId,String displayName){
        this.userId=userId; // assign id
        this.displayName=displayName; // assign name
    }
}
// ================= MONEY UTILITY =================
class MoneyUtil{
    public static double round2(double value){ // round to 2 decimals (half up)
        return Math.round(value*100.0)/100.0;
    }
}
// ================= SPLIT STRATEGY =================
interface SplitStrategy{
    Map<String,Double> calculateSplit(
        List<String> members,
        List<Integer> paid,
        List<Double> extraData // optional: percentage or other data
    ); // compute net change per user
}
// ================= EQUAL SPLIT STRATEGY =================
class EqualSplitStrategy implements SplitStrategy{
    // Time Complexity: O(n)
    public Map<String,Double> calculateSplit(List<String> members,List<Integer> paid,List<Double> extraData){
        Map<String,Double> result=new HashMap<>(); // net change map
        int n=members.size(); // participants
        double total=0; // total expense
        for(int p:paid)total+=p; // sum payments
        double share=MoneyUtil.round2(total/n); // equal share
        for(int i=0;i<n;i++){
            double net=MoneyUtil.round2(paid.get(i)-share); // paid-share
            result.put(members.get(i),net); // store net
        }
        return result; // return net changes
    }
}
// ================= PERCENTAGE SPLIT STRATEGY =================
class PercentageSplitStrategy implements SplitStrategy{
    // Time Complexity: O(n)
    public Map<String,Double> calculateSplit(List<String> members,List<Integer> paid,List<Double> percentages){
        Map<String,Double> result=new HashMap<>(); // net change map
        int n=members.size(); // participants
        if(percentages==null||percentages.size()!=n)throw new RuntimeException("Invalid percentages");
        double total=0; // total expense
        for(int p:paid)total+=p; // sum payments
        double percentSum=0; // validate sum = 100
        for(double p:percentages)percentSum+=p;
        if(Math.abs(percentSum-100.0)>0.01)throw new RuntimeException("Percent must sum to 100");
        for(int i=0;i<n;i++){
            double share=MoneyUtil.round2(total*(percentages.get(i)/100.0)); // individual share
            double net=MoneyUtil.round2(paid.get(i)-share); // paid-share
            result.put(members.get(i),net); // store net
        }
        return result; // return net changes
    }
}
// ================= MAIN SERVICE =================
class ExpenseSharingService{
    private Map<String,User> users; // userId -> user
    private Set<Integer> usedExpenseIds; // track expense ids
    private Map<String,Double> netBalance; // userId -> net balance
    public ExpenseSharingService(){
        this.users=new HashMap<>(); // initialize users
        this.usedExpenseIds=new HashSet<>(); // initialize expense ids
        this.netBalance=new HashMap<>(); // initialize balances
    }
    // ================= REGISTER USER =================
    // Time Complexity: O(1)
    public void registerUser(String userId,String displayName){
        if(userId==null||userId.isBlank())return; // invalid input
        if(users.containsKey(userId))return; // ignore duplicate
        users.put(userId,new User(userId,displayName)); // add user
        netBalance.put(userId,0.0); // initialize balance
    }
    // ================= RECORD EXPENSE =================
    // Time Complexity: O(n)
    public void recordExpense(
        int expenseId,
        List<String> members,
        List<Integer> paid,
        SplitStrategy strategy,
        List<Double> extraData
    ){
        if(usedExpenseIds.contains(expenseId))return; // ignore duplicate
        if(members.size()!=paid.size())return; // invalid input
        for(String m:members)if(!users.containsKey(m))return; // validate users
        usedExpenseIds.add(expenseId); // mark used
        Map<String,Double> netChanges=strategy.calculateSplit(members,paid,extraData); // compute split
        for(String userId:netChanges.keySet()){ // update balances
            double curr=netBalance.get(userId);
            netBalance.put(userId,MoneyUtil.round2(curr+netChanges.get(userId)));
        }
    }

    // ================= GET SINGLE USER BALANCE =================
    // positive -> should receive, negative -> owes
    // Time Complexity: O(1)
    public double getUserBalance(String userId){
        if(!users.containsKey(userId))return 0.0; // invalid user
        return MoneyUtil.round2(netBalance.get(userId)); // return balance
    }

    // ================= SHOW USER BALANCE STRING =================
    // Time Complexity: O(1)
    public String showUserBalance(String userId){

        double bal=getUserBalance(userId); // fetch balance

        if(bal>0)return userId+" should receive "+String.format("%.2f",bal);
        if(bal<0)return userId+" owes "+String.format("%.2f",-bal);
        return userId+" is settled";
    }

    // ================= LIST SIMPLIFIED BALANCES =================
    // Time Complexity: O(U log U)
    public List<String> listBalances(){
        List<Map.Entry<String,Double>> creditors=new ArrayList<>(); // positive balances
        List<Map.Entry<String,Double>> debtors=new ArrayList<>(); // negative balances
        // separate creditors and debtors
        for(String userId:netBalance.keySet()){
            double bal=MoneyUtil.round2(netBalance.get(userId));
            if(bal>0)creditors.add(new AbstractMap.SimpleEntry<>(userId,bal));
            else if(bal<0)debtors.add(new AbstractMap.SimpleEntry<>(userId,-bal));
        }
        creditors.sort(Map.Entry.comparingByKey()); // sort lexicographically
        debtors.sort(Map.Entry.comparingByKey());
        List<String> result=new ArrayList<>(); // output
        int i=0,j=0;
        // greedy settlement
        while(i<debtors.size()&&j<creditors.size()){
            String debtorId=debtors.get(i).getKey();
            String creditorId=creditors.get(j).getKey();
            double owe=debtors.get(i).getValue();
            double receive=creditors.get(j).getValue();
            double settled=MoneyUtil.round2(Math.min(owe,receive));
            result.add(debtorId+" owes "+creditorId+": "+String.format("%.2f",settled));
            debtors.get(i).setValue(MoneyUtil.round2(owe-settled));
            creditors.get(j).setValue(MoneyUtil.round2(receive-settled));
            if(debtors.get(i).getValue()==0)i++;
            if(creditors.get(j).getValue()==0)j++;
        }
        return result;
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){ // Time Complexity: O(1)
        ExpenseSharingService service=new ExpenseSharingService();
        service.registerUser("U1","Alice");
        service.registerUser("U2","Bob");
        service.registerUser("U3","Charlie");
        // equal split expense
        service.recordExpense(
            1,
            Arrays.asList("U1","U2","U3"),
            Arrays.asList(300,0,0),
            new EqualSplitStrategy(),
            null
        );
        // percentage split expense
        service.recordExpense(
            2,
            Arrays.asList("U1","U2","U3"),
            Arrays.asList(0,300,0),
            new PercentageSplitStrategy(),
            Arrays.asList(50.0,30.0,20.0)
        );
        // show individual balances
        System.out.println(service.showUserBalance("U1"));
        System.out.println(service.showUserBalance("U2"));
        System.out.println(service.showUserBalance("U3"));
        // show all simplified debts
        List<String> balances=service.listBalances();
        for(String s:balances)System.out.println(s);
    }
}