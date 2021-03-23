package com.chessAI.gui;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chessAI.board.Move;
import com.chessAI.gui.Table.MoveLog;
import com.chessAI.piece.Piece;
import com.google.common.primitives.Ints;

import static javax.imageio.ImageIO.read;

public class TakenPiecesPanel extends JPanel {

    private final JPanel northPanel;
    private final JPanel southPanel;
    private static final Color PANEL_COLOR = Color.lightGray;
    private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(40,80);

    public TakenPiecesPanel(){
        super(new BorderLayout());
        this.setBackground(PANEL_COLOR);
        this.northPanel = new JPanel(new GridLayout(8,2));
        this.southPanel = new JPanel(new GridLayout(8,2));
        this.northPanel.setBackground(PANEL_COLOR);
        this.southPanel.setBackground(PANEL_COLOR);
        add(this.northPanel, BorderLayout.NORTH);
        add(this.southPanel, BorderLayout.SOUTH);
        setPreferredSize(TAKEN_PIECES_DIMENSION);
    }

    public void redo(final MoveLog movelog) {
        this.southPanel.removeAll();
        this.northPanel.removeAll();

        final List<Piece> whiteTakenPieces = new ArrayList<>();
        final List<Piece> blackTakenPieces = new ArrayList<>();

        for (final Move move : movelog.getMoves()) {
            if (move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if (takenPiece.getPieceAlliance().isWhite()) {
                    whiteTakenPieces.add(takenPiece);
                } else if (takenPiece.getPieceAlliance().isBlack()) {
                    blackTakenPieces.add(takenPiece);
                } else {
                    throw new RuntimeException("HUH? NO COLOR?");
                }
            }

        }
        Collections.sort(whiteTakenPieces, new Comparator<Piece>() {
           @Override
            public int compare(Piece o1, Piece o2){
               return Ints.compare(o1.getPieceValue(), o2.getPieceValue());
           }
        });
        Collections.sort(blackTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(Piece o1, Piece o2){
                return Ints.compare(o1.getPieceValue(), o2.getPieceValue());
            }
        });

        for(final Piece takenPiece : whiteTakenPieces){
            try{
                final BufferedImage image =
                        read(new File("art/" +
                                takenPiece.getPieceAlliance().toString().substring(0,1)
                                + "" + takenPiece.getPieceType().toString() + ".png"));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(
                        icon.getIconWidth() - 30, icon.getIconWidth() - 30, Image.SCALE_SMOOTH)));
                this.southPanel.add(imageLabel);

            }catch(final IOException e){
                e.printStackTrace();

            }
        }
        for(final Piece takenPiece : blackTakenPieces){
            try{
                final BufferedImage image =
                        read(new File("art/" +
                                takenPiece.getPieceAlliance().toString().substring(0,1)
                                + "" + takenPiece.getPieceType().toString() + ".png"));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(
                        icon.getIconWidth() - 30, icon.getIconWidth() - 30, Image.SCALE_SMOOTH)));
                this.northPanel.add(imageLabel);


            }catch(final IOException e){
                e.printStackTrace();

            }
        }
        validate();
    }

}
