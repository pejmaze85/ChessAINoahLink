package com.chessAI.player;

import com.chessAI.board.Move;

public enum MoveStatus {
    DONE {
        @Override
        public boolean isDone() {
            return true;
        }
    },
    ILLEGAL_MOVE {
        @Override
        public boolean isDone() {
            System.out.println("NOT LEGAL");
            return false;
        }
        },
    LEAVES_PLAYER_IN_CHECK{
        @Override
        public boolean isDone(){
            return false;
        }
    };


    public abstract boolean isDone();
}
