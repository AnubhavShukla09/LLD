import java.util.*; // import utilities
// ================= LOCKER SIZE ENUM =================
enum LockerSize{SMALL,MEDIUM,LARGE} // locker sizes
// ================= LOCKER STATUS ENUM =================
enum LockerStatus{AVAILABLE,OCCUPIED} // locker states
// ================= PACKAGE CLASS =================
class PackageItem{
    private String packageId; // unique package id
    private LockerSize size; // required locker size
    public PackageItem(String packageId,LockerSize size){
        this.packageId=packageId; // assign id
        this.size=size; // assign size
    }
    public String getPackageId(){return packageId;} // return id
    public LockerSize getSize(){return size;} // return size
}
// ================= LOCKER CLASS =================
class Locker{
    private String lockerId; // locker id
    private LockerSize size; // locker size
    private LockerStatus status; // current status
    private PackageItem storedPackage; // stored package
    private String otp; // pickup otp
    private long otpGeneratedTime; // otp generation timestamp
    private static final long OTP_EXPIRY=5*60*1000; // 5 min expiry
    public Locker(String lockerId,LockerSize size){
        this.lockerId=lockerId; // assign id
        this.size=size; // assign size
        this.status=LockerStatus.AVAILABLE; // initially free
    }
    public String getLockerId(){return lockerId;} // return id
    public LockerSize getSize(){return size;} // return size
    public LockerStatus getStatus(){return status;} // return status
    public boolean isAvailable(){return status==LockerStatus.AVAILABLE;} // check availability
    // ================= STORE PACKAGE =================
    public synchronized void storePackage(PackageItem pkg,String otp){
        this.storedPackage=pkg; // assign package
        this.otp=otp; // store otp
        this.otpGeneratedTime=System.currentTimeMillis(); // store time
        this.status=LockerStatus.OCCUPIED; // mark occupied
    }
    // ================= RELEASE PACKAGE =================
    public synchronized boolean pickupPackage(String enteredOtp){
        if(status==LockerStatus.AVAILABLE)return false; // nothing stored
        // check expiry
        long now=System.currentTimeMillis();
        if(now-otpGeneratedTime>OTP_EXPIRY){
            releaseLocker(); // auto release expired package
            System.out.println("OTP expired. Locker released");
            return false;
        }
        if(!otp.equals(enteredOtp))return false; // wrong otp
        releaseLocker(); // successful pickup
        return true;
    }
    // ================= RELEASE LOCKER =================
    private void releaseLocker(){
        storedPackage=null; // remove package
        otp=null; // clear otp
        otpGeneratedTime=0; // reset time
        status=LockerStatus.AVAILABLE; // mark free
    }
}
// ================= LOCKER ALLOCATION STRATEGY =================
interface LockerAllocationStrategy{
    Locker allocate(List<Locker> lockers,LockerSize requiredSize); // allocation contract
}
// ================= SMALLEST FIT STRATEGY =================
class SmallestFitStrategy implements LockerAllocationStrategy{
    public Locker allocate(List<Locker> lockers,LockerSize requiredSize){
        Locker best=null; // best locker
        for(Locker locker:lockers){ // iterate lockers
            if(!locker.isAvailable())continue; // skip occupied
            if(canFit(locker.getSize(),requiredSize)){ // size check
                if(best==null || locker.getSize().ordinal()<best.getSize().ordinal())
                    best=locker; // pick smallest fitting locker
            }
        }
        return best; // return locker
    }
    private boolean canFit(LockerSize lockerSize,LockerSize requiredSize){
        return lockerSize.ordinal()>=requiredSize.ordinal(); // check capacity
    }
}
// ================= LOCKER SERVICE =================
class AmazonLockerService{
    private List<Locker> lockers; // all lockers
    private LockerAllocationStrategy strategy; // allocation strategy
    public AmazonLockerService(){
        this.lockers=new ArrayList<>(); // initialize locker list
        this.strategy=new SmallestFitStrategy(); // default strategy
    }
    public synchronized void addLocker(Locker locker){lockers.add(locker);} // register locker
    // ================= STORE PACKAGE =================
    public synchronized String storePackage(PackageItem pkg){
        Locker locker=strategy.allocate(lockers,pkg.getSize()); // find locker
        if(locker==null){
            System.out.println("No locker available"); // no locker
            return null;
        }
        String otp=generateOtp(); // generate otp
        locker.storePackage(pkg,otp); // store package
        System.out.println("Package stored in locker:"+locker.getLockerId()+" OTP:"+otp); // log
        return locker.getLockerId(); // return locker id
    }
    // ================= PICKUP PACKAGE =================
    public synchronized boolean pickupPackage(String lockerId,String otp){
        for(Locker locker:lockers){ // search locker
            if(locker.getLockerId().equals(lockerId)){
                boolean success=locker.pickupPackage(otp); // try pickup
                if(success)System.out.println("Package picked up");
                else System.out.println("Invalid OTP / expired OTP / empty locker");
                return success;
            }
        }
        System.out.println("Locker not found"); // validation
        return false;
    }
    private String generateOtp(){ // simple otp generator
        return String.valueOf(1000+(int)(Math.random()*9000));
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){
        AmazonLockerService service=new AmazonLockerService(); // create service
        service.addLocker(new Locker("L1",LockerSize.SMALL)); // add lockers
        service.addLocker(new Locker("L2",LockerSize.MEDIUM));
        service.addLocker(new Locker("L3",LockerSize.LARGE));
        PackageItem pkg=new PackageItem("PKG1",LockerSize.SMALL); // create package
        String lockerId=service.storePackage(pkg); // store package
        // use printed OTP to pickup (may fail if wrong/expired)
        service.pickupPackage(lockerId,"1234"); // example pickup
    }
}
