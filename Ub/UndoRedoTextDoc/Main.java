import java.util.*; // import utility classes
// ================= OPERATION TYPE =================
enum OperationType{ADD,DELETE} // type of edit
// ================= EDIT OPERATION =================
class EditOperation{
    OperationType type; // operation type
    int row; // target row
    int column; // column position
    String text; // affected text
    public EditOperation(OperationType type,int row,int column,String text){
        this.type=type; // assign type
        this.row=row; // assign row
        this.column=column; // assign column
        this.text=text; // assign text
    }
}
// ================= TEXT EDITOR =================
class TextEditor{
    private List<StringBuilder> document; // rows of text
    private Stack<EditOperation> undoStack; // undo history
    private Stack<EditOperation> redoStack; // redo history
    public TextEditor(){
        document=new ArrayList<>(); // initialize document
        undoStack=new Stack<>(); // initialize undo stack
        redoStack=new Stack<>(); // initialize redo stack
    }
    // ================= ADD TEXT =================
    // Time Complexity: O(N)
    public void addText(int row,int column,String text){
        // create new row if needed
        if(row==document.size())
            document.add(new StringBuilder()); // append empty row
        StringBuilder target=document.get(row); // target row
        target.insert(column,text); // insert text
        // store reverse operation for undo
        undoStack.push(new EditOperation(OperationType.DELETE,row,column,text));
        redoStack.clear(); // clear redo history
    }
    // ================= DELETE TEXT =================
    // Time Complexity: O(N)
    public void deleteText(int row,int startColumn,int length){
        StringBuilder target=document.get(row); // target row
        String deleted=target.substring(startColumn,startColumn+length); // extract text
        target.delete(startColumn,startColumn+length); // delete text
        // store reverse operation for undo
        undoStack.push(new EditOperation(OperationType.ADD,row,startColumn,deleted));
        redoStack.clear(); // clear redo history
    }
    // ================= UNDO =================
    // Time Complexity: O(N)
    public void undo(){
        if(undoStack.isEmpty())return; // no-op
        EditOperation op=undoStack.pop(); // last operation
        if(op.type==OperationType.ADD){ // undo delete → add back
            document.get(op.row).insert(op.column,op.text);
            redoStack.push(new EditOperation(OperationType.DELETE,op.row,op.column,op.text));
        }
        else{ // undo add → delete
            document.get(op.row).delete(op.column,op.column+op.text.length());
            redoStack.push(new EditOperation(OperationType.ADD,op.row,op.column,op.text));
        }
    }
    // ================= REDO =================
    // Time Complexity: O(N)
    public void redo(){
        if(redoStack.isEmpty())return; // no-op
        EditOperation op=redoStack.pop();
        if(op.type==OperationType.ADD){ // reapply add
            document.get(op.row).insert(op.column,op.text);
            undoStack.push(new EditOperation(OperationType.DELETE,op.row,op.column,op.text));
        }
        else{ // reapply delete
            document.get(op.row).delete(op.column,op.column+op.text.length());
            undoStack.push(new EditOperation(OperationType.ADD,op.row,op.column,op.text));
        }
    }
    // ================= READ LINE =================
    // Time Complexity: O(L)
    public String readLine(int row){
        if(row<0||row>=document.size())return ""; // safety
        return document.get(row).toString();
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){
        TextEditor editor=new TextEditor();
        editor.addText(0,0,"Hello"); // add
        editor.addText(0,5," World");
        System.out.println(editor.readLine(0)); // Hello World
        editor.deleteText(0,5,6);
        System.out.println(editor.readLine(0)); // Hello
        editor.undo();
        System.out.println(editor.readLine(0)); // Hello World
        editor.redo();
        System.out.println(editor.readLine(0)); // Hello
    }
}