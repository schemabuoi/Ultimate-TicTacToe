/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultimatetictactoe.gui.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import ultimatetictactoe.bll.game.GameManager.GameMode;
import ultimatetictactoe.bll.move.IMove;
import ultimatetictactoe.gui.model.GameModel;
import ultimatetictactoe.gui.util.AnimationUtil;

/**
 * FXML Controller class
 *
 * @author Acer
 */
public class GameViewController implements Initializable {

    private GameModel model;
    private GameMode mode;
    private HashMap<Integer, HashMap<Integer, Button>> board = new HashMap();
    
    @FXML
    private GridPane grdGameboard;
    @FXML
    private Rectangle imgCrossPartOne;
    @FXML
    private Rectangle imgCrossPartTwo;
    @FXML
    private Circle imgCirclePartOne;
    @FXML
    private Circle imgCirclePartTwo;
    @FXML
    private Label lblPlayer1;
    @FXML
    private Label lblPlayer2;
    
    public GameViewController()
    {
        model = GameModel.getInstance();
        mode = model.getGameMode();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        initializeBoard();
        checkForSimulationMode();
    }    
    
    private void initializeBoard()
    {
       for(Node microboard : grdGameboard.getChildren())
       {
           Integer microboardXPosition = GridPane.getRowIndex(microboard);
           Integer microboardYPosition = GridPane.getColumnIndex(microboard);
           int microboardX = (microboardXPosition == null) ? 0 : microboardXPosition;
           int microboardY = (microboardYPosition == null) ? 0 : microboardYPosition;
           for(Node field : ((GridPane) ((StackPane) microboard).getChildren().get(0)).getChildren())
           {
               Integer row = GridPane.getRowIndex(field);
               Integer col = GridPane.getColumnIndex(field);
               int fieldXPosition = (row == null) ? microboardX*3+0 : microboardX*3 + row;
               int fieldYPosition = (col == null) ? microboardY*3+0 : microboardY*3 + col;
               Button button = ((Button) ((StackPane) field).getChildren().get(0));
               addToBoard(fieldXPosition , fieldYPosition, button);
           }
       }
    }
    
    private void addToBoard(int fieldXPosition, int fieldYPosition, Button field)
    {
        if(board.get(fieldXPosition) == null)
        {
            HashMap<Integer, Button> map = new HashMap();
            map.put(fieldYPosition, field);
            board.put(fieldXPosition, map);
        }
        else
        {
            board.get(fieldXPosition).put(fieldYPosition, field);
        }
    }

    private void checkForSimulationMode()
    {
        if(mode == GameMode.BotVsBot)
        {
            performBotVsBotSimulation();
        }
    }
    
    private void performBotVsBotSimulation()
    {
        grdGameboard.setDisable(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() 
            {
                try 
                {
                    while(!model.isGameOver())
                    {
                        Thread.sleep(100);
                        Platform.runLater(() -> performBotMove());
                    }
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(GameViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }
    
    @FXML
    private void clickOnField(ActionEvent event)
    {
        if(performPlayerMove(event))
        {
            if(mode == GameMode.HumanVsBot)
            {
                imitateBotMove();          
            }
        }
    }
    
    private void imitateBotMove()
    {
        grdGameboard.setDisable(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() 
            {
                try 
                {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        if(performBotMove())
                        {
                            grdGameboard.setDisable(false);
                        }
                    });
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(GameViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }
    
    private boolean performBotMove()
    {
        int currentPlayer = model.getCurrentPlayer();
        IMove botMove = null;
        if(model.performBotMove())
        {
            botMove = model.getBotMove();
            placePlayerMarker(currentPlayer, botMove.getX(), botMove.getY());          
            updateGameboard(currentPlayer, botMove.getX(), botMove.getY());
            return true;
        }
        return false;
    }
    
    private void doMove(int currentPlayer, int fieldXPosition, int fieldYPosition)
    {
        
    }
    
    private void updateGameboard(int currentPlayer, int fieldXPosition, int fieldYPosition)
    {
        checkIfMicroboardWon(currentPlayer, fieldXPosition, fieldYPosition);
        if(!checkIfGameIsOver(currentPlayer))
        {
            switchPlayerPointer(currentPlayer);
            setAvailableFields();
        }
    }
    
    private int[] getFieldPosition(ActionEvent event)
    {
        int[] macroboardPosition = getPositionOnMacroboard(event);
        int[] microboardPosition = getPositionOnMicroboard(event);
        int fieldXPosition = macroboardPosition[0] * 3 + microboardPosition[0];
        int fieldYPosition = macroboardPosition[1] * 3 + microboardPosition[1];
        int[] fieldPosition = {fieldXPosition, fieldYPosition};
        return fieldPosition;
    }
    
    private int[] getPositionOnMacroboard(ActionEvent event)
    {
        Integer macroboardXPosition = GridPane.getRowIndex(((Node) event.getSource()).getParent().getParent().getParent());
        Integer macroboardYPosition = GridPane.getColumnIndex(((Node) event.getSource()).getParent().getParent().getParent());
        macroboardXPosition = (macroboardXPosition == null) ? 0 : macroboardXPosition;
        macroboardYPosition = (macroboardYPosition == null) ? 0 : macroboardYPosition;
        int[] macroboardPosition = {macroboardXPosition, macroboardYPosition};
        return macroboardPosition;
    }
    
    private int[] getPositionOnMicroboard(ActionEvent event)
    {
        Integer microboardXPosition = GridPane.getRowIndex(((Node) event.getSource()).getParent());
        Integer microboardYPosition = GridPane.getColumnIndex(((Node) event.getSource()).getParent());
        microboardXPosition = (microboardXPosition == null) ? 0 : microboardXPosition;
        microboardYPosition = (microboardYPosition == null) ? 0 : microboardYPosition;
        int[] microboardPosition = {microboardXPosition, microboardYPosition};
        return microboardPosition;
    }
    
    private void setAvailableFields()
    {
        List<IMove> availableMoves = model.getAvailableMoves();
        for(int x = 0; x < 9; x++)
        {
            for(int y = 0; y < 9; y++)
            {
                board.get(x).get(y).setDisable(true);
                for(IMove move : availableMoves)
                {
                    if(move.getX() == x && move.getY() == y)
                    {
                        board.get(x).get(y).setDisable(false);
                    }

                }
            }
        }
    }
    
    private boolean performPlayerMove(ActionEvent event)
    {
        int currentPlayer = model.getCurrentPlayer();
        int[] fieldPosition = getFieldPosition(event);
        if(model.performPlayerMove(fieldPosition[0], fieldPosition[1]))
        {
            placePlayerMarker(currentPlayer, fieldPosition[0], fieldPosition[1]);
            updateGameboard(currentPlayer, fieldPosition[0], fieldPosition[1]);   
            return true;
        }
        return false;
    }
    
    private void placePlayerMarker(int player, int fieldXPosition, int fieldYPosition)
    {
        Button field = board.get(fieldXPosition).get(fieldYPosition);
        field.setGraphic(getPlayerMarker(player));
        ParallelTransition transition = AnimationUtil.createFieldAnimation((ImageView) field.getGraphic());
        transition.play();
    }
    
    private void checkIfMicroboardWon(int currentPlayer, int fieldXPosition, int fieldYPosition)
    {
        if(model.isMicroboardWon(fieldXPosition/3, fieldYPosition/3))
        {
            GridPane microboard = (GridPane) board.get(fieldXPosition).get(fieldYPosition).getParent().getParent();
            setMicroboardToWon(currentPlayer, microboard);
        }
    }
    
    private boolean checkIfGameIsOver(int currentPlayer)
    {
        if(model.isMacroboardWon())
        {
            setGameOver(currentPlayer);
            return true;
        }
        else if(model.isDraw())
        {
            setDraw();
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private ImageView getPlayerMarker(int playerNumber)
    {
        if(playerNumber == 0)
        {
            return new ImageView("/ultimatetictactoe/gui/images/PlayerOne.png");
        }
        else
        {
            return new ImageView("/ultimatetictactoe/gui/images/PlayerTwo.png");
        }
    }
    
    private void switchPlayerPointer(int currentPlayer)
    {
        ParallelTransition transition = new ParallelTransition();
        
        List<Node> cross = new ArrayList();
        cross.add(imgCrossPartOne);
        cross.add(imgCrossPartTwo);
        
        if(currentPlayer == 0)
        {
            ParallelTransition showTransition = AnimationUtil.createShowCircleAnimation(imgCirclePartOne, imgCirclePartTwo);
            ParallelTransition hideTransition = AnimationUtil.createHideCrossAnimation(cross);
            transition.getChildren().addAll(showTransition, hideTransition);
        }
        else
        {
            ParallelTransition showTransition = AnimationUtil.createHideCircleAnimation(imgCirclePartOne, imgCirclePartTwo);
            ParallelTransition hideTransition = AnimationUtil.createShowCrossAnimation(cross);
            transition.getChildren().addAll(showTransition, hideTransition);
        }
        transition.play();
    }
    
    private void setMicroboardToWon(int microboardWinner, GridPane microboard)
    {
        StackPane microboardField = (StackPane) microboard.getParent();
        microboard.setVisible(false);
        microboardField.getChildren().add(getPlayerMarker(microboardWinner));
    }
    
    private void setGameOver(int winner)
    {
        grdGameboard.setDisable(true);
        StackPane gameboardField = (StackPane) grdGameboard.getParent();
        gameboardField.getChildren().add(new Label(getPlayerMarker(winner) + " won the game"));
    }
    
    private void setDraw()
    {
        grdGameboard.setDisable(true);
        StackPane gameboardField = (StackPane) grdGameboard.getParent();
        gameboardField.getChildren().add(new Label("Draw"));
    }
    
    public void setPlayerLabels(String player1, String player2)
    {
        lblPlayer1.setText(player1);
        lblPlayer2.setText(player2);
    }
    
}
