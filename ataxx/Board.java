package ataxx;

/* Author: P. N. Hilfinger, (C) 2008. */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Observable;

import static ataxx.GameException.error;
import static ataxx.PieceColor.*;
import static java.lang.Math.abs;

/** An Ataxx board.   The squares are labeled by column (a char value between
 *  'a' - 2 and 'g' + 2) and row (a char value between '1' - 2 and '7'
 *  + 2) or by linearized index, an integer described below.  Values of
 *  the column outside 'a' and 'g' and of the row outside '1' to '7' denote
 *  two layers of border squares, which are always blocked.
 *  This artificial border (which is never actually printed) is a common
 *  trick that allows one to avoid testing for edge conditions.
 *  For example, to look at all the possible moves from a square, sq,
 *  on the normal board (i.e., not in the border region), one can simply
 *  look at all squares within two rows and columns of sq without worrying
 *  about going off the board. Since squares in the border region are
 *  blocked, the normal logic that prevents moving to a blocked square
 *  will apply.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Ahmad Badary
 */
class Board extends Observable {

    /**
     * Number of squares on a side of the board.
     */
    static final int SIDE = 7;
    /**
     * Length of a side + an artificial 2-deep border region.
     */
    static final int EXTENDED_SIDE = SIDE + 4;

    /**
     * Number of non-extending moves before game ends.
     */
    static final int JUMP_LIMIT = 25;

    /**
     * Array that represents the board.
     * @return PieceColor[] Array of the board.
     */
    public PieceColor[] getboard() {
        return _board;
    }

    /**
     * A new, cleared board at the start of the game.
     */
    Board() {
        _board = new PieceColor[EXTENDED_SIDE * EXTENDED_SIDE];
        clear();
    }

    /**
     * A copy of B.
     */
    Board(Board b) {
        _board = b._board.clone();
        numBlues = b.numBlues;
        numBlocks = b.numBlocks;
        numReds = b.numReds;
        numMoves = b.numMoves;
        numNPMoves = b.numNPMoves;
        _whoseMove = b._whoseMove;
    }

    /**
     * Return the linearized index of square COL ROW.
     */
    static int index(char col, char row) {
        return (row - '1' + 2) * EXTENDED_SIDE + (col - 'a' + 2);
    }

    /**
     * Return the linearized index of the square that is DC columns and DR
     * rows away from the square with index SQ.
     */
    static int neighbor(int sq, int dc, int dr) {
        return sq + dc + dr * EXTENDED_SIDE;
    }

    /**
     * Clear me to my starting state, with pieces in their initial
     * positions and no blocks.
     */
    void clear() {
        for (int i = 0; i < EXTENDED_SIDE * EXTENDED_SIDE; i++) {
            if (i <= (EXTENDED_SIDE * 2 + 1) || i >= (EXTENDED_SIDE * 9 - 2)) {
                _board[i] = BLOCKED;
            } else if ((i >= (EXTENDED_SIDE * 3 - 2)
                    &&
                    i <= (EXTENDED_SIDE * 3 + 1))
                    ||
                    (i >= (EXTENDED_SIDE * 4 - 2)
                            &&
                            i <= (EXTENDED_SIDE * 4 + 1))
                    ||
                    (i >= (EXTENDED_SIDE * 5 - 2)
                            &&
                            i <= (EXTENDED_SIDE * 5 + 1))
                    ||
                    (i >= (EXTENDED_SIDE * 6 - 2)
                            &&
                            i <= (EXTENDED_SIDE * 6 + 1))
                    ||
                    (i >= (EXTENDED_SIDE * 7 - 2)
                            &&
                            i <= (EXTENDED_SIDE * 7 + 1))
                    ||
                    (i >= (EXTENDED_SIDE * 8 - 2)
                            &&
                            i <= (EXTENDED_SIDE * 8 + 1))) {
                _board[i] = BLOCKED;
            } else {
                _board[i] = EMPTY;
            }
        }
        int begR = EXTENDED_SIDE * 8 + 2;
        int begB = EXTENDED_SIDE * 9 - 3;
        _whoseMove = RED;
        _board[EXTENDED_SIDE * 2 + 8] = RED;
        _board[EXTENDED_SIDE * 2 + 2] = BLUE;
        _board[begR] = RED;
        _board[begB] = BLUE;
        numBlocks = 0;
        numBlues = 2;
        numReds = 2;
        setChanged();
        notifyObservers();
    }

    /**
     * Return true iff the game is over: i.e., if neither side has
     * any moves, if one side has no pieces, or if there have been
     * MAX_JUMPS consecutive jumps without intervening extends.
     */
    boolean gameOver() {
        if (redPieces() == 0 || bluePieces() == 0) {
            return true;
        } else if (redPieces() + bluePieces() == (7 * 7 - numBlocks)) {
            return true;
        } else if (maxJumps >= JUMP_LIMIT) {
            return true;
        }
        return (!canMove(_whoseMove) && !canMove(_whoseMove.opposite()));
    }

    /**
     * Return number of red pieces on the board.
     */
    int redPieces() {
        return numPieces(RED);
    }

    /**
     * Return number of blue pieces on the board.
     */
    int bluePieces() {
        return numPieces(BLUE);
    }

    /**
     * Return number of COLOR pieces on the board.
     */
    int numPieces(PieceColor color) {
        if (color == RED) {
            return this.numReds;
        } else {
            return this.numBlues;
        }
    }

    /**
     * Increment numPieces(COLOR) by K.
     */
    private void incrPieces(PieceColor color, int k) {
        if (color == RED) {
            this.numReds += k;
        } else {
            this.numBlues += k;
        }
    }

    /**
     * The current contents of square CR, where 'a'-2 <= C <= 'g'+2, and
     * '1'-2 <= R <= '7'+2.  Squares outside the range a1-g7 are all
     * BLOCKED.  Returns the same value as get(index(C, R)).
     */
    PieceColor get(char c, char r) {
        return _board[index(c, r)];
    }

    /**
     * Return the current contents of square with linearized index SQ.
     */
    PieceColor get(int sq) {
        return _board[sq];
    }

    /**
     * Set get(C, R) to V, where 'a' <= C <= 'g', and
     * '1' <= R <= '7'.
     */
    private void set(char c, char r, PieceColor v) {
        set(index(c, r), v);
    }

    /**
     * Set square with linearized index SQ to V.  This operation is
     * undoable.
     */
    private void set(int sq, PieceColor v) {
        undoable = true;
        _board[sq] = v;
    }

    /**
     * Set square at C R to V (not undoable).
     */
    private void unrecordedSet(char c, char r, PieceColor v) {
        _board[index(c, r)] = v;
    }

    /**
     * Set square at linearized index SQ to V (not undoable).
     */
    private void unrecordedSet(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /**
     * Return true iff MOVE is legal on the current board.
     */
    void legalMove(Move move) {
        if (move == null) {
            throw new GameException("Illegal move.");
        }
        if (move.isPass()) {
            if (!canMove(whoseMove())) {
                return;
            }
            throw new GameException("Illegal move.");
        }
        if (_board[move.toIndex()] == BLOCKED) {
            throw new GameException("Illegal move.");
        }
        if (!get(move.col0(), move.row0()).equals(whoseMove())) {
            throw new GameException("Illegal move.");
        }
        if (get(move.col1(), move.row1()) != EMPTY) {
            throw new GameException("Illegal move.");
        }
        if ((move.col1() - move.col0()) == 0
                &&
                (move.row1() - move.row0()) == 0) {
            throw new GameException("Illegal move.");
        }
        if (Math.abs((move.col1() - move.col0())) > 2
                ||
                Math.abs((move.row1() - move.row0())) > 2) {
            throw new GameException("Illegal move.");
        }
        return;
    }

    /**
     * Return true iff MOVE is legal on the current board.
     */
    boolean islegalMove(Move move) {
        if (move.isPass()) {
            if (!canMove(whoseMove())) {
                return true;
            }
            return false;
        }
        if (_board[move.toIndex()] == BLOCKED) {
            return false;
        }
        if (!get(move.col0(), move.row0()).equals(whoseMove())) {
            return false;
        }
        if (get(move.col1(), move.row1()) != EMPTY) {
            return false;
        }
        if ((move.col1() - move.col0()) == 0
                &&
                (move.row1() - move.row0()) == 0) {
            return false;
        }
        if (Math.abs((move.col1() - move.col0())) > 2
                ||
                Math.abs((move.row1() - move.row0())) > 2) {
            return false;
        }
        return true;
    }

    /**
     * Return true iff player WHO can move, ignoring whether it is
     * that player's move and whether the game is over.
     */
    boolean canMove(PieceColor who) {
        for (int i = 0; i < _board.length; i++) {
            if (_board[i] == who) {
                int k;
                for (k = (i - (EXTENDED_SIDE * 2 + 2));
                     k <= (i - (EXTENDED_SIDE * 2 - 2)); k++) {
                    if (isMove(k)) {
                        return true;
                    }
                }
                for (k = (i - 13); k <= (i - 9); k++) {
                    if (isMove(k)) {
                        return true;
                    }
                }
                for (k = (i - 2); k <= (i + 2); k++) {
                    if (isMove(k)) {
                        return true;
                    }
                }
                for (k = (i + 9); k <= (i + 13); k++) {
                    if (isMove(k)) {
                        return true;
                    }
                }
                for (k = (i + (EXTENDED_SIDE * 2 - 2));
                     k <= (i + (EXTENDED_SIDE * 2 + 2)); k++) {
                    if (isMove(k)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return the color of the player who has the next move.  The
     * value is arbitrary if gameOver().
     */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /**
     * Return total number of moves and passes since the last
     * clear or the creation of the board.
     */
    int numMoves() {
        return numMoves;
    }

    /**
     * Return number of non-pass moves made in the current game since the
     * last extend move added a piece to the board (or since the
     * start of the game). Used to detect end-of-game.
     */
    int numJumps() {
        return numNPMoves;
    }

    /**
     * Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     * other than pass, assumes that legalMove(C0, R0, C1, R1).
     */
    void makeMove(char c0, char r0, char c1, char r1) {
        if (c0 == '-') {
            makeMove(Move.pass());
        } else {
            makeMove(Move.move(c0, r0, c1, r1));
        }
    }

    /**
     * Make the MOVE on this Board, assuming it is legal.
     */
    void makeMove(Move move) {
        legalMove(move);
        lastBoard = null;
        lastBoard = new Board(this);
        lastMove = move;
        if (move.isPass()) {
            pass();
            numMoves += 1;
            allMovesL.add(move);
            return;
        }
        if (move.isJump()) {
            turn(move.toIndex());
            _board[move.fromIndex()] = EMPTY;
            if (!extend) {
                maxJumps += 1;
            } else {
                maxJumps = 0;
                extend = false;
            }
        }
        if (move.isExtend()) {
            incrPieces(whoseMove(), 1);
            turn(move.toIndex());
            extend = true;
        }
        _board[move.toIndex()] = _whoseMove;
        PieceColor opponent = _whoseMove.opposite();
        _whoseMove = opponent;
        numNPMoves += 1;
        numMoves += 1;
        allMovesL.add(move);
        setChanged();
        notifyObservers();
    }

    /**
     * Update to indicate that the current player passes, assuming it
     * is legal to do so.  The only effect is to change whoseMove().
     */
    void pass() {
        PieceColor opponent = _whoseMove.opposite();
        legalMove(new Move());
        _whoseMove = opponent;
        setChanged();
        notifyObservers();
    }

    /**
     * Undo the last move.
     */
    void undo() {
        if (lastMove == null) {
            clear();
            return;
        }
        if (lastMove.isExtend()) {
            decrPieces(get(lastMove.toIndex()), 1);
        }
        _board = lastBoard._board.clone();
        numBlues = lastBoard.numBlues;
        numBlocks = lastBoard.numBlocks;
        numReds = lastBoard.numReds;
        numMoves = lastBoard.numMoves;
        numNPMoves = lastBoard.numNPMoves;
        _whoseMove = lastBoard.whoseMove();
        lastMove = lastBoard.lastMove;
        lastBoard = lastBoard.lastBoard;


        setChanged();
        notifyObservers();
    }

    /**
     * Indicate beginning of a move in the undo stack.
     */
    private void startUndo() {
    }

    /**
     * Add an undo action for changing SQ to NEWCOLOR on current
     * board.
     */
    private void addUndo(int sq, PieceColor newColor) {
    }

    /**
     * Return true iff it is legal to place a block at C R.
     */
    boolean legalBlock(char c, char r) {
        if (_board[index(c, r)] == null || _board[index(c, r)] == EMPTY) {
            return true;
        }
        return false;
    }


    /**
     * Return true iff it is legal to place a block at CR.
     */
    boolean legalBlock(String cr) {
        return legalBlock(cr.charAt(0), cr.charAt(1));
    }

    /**
     * Set a block on the square C R and its reflections across the middle
     * row and/or column, if that square is unoccupied and not
     * in one of the corners. Has no effect if any of the squares is
     * already occupied by a block.  It is an error to place a block on a
     * piece.
     */
    void setBlock(char c, char r) {
        if (!legalBlock(c, r)) {
            throw error("illegal block placement");
        }
        _board[index(c, r)] = BLOCKED;
        numBlocks += 1;
        char opRow = (char) (abs((int) (r - '0') - 8) + '0');
        char opCol = (char) (abs((int) (c - 'a') - 7) - 1 + 'a');
        if (legalBlock(c, opRow)) {
            _board[index(c, opRow)] = BLOCKED;
            numBlocks += 1;
        }
        if (legalBlock(opCol, r)) {
            _board[index(opCol, r)] = BLOCKED;
            numBlocks += 1;
        }
        if (legalBlock(opCol, opRow)) {
            _board[index(opCol, opRow)] = BLOCKED;
            numBlocks += 1;
        }

        setChanged();
        notifyObservers();
    }

    /**
     * Place a block at CR.
     */
    void setBlock(String cr) {
        setBlock(cr.charAt(0), cr.charAt(1));
    }

    /**
     * Return a list of all moves made since the last clear (or start of
     * game).
     */
    List<Move> allMoves() {
        return allMovesL;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /* .equals used only for testing purposes. */
    @Override
    public boolean equals(Object obj) {
        Board other = (Board) obj;
        return Arrays.equals(_board, other._board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_board);
    }

    /**
     * Return a text depiction of the board (not a dump).  If LEGEND,
     * supply row and column numbers around the edges.
     */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        return out.toString();
    }

    /**
     * For reasons of efficiency in copying the board,
     * we use a 1D array to represent it, using the usual access
     * algorithm: row r, column c => index(r, c).
     * <p>
     * Next, instead of using a 7x7 board, we use an 11x11 board in
     * which the outer two rows and columns are blocks, and
     * row 2, column 2 actually represents row 0, column 0
     * of the real board.  As a result of this trick, there is no
     * need to special-case being near the edge: we don't move
     * off the edge because it looks blocked.
     * <p>
     * Using characters as indices, it follows that if 'a' <= c <= 'g'
     * and '1' <= r <= '7', then row c, column r of the board corresponds
     * to board[(c -'a' + 2) + 11 (r - '1' + 2) ], or by a little
     * re-grouping of terms, board[c + 11 * r + SQUARE_CORRECTION].
     */
    private PieceColor[] _board;

    /**
     * Player that is on move.
     */
    private PieceColor _whoseMove;

    /**
     * Number of RED pieces.
     */
    private int numReds = 2;

    /**
     * Number of BLUE pieces.
     */
    private int numBlues = 2;

    /**
     * A List of all the moves made in the game.
     */
    private List<Move> allMovesL = new ArrayList<Move>();

    /**
     * Number of BLOCKS on the board.
     */
    private int numBlocks = 0;

    /**
     * Number of non-extended Jumps.
     */
    private int maxJumps;

    /**
     * Boolean to check if a move is undoable.
     */
    private boolean undoable;

    /**
     * Boolean to check if a move is an extend.
     */
    private boolean extend = false;

    /**
     * Boolean to check if a move is an extend.
     */
    private int lastboardcheck = 1;

    /**
     * Number of Moves since the beginning of the game.
     */
    private int numMoves = 0;

    /**
     * Number of Moves since the beginning of the game.
     */
    private int numNPMoves = 0;

    /**
     * The last move performed on the board.
     */
    private Move lastMove;

    /**
     * The last BOARD before the latest move.
     */
    private Board lastBoard = null;

    /**
     * Function to check if a move is viable.
     * @param ind The index of the current players move.
     * @return  boolean Can we move.
     */
    private boolean isMove(int ind) {
        if (_board[ind] == BLOCKED || _board[ind] != EMPTY) {
            return false;
        }
        return true;
    }


    /**
     * Function to change the pieces around you after a jump.
     * @param ind The index of the current players move.
     */
    private void turn(int ind) {
        int k;
        for (k = (ind - 12); k <= (ind - 10); k++) {
            if (_board[k] == whoseMove().opposite() && k != ind) {
                _board[k] = whoseMove();
                incrPieces(whoseMove(), 1);
                decrPieces(whoseMove().opposite(), 1);
            }
        }
        for (k = (ind - 1); k <= (ind + 1); k++) {
            if (_board[k] == whoseMove().opposite() && k != ind) {
                _board[k] = whoseMove();
                incrPieces(whoseMove(), 1);
                decrPieces(whoseMove().opposite(), 1);
            }
        }

        for (k = (ind + 10); k <= (ind + 12); k++) {
            if (_board[k] == whoseMove().opposite() && k != ind) {
                _board[k] = whoseMove();
                incrPieces(whoseMove(), 1);
                decrPieces(whoseMove().opposite(), 1);
            }
        }
    }

    /**
     * Decrement numPieces(COLOR) by K.
     */
    private void decrPieces(PieceColor color, int k) {
        if (color == RED) {
            this.numReds -= k;
        } else {
            this.numBlues -= k;
        }
    }

    /**
     * Concatenates all the moves that a piece can move.
     * @param arr The array that holds all moves.
     * @param ind The index of the player of the current move.
     * @param who The player.
     */
    void allocateAllMoves(ArrayList<Move> arr, int ind, PieceColor who) {
        int k;
        int cf = ((ind % 11) - 1) + 1;
        int rf = ((ind - cf - 2) / 11) + 1;
        for (k = (ind - (EXTENDED_SIDE * 2 + 2));
             k <= (ind - (EXTENDED_SIDE * 2 - 2)); k++) {
            int ct = ((((k % 11) - 1)) + 1);
            int rt = (((k - ct - 2) / 11) + 1);
            if (ct < 2 || rt < 2) {
                continue;
            }
            if (islegalMove(new Move(cf, rf, ct, rt))) {
                arr.add(new Move(cf, rf, ct, rt));
            }
        }
        for (k = (ind - 13); k <= (ind - 9); k++) {
            int ct = ((((k % 11) - 1)) + 1);
            int rt = (((k - ct - 2) / 11) + 1);
            if (ct < 2 || rt < 2) {
                continue;
            }
            if (islegalMove(new Move(cf, rf, ct, rt))) {
                arr.add(new Move(cf, rf, ct, rt));
            }
        }
        for (k = (ind - 2); k <= (ind + 2); k++) {
            int ct = ((((k % 11) - 1)) + 1);
            int rt = (((k - ct - 2) / 11) + 1);
            if (ct < 2 || rt < 2) {
                continue;
            }
            if (islegalMove(new Move(cf, rf, ct, rt))) {
                arr.add(new Move(cf, rf, ct, rt));
            }
        }
        for (k = (ind + 9); k <= (ind + 13); k++) {
            int ct = ((((k % 11) - 1)) + 1);
            int rt = (((k - ct - 2) / 11) + 1);
            if (ct < 2 || rt < 2) {
                continue;
            }
            if (islegalMove(new Move(cf, rf, ct, rt))) {
                arr.add(new Move(cf, rf, ct, rt));
            }
        }
        for (k = (ind + (EXTENDED_SIDE * 2 - 2));
             k <= (ind + (EXTENDED_SIDE * 2 + 2)); k++) {
            int ct = ((((k % 11) - 1)) + 1);
            int rt = (((k - ct - 2) / 11) + 1);
            if (ct < 2 || rt < 2) {
                continue;
            }
            if (islegalMove(new Move(cf, rf, ct, rt))) {
                arr.add(new Move(cf, rf, ct, rt));
            }
        }
    }
}
