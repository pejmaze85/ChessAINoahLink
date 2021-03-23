package com.chessAI.board;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class GameHistory {
    public List<Board> gameHistory = new ArrayList<>();

    public void addToHistory(Board newBoard){
        gameHistory.add(newBoard);
    }

    public List<Board> getGameHistory(){
        return gameHistory;
    }

}
