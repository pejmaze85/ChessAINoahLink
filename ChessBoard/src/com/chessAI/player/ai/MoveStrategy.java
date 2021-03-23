package com.chessAI.player.ai;

import com.chessAI.board.Board;
import com.chessAI.board.Move;

public interface MoveStrategy {

    Move execute(Board board);
}
