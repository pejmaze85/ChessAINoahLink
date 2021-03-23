package com.chessAI.piece;

import com.chessAI.Alliance;
import com.chessAI.board.Board;
import com.chessAI.board.BoardUtils;
import com.chessAI.board.Move;
import com.chessAI.board.Move.MajorAttackMove;
import com.chessAI.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Bishop extends Piece{

    private final int[] CAN_MOVE_VECTOR_COORD = {-9, -7, 7, 9};


    public Bishop(Alliance pieceAlliance, int piecePosition) {

        super(PieceType.BISHOP, piecePosition, pieceAlliance, true);
    }

    public Bishop(Alliance pieceAlliance, int piecePosition, final boolean isFirstMove) {

        super(PieceType.BISHOP, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {

        final List<Move> legalMoves = new ArrayList<>();

        for(final int candidateCoordinateOffset: CAN_MOVE_VECTOR_COORD){

            int candidateDestinationCoordinate = this.piecePosition;

            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){

                if (isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
                        isEighthColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset)){
                        break;
                }
                candidateDestinationCoordinate += candidateCoordinateOffset;

                if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
                    if(!candidateDestinationTile.isTileOccupied()){
                        legalMoves.add(new Move.MajorMove(board, this, candidateDestinationCoordinate));
                    }else{
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();
                        if (this.pieceAlliance != pieceAlliance){
                            legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                        }
                        break;
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Bishop movePiece(final Move move) {
        return new Bishop(move.getMovePiece().getPieceAlliance(), move.getDestinationCoordinate());
    }

    @Override
    public String toString(){
        return PieceType.BISHOP.toString();
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset){
            return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -9 || candidateOffset == 7);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset){
            return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == -7 || candidateOffset == 9);
        }

}
