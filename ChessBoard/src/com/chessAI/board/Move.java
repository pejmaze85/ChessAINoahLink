package com.chessAI.board;

import com.chessAI.board.Board.Builder;
import com.chessAI.gui.Table;
import com.chessAI.piece.Piece;
import com.chessAI.piece.*;

import static com.chessAI.board.BoardUtils.*;

public abstract class Move {

        final Board board;
        final Piece movedPiece;
        final int destinationCoordinate;
        protected final boolean isFirstMove;

        public static final Move NULL_MOVE = new NullMove();

        Move(final Board board, final Piece piece, final int destinationCoordinate){
            this.board = board;
            this.movedPiece = piece;
            this.destinationCoordinate = destinationCoordinate;
            this.isFirstMove = movedPiece.isFirstMove();

        }

        private Move(final Board board, final int destinationCoordinate){
            this.board = board;
            this.destinationCoordinate = destinationCoordinate;
            this.movedPiece = null;
            this.isFirstMove = false;
        }

        @Override
        public int hashCode(){
            final int prime = 31;
            int result = 1;
            result = prime * result + this.destinationCoordinate;
            result = prime * result + this.movedPiece.hashCode();
            result = prime * result + this.movedPiece.getPiecePosition();

            return result;
        }

        @Override
        public boolean equals(final Object other){
            if (this == other){
                return true;
            }
            if (!(other instanceof Move)){
                return false;
            }
            final Move otherMove = (Move) other;
            return  getCurrentCoordinate() == otherMove.getCurrentCoordinate() &&
                    getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
                    getMovePiece().equals(otherMove.getMovePiece());
        }

        public int getCurrentCoordinate(){
            return this.movedPiece.getPiecePosition();
        }

        public Board getBoard(){
            return this.board;
        }


        public int getDestinationCoordinate(){
            return this.destinationCoordinate;
        }

        public Piece getMovePiece(){
            return this.movedPiece;
        }

        public boolean isAttack(){
            return false;
        }

        public boolean isCastlingMove(){
            return false;
        }

        public Piece getAttackedPiece(){
            return null;
        }


    public Board execute(){
        final Builder builder = new Builder();

        for(final Piece piece : this.board.currentPlayer().getActivePieces()){
            if(!this.movedPiece.equals(piece)){
                builder.setPiece(piece);
            }
        }
        for(final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
            builder.setPiece((piece));
        }
        // MOVE THE MOVED PIECE
        builder.setPiece(this.movedPiece.movePiece(this));
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
        return builder.build();
    }

    public static class MajorAttackMove extends AttackMove{

            public MajorAttackMove(final Board board, final Piece pieceMoved, final int destinationCoordinate, final Piece pieceAttacked){
                super(board, pieceMoved, destinationCoordinate, pieceAttacked);
            }

        @Override
        public boolean equals(final Object object){
                return this == object || object instanceof MajorAttackMove && super.equals(object);
        }

        @Override
        public String toString(){
                return movedPiece.getPieceType() + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static final class MajorMove extends Move{
            public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinate){
                super(board, movedPiece, destinationCoordinate);
            }

            @Override
            public boolean equals(final Object other){
                return this == other || other instanceof MajorMove && super.equals(other);
            }

            @Override
            public String toString(){
                return movedPiece.getPieceType().toString() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
            }

    }

    public static class AttackMove extends Move{

        final Piece attackedPiece;

        public AttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                          final Piece attackedPiece){
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
            }

            @Override
            public int hashCode(){
            return this.attackedPiece.hashCode() + super.hashCode();
            }

        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AttackMove)) {
                return false;
            }
            final AttackMove otherAttackMove = (AttackMove) other;
            return super.equals(otherAttackMove) && getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public String toString(){
            return movedPiece.getPieceType() + "" + BoardUtils.getPositionAtCoordinate(movedPiece.getPiecePosition())
                    + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }

    public static final class PawnMove extends Move{
        public PawnMove(final Board board, final Piece movedPiece, final int destinationCoordinate){
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof PawnMove && super.equals(other);
        }

        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }

    public static class PawnAttackMove extends AttackMove{
        public PawnAttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece){
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof PawnAttackMove && super.equals(other);
        }

        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.movedPiece.getPiecePosition()).substring(0,1)
                    + "x" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }


    }

    public static final class PawnEnPassantAttack extends PawnAttackMove{
        public PawnEnPassantAttack(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece){
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object other){
            return this == other || other instanceof PawnEnPassantAttack && super.equals(other);
        }

        @Override
        public Board execute(){
            final Builder builder = new Builder();
            for(final Piece piece : this.board.currentPlayer().getActivePieces()){
                if(!this.movedPiece.equals(piece)){
                    builder.setPiece(piece);
                }
            }

            for (final Piece piece :this.board.currentPlayer().getOpponent().getActivePieces()){
                if(!piece.equals(this.getAttackedPiece())){
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }

    public static class PawnPromotion extends Move{
            final Move decoratedMoved;
            final Pawn promotedPawn;

            public PawnPromotion(final Move decoratedMove){
                super(decoratedMove.getBoard(), decoratedMove.getMovePiece(), decoratedMove.getDestinationCoordinate());
                this.decoratedMoved = decoratedMove;
                this.promotedPawn = (Pawn) decoratedMove.getMovePiece();
            }

            @Override
            public Board execute(){
                final Board pawnMovedBoard = this.decoratedMoved.execute();
                final Board.Builder builder = new Builder();
                for (final Piece piece : pawnMovedBoard.currentPlayer().getActivePieces()){
                    if (!this.promotedPawn.equals(piece)){
                        builder.setPiece(piece);
                    }
                }
                for(final Piece piece : pawnMovedBoard.currentPlayer().getOpponent().getActivePieces()){
                    builder.setPiece(piece);
                }
                builder.setPiece(this.promotedPawn.getPromotionPiece().movePiece(this));
                builder.setMoveMaker(pawnMovedBoard.currentPlayer().getAlliance());
                return builder.build();
            }
            @Override
            public boolean isAttack(){
                return this.decoratedMoved.isAttack();
            }
            @Override
            public Piece getAttackedPiece(){
                return this.decoratedMoved.getAttackedPiece();
            }
            @Override
            public String toString(){
                return "";
            }
            @Override
            public int hashCode(){
                return decoratedMoved.hashCode() + (31*promotedPawn.hashCode());
            }
            @Override
            public boolean equals (final Object other){
                return this == other || other instanceof PawnPromotion && (super.equals(other));
            }

    }

    public static final class PawnJump extends Move {
        public PawnJump(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute(){
            final Builder builder = new Builder();
            for(final Piece piece : this.board.currentPlayer().getActivePieces()){
                if(!this.movedPiece.equals(piece)){
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }
            final Pawn movedPawn = (Pawn)this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }

    public static abstract class CastleMove extends Move{

            protected final Rook castleRook;
            protected final int castleRookStartPosition;
            protected final int castleRookDestination;

        public CastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                          final Rook castleRook, final int castleRookStartPosition, final int castleRookDestination){
            super(board, movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStartPosition = castleRookStartPosition;
            this.castleRookDestination = castleRookDestination;
        }

        public Rook getCastleRook(){
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove(){
            return true;
        }

        @Override
        public Board execute(){

            final Builder builder = new Builder();

            for(final Piece piece : this.board.currentPlayer().getActivePieces()){
                if(!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)){
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setPiece(new Rook(this.castleRook.getPieceAlliance(), this.castleRookDestination));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public int hashCode(){
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.castleRook.hashCode();
            result = prime * result + this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CastleMove)) {
                return false;
            }
            final CastleMove otherCastleMove = (CastleMove) other;
            return super.equals(otherCastleMove) && this.castleRook.equals(otherCastleMove.getCastleRook());
        }

    }

    public static final class KingSideCastleMove extends CastleMove{
        public KingSideCastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                                  final Rook castleRook, final int castleRookStartPosition, final int castleRookDestination){
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStartPosition, castleRookDestination);
        }

        @Override
        public String toString(){
            return "O-O";
        }

        @Override
        public boolean equals (final Object other){
            return this == other || this instanceof KingSideCastleMove && super.equals(other);
        }

    }

    public static final class QueenSideCastleMove extends CastleMove{
        public QueenSideCastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                                   final Rook castleRook, final int castleRookStartPosition, final int castleRookDestination){
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStartPosition, castleRookDestination);
        }
        @Override
        public String toString(){
            return "O-O-O";
        }

        @Override
        public boolean equals(Object other){
            return this == other || other instanceof QueenSideCastleMove && super.equals(other);
        }

    }

    public static final class NullMove extends Move{
        public NullMove(){
            super(null, -1);
        }

        @Override
        public Board execute(){
            throw new RuntimeException("Cannot Execute Null Move");
        }

        @Override
        public int getCurrentCoordinate(){
            return -1;
        }

    }

    public static class MoveFactory {

            private MoveFactory(){
                throw new RuntimeException("Not Possible");
            }
            public static Move createMove(final Board board, final int currentCoordinate, final int destinationCoordinate){
                for(final Move move : board.getAllLegalMoves()){
                    if(move.getCurrentCoordinate() == currentCoordinate && move.getDestinationCoordinate() == destinationCoordinate){
                        return move;
                    }
                }
                return NULL_MOVE;
            }
    }
}
