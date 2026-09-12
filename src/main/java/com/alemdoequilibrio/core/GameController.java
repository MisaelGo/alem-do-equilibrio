/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.alemdoequilibrio.core;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameController {
    
    private final SceneRouter sceneRouter;
    private GameState currentState;
    
    public GameController(Stage stage){
        this.sceneRouter = new SceneRouter(stage);
        this.currentState = GameState.MENU;
    }
    
    public void startGame(){
        Label titulo = new Label("Além do Equilíbrio");

        StackPane root = new StackPane(titulo);

        Scene scene = new Scene(root, 800, 600);

        sceneRouter.setTitle("Além do Equilíbrio");
        sceneRouter.show(scene);
    }
    
    public void changeState(GameState newState){
        this.currentState = newState;
    }
    
}
