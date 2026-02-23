import java.util.*; // import utility classes
// ================= GAME STATE ENUM =================
enum GameState{IN_PROGRESS,WIN,DRAW} // game lifecycle states
// ================= SYMBOL ENUM =================
enum Symbol{X,O,EMPTY} // possible cell values
// ================= PLAYER CLASS =================
class Player{
    private Symbol symbol; // store player symbol
    public Player(Symbol symbol){this.symbol=symbol;} // constructor
    public Symbol getSymbol(){return symbol;} // return symbol
}
// ================= BOARD CLASS =================
class Board{
    private int size; // board size m
    private Symbol[][] grid; // grid storage
    private int filledCells; // track filled cells
    public Board(int size){
        this.size=size; // assign size
        this.grid=new Symbol[size][size]; // create grid
        this.filledCells=0; // initialize counter
        for(int i=0;i<size;i++)Arrays.fill(grid[i],Symbol.EMPTY); // fill empty
    }
    public boolean isCellEmpty(int row,int col){return grid[row][col]==Symbol.EMPTY;} // check empty
    public void placeSymbol(int row,int col,Symbol symbol){
        grid[row][col]=symbol; // place symbol
        filledCells++; // increase filled count
    }
    public boolean isFull(){return filledCells==size*size;} // check if board full
    public int getSize(){return size;} // return size
    public void printBoard(){ // display board
        for(int i=0;i<size;i++){ // iterate rows
            for(int j=0;j<size;j++){ // iterate columns
                if(grid[i][j]==Symbol.EMPTY)System.out.print("_ "); // print empty
                else System.out.print(grid[i][j]+" "); // print symbol
            }
            System.out.println(); // next line
        }
        System.out.println(); // spacing
    }
}
// ================= GAME CONTROLLER =================
class TicTacGame{
    private Board board; // board instance
    private int size; // board size
    private Player player1; // player X
    private Player player2; // player O
    private Player currentPlayer; // track current turn
    private int[] rows; // row counters
    private int[] cols; // column counters
    private int diag; // main diagonal counter
    private int antiDiag; // anti diagonal counter
    private GameState gameState; // current game state
    public TicTacGame(int m){
        this.board=new Board(m); // initialize board
        this.size=m; // assign size
        this.player1=new Player(Symbol.X); // create player1
        this.player2=new Player(Symbol.O); // create player2
        this.currentPlayer=player1; // player1 starts
        this.rows=new int[m]; // initialize rows
        this.cols=new int[m]; // initialize cols
        this.diag=0; // initialize diag
        this.antiDiag=0; // initialize anti diag
        this.gameState=GameState.IN_PROGRESS; // game active
    }
    public void printBoard(){board.printBoard();} // delegate board printing
    // ================= SWITCH PLAYER =================
    private void switchPlayer(){ // O(1)
        currentPlayer=(currentPlayer==player1)?player2:player1; // toggle turn
    }
    // ================= MAKE MOVE =================
    // Time Complexity: O(1)
    // Space Complexity: O(1)
    public GameState move(int row,int col){
        if(gameState!=GameState.IN_PROGRESS){ // if game finished
            System.out.println("Game already finished"); // message
            return gameState; // return state
        }
        if(row<0||row>=size||col<0||col>=size){ // validate bounds
            System.out.println("Invalid position"); // invalid input
            return gameState; // return state
        }
        if(!board.isCellEmpty(row,col)){ // check if cell occupied
            System.out.println("Cell already occupied"); // invalid move
            return gameState; // return state
        }
        board.placeSymbol(row,col,currentPlayer.getSymbol()); // place symbol
        int value=(currentPlayer.getSymbol()==Symbol.X)?1:-1; // assign value
        rows[row]+=value; // update row counter
        cols[col]+=value; // update column counter
        if(row==col)diag+=value; // update main diagonal
        if(row+col==size-1)antiDiag+=value; // update anti diagonal
        board.printBoard(); // display board
        // check winner
        if(Math.abs(rows[row])==size||Math.abs(cols[col])==size||
           Math.abs(diag)==size||Math.abs(antiDiag)==size){
            gameState=GameState.WIN; // mark win
            System.out.println(currentPlayer.getSymbol()+" wins!"); // print winner
            return gameState; // return state
        }
        // check draw
        if(board.isFull()){ // board filled
            gameState=GameState.DRAW; // mark draw
            System.out.println("Game ended in DRAW"); // print message
            return gameState; // return state
        }
        switchPlayer(); // change turn
        return GameState.IN_PROGRESS; // continue game
    }
}
// ================= DRIVER =================
public class Main{
    public static void main(String[] args){ // O(1)
        TicTacGame game=new TicTacGame(3); // create 3x3 game
        game.printBoard(); // show initial board
        game.move(0,0); // X
        game.move(0,1); // O
        game.move(0,2); // X
        game.move(1,1); // O
        game.move(1,0); // X
        game.move(1,2); // O
        game.move(2,1); // X
        game.move(2,0); // O
        game.move(2,2); // final move
    }
}