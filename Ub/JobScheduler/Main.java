import java.util.*; // import utility classes
// ================= MACHINE =================
class Machine{
    String machineId; // unique machine id
    Set<String> capabilities; // capability set (stored lowercase)
    int unfinishedJobs; // number of running jobs
    int finishedJobs; // number of completed jobs
    public Machine(String machineId,String[] capabilities){
        this.machineId=machineId; // assign machine id
        this.capabilities=new HashSet<>(); // initialize capability set
        for(String cap:capabilities) // store capabilities in lowercase
            this.capabilities.add(cap.toLowerCase());
        this.unfinishedJobs=0; // initialize counters
        this.finishedJobs=0;
    }
    // ================= CHECK CAPABILITY MATCH =================
    // Time Complexity: O(C)
    public boolean canRun(String[] required){
        for(String cap:required){ // check each required capability
            if(!capabilities.contains(cap.toLowerCase())) // case insensitive check
                return false;
        }
        return true; // all requirements satisfied
    }
}
// ================= JOB =================
class Job{
    String jobId; // job id
    Machine machine; // assigned machine
    public Job(String jobId,Machine machine){
        this.jobId=jobId; // assign id
        this.machine=machine; // assign machine
    }
}
// ================= STRATEGY INTERFACE =================
interface MachineSelectionStrategy{
    Machine select(List<Machine> machines); // select best machine
}
// ================= STRATEGY: MOST FINISHED JOBS =================
class MostFinishedJobsStrategy implements MachineSelectionStrategy{
    // Time Complexity: O(M)
    public Machine select(List<Machine> machines){
        Machine best=null; // best machine
        for(Machine m:machines){ // iterate machines
            if(best==null || // first candidate
               m.finishedJobs>best.finishedJobs || // more completed jobs
               (m.finishedJobs==best.finishedJobs && // tie → lexicographic order
                m.machineId.compareTo(best.machineId)<0))
                best=m;
        }
        return best; // return selected machine
    }
}
// ================= SCHEDULER =================
class Scheduler{
    private Map<String,Machine> machines; // machine registry
    private Map<String,Job> jobs; // job mapping
    private Map<Integer,MachineSelectionStrategy> strategies; // criteria -> strategy
    public Scheduler(){
        machines=new HashMap<>(); // initialize machines
        jobs=new HashMap<>(); // initialize jobs
        strategies=new HashMap<>(); // initialize strategies
        strategies.put(1,new MostFinishedJobsStrategy()); // register only criteria 1
    }
    // ================= ADD MACHINE =================
    // Time Complexity: O(C)
    public void addMachine(String machineId,String[] capabilities){
        machines.put(machineId,new Machine(machineId,capabilities)); // add machine
    }
    // ================= ASSIGN MACHINE TO JOB =================
    // Time Complexity: O(M × C)
    public String assignMachineToJob(String jobId,String[] capabilitiesRequired,int criteria){
        MachineSelectionStrategy strategy=strategies.get(criteria); // fetch strategy
        if(strategy==null)return ""; // invalid criteria
        List<Machine> compatible=new ArrayList<>(); // compatible machines
        for(Machine m:machines.values()){ // filter machines
            if(m.canRun(capabilitiesRequired))
                compatible.add(m);
        }
        if(compatible.isEmpty())return ""; // no compatible machine
        Machine selected=strategy.select(compatible); // select machine
        selected.unfinishedJobs++; // update running jobs
        jobs.put(jobId,new Job(jobId,selected)); // store job
        return selected.machineId; // return machine id
    }

    // ================= JOB COMPLETED =================
    // Time Complexity: O(1)
    public void jobCompleted(String jobId){
        Job job=jobs.get(jobId); // fetch job
        if(job==null)return; // validation
        Machine m=job.machine; // get machine
        m.unfinishedJobs--; // decrease running jobs
        m.finishedJobs++; // increase completed jobs
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){

        Scheduler scheduler=new Scheduler(); // create scheduler

        scheduler.addMachine("machineA",
                new String[]{"image compression"}); // add machine A

        scheduler.addMachine("machineB",
                new String[]{"image compression"}); // add machine B

        String assigned=scheduler.assignMachineToJob(
                "job1",
                new String[]{"image compression"},
                1); // use most finished jobs strategy

        System.out.println(assigned); // print assigned machine
    }
}