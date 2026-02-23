import java.util.*; // import utility classes
// ================= GAME STATE ENUM =================
enum GameState{IN_PROGRESS,WIN,DRAW} // game lifecycle states
// ================= DISC COLOR ENUM =================
enum DiscColor{RED,YELLOW,EMPTY} // connect4 disc colors
// ================= PLAYER CLASS =================
class Player{
    private DiscColor color; // store player disc color
    public Player(DiscColor color){this.color=color;} // constructor
    public DiscColor getColor(){return color;} // return color
}
// ================= BOARD CLASS =================
class Board{
    private int rows; // number of rows
    private int cols; // number of columns
    private DiscColor[][] grid; // board grid
    private int filledCells; // track filled cells
    public Board(int rows,int cols){
        this.rows=rows; // assign rows
        this.cols=cols; // assign cols
        this.grid=new DiscColor[rows][cols]; // create grid
        this.filledCells=0; // initialize counter
        for(int i=0;i<rows;i++)Arrays.fill(grid[i],DiscColor.EMPTY); // fill empty cells
    }
    // ================= DROP DISC =================
    // Time Complexity: O(R)
    public int dropDisc(int col,DiscColor color){
        if(col<0||col>=cols)return -1; // invalid column
        for(int row=rows-1;row>=0;row--){ // start from bottom (gravity)
            if(grid[row][col]==DiscColor.EMPTY){ // find empty cell
                grid[row][col]=color; // place disc
                filledCells++; // update count
                return row; // return row where disc placed
            }
        }
        return -1; // column full
    }
    public boolean isFull(){return filledCells==rows*cols;} // check board full
    public DiscColor getCell(int r,int c){return grid[r][c];} // read cell
    public int getRows(){return rows;} // return rows
    public int getCols(){return cols;} // return cols
    public void printBoard(){ // display board
        for(int i=0;i<rows;i++){ // iterate rows
            for(int j=0;j<cols;j++){ // iterate columns
                if(grid[i][j]==DiscColor.EMPTY)System.out.print("_ "); // empty
                else if(grid[i][j]==DiscColor.RED)System.out.print("R "); // red
                else System.out.print("Y "); // yellow
            }
            System.out.println(); // next line
        }
        System.out.println(); // spacing
    }
}

// ================= GAME CONTROLLER =================
class Connect4Game{
    private Board board; // board instance
    private Player player1; // red player
    private Player player2; // yellow player
    private Player currentPlayer; // track current turn
    private GameState gameState; // current game state
    private static final int WIN_COUNT=4; // need 4 in a row to win
    public Connect4Game(int rows,int cols){
        this.board=new Board(rows,cols); // initialize board
        this.player1=new Player(DiscColor.RED); // create red player
        this.player2=new Player(DiscColor.YELLOW); // create yellow player
        this.currentPlayer=player1; // red starts
        this.gameState=GameState.IN_PROGRESS; // game active
    }
    public void printBoard(){board.printBoard();} // delegate printing
    // ================= SWITCH PLAYER =================
    private void switchPlayer(){ // Time Complexity: O(1)
        currentPlayer=(currentPlayer==player1)?player2:player1; // toggle turn
    }
    // ================= DROP MOVE =================
    // Time Complexity: O(R×C) for win check
    // Space Complexity: O(1)
    public GameState drop(int col){
        if(gameState!=GameState.IN_PROGRESS){ // if game finished
            System.out.println("Game already finished"); // message
            return gameState; // return state
        }
        int row=board.dropDisc(col,currentPlayer.getColor()); // drop disc
        if(row==-1){ // invalid move
            System.out.println("Invalid move, column full or out of bounds");
            return gameState;
        }
        board.printBoard(); // show board
        if(checkWin(row,col,currentPlayer.getColor())){ // check winner
            gameState=GameState.WIN; // mark win
            System.out.println(currentPlayer.getColor()+" wins!"); // print winner
            return gameState;
        }
        if(board.isFull()){ // check draw
            gameState=GameState.DRAW; // mark draw
            System.out.println("Game ended in DRAW");
            return gameState;
        }
        switchPlayer(); // change turn
        return GameState.IN_PROGRESS; // continue game
    }
    // ================= WIN CHECK =================
    // check horizontal, vertical, diagonal, anti-diagonal
    private boolean checkWin(int row,int col,DiscColor color){

        return checkDirection(row,col,0,1,color)|| // horizontal
               checkDirection(row,col,1,0,color)|| // vertical
               checkDirection(row,col,1,1,color)|| // diagonal
               checkDirection(row,col,1,-1,color); // anti diagonal
    }

    // ================= CHECK ONE DIRECTION =================
    private boolean checkDirection(int row,int col,int dr,int dc,DiscColor color){

        int count=1; // include current disc

        count+=countCells(row,col,dr,dc,color); // forward direction
        count+=countCells(row,col,-dr,-dc,color); // backward direction

        return count>=WIN_COUNT; // check 4 consecutive
    }

    // ================= COUNT MATCHING CELLS =================
    private int countCells(int r,int c,int dr,int dc,DiscColor color){
        int count=0; // initialize
        r+=dr; c+=dc; // move first step

        while(r>=0&&r<board.getRows()&&c>=0&&c<board.getCols()
              &&board.getCell(r,c)==color){
            count++; // increase count
            r+=dr; c+=dc; // move next
        }
        return count;
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){ // Time Complexity: O(1)

        Connect4Game game=new Connect4Game(6,7); // standard 6x7 board
        game.printBoard(); // show initial board

        game.drop(0); // RED
        game.drop(1); // YELLOW
        game.drop(0); // RED
        game.drop(1); // YELLOW
        game.drop(0); // RED
        game.drop(1); // YELLOW
        game.drop(0); // RED wins vertically
    }
}