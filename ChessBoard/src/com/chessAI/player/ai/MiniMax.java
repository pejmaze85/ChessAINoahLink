package com.chessAI.player.ai;

import com.chessAI.board.Board;
import com.chessAI.board.Move;
import com.chessAI.player.MoveTransition;

import java.util.concurrent.TimeUnit;

public class MiniMax implements MoveStrategy{

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;

    public MiniMax(final int searchDepth){
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
    }

    @Override public String toString(){
        return "MiniMax";
    }

    @Override
    public Move execute(Board board) {

        // IF IN DATABASE
        // SPECIAL CASE
        // TRANSPOSITION
        // PULL FROM MINIMAX

        return getBestMiniMaxMove(board);
    }

    public Move getBestMiniMaxMove(Board board){
        final long startTime = System.currentTimeMillis();

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        int numMoves = board.currentPlayer().getLegalMoves().size();

        System.out.println(board.currentPlayer().getAlliance().toString() + " thinking with depth " + this.searchDepth + " through " + numMoves + "moves.");

        for(final Move move : board.currentPlayer().getLegalMoves()){

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);

            if(moveTransition.getMoveStatus().isDone()){
                currentValue = board.currentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getTransistionBoard(), this.searchDepth - 1, Integer.MIN_VALUE,Integer.MAX_VALUE) :
                        max(moveTransition.getTransistionBoard(), this.searchDepth - 1, Integer.MIN_VALUE,Integer.MAX_VALUE);

                if(board.currentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue){
                    highestSeenValue = currentValue;
                    bestMove = move;

                }else if (board.currentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue){
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;

        System.out.println("Search Took " + TimeUnit.MILLISECONDS.toSeconds(executionTime) + " Seconds");

        return bestMove;
    }

    public int min(final Board board, final int depth, int alpha, int beta){
        if (depth == 0  || endGame(board)){
            return this.boardEvaluator.evaluate(board, depth);
        }

        int currentLowest = beta;

        for(final Move move : board.currentPlayer().getLegalMoves()){
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()){
                final Board toBoard = moveTransition.getTransistionBoard();
                final int currentValue = max(toBoard, depth - 1, alpha, currentLowest);
                currentLowest = Math.min(currentLowest, currentValue);
                if(currentLowest <= alpha){
                    return alpha;
                }
            }
        }

        return currentLowest;

    }


    public int max(final Board board, final int depth, int alpha, int beta){
        if (depth == 0 || endGame(board)){
            return this.boardEvaluator.evaluate(board, depth);
        }

        int currentHighest = alpha;

        for(final Move move : board.currentPlayer().getLegalMoves()){
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()){
                final Board toBoard = moveTransition.getTransistionBoard();
                final int currentValue = min(toBoard, depth - 1, currentHighest, beta);
                currentHighest = Math.max(currentHighest, currentValue);
                if (currentHighest >= beta){
                    return beta;
                }
            }
        }

        return currentHighest;
    }

    private boolean endGame(Board board) {

        return board.currentPlayer().isInCheckMate() || board.currentPlayer().isInStaleMate() ;
    }

}
