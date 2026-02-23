import java.util.*; // import utility classes

// ================= SNAKE GAME =================
class SnakeGame{

    private int rows; // grid rows
    private int cols; // grid cols

    private Deque<int[]> snake; // snake body (head at front)
    private Set<Integer> occupied; // occupied cells for O(1) collision

    private int[][] food; // food positions
    private int foodIndex; // next food pointer

    private int score; // current score

    // ================= CONSTRUCTOR =================
    // Time Complexity: O(F)
    public SnakeGame(int rows,int cols,String[] foodPositions){

        this.rows=rows;
        this.cols=cols;

        snake=new LinkedList<>(); // initialize snake body
        occupied=new HashSet<>(); // initialize occupied set

        // initial snake position (0,0)
        snake.offerFirst(new int[]{0,0});
        occupied.add(encode(0,0));

        // parse food positions
        food=new int[foodPositions.length][2];
        for(int i=0;i<foodPositions.length;i++){
            String[] parts=foodPositions[i].split(",");
            food[i][0]=Integer.parseInt(parts[0]);
            food[i][1]=Integer.parseInt(parts[1]);
        }

        foodIndex=0;
        score=0;
    }

    // ================= MOVE =================
    // Time Complexity: O(1)
    public int move(String direction){

        int[] head=snake.peekFirst(); // current head
        int newRow=head[0];
        int newCol=head[1];

        // compute new position
        if(direction.equals("U"))newRow--;
        else if(direction.equals("D"))newRow++;
        else if(direction.equals("L"))newCol--;
        else if(direction.equals("R"))newCol++;

        // check wall collision
        if(newRow<0||newRow>=rows||newCol<0||newCol>=cols)
            return -1;

        int newPos=encode(newRow,newCol);

        // check if eating food
        boolean isFood=false;
        if(foodIndex<food.length &&
           food[foodIndex][0]==newRow &&
           food[foodIndex][1]==newCol){

            isFood=true;
            foodIndex++;
            score++;
        }

        // if not growing remove tail first
        if(!isFood){
            int[] tail=snake.pollLast();
            occupied.remove(encode(tail[0],tail[1]));
        }

        // check self collision
        if(occupied.contains(newPos))
            return -1;

        // add new head
        snake.offerFirst(new int[]{newRow,newCol});
        occupied.add(newPos);

        return score;
    }

    // ================= ENCODE CELL =================
    private int encode(int r,int c){ // O(1)
        return r*cols+c;
    }
}

// ================= DRIVER =================
public class Main{
    public static void main(String[] args){

        String[] food={"1,2","0,1"}; // food positions
        SnakeGame game=new SnakeGame(3,3,food);

        System.out.println(game.move("R")); // 0
        System.out.println(game.move("D")); // 0
        System.out.println(game.move("R")); // 1 (eat food)
        System.out.println(game.move("U")); // 1
        System.out.println(game.move("L")); // 2 (eat food)
        System.out.println(game.move("U")); // -1 (wall hit)
    }
}