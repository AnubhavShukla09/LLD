import java.util.*; // import utility classes
// ================= GAME STATE ENUM =================
enum TicTacToeGameState{IN_PROGRESS,WIN,DRAW} // game lifecycle states
// ================= SYMBOL ENUM =================
enum Symbol{X,O,EMPTY} // player symbols
// ================= PLAYER CLASS =================
class TicTacToePlayer{
    private Symbol symbol; // store player symbol
    public TicTacToePlayer(Symbol symbol){this.symbol=symbol;} // constructor
    public Symbol getSymbol(){return symbol;} // return symbol
}
// ================= BOARD CLASS =================
class TicTacToeBoard{
    private int size; // board size (n x n)
    private Symbol[][] grid; // board grid
    private int filledCells; // track filled cells
    public TicTacToeBoard(int size){
        this.size=size; // assign size
        this.grid=new Symbol[size][size]; // create grid
        this.filledCells=0; // initialize counter
        for(int i=0;i<size;i++)Arrays.fill(grid[i],Symbol.EMPTY); // fill empty cells
    }
    // ================= PLACE MOVE =================
    // Time Complexity: O(1)
    public boolean placeMove(int row,int col,Symbol symbol){
        if(row<0||row>=size||col<0||col>=size)return false; // invalid position
        if(grid[row][col]!=Symbol.EMPTY)return false; // already occupied
        grid[row][col]=symbol; // place symbol
        filledCells++; // update count
        return true; // success
    }
    public boolean isFull(){return filledCells==size*size;} // check board full
    public int getSize(){return size;} // return size
    // ================= PRINT BOARD =================
    public void printBoard(){
        for(int i=0;i<size;i++){ // iterate rows
            for(int j=0;j<size;j++){ // iterate columns
                if(grid[i][j]==Symbol.EMPTY)System.out.print("_ "); // empty
                else if(grid[i][j]==Symbol.X)System.out.print("X "); // X
                else System.out.print("O "); // O
            }
            System.out.println(); // next line
        }
        System.out.println(); // spacing
    }
}
// ================= GAME CONTROLLER =================
class TicTacToeGame{
    private TicTacToeBoard board; // board instance
    private TicTacToePlayer player1; // X player
    private TicTacToePlayer player2; // O player
    private TicTacToePlayer currentPlayer; // track current turn
    private TicTacToeGameState gameState; // current state
    private int[] rowCount; // row counters
    private int[] colCount; // column counters
    private int mainDiagonal; // main diagonal counter
    private int antiDiagonal; // anti diagonal counter
    public TicTacToeGame(int size){
        this.board=new TicTacToeBoard(size); // initialize board
        this.player1=new TicTacToePlayer(Symbol.X); // create X player
        this.player2=new TicTacToePlayer(Symbol.O); // create O player
        this.currentPlayer=player1; // X starts
        this.gameState=TicTacToeGameState.IN_PROGRESS; // game active
        this.rowCount=new int[size]; // initialize row counters
        this.colCount=new int[size]; // initialize column counters
        this.mainDiagonal=0; // initialize diagonal
        this.antiDiagonal=0; // initialize anti diagonal
    }
    public void printBoard(){board.printBoard();} // delegate printing
    // ================= SWITCH PLAYER =================
    private void switchPlayer(){ // Time Complexity: O(1)
        currentPlayer=(currentPlayer==player1)?player2:player1; // toggle turn
    }
    // ================= MAKE MOVE =================
    // Time Complexity: O(1) win check
    public TicTacToeGameState makeMove(int row,int col){
        if(gameState!=TicTacToeGameState.IN_PROGRESS){ // if game finished
            System.out.println("Game already finished");
            return gameState;
        }
        boolean placed=board.placeMove(row,col,currentPlayer.getSymbol()); // place move
        if(!placed){ // invalid move
            System.out.println("Invalid move, cell occupied or out of bounds");
            return gameState;
        }
        int moveValue=(currentPlayer.getSymbol()==Symbol.X)?1:-1; // X=+1, O=-1
        rowCount[row]+=moveValue; // update row counter
        colCount[col]+=moveValue; // update column counter
        if(row==col)mainDiagonal+=moveValue; // update main diagonal
        if(row+col==board.getSize()-1)antiDiagonal+=moveValue; // update anti diagonal
        board.printBoard(); // show board
        int size=board.getSize(); // board size
        // ================= O(1) WIN CHECK =================
        if(Math.abs(rowCount[row])==size||
           Math.abs(colCount[col])==size||
           Math.abs(mainDiagonal)==size||
           Math.abs(antiDiagonal)==size){
            gameState=TicTacToeGameState.WIN; // mark win
            System.out.println(currentPlayer.getSymbol()+" wins!");
            return gameState;
        }
        if(board.isFull()){ // check draw
            gameState=TicTacToeGameState.DRAW; // mark draw
            System.out.println("Game ended in DRAW");
            return gameState;
        }
        switchPlayer(); // change turn
        return TicTacToeGameState.IN_PROGRESS; // continue game
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){

        TicTacToeGame game=new TicTacToeGame(3); // standard 3x3 board
        game.printBoard(); // show initial board

        game.makeMove(0,0); // X
        game.makeMove(1,0); // O
        game.makeMove(0,1); // X
        game.makeMove(1,1); // O
        game.makeMove(0,2); // X wins
    }
}
