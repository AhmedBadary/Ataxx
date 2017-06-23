package ataxx;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests of the Board class.
 *  @author Ahmad Badary
 */
public class BoardTest {

    private static final String[]
        GAME1 = { "a7-b7", "a1-a2",
                  "a7-a6", "a2-a3",
                  "a6-a5", "a3-a4" };

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(s.charAt(0), s.charAt(1),
                       s.charAt(3), s.charAt(4));
        }
    }

    @Test public void testdoOnce() {
        Board b0 = new Board();
        String[] teststr = {"a7-b7"};
        String[] teststr2 = {"g7-f6"};
        makeMoves(b0, teststr);
        assertEquals("failed to move correctly 1",
                PieceColor.RED, b0.getboard()[90]);
        makeMoves(b0, teststr2);
        assertEquals("failed to move correctly 2",
                PieceColor.BLUE, b0.getboard()[84]);
    }

    @Test public void testEmptyOnce() {
        Board b0 = new Board();
        assertEquals("failed to set Empty 1",
                PieceColor.EMPTY,  b0.getboard()[50]);
        assertEquals("failed to set Empty 2",
                PieceColor.EMPTY, b0.getboard()[59]);
    }

    @Test public void testBlockOnce() {
        Board b0 = new Board();
        String teststr = "b7";
        String teststr2 = "f6";
        b0.setBlock(teststr);
        assertEquals("failed to set Block 1-0",
                PieceColor.BLOCKED, b0.getboard()[91]);
        assertEquals("failed to set Block 1-1",
                PieceColor.BLOCKED, b0.getboard()[25]);
        assertEquals("failed to set Block 1-2",
                PieceColor.BLOCKED, b0.getboard()[95]);
        assertEquals("failed to set Block 1-3",
                PieceColor.BLOCKED, b0.getboard()[29]);
        b0.setBlock(teststr2);
        assertEquals("failed to set Block 2-0",
                PieceColor.BLOCKED, b0.getboard()[84]);
        assertEquals("failed to set Block 2-1",
                PieceColor.BLOCKED, b0.getboard()[80]);
        assertEquals("failed to set Block 2-2",
                PieceColor.BLOCKED, b0.getboard()[40]);
        assertEquals("failed to set Block 2-3",
                PieceColor.BLOCKED, b0.getboard()[36]);

    }

    @Test public void testUndoOnce() {
        Board b0 = new Board();
        Board b1 = new Board();
        String[] teststr = {"a7-b7"};
        makeMoves(b0, teststr);
        Board b2 = new Board(b0);
        for (int i = 0; i < 1; i += 1) {
            b0.undo();
        }
        boolean tr = b0.equals(b1);
        assertEquals("failed to return to start", b1, b0);
        makeMoves(b0, teststr);
        assertEquals("second pass failed to reach same position", b2, b0);
    }


    @Test public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        assertEquals("failed to return to start", b1, b0);
    }
}
