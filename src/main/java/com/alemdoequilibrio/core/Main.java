/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.alemdoequilibrio.core;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        
        GameController gameController = new GameController(stage);
        
        gameController.startGame();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
