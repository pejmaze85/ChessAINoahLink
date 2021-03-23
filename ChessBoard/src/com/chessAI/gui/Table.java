package com.chessAI.gui;

import sun.audio.*;
import com.chessAI.board.*;
import com.chessAI.piece.Piece;
import com.chessAI.player.MoveTransition;
import com.chessAI.player.ai.MiniMax;
import com.chessAI.player.ai.MoveStrategy;
import com.google.common.collect.Lists;
import javafx.scene.control.Tab;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static javax.imageio.ImageIO.read;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;
import static javax.swing.text.StyleConstants.setBackground;

public class Table extends Observable {

    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    public Move lastMove;

    private Board chessBoard;

    private Tile sourceTile;
    private Tile destinationTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private boolean highlightLegalMoves = true;
    private Move computerMove;
    private boolean holdingAPiece = false;
    public int moveCount;

    private static final String defaultPieceImagesPath = "art/";

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(680,600);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400,350);
    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(15,15);
    private static final Color lightTileColor = Color.white;
    private static final Color darkTileColor = Color.darkGray;
    private static final Color lightHighlightedColor = Color.decode("#bbffa3");
    private static final Color darkHighlightedColor = Color.decode("#7fb96a");
    public GameHistory gameHistory = new GameHistory();

    private static final Table INSTANCE = new Table();


    private Table(){
        this.chessBoard = Board.createStandardBoard();
        JFrame gameFrame = new JFrame("PJChess");
        final JMenuBar tableMenuBar = createMenuBar();
        gameFrame.setJMenuBar(tableMenuBar);
        gameFrame.setSize(OUTER_FRAME_DIMENSION);
        gameFrame.setLayout(new BorderLayout());
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.gameSetup = new GameSetup();
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAIWatcher());
        this.boardDirection = BoardDirection.NORMAL;
        gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        gameFrame.setVisible(true);
        this.gameHistory.addToHistory(chessBoard);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static Table get(){
        return INSTANCE;
    }


    public void show(){
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
    }

    private JMenuBar createMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("OPEN UP DAT PGN");
            }
        });
         fileMenu.add(openPGN);

         final JMenuItem exitMenuItem = new JMenuItem("Exit");
         exitMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         fileMenu.add(exitMenuItem);
         return fileMenu;
    }

    private JMenu createPreferencesMenu(){
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);

        preferencesMenu.addSeparator();
        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight Legals", true);
        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
            }
        });
        //preferencesMenu.add(legalMoveHighlighterCheckbox);

        return preferencesMenu;
    }

    private GameSetup getGameSetup(){
        return this.gameSetup;
    }

    private Board getGameBoard(){
        return this.chessBoard;
    }

    private void setupUpdate(final GameSetup gameSetup){
        setChanged();
        notifyObservers(gameSetup);


    }

    private static class TableGameAIWatcher implements Observer {

        @Override
        public void update(final Observable o, final Object arg) {
            if(Table.get().gameSetup.isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
            !Table.get().getGameBoard().currentPlayer().isInCheckMate() && !Table.get().getGameBoard().currentPlayer().isInStaleMate()){

                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if(Table.get().getGameBoard().currentPlayer().isInCheckMate()){
                System.out.println("CHECKMATE - " + Table.get().getGameBoard().currentPlayer().getOpponent() + " WINS");
            }

            if(Table.get().getGameBoard().currentPlayer().isInStaleMate()){
                System.out.println("CHECKMATE - " + Table.get().getGameBoard().currentPlayer() + " IS IN STALEMATE");
            }
        }
    }

    private static class AIThinkTank extends SwingWorker<Move,String>{

        private AIThinkTank(){

        }

        @Override
        protected Move doInBackground() throws Exception{

            final MoveStrategy miniMax = new MiniMax(4);
            final Move bestMove = miniMax.execute(Table.get().getGameBoard());
            return bestMove;
        }

        @Override
        public void done(){

            try {
                final Move bestmove = get();
                Table.get().lastMove = bestmove;

                Table.get().updateComputerMove(bestmove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestmove).getTransistionBoard());
                Table.get().getMoveLog().addMove(bestmove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);


            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


        }
    }

    private void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    private MoveLog getMoveLog(){
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel(){
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel(){
        return this.takenPiecesPanel;
    }

    private BoardPanel getBoardPanel(){
        return this.boardPanel;
    }

    private void moveMadeUpdate(final PlayerType playerType){
        setChanged();
        notifyObservers(playerType);
        moveCount ++;
    }

    private void updateGameBoard(final Board transistionBoard) {
        this.chessBoard = transistionBoard;

    }

    public enum BoardDirection{

        NORMAL{
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles){
                return boardTiles;
            }
            @Override
            BoardDirection opposite(){
                return FLIPPED;
            }
        },
        FLIPPED{
            @Override
            List<TilePanel> traverse (final List<TilePanel> boardTiles){
                return Lists.reverse(boardTiles);
            }
            @Override
            BoardDirection opposite(){
                return NORMAL;
            }
        };
        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;

        BoardPanel(){
            super(new GridLayout(8,8));
            this.boardTiles = new ArrayList<>();

            for(int i = 0; i < BoardUtils.NUM_TILES; i++){
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();

        }

        public void drawBoard(Board board) {
            removeAll();
            for(final TilePanel tilePanel : boardDirection.traverse(boardTiles)){
                tilePanel.drawTile(board);
                add(tilePanel);
            }
           /*
           FOR ADDING MOVE SOUNDS LATER -> Regular Move & Attack Move & Mate
           if(Table.get().lastMove != null) {
                if (Table.get().lastMove.isAttack()) {
                    System.out.println("ATTACK");
                } else {
                    System.out.println("NORMAL");
                }
            }
            */

            validate();
            repaint();
        }
    }

    public static class MoveLog {

        private final List<Move> moves;

        MoveLog(){
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves(){
            return moves;
        }

        public void addMove(final Move move){
            this.moves.add(move);
        }

        public int size(){
            return this.moves.size();
        }

        public void clear(){
            this.moves.clear();
        }

        public boolean removeMove(final Move move){
            return this.moves.remove(move);
        }

        public Move removeMove(int index){
            return this.moves.remove(index);
        }

    }

    public enum PlayerType{
        HUMAN,
        COMPUTER
    }



    private class TilePanel extends JPanel{

        private final int tileId;
        private Board board;

        TilePanel(final BoardPanel boardPanel, final int tileId){
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                   if(isRightMouseButton(e)){
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;

                        }else if (isLeftMouseButton(e)) {
                       sourceTile = null;
                       destinationTile = null;
                       humanMovedPiece = null;
                   }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    holdingAPiece = false;
                    if (isLeftMouseButton(e)) {
                        if (sourceTile == null && (chessBoard.getTile(tileId).isTileOccupied())) {
                            if ((chessBoard.getTile(tileId).getPiece().getPieceAlliance()
                                    == chessBoard.currentPlayer().getAlliance())) {

                                holdingAPiece = true;
                                sourceTile = chessBoard.getTile(tileId);
                                humanMovedPiece = sourceTile.getPiece();
                                try {
                                    Toolkit t1 = Toolkit.getDefaultToolkit();
                                    BufferedImage image =
                                            read(new File(defaultPieceImagesPath +
                                                    chessBoard.getTile(tileId).getPiece().getPieceAlliance().toString().charAt(0)
                                                    + chessBoard.getTile(tileId).getPiece().toString() + ".png"));
                                    Point point = new Point(0, 0);
                                    Cursor cursor = t1.createCustomCursor(image, point, "Cursor");

                                    setCursor(cursor);
                                } catch (Exception ignored) {
                                }
                                if (humanMovedPiece == null) {
                                    sourceTile = null;
                                    holdingAPiece = false;
                                }
                            } else {
                                sourceTile = null;
                                destinationTile = null;
                                humanMovedPiece = null;
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            boardPanel.drawBoard(chessBoard);
                        }
                    });

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    TilePanel.super.setCursor(c);

                    if(holdingAPiece && sourceTile != null && destinationTile != null) {
                        final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(),
                                destinationTile.getTileCoordinate());
                        final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                        if (transition.getMoveStatus().isDone()){
                            chessBoard = transition.getTransistionBoard();
                            moveLog.addMove(move);

                        }
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.redo(chessBoard,moveLog);
                                takenPiecesPanel.redo(moveLog);

                                if(gameSetup.isAIPlayer(chessBoard.currentPlayer())){
                                    Table.get().moveMadeUpdate(PlayerType.HUMAN);
                                }
                                lastMove = move;
                                boardPanel.drawBoard(chessBoard);


                            }
                        });
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                        if(holdingAPiece){
                            destinationTile = chessBoard.getTile(tileId);
                        }
                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });


            validate();
        }

        public void drawTile(Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightLegals(chessBoard);
            highlightLastMove(chessBoard);
            validate();
            repaint();

        }

        private void assignTilePieceIcon(Board board){

            this.removeAll();
            if(board.getTile(this.tileId).isTileOccupied()){
                try {
                    final BufferedImage image =
                             read(new File(defaultPieceImagesPath +
                                    board.getTile(this.tileId).getPiece().getPieceAlliance().toString().charAt(0)
                                    + board.getTile(this.tileId).getPiece().toString() + ".png"));

                            add(new JLabel(new ImageIcon(image)));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        private void highlightLastMove(final Board board) {
            if (lastMove != null) {
                if(tileId == lastMove.getDestinationCoordinate()) {
                        if (BoardUtils.EIGHTH_RANK[lastMove.getDestinationCoordinate()] ||
                                BoardUtils.SIXTH_RANK[lastMove.getDestinationCoordinate()] ||
                                BoardUtils.FOURTH_RANK[lastMove.getDestinationCoordinate()] ||
                                BoardUtils.SECOND_RANK[lastMove.getDestinationCoordinate()]) {
                            setBackground(this.tileId % 2 == 0 ? lightHighlightedColor : darkHighlightedColor);
                        } else if (BoardUtils.SEVENTH_RANK[this.tileId] ||
                                BoardUtils.FIFTH_RANK[lastMove.getDestinationCoordinate()] ||
                                BoardUtils.THIRD_RANK[lastMove.getDestinationCoordinate()] ||
                                BoardUtils.FIRST_RANK[lastMove.getDestinationCoordinate()]) {
                            setBackground(this.tileId % 2 != 0 ? lightHighlightedColor : darkHighlightedColor);
                        }
                }
                if(tileId == lastMove.getCurrentCoordinate()) {
                    if (BoardUtils.EIGHTH_RANK[lastMove.getCurrentCoordinate()] ||
                            BoardUtils.SIXTH_RANK[lastMove.getCurrentCoordinate()] ||
                            BoardUtils.FOURTH_RANK[lastMove.getCurrentCoordinate()] ||
                            BoardUtils.SECOND_RANK[lastMove.getCurrentCoordinate()]) {
                        setBackground(this.tileId % 2 == 0 ? lightHighlightedColor : darkHighlightedColor);
                    } else if (BoardUtils.SEVENTH_RANK[this.tileId] ||
                            BoardUtils.FIFTH_RANK[lastMove.getCurrentCoordinate()] ||
                            BoardUtils.THIRD_RANK[lastMove.getCurrentCoordinate()] ||
                            BoardUtils.FIRST_RANK[lastMove.getCurrentCoordinate()]) {
                        setBackground(this.tileId % 2 != 0 ? lightHighlightedColor : darkHighlightedColor);
                    }
                }
            }
        }


        private void highlightLegals(final Board board) {
            if (highlightLegalMoves) {
                for (final Move move : pieceLegalMoves(board)) {
                    if (move.getDestinationCoordinate() == this.tileId) {
                        if(BoardUtils.EIGHTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.SIXTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.FOURTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.SECOND_RANK[move.getDestinationCoordinate()]){
                            setBackground(this.tileId % 2 == 0 ? lightHighlightedColor : darkHighlightedColor);
                        }else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
                                BoardUtils.FIFTH_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.THIRD_RANK[move.getDestinationCoordinate()] ||
                                BoardUtils.FIRST_RANK[move.getDestinationCoordinate()]){
                            setBackground(this.tileId % 2 != 0 ? lightHighlightedColor : darkHighlightedColor);
                        }
                    }
                }
            }
            if(board.currentPlayer().isInCheck()){
                if(board.currentPlayer().getPlayerKing().getPiecePosition() == this.tileId){
                    setBackground(Color.decode("#f08686"));
                }
            }
            if(board.currentPlayer().isInCheckMate()){
                if(board.currentPlayer().getPlayerKing().getPiecePosition() == this.tileId){
                    setBackground(Color.decode("#c82c2c"));
                }
            }
        }

            private Collection<Move> pieceLegalMoves(final Board board){
                if(humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()){
                    return humanMovedPiece.calculateLegalMoves(board);
                }
                return Collections.emptyList();
            }




        private void assignTileColor() {
            if(BoardUtils.EIGHTH_RANK[this.tileId] ||
                    BoardUtils.SIXTH_RANK[this.tileId] ||
                    BoardUtils.FOURTH_RANK[this.tileId] ||
                    BoardUtils.SECOND_RANK[this.tileId]){
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            }else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
                    BoardUtils.FIFTH_RANK[this.tileId] ||
                    BoardUtils.THIRD_RANK[this.tileId] ||
                    BoardUtils.FIRST_RANK[this.tileId]){
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }


        }
    }
}
