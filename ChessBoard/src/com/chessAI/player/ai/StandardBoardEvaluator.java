package com.chessAI.player.ai;

import com.chessAI.board.Board;
import com.chessAI.board.BoardUtils;
import com.chessAI.gui.Table;
import com.chessAI.piece.Piece;
import com.chessAI.player.Player;
import com.chessAI.player.ai.PositionBonus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.chessAI.board.Board.*;

public final class StandardBoardEvaluator implements BoardEvaluator {


    private static final int CHECK_BONUS = 5;
    private static final int MOBILITY_BONUS = 1;
    private static final int CHECKMATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 100;
    private static final int CASTLE_BONUS = 60;
    public enum GAMESTATE{OPENING, MIDGAME, ENDGAME, UNSURE;}


    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board, board.whitePlayer(), depth) - scorePlayer(board, board.blackPlayer(), depth);
    }

    public GAMESTATE getGameState(final Board board){

        if(board.getAllPieces().size() < 14) {
            return GAMESTATE.ENDGAME;
        }else if((board.getAllPieces().size() >= 14 && board.getAllPieces().size() <= 22) || Table.get().moveCount > 15){
            return GAMESTATE.MIDGAME;
        }else if((board.getAllPieces().size() > 26 ) || Table.get().moveCount < 15){
            return GAMESTATE.OPENING;
        }

        return GAMESTATE.UNSURE;

    }

    private int scorePlayer(final Board board, final Player player, final int depth) {
        int moveCount = Table.get().moveCount;
        int score =
                pieceValue(player) + mobility(player)
                + check(player)
                + checkmate(player, depth)
                + castled(player)
                + pawnPromoteBonus(player)
                + twoBishopBonus(player);

                if(getGameState(board) == GAMESTATE.OPENING) {
                    score += TileBonus(player);
                }

                return score;
    }

    private int TileBonus(Player player) {
        int bonus = 0;
        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.PAWN && player.getAlliance().isWhite()){
                bonus += PositionBonus.pawnPosBonus[piece.getPiecePosition()];
            } else if(piece.getPieceType() == Piece.PieceType.PAWN && player.getAlliance().isBlack()){
                bonus += PositionBonus.pawnPosBonus[63 - piece.getPiecePosition()];
            }
        }
        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.QUEEN && player.getAlliance().isWhite()){
                bonus += PositionBonus.queenPosBonus[piece.getPiecePosition()];
            } else if(piece.getPieceType() == Piece.PieceType.QUEEN && player.getAlliance().isBlack()){
                bonus += PositionBonus.queenPosBonus[63 - piece.getPiecePosition()];
            }
        }
        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.KNIGHT && player.getAlliance().isWhite()){
                bonus += PositionBonus.knightPosBonus[piece.getPiecePosition()] - 10;
            } else if(piece.getPieceType() == Piece.PieceType.KNIGHT && player.getAlliance().isBlack()){
                bonus += PositionBonus.knightPosBonus[63 - piece.getPiecePosition()] - 10;
            }
        }
        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.BISHOP && player.getAlliance().isWhite()){
                bonus += PositionBonus.bishopPosBonus[piece.getPiecePosition()];
            } else if(piece.getPieceType() == Piece.PieceType.BISHOP && player.getAlliance().isBlack()){
                bonus += PositionBonus.bishopPosBonus[63 - piece.getPiecePosition()];
            }
        }

        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.ROOK && player.getAlliance().isWhite()){
                bonus += PositionBonus.rookPOSBonus[piece.getPiecePosition()];
            } else if(piece.getPieceType() == Piece.PieceType.ROOK && player.getAlliance().isBlack()){
                bonus += PositionBonus.rookPOSBonus[63 - piece.getPiecePosition()];
            }
        }
        return bonus;
    }

    private int twoBishopBonus(Player player){
        int bishopCount = 0;
        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.BISHOP){
                bishopCount ++;
            }
        }
        if(bishopCount == 2){
            return 50;
        }else{
            return 0;
        }
    }

    private int pawnPromoteBonus(Player player) {
        int bonus = 0;
        for(Piece piece : player.getActivePieces()){
            if(piece.getPieceType() == Piece.PieceType.PAWN){
                if((BoardUtils.EIGHTH_RANK[piece.getPiecePosition()] || BoardUtils.FIRST_RANK[piece.getPiecePosition()]) && (player.calculateAttacksOnTile(piece.getPiecePosition(), player.getOpponent().getLegalMoves()).isEmpty())){
                    bonus = bonus + 300;
                }
            }
        }
        return bonus;
    }

    private static int castled(Player player) {
        return player.isCastled() ? CASTLE_BONUS : 0;
    }

    private static int checkmate(Player player, int depth) {
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS * depthBonus(depth) :  0;
    }

    private static int depthBonus(int depth){
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    private static int check(Player player) {
        return player.getOpponent().isInCheck() ?  CHECK_BONUS :  0;
    }

    private static int mobility(Player player) {
        return (player.getLegalMoves().size()) * MOBILITY_BONUS;
    }

    private static int pieceValue(final Player player) {
        int pieceValueScore = 0;
        for(final Piece piece : player.getActivePieces()){
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }

}
