package application.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class Controller implements Initializable {
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    private static boolean TURN = false;

    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            refreshBoard(x, y);
        });
    }
    public Rectangle getGame_panel(){
        return game_panel;
    }

    public boolean refreshBoard (int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            drawChess();
            System.out.println("Refresh Finished.");
            TURN = !TURN;
            return true;
        }
        return false;
    }

    private void drawChess () {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle (int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine (int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
    public boolean judgeLine(){
        for (int[] ints : chessBoard) {
            if (ints[0] == ints[1] && ints[1] == ints[2] && ints[0] > 0) return true;
        }
        return false;
    }

    public boolean judgeColumn(){
        for(int j = 0;j < chessBoard.length; j++)
        {
            if(chessBoard[0][j] == chessBoard[1][j] && chessBoard[1][j] == chessBoard[2][j] && chessBoard[0][j] > 0) return true;
        }
        return false;
    }

    public boolean judgeCross(){
        if(chessBoard[0][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][2] && chessBoard[1][1] > 0) return true;
        return chessBoard[0][2] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][0] && chessBoard[1][1] > 0;
    }

    public boolean judgeWin(){
        return judgeCross() || judgeColumn() || judgeLine();
    }

    public boolean judgeDraw(){
        for (int[] ints : chessBoard) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (ints[j] == 0) return false;
            }
        }
        return !judgeWin();
    }

    public int getBound(){
        return BOUND;
    }

}
