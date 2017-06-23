package ataxx;

/* Author: P. N. Hilfinger */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import static ataxx.Command.Type.*;
import static ataxx.Game.State.*;
import static ataxx.GameException.error;

/** Controls the play of the game.
 *  @author Ahmad Badary
 */
class Game {

    /**
     * States of play.
     */
    static enum State {
        SETUP, PLAYING, FINISHED;
    }

    /**
     * A new Game, using BOARD to play on, reading initially from
     * BASESOURCE and using REPORTER for error and informational messages.
     */
    Game(Board board, CommandSource baseSource, Reporter reporter) {
        _inputs.addSource(baseSource);
        _board = board;
        _reporter = reporter;
    }

    /**
     * Run a session of Ataxx gaming.  Use an AtaxxGUI iff USEGUI.
     */
    void process(boolean useGUI) {
        red = new Manual(this, PieceColor.RED);
        blue = new AI(this, PieceColor.BLUE);
        _state = SETUP;
        currPlayer = red;
        while (true) {
            doClear(null);
            while (_state == SETUP) {
                doCommand();
            }
            _state = PLAYING;
            if (redISauto) {
                if (currPlayer == red) {
                    red = new AI(this, PieceColor.RED);
                    redISauto = false;
                    currPlayer = red;
                } else {
                    red = new AI(this, PieceColor.RED);
                    redISauto = false;
                }
            }
            if (blueISman) {
                if (currPlayer == blue) {
                    blue = new Manual(this, PieceColor.BLUE);
                    blueISman = false;
                    currPlayer = blue;
                } else {
                    blue = new Manual(this, PieceColor.BLUE);
                    blueISman = false;
                }
            }
            while (_state != SETUP && !_board.gameOver()) {
                try {
                    Move move = currPlayer.myMove();
                    if (_state == PLAYING) {
                        if (currPlayer instanceof AI) {
                            if (move.isPass()) {
                                System.out.println(currPlayer.myColor()
                                        + " passes" + ".");
                            } else {
                                System.out.println(currPlayer.myColor()
                                        + " moves " + move.toString() + ".");
                            }
                        }
                        _board.makeMove(move);
                    }
                    currPlayer = (currPlayer == red) ? blue : red;
                } catch (GameException E) {
                    System.out.println("Illegal move");
                }
            }
            if (_state != SETUP) {
                printResult(_board);
                _state = FINISHED;
            }
            while (_state == FINISHED) {
                doCommand();
            }
        }
    }

    /**
     * Return a view of my game board that should not be modified by
     * the caller.
     */
    Board board() {
        return _board;
    }

    /**
     * Perform the next command from our input source.
     */
    void doCommand() {
        try {
            Command cmnd =
                    Command.parseCommand(_inputs.getLine("ataxx: "));
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GameException excp) {
            _reporter.errMsg(excp.getMessage());
        }
    }

    /**
     * Read and execute commands until encountering a move or until
     * the game leaves playing state due to one of the commands. Return
     * the terminating move command, or null if the game first drops out
     * of playing mode. If appropriate to the current input source, use
     * PROMPT to prompt for input.
     */
    Command getMoveCmnd(String prompt) {
        Command mvCmnd = null;
        while (_state == PLAYING) {
            try {
                Command cmnd = Command.parseCommand(_inputs.getLine(prompt));
                if (cmnd.commandType() == PIECEMOVE
                        ||
                        cmnd.commandType() == PASS) {
                    mvCmnd = cmnd;
                    return mvCmnd;
                }
                if (cmnd.commandType() == QUIT || cmnd.commandType() == EOF) {
                    doQuit(null);
                }
                if (cmnd.commandType().equals(Command.Type.BLOCK)) {
                    throw new GameException("Not available now");
                }
                _commands.get(cmnd.commandType()).accept(cmnd.operands());

            } catch (GameException excp) {
                _reporter.errMsg(excp.getMessage());
            }
        }
        return mvCmnd;
    }


    /**
     * Return random integer between 0 (inclusive) and MAX>0 (exclusive).
     */
    int nextRandom(int max) {
        return _randoms.nextInt(max);
    }

    /**
     * Report a move, using a message formed from FORMAT and ARGS as
     * for String.format.
     */
    void reportMove(String format, Object... args) {
        _reporter.moveMsg(format, args);
    }

    /**
     * Report an error, using a message formed from FORMAT and ARGS as
     * for String.format.
     */
    void reportError(String format, Object... args) {
        _reporter.errMsg(format, args);
    }

    /* Command Processors */

    /**
     * Perform the command 'auto OPERANDS[0]'.
     */
    void doAuto(String[] operands) {
        if (operands[0].equals("red")) {
            redISauto = true;
        }
    }

    /**
     * Perform a 'help' command.
     */
    void doHelp(String[] unused) {
        InputStream helpIn =
                Game.class.getClassLoader().getResourceAsStream(
                        "ataxx/help.txt");
        if (helpIn == null) {
            System.err.println("No help available.");
        } else {
            try {
                BufferedReader r
                        = new BufferedReader(new InputStreamReader(helpIn));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(line);
                }
                r.close();
            } catch (IOException e) {
                /* Ignore IOException */
            }
        }
    }

    /**
     * Perform the command 'load OPERANDS[0]'.
     */
    void doLoad(String[] operands) {
        try {
            FileReader reader = new FileReader(operands[0]);
            ReaderSource source = new ReaderSource(reader, true);
            _inputs.addSource(source);
        } catch (IOException e) {
            throw error("Cannot open file %s", operands[0]);
        }
    }

    /**
     * Perform the command 'manual OPERANDS[0]'.
     */
    void doManual(String[] operands) {
        if (operands[0].equals("blue")) {
            blueISman = true;
        }
    }

    /**
     * Exit the program.
     */
    void doQuit(String[] unused) {
        System.exit(0);
    }

    /**
     * Perform the command 'start'.
     */
    void doStart(String[] unused) {
        checkState("start", SETUP);
        _state = PLAYING;
    }

    /**
     * Perform the move OPERANDS[0].
     */
    void doMove(String[] operands) {
        if (operands.length == 4) {
            _board.makeMove(operands[0].charAt(0), operands[1].charAt(0),
                    operands[2].charAt(0), operands[3].charAt(0));
            currPlayer = (currPlayer == red) ? blue : red;
        }
    }

    /**
     * Cause current player to pass.
     */
    void doPass(String[] unused) {
        _board.pass();
    }

    /**
     * Perform the command 'clear'.
     */
    void doClear(String[] unused) {
        _board.clear();
    }

    /**
     * Perform the command 'dump'.
     */
    void doDump(String[] unused) {
        naivePrint();
    }

    /**
     * Execute 'seed OPERANDS[0]' command, where the operand is a string
     * of decimal digits. Silently substitutes another value if
     * too large.
     */
    void doSeed(String[] operands) {
        _randoms = new Random(Long.parseLong(operands[0]));

    }

    /**
     * Execute the command 'block OPERANDS[0]'.
     */
    void doBlock(String[] operands) {
        _board.setBlock(operands[0]);
    }

    /**
     * Execute the artificial 'error' command.
     */
    void doError(String[] unused) {
        throw error("Command not understood");
    }

    /**
     * Report the outcome of the current game.
     * @param  str is the message to report.
     */
    void reportWinner(String str) {
        _reporter.outcomeMsg(str);
    }

    /**
     * Check that game is currently in one of the states STATES, assuming
     * CMND is the command to be executed.
     */
    private void checkState(Command cmnd, State... states) {
        for (State s : states) {
            if (s == _state) {
                return;
            }
        }
        throw error("'%s' command is not allowed now.", cmnd.commandType());
    }

    /**
     * Check that game is currently in one of the states STATES, using
     * CMND in error messages as the name of the command to be executed.
     */
    private void checkState(String cmnd, State... states) {
        for (State s : states) {
            if (s == _state) {
                return;
            }
        }
        throw error("'%s' command is not allowed now.", cmnd);
    }

    /**
     * Mapping of command types to methods that process them.
     */
    private final HashMap<Command.Type, Consumer<String[]>> _commands =
            new HashMap<>();

    {
        _commands.put(AUTO, this::doAuto);
        _commands.put(BLOCK, this::doBlock);
        _commands.put(CLEAR, this::doClear);
        _commands.put(DUMP, this::doDump);
        _commands.put(HELP, this::doHelp);
        _commands.put(MANUAL, this::doManual);
        _commands.put(PASS, this::doPass);
        _commands.put(PIECEMOVE, this::doMove);
        _commands.put(SEED, this::doSeed);
        _commands.put(START, this::doStart);
        _commands.put(LOAD, this::doLoad);
        _commands.put(QUIT, this::doQuit);
        _commands.put(ERROR, this::doError);
        _commands.put(EOF, this::doQuit);
    }

    /**
     * Input source.
     */
    private final CommandSources _inputs = new CommandSources();

    /**
     * My board.
     */
    private Board _board;
    /**
     * Current game state.
     */
    private State _state;
    /**
     * Used to send messages to the user.
     */
    private Reporter _reporter;
    /**
     * Source of pseudo-random numbers (used by AIs).
     */
    private Random _randoms = new Random();
    /**
     * Source of players of the game (used by AIs).
     */
    private Player red, blue;
    /**
     * Is red auto?.
     */
    private boolean blueISman = false;
    /**
     * Is blue man?.
     */
    private boolean redISauto = false;

    /**
     * helper to dump.
     */
    private void naivePrint() {
        System.out.println("===");
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 9 - 9),
                (_board.EXTENDED_SIDE * 9 - 3));
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 8 - 9),
                (_board.EXTENDED_SIDE * 8 - 3));
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 7 - 9),
                (_board.EXTENDED_SIDE * 7 - 3));
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 6 - 9),
                (_board.EXTENDED_SIDE * 6 - 3));
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 5 - 9),
                (_board.EXTENDED_SIDE * 5 - 3));
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 4 - 9),
                (_board.EXTENDED_SIDE * 4 - 3));
        System.out.print(" ");
        naiveprinthelper((_board.EXTENDED_SIDE * 3 - 9),
                (_board.EXTENDED_SIDE * 3 - 3));
        System.out.println("===");
    }
    /**
     * helper to dump.
     * @param i int.
     * @param k int.
     */
    private void naiveprinthelper(int i, int k) {
        for (; i <= k; i++) {
            if (i == k) {
                if (_board.getboard()[i] == PieceColor.BLOCKED) {
                    System.out.println("X");
                } else if (_board.getboard()[i] == PieceColor.EMPTY) {
                    System.out.println("-");
                } else if (_board.getboard()[i] == PieceColor.RED) {
                    System.out.println("r");
                } else if (_board.getboard()[i] == PieceColor.BLUE) {
                    System.out.println("b");
                }
            } else {
                if (_board.getboard()[i] == PieceColor.BLOCKED) {
                    System.out.print("X ");
                } else if (_board.getboard()[i] == PieceColor.EMPTY) {
                    System.out.print("- ");
                } else if (_board.getboard()[i] == PieceColor.RED) {
                    System.out.print("r ");
                } else if (_board.getboard()[i] == PieceColor.BLUE) {
                    System.out.print("b ");
                }
            }
        }
    }
    /** Player representing the current player.
     * @param board phony.
     * **/
    private void printResult(Board board) {
        if (board.numPieces(PieceColor.RED)
                >
                _board.numPieces(PieceColor.BLUE)) {
            reportWinner("Red wins.");
        } else if (board.numPieces(PieceColor.BLUE)
                >
                board.numPieces(PieceColor.RED)) {
            reportWinner("Blue wins.");
        } else {
            reportWinner("Draw.");
        }
    }
/** Player representing the current player. **/
    private Player currPlayer;
}
