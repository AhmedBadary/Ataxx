package ataxx;

import java.util.ArrayList;

import static ataxx.PieceColor.RED;

/** A Player that computes its own moves.
 *  @author Ahmad Badary
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
            return Move.pass();
        }
        Move move = findMove();
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
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value >= BETA if SENSE==1,
     * and minimal value or value <= ALPHA if SENSE==-1. Searches up to
     * DEPTH levels before using a static estimate.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Board currBoard = board;
        _lastFoundMove = new Move();
        ArrayList<Move> allMoves = availableMoves(currBoard, myColor());
        for (Move mv : allMoves) {
            if (staticScore(currBoard, mv) > alpha) {
                _lastFoundMove = mv;
                alpha = staticScore(currBoard, mv);
            }
        }
        return 0;
    }

    /**
     * Return a heuristic value for BOARD.
     * @param  board is the current board.
     * @param move is the move to evaluate.
     * @return int is the current static score.
     */
    private int staticScore(Board board, Move move) {
        Board tempBoard = new Board(board);
        tempBoard.makeMove(move);
        int totalScore;
        int sizeAdvantage = tempBoard.numPieces(myColor())
                -
                tempBoard.numPieces(myColor().opposite());
        totalScore = sizeAdvantage;
        return totalScore;
    }

    /**
     * ALL MOVES GENERATED AS AN ARRAY OF MOVES.
     * @param board is the current board to search in.
     * @param player the current player.
     * @return ArrayList<Move> array of all moves.
     **/
    public ArrayList<Move> availableMoves(Board board, PieceColor player) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for (int i = (board.EXTENDED_SIDE * 2 + 2);
             i <= (board.EXTENDED_SIDE * 9 - 3); i++) {
            if (board.get(i) == myColor()) {
                board.allocateAllMoves(allMoves, i, myColor());
            }
        }
        return allMoves;
    }
}
