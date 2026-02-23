import java.util.*; // import utility classes

// ================= BOOK =================
class Book{
    String bookId; // unique book id
    int totalCopies; // total copies in library
    int availableCopies; // currently available copies
    Queue<String> waitlist; // FIFO reservation queue
    Set<String> issuedUsers; // users currently holding this book

    public Book(String bookId,int copies){
        this.bookId=bookId; // assign id
        this.totalCopies=copies; // set total copies
        this.availableCopies=copies; // initially all available
        this.waitlist=new LinkedList<>(); // initialize waitlist
        this.issuedUsers=new HashSet<>(); // initialize issued users
    }
}

// ================= USER =================
class User{
    String userId; // unique user id

    // bookId -> borrow day
    Map<String,Integer> borrowedBooks; // issued books with borrow date

    public User(String userId){
        this.userId=userId; // assign id
        this.borrowedBooks=new HashMap<>(); // initialize borrowed books
    }
}

// ================= LIBRARY SYSTEM =================
class LibrarySystem{

    private Map<String,Book> books; // book catalog
    private Map<String,User> users; // registered users

    private static final int FREE_DAYS=14; // free borrowing days
    private static final int FINE_PER_DAY=20; // fine per delayed day

    public LibrarySystem(){
        books=new HashMap<>(); // initialize book catalog
        users=new HashMap<>(); // initialize user registry
    }

    // ================= ADD BOOK =================
    // Time Complexity: O(1)
    public void addBook(String bookId,int copies){

        if(books.containsKey(bookId)){ // if book already exists
            Book b=books.get(bookId); // fetch book
            b.totalCopies+=copies; // increase total copies
            b.availableCopies+=copies; // increase available copies
        }else{
            books.put(bookId,new Book(bookId,copies)); // add new book
        }
    }

    // ================= REGISTER USER =================
    // Time Complexity: O(1)
    public void registerUser(String userId){
        users.putIfAbsent(userId,new User(userId)); // register user if not present
    }

    // ================= UNREGISTER USER =================
    // Time Complexity: O(1)
    public void unregisterUser(String userId){
        users.remove(userId); // remove user
    }

    // ================= BORROW BOOK =================
    // Time Complexity: O(1)
    public void borrowBook(String userId,String bookId,int currentDay){

        User user=users.get(userId); // fetch user
        Book book=books.get(bookId); // fetch book

        if(user==null||book==null)return; // validation

        if(user.borrowedBooks.containsKey(bookId))return; // bonus: only one copy per user

        if(book.availableCopies>0){ // if copy available
            book.availableCopies--; // reduce availability
            book.issuedUsers.add(userId); // mark issued
            user.borrowedBooks.put(bookId,currentDay); // record borrow date
        }else{
            book.waitlist.offer(userId); // add to FIFO waitlist
        }
    }

    // ================= RETURN BOOK =================
    // Time Complexity: O(1)
    public int returnBook(String userId,String bookId,int returnDay){

        User user=users.get(userId); // fetch user
        Book book=books.get(bookId); // fetch book

        if(user==null||book==null)return 0; // validation
        if(!user.borrowedBooks.containsKey(bookId))return 0; // not borrowed

        int borrowDay=user.borrowedBooks.get(bookId); // get borrow day
        user.borrowedBooks.remove(bookId); // remove from user
        book.issuedUsers.remove(userId); // remove from issued users

        int daysKept=returnDay-borrowDay; // calculate usage days
        int fine=0; // initialize fine

        if(daysKept>FREE_DAYS) // late return
            fine=(daysKept-FREE_DAYS)*FINE_PER_DAY; // calculate fine

        // assign to next waiting user if exists
        if(!book.waitlist.isEmpty()){ // check waitlist
            String nextUserId=book.waitlist.poll(); // next in queue
            User nextUser=users.get(nextUserId); // fetch next user

            if(nextUser!=null){
                book.issuedUsers.add(nextUserId); // issue book
                nextUser.borrowedBooks.put(bookId,returnDay); // new borrow date
            }
        }else{
            book.availableCopies++; // increase availability
        }

        return fine; // return fine amount
    }

    // ================= AUDIT: USERS HAVING BOOK =================
    // Time Complexity: O(N)
    public List<String> getUsersHavingBook(String bookId){

        List<String> result=new ArrayList<>(); // result list
        Book book=books.get(bookId); // fetch book

        if(book==null)return result; // validation

        result.addAll(book.issuedUsers); // add all users
        return result; // return result
    }

    // ================= AUDIT: BOOKS WITH USER =================
    // Time Complexity: O(N)
    public List<String> getBooksBorrowedByUser(String userId){

        List<String> result=new ArrayList<>(); // result list
        User user=users.get(userId); // fetch user

        if(user==null)return result; // validation

        result.addAll(user.borrowedBooks.keySet()); // add borrowed books
        return result; // return result
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){

        LibrarySystem library=new LibrarySystem(); // create library

        library.addBook("B1",2); // add book
        library.registerUser("U1"); // register users
        library.registerUser("U2");
        library.registerUser("U3");

        library.borrowBook("U1","B1",1); // borrow book
        library.borrowBook("U2","B1",2);
        library.borrowBook("U3","B1",3); // goes to waitlist

        System.out.println(library.getUsersHavingBook("B1")); // audit users

        int fine=library.returnBook("U1","B1",20); // return book
        System.out.println("Fine:"+fine); // print fine

        System.out.println(library.getUsersHavingBook("B1")); // audit again
    }
}