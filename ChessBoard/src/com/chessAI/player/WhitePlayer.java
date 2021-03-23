package com.chessAI.player;

import com.chessAI.Alliance;
import com.chessAI.board.Board;
import com.chessAI.board.BoardUtils;
import com.chessAI.board.Move;
import com.chessAI.board.Tile;
import com.chessAI.piece.Piece;
import com.chessAI.piece.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.chessAI.board.Move.*;

public class WhitePlayer extends Player {

    public WhitePlayer(Board board, Collection<Move> whiteStandardLegalMoves,
                       Collection<Move> blackStandardLegalMoves){

        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);

    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals,
                                                    final Collection<Move> opponentLegals) {

        if(!hasCastleOpportunities()) {
            return Collections.emptyList();
        }

        final List<Move> kingCastles = new ArrayList<>();

        if(this.playerKing.isFirstMove() && this.playerKing.getPiecePosition() == 60 && Player.calculateAttacksOnTile(60, opponentLegals).isEmpty()) {
            //whites king side castle
            if(this.board.getTile(61).getPiece() == null && this.board.getTile(62).getPiece() == null) {
                final Piece kingSideRook = this.board.getTile(63).getPiece();
                if(kingSideRook != null && kingSideRook.isFirstMove()) {
                    if(Player.calculateAttacksOnTile(61, opponentLegals).isEmpty() && Player.calculateAttacksOnTile(61, opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(62, opponentLegals).isEmpty() &&
                            kingSideRook.getPieceType() == Piece.PieceType.ROOK) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 52)) {
                            kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 62, (Rook) kingSideRook, kingSideRook.getPiecePosition(), 61));
                        }
                    }
                }
            }
            //whites queen side castle
            if(this.board.getTile(59).getPiece() == null && this.board.getTile(58).getPiece() == null &&
                    this.board.getTile(57).getPiece() == null) {
                final Piece queenSideRook = this.board.getTile(56).getPiece();
                if(queenSideRook != null && queenSideRook.isFirstMove()) {
                    if(Player.calculateAttacksOnTile(58, opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(59, opponentLegals).isEmpty() && queenSideRook.getPieceType() == Piece.PieceType.ROOK) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 52)) {
                            kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 58, (Rook) queenSideRook, queenSideRook.getPiecePosition(), 59));
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(kingCastles);
    }

}
