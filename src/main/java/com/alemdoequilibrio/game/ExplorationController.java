/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.alemdoequilibrio.game;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class ExplorationController {

    private final double gameWidth;
    private final double gameHeight;

    private final Hero hero;

    private final Rectangle heroView;

    private final Pane root;
    private final Scene scene;
    
    private final boolean[] keys;
    
    private final AnimationTimer gameLoop;
    private long lastTime;

    public ExplorationController(double gameWidth, double gameHeight) {

        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;

        this.hero = new Hero(100, 100, 200);

        this.heroView = new Rectangle(40, 40);

        heroView.relocate(
                hero.getX(),
                hero.getY()
        );

        this.root = new Pane();
        root.getChildren().add(heroView);

        this.scene = new Scene(
                root,
                gameWidth,
                gameHeight
        );
        
        this.keys = new boolean[4];
        
        configureInput();
        
        this.gameLoop = createGameLoop();
    }

    public Scene getScene() {
        return scene;
    }
    
    private void configureInput() {

        scene.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.W
                || event.getCode() == KeyCode.UP) {
                keys[0] = true;
            }

            if (event.getCode() == KeyCode.S
                || event.getCode() == KeyCode.DOWN) {
                keys[1] = true;
            }

            if (event.getCode() == KeyCode.A
                || event.getCode() == KeyCode.LEFT) {
                keys[2] = true;
            }

            if (event.getCode() == KeyCode.D
                || event.getCode() == KeyCode.RIGHT) {
                keys[3] = true;
            }
        });

        scene.setOnKeyReleased(event -> {

            if (event.getCode() == KeyCode.W
                || event.getCode() == KeyCode.UP) {
                keys[0] = false;
            }

            if (event.getCode() == KeyCode.S
                || event.getCode() == KeyCode.DOWN) {
                keys[1] = false;
            }

            if (event.getCode() == KeyCode.A
                || event.getCode() == KeyCode.LEFT) {
                keys[2] = false;
            }

            if (event.getCode() == KeyCode.D
                || event.getCode() == KeyCode.RIGHT) {
                keys[3] = false;
            }
        });
    }
    
    private AnimationTimer createGameLoop() {

        return new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double deltaTime
                    = (now - lastTime) / 1_000_000_000.0;

                lastTime = now;

                double directionX = 0;
                double directionY = 0;

                if (keys[0]) {
                    directionY -= 1;
                }

                if (keys[1]) {
                    directionY += 1;
                }

                if (keys[2]) {
                    directionX -= 1;
                }

                if (keys[3]) {
                    directionX += 1;
                }

                hero.updateMovement(
                    directionX,
                    directionY,
                    deltaTime,
                    gameWidth - heroView.getWidth(),
                    gameHeight - heroView.getHeight()
                );

                heroView.relocate(
                    hero.getX(),
                    hero.getY()
                );
            }
        };
    }
    
    public void start() {
        lastTime = 0;
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }
}
