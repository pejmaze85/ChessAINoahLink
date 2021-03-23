package com.chessAI.gui;

import com.chessAI.Alliance;
import com.chessAI.player.Player;
import com.chessAI.gui.Table.PlayerType;

import javax.swing.*;

class GameSetup extends JDialog {

    private PlayerType whitePlayerType;
    private PlayerType blackPlayerType;


    GameSetup() {


                whitePlayerType =  PlayerType.HUMAN;
                blackPlayerType =  PlayerType.COMPUTER;

        }


    boolean isAIPlayer(final Player player) {
        if(player.getAlliance() == Alliance.WHITE) {
            return getWhitePlayerType() == PlayerType.COMPUTER;
        }
        return getBlackPlayerType() == PlayerType.COMPUTER;
    }

    PlayerType getWhitePlayerType() {
        return this.whitePlayerType;
    }

    PlayerType getBlackPlayerType() {
        return this.blackPlayerType;
    }

}