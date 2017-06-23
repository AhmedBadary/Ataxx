# Ataxx
Ataxx is a two-person game played with red and blue pieces on a 7-by-7 board. 

## Running Your Program
Run the program with the command:
```
java -ea ataxx.Main
```

## Game States

At any given time, your program can be in one of several states:

* **set-up state**, meaning that no game is in progress is moving, and pieces may be placed on the board to set up a position (this is the initial state);
* **playing state**, where players are entering moves and the game is not yet over; or
* **finished state**, where one or the other player has won and no more moves are possible.

## Commands

* **clear** Abandons the current game (if one is in progress), clears the board to its initial configuration, and places the program in the set-up state. Abandoning a game implies that you resign. This command is valid in any state.
* **start** Valid only in set-up state. Enters playing state. Red and Blue alternate moves. If there have been moves made during the set-up state, then play picks up at the point where these moves leave off (so, for example, if there was one set-up move made before start, then Blue will move first). In the (unusual) case where the set-up moves have already won the game, start puts the program in finished state.
* **quit** Abandons any current game (as for clear) and exits the program. Valid in any state. The end of input has the same effect.

The following commands are valid only in set-up mode. They set various game parameters prior to the start of play.

* **auto C** Sets up the program so that player C (Red or Blue) is an AI. Initially, and after a clear command, Red is a manual player and Blue is an AI. Thus, the command auto Red causes both Red and Blue to be AIs, so that the start command causes the machine to play a game against itself.
* **manual C** Sets up the program so that player C (Red or Blue) is a manual player. Thus, the command manual Blue causes both Red and Blue to be manual players (who presumably alternate entering moves on a terminal).
* **block CR** Sets a block at square CR and all reflections of that square around the middle row and middle column, as described in Blocks. It is an error to place a block (or one of its reflections) on a previously set up piece. Blocks may not be placed after a piece move, until the board is again cleared. In case of any errors, the command has no effect.
* **seed N** If your program's AIs use pseudo-random numbers to choose moves, this command sets the random seed to N (a long integer). This command has no effect if there is no random component to your automated players (or if you don't use them in a particular game). It doesn't matter exactly how you use N as long as your automated player(s) behave(s) identically in response to any given sequence of moves. In the absence of a seed command, do what you want to seed your generator.

The following commands are valid in any state.

* **help** Print a brief summary of the commands.
dump This command is especially for testing and debugging. It prints the board out in exactly the following format:
```
===
  r - - - - - b
  - - - - - - -
  - - X - X - -
  - - - - - - -
  - - X - X - -
  - - - - - - -
  b - - - - - r
===
```
Here, - indicates an empty square, r indicates a red square, b indicates a blue square, and X indicates a block. Don't use === markers anywhere else in your output. This gives the autograder a way to determine the state of your game board at any point. It does not change any of the state of the program.

* **load** file Reads the given file and in effect substitutes its contents for the load command itself.
