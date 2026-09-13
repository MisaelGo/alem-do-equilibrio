/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.alemdoequilibrio.core;

import com.alemdoequilibrio.game.ExplorationController;
import javafx.stage.Stage;

public class GameController {
    
    private final SceneRouter sceneRouter;
    private GameState currentState;
    private final ExplorationController explorationController;
    
    public GameController(Stage stage){
        this.sceneRouter = new SceneRouter(stage);
        this.currentState = GameState.MENU;
        this.explorationController = new ExplorationController(800, 600);
    }
    
    public void startGame() {
    
        currentState = GameState.EXPLORATION;

        sceneRouter.setTitle("Além do Equilíbrio");

        sceneRouter.show(explorationController.getScene());

        explorationController.start();
        
    }
    
    public void changeState(GameState newState){
        this.currentState = newState;
    }
    
}
