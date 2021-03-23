package com.chessAI.player.ai;

import com.chessAI.board.Board;

public interface BoardEvaluator {

    int evaluate(Board board, int depth);

}
