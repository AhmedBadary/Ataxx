package ataxx;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Ahmad Badary
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Move currMV = null;
        Command currmCMND = game().getMoveCmnd(myColor() + ": ");
        if (currmCMND.commandType().equals(Command.Type.PASS)) {
            currMV = new Move();
        } else {
            int col0 = ((currmCMND.operands()[0]).charAt(0) - 'a') + 2;
            int row0 = ((currmCMND.operands()[1]).charAt(0) - '1') + 2;
            int col1 = ((currmCMND.operands()[2]).charAt(0) - 'a') + 2;
            int row1 = ((currmCMND.operands()[3]).charAt(0) - '1') + 2;
            currMV = new Move(col0, row0, col1, row1);

        }
        return currMV;
    }
}

