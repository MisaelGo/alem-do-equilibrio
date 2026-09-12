/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.alemdoequilibrio.core;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneRouter {
    
    private final Stage stage;
    
    public SceneRouter(Stage stage){
        this.stage = stage;
    }
    
    public void show(Scene scene){
        stage.setScene(scene);
        stage.show();
    }
    
    public void setTitle(String title){
        stage.setTitle(title);
    }
}
