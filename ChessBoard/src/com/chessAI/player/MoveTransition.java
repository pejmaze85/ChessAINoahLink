package com.chessAI.player;

import com.chessAI.board.Board;
import com.chessAI.board.Move;

public class MoveTransition {

    private Board transistionBoard;
    private final Move move;
    private final MoveStatus moveStatus;

    public MoveTransition(final Board transistionBoard, final Move move, final MoveStatus moveStatus){
        this.transistionBoard = transistionBoard;
        this.move = move;
        this.moveStatus = moveStatus;
    }

    public MoveStatus getMoveStatus(){
        return this.moveStatus;
    }

    public Board getTransistionBoard() {
        return this.transistionBoard;
    }
}
