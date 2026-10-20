package com.alemdoequilibrio.core;

import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Menu inicial de Além do Equilíbrio.
 * Somente aciona as ações fornecidas pelo GameController.
 */
public final class MainMenuController {

    private static final double WIDTH = 1280;
    private static final double HEIGHT = 720;

    private final Scene scene;

    public MainMenuController(
            Runnable onPlay,
            Runnable onRestart,
            Runnable onExit) {

        Objects.requireNonNull(onPlay);
        Objects.requireNonNull(onRestart);
        Objects.requireNonNull(onExit);

        Label title = new Label("ALÉM DO EQUILÍBRIO");
        title.setStyle(
                "-fx-text-fill: #F5DA93;"
                + "-fx-font-family: 'Consolas', monospace;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 42px;"
        );

        Label subtitle = new Label("CAPÍTULO 1 — A TERRA DAS CARGAS");
        subtitle.setStyle(
                "-fx-text-fill: #A8B8AF;"
                + "-fx-font-family: 'Consolas', monospace;"
                + "-fx-font-size: 17px;"
        );

        Button play = createButton("JOGAR", onPlay);
        Button restart = createButton("REINICIAR", onRestart);
        Button exit = createButton("SAIR", onExit);
        play.setDefaultButton(true);

        VBox options = new VBox(16, play, restart, exit);
        options.setAlignment(Pos.CENTER);
        options.setPadding(new Insets(25, 0, 0, 0));

        VBox panel = new VBox(18, title, subtitle, options);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(56, 65, 56, 65));
        panel.setMaxWidth(730);
        panel.setStyle(
                "-fx-background-color: #17262B;"
                + "-fx-border-color: #B48A42;"
                + "-fx-border-width: 4px;"
        );

        StackPane root = new StackPane(panel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #0D171B;");
        scene = new Scene(root, WIDTH, HEIGHT);
    }

    private static Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefSize(315, 54);
        button.setFocusTraversable(true);
        button.setStyle(buttonStyle(false));

        button.setOnMouseEntered(event -> button.setStyle(buttonStyle(true)));
        button.setOnMouseExited(event -> button.setStyle(buttonStyle(false)));
        button.setOnAction(event -> action.run());

        return button;
    }

    private static String buttonStyle(boolean selected) {
        return "-fx-background-color: "
                + (selected ? "#334C4C;" : "#22383D;")
                + "-fx-text-fill: #F6E6B8;"
                + "-fx-border-color: #BA9350;"
                + "-fx-border-width: 3px;"
                + "-fx-font-family: 'Consolas', monospace;"
                + "-fx-font-size: 23px;"
                + "-fx-font-weight: bold;"
                + "-fx-cursor: hand;";
    }

    public Scene getScene() {
        return scene;
    }
}
