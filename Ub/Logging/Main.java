import java.util.*; // import utility classes
enum LogLevel{DEBUG,INFO,WARN,ERROR} // define log levels
class LogEntry{
    private LogLevel level; // log level
    private String message; // log message
    private long timestamp; // creation time
    public LogEntry(LogLevel level,String message){
        this.level=level; // assign level
        this.message=message; // assign message
        this.timestamp=System.currentTimeMillis(); // store timestamp
    }
    public LogLevel getLevel(){return level;} // return level
    public String getMessage(){return message;} // return message
    public long getTimestamp(){return timestamp;} // return timestamp
    public String toString(){ // printable log format
        return "["+level+"] "+timestamp+" : "+message;
    }
}
class Logger{
    private Map<LogLevel,List<LogEntry>> logsByLevel; // level -> logs
    private List<LogEntry> allLogs; // maintain full history
    public Logger(){
        this.logsByLevel=new HashMap<>(); // initialize map
        this.allLogs=new ArrayList<>(); // initialize list
        for(LogLevel level:LogLevel.values()) // create bucket for each level
            logsByLevel.put(level,new ArrayList<>());
    }

    public synchronized void log(LogLevel level,String message){ // thread safe append
        LogEntry entry=new LogEntry(level,message); // create log
        logsByLevel.get(level).add(entry); // add to level bucket
        allLogs.add(entry); // add to global list
    }

    public synchronized List<LogEntry> getLogs(LogLevel level){ // fetch logs by level
        return new ArrayList<>(logsByLevel.get(level)); // return copy
    }

    public synchronized List<LogEntry> getAllLogs(){ // fetch all logs
        return new ArrayList<>(allLogs); // return copy
    }

    public synchronized List<LogEntry> getLogs(long startTime,long endTime){ // filter by time range
        List<LogEntry> result=new ArrayList<>(); // store filtered logs
        for(LogEntry log:allLogs){ // iterate all logs
            if(log.getTimestamp()>=startTime && log.getTimestamp()<=endTime)
                result.add(log); // add if in range
        }
        return result; // return filtered logs
    }
}

public class Main{
    public static void main(String[] args) throws InterruptedException{
        Logger logger=new Logger(); // create logger

        logger.log(LogLevel.INFO,"Application started"); // log info
        logger.log(LogLevel.DEBUG,"Debug value x=10"); // log debug
        logger.log(LogLevel.ERROR,"Database connection failed"); // log error

        Thread.sleep(10); // simulate delay
        long start=System.currentTimeMillis(); // mark time

        logger.log(LogLevel.WARN,"Memory usage high"); // log warn
        logger.log(LogLevel.INFO,"User login success"); // log info

        System.out.println("---- ALL LOGS ----");
        for(LogEntry log:logger.getAllLogs()) // print all
            System.out.println(log);

        System.out.println("---- ERROR LOGS ----");
        for(LogEntry log:logger.getLogs(LogLevel.ERROR)) // filter by level
            System.out.println(log);

        System.out.println("---- TIME RANGE LOGS ----");
        for(LogEntry log:logger.getLogs(start,System.currentTimeMillis())) // filter by time
            System.out.println(log);
    }
}