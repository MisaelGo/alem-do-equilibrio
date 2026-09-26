package com.alemdoequilibrio.game;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Controla a sequência e a exibição dos diálogos.
 */
public class DialogueController {

    private final Label dialogueLabel;
    private final StackPane dialogueBox;

    private Interactable currentInteractable;
    private List<String> currentTexts;

    private int currentTextIndex;

    public DialogueController(Pane hudLayer) {

        this.dialogueLabel = new Label("");

        dialogueLabel.setTextFill(Color.BLACK);
        dialogueLabel.setWrapText(true);

        dialogueLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        Label arrowIndicator =
                new Label("▼");

        arrowIndicator.setTextFill(Color.BLACK);

        arrowIndicator.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );

        StackPane.setAlignment(
                arrowIndicator,
                Pos.BOTTOM_RIGHT
        );

        this.dialogueBox =
                new StackPane(
                        dialogueLabel,
                        arrowIndicator
                );

        dialogueBox.setPrefWidth(900);
        dialogueBox.setPrefHeight(140);

        dialogueBox.setPadding(
                new Insets(
                        15,
                        25,
                        15,
                        25
                )
        );

        StackPane.setAlignment(
                dialogueLabel,
                Pos.TOP_LEFT
        );

        /*
         * Visual temporário.
         * A UI definitiva poderá ser substituída pelo M5
         * sem alterar a lógica de diálogo.
         */
        dialogueBox.setStyle(
                "-fx-background-color: #F8F8F8;"
                + "-fx-border-color: #000000;"
                + "-fx-border-width: 6px;"
                + "-fx-border-style: solid;"
        );

        dialogueBox.relocate(
                (1280 - 900) / 2.0,
                720 - 170
        );

        dialogueBox.setVisible(false);

        hudLayer.getChildren().add(
                dialogueBox
        );
    }

    /**
     * Inicia uma interação.
     */
    public void startDialogue(
            Interactable interactable) {

        this.currentInteractable =
                interactable;

        this.currentTexts =
                interactable.getInteractionTexts();

        this.currentTextIndex = 0;

        if (currentTexts.isEmpty()) {
            closeDialogue();
            return;
        }

        showCurrentText();

        dialogueBox.setVisible(true);
    }

    /**
     * Avança para a próxima fala.
     *
     * @return true enquanto o diálogo continuar aberto
     */
    public boolean advanceDialogue() {

        if (!isOpen()) {
            return false;
        }

        currentTextIndex++;

        if (currentTextIndex
                >= currentTexts.size()) {

            closeDialogue();

            return false;
        }

        showCurrentText();

        return true;
    }

    private void showCurrentText() {

        dialogueLabel.setText(
                currentInteractable
                        .getInteractionName()
                + ": "
                + currentTexts.get(
                        currentTextIndex
                )
        );
    }

    public void closeDialogue() {

        dialogueBox.setVisible(false);

        currentInteractable = null;
        currentTexts = null;

        currentTextIndex = 0;
    }

    public boolean isOpen() {
        return dialogueBox.isVisible();
    }
}