package ataxx;

import com.sun.tools.javac.code.Attribute;

import java.util.ArrayList;

import static ataxx.PieceColor.RED;

/** A Player that computes its own moves.
 *  @author
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 4;
    /**
     * A position magnitude indicating a win (for red if positive, blue
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR.
     */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        if (!board().canMove(myColor())) {
            // FIXME?
            return Move.pass();
        }
        Move move = findMove();
        // FIXME?
        //return Move.pass();
        return move;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == RED) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * Used to communicate best moves found by findMove, when asked for.
     */
    private Move _lastFoundMove;

    /**
     * Used to communicate best moves found by findMove, when asked for.
     */
    private Move currBest;

    /**
     * Used to communicate best moves found by findMove, when asked for.
     */
    private Move currWorst;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value >= BETA if SENSE==1,
     * and minimal value or value <= ALPHA if SENSE==-1. Searches up to
     * DEPTH levels before using a static estimate.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        // FIXME

        Board currBoard = board;
        _lastFoundMove = new Move();
        //Move currBestMove = new Move();
        ArrayList<Move> allMoves = availableMoves(currBoard, myColor());
        for (Move mv : allMoves) {
            if (staticScore(currBoard, mv) > alpha) {
                currBest = mv;
                alpha = staticScore(currBoard, mv);
            }
        }
        return 0;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private Move MiniMax(ArrayList<Move> , Move move) {
        // FIXME
        //ArrayList<Move> arrM2 = new ArrayList<>();
        //ArrayList<ArrayList<Move>> allMovesofBoard1 = new ArrayList<>();
        //ArrayList<ArrayList<Move>> allMovesofBoard2 = new ArrayList<>();
        //ArrayList<ArrayList<Board>> arrB = new ArrayList<>();
        ArrayList<Move> arrM1 = availableMoves(board(), myColor());
        ArrayList<Board> arrB1 = findBoards(board(), arrm1);
        ArrayList<ArrayList<Move>> arrArrM12 = new ArrayList<>();
        for (Board brd : arrB1) {
            arrArrM12.add(availableMoves(brd, myColor().opposite()));
        }
        ArrayList<ArrayList<Board>> arrArrB12 = new ArrayList<>();
        for (ArrayList<Move> mv : arrArrM12) {
            ArrayList<Board> temp = findBoards(, arrm1);
            arrArrM12.add(availableMoves(brd, myColor().opposite()));
        }

        MiniMaxHelper(pickher, arrM1, allMovesofBoard1, arrB);

        Board tempBoard = new Board(board);
        tempBoard.makeMove(move);
        int totalScore;
        int sizeAdvantage = tempBoard.numPieces(myColor()) - tempBoard.numPieces(myColor().opposite());
        totalScore = sizeAdvantage;
        return totalScore;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int MiniMaxHelper(ArrayList<Board> pickhere, ArrayList<Move> arrM, ArrayList<ArrayList<Move>> arrofarrofMoves, ArrayList<ArrayList<Board>> arrB) {
        //for (Board ph : pickhere) {
            Board tempBoard;
            ArrayList<Board> temparrB = null;
            for (Move mv : arrM) {
                tempBoard = new Board(ph);
                tempBoard.makeMove(mv);
                temparrB.add(new Board(tempBoard));
            }
            arrB.add(temparrB);
            for (Board brd : temparrB) {
                ArrayList<Move> arrM1 = availableMoves(brd, myColor());
                arrofarrofMoves.add(arrM1);
            }
        //}
    }




    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board, Move move) {
        // FIXME
        Board tempBoard = new Board(board);
        tempBoard.makeMove(move);
        int totalScore;
        int sizeAdvantage = tempBoard.numPieces(myColor()) - tempBoard.numPieces(myColor().opposite());
        totalScore = sizeAdvantage;
        return totalScore;
    }

    /**
     * ALL MOVES GENERATED AS AN ARRAY OF MOVES.
     **/
    public ArrayList<Move> availableMoves(Board board, PieceColor player) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for (int i = 24; i <= 96; i++) {
            if (board.get(i) == myColor()) {
                board.allocateAllMoves(allMoves, i, myColor());
            }
        }
        return allMoves;
    }

    /**
     * ALL MOVES GENERATED AS AN ARRAY OF MOVES.
     **/
    class TreeStruct<Board> {
        private ArrayList<Move> data;
        private ArrayList<TreeStruct<Board>> children;
        private TreeStruct<Board> parent;

        public TreeStr

        uct(ArrayList<Move> data) {
            this.data = data;
            this.children = new ArrayList<>();
        }

        /**
         * Initialize a tree with another tree's data.
         *
         * @param tree The tree to be copied.
         */
        public TreeStruct(TreeStruct<Board> tree) {
            this.data = tree.data;
            children = new ArrayList<TreeStruct<Board>>();
        }
        /**
         * Add child.
         * @param child child.
         */
        public void addChild(TreeStruct<Board> child) {
            child.parent = this;
            children.add(child);
        }

    }
}

