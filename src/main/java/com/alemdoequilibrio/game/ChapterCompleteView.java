package com.alemdoequilibrio.game;

import java.util.Objects;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Tela simples exibida depois da vitoria sobre o Guardiao da Ruptura.
 */
public class ChapterCompleteView {

    private final Scene scene;

    public ChapterCompleteView(Runnable onReturnToExploration) {
        Objects.requireNonNull(onReturnToExploration);

        Label title = new Label("Capítulo concluído!");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Label message = new Label(
                "O Guardião da Ruptura foi derrotado."
        );

        Button returnButton = new Button("Voltar à exploração");
        returnButton.setOnAction(event -> onReturnToExploration.run());

        VBox root = new VBox(24, title, message, returnButton);
        root.setAlignment(Pos.CENTER);
        scene = new Scene(root, 1280, 720);
    }

    public Scene getScene() {
        return scene;
    }
}
