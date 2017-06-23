 @Override
   Move myMove() {
       if (!board().canMove(myColor())) {
           return Move.pass();
       }
       Move move = findMove();
       return move;
   }


   /** Return a move for me from the current position, assuming there
    *  is a move. */
   private Move findMove() {
       //Board b = new Board(game().board());
       Board b = new Board(board());
       if (myColor() == RED) {
           findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
       } else {
           findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
       }
       return _lastFoundMove;
   }


   /** Return a move for me from the current position, assuming there
    *  is a move. */
   private int findMove(Board board, int depth, boolean saveMove, int sense,
                        int alpha, int beta) {
       player = board.whoseMove();
       _lastFoundMove = null;
       if (depth == 0) {
           return staticScore(board);
       }
       for (int c = 0; c < board().SIDE; c++) {
           for (int r = 0; r < board().SIDE; r++) {
               int index = board.index((char) ('a' + c), (char) ('1' + r));
               if (board.get(index).equals(myColor())) {
                   for (int dc = -2; dc < 3; dc++) {
                       for (int dr = -2; dr < 3; dr++) {
                           /** if (board.get(Board.neighbor(index, dc, dr)).equals(EMPTY)) {
                               Move move = Move.move((char) ('a' + c), (char) ('1' + r), (char) ('a' + c + dc), (char) ('1' + r + dr));
                               if (board.legalMove(move)) {
                                   _lastFoundMove = move;
                               } else {
                                   break;
                               }
                           } */




                           if (alpha < beta) {
                               Move move = Move.move((char) ('a' + c), (char) ('1' + r), (char) ('a' + c + dc), (char) ('1' + r + dr));
                               if (board.legalMove(move)) {
                                   board.makeMove(move);
                                   if ((board.gameOver() && staticScore(board) >= 0) || board.numPieces(player.opposite()) < 1) {
                                       if (saveMove) {
                                           _lastFoundMove = move;
                                       }
                                       board.undo();//
                                       if (sense > 0) {
                                           return WINNING_VALUE;
                                       } else {
                                           return -WINNING_VALUE;
                                       }
                                   } else if (board.gameOver() && staticScore(board) < 0 || board.numPieces(player) < 1) {
                                       if (saveMove) {
                                           if (_lastFoundMove == null) {
                                               _lastFoundMove = move;
                                           }
                                       }
                                       board.undo();


                                       break;
                                   }


                                   int temp = findMove(board, depth - 1, false, sense * (- 1), alpha, beta);
                                   if (sense > 0) {
                                       if (alpha < temp) {
                                           alpha = temp;
                                           if (saveMove ) {
                                               _lastFoundMove = move;//
                                           }
                                           if (alpha >= beta) {
                                               board.undo();
                                               return alpha;
                                           }
                                       }


                                   } else {
                                       if (beta > temp) {
                                           beta = temp;
                                           if (saveMove) {
                                               _lastFoundMove = move;//


                                               
                                           }
                                           if (alpha >= beta) {
                                               board.undo();
                                               return beta;
                                           }
                                       }


                                   }


                                   board.undo();
                                   if (alpha == WINNING_VALUE) {
                                       return alpha;
                                   }
                                   if (beta == -WINNING_VALUE) {
                                       return beta;
                                   }
                                   if (_lastFoundMove == null) {
                                       _lastFoundMove = move;
                                   }
                               }
                           }
                          




                       }
                   }
               }
           }
       }
       return 0;
   }