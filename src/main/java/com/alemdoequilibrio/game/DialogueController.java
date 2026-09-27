package com.alemdoequilibrio.game;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

    private final DialogueRepository
            dialogueRepository;

    private final Consumer<Interactable>
            onDialogueCompleted;

    private Interactable currentInteractable;

    private List<DialogueLine> currentLines;

    private int currentLineIndex;

    public DialogueController(
            Pane hudLayer,
            DialogueRepository dialogueRepository,
            Consumer<Interactable>
                    onDialogueCompleted) {

        this.dialogueRepository =
                Objects.requireNonNull(
                        dialogueRepository
                );

        this.onDialogueCompleted =
                Objects.requireNonNull(
                        onDialogueCompleted
                );

        dialogueLabel =
                new Label("");

        dialogueLabel.setTextFill(
                Color.BLACK
        );

        dialogueLabel.setWrapText(
                true
        );

        dialogueLabel.setMaxWidth(
                820
        );

        dialogueLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        Label arrowIndicator =
                new Label("▼");

        arrowIndicator.setTextFill(
                Color.BLACK
        );

        StackPane.setAlignment(
                arrowIndicator,
                Pos.BOTTOM_RIGHT
        );

        dialogueBox =
                new StackPane(
                        dialogueLabel,
                        arrowIndicator
                );

        dialogueBox.setPrefSize(
                900,
                140
        );

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

        dialogueBox.setStyle(
                "-fx-background-color: #F8F8F8;"
                + "-fx-border-color: #000000;"
                + "-fx-border-width: 6px;"
        );

        dialogueBox.relocate(
                190,
                550
        );

        dialogueBox.setVisible(
                false
        );

        hudLayer.getChildren().add(
                dialogueBox
        );
    }

    public void startDialogue(
            Interactable interactable) {

        currentInteractable =
                interactable;

        currentLines =
                dialogueRepository
                        .getDialogue(
                                interactable
                                        .getDialogueId()
                        );

        currentLineIndex = 0;

        if (currentLines.isEmpty()) {

            closeDialogue();

            return;
        }

        showCurrentLine();

        dialogueBox.setVisible(
                true
        );
    }

    public boolean advanceDialogue() {

        if (!isOpen()) {
            return false;
        }

        currentLineIndex++;

        if (currentLineIndex
                >= currentLines.size()) {

            Interactable completed =
                    currentInteractable;

            closeDialogue();

            onDialogueCompleted.accept(
                    completed
            );

            return false;
        }

        showCurrentLine();

        return true;
    }

    private void showCurrentLine() {

        DialogueLine line =
                currentLines.get(
                        currentLineIndex
                );

        dialogueLabel.setText(
                line.getSpeaker()
                + ": "
                + line.getText()
        );
    }

    public void closeDialogue() {

        dialogueBox.setVisible(
                false
        );

        currentInteractable = null;

        currentLines = null;

        currentLineIndex = 0;
    }

    public boolean isOpen() {

        return dialogueBox.isVisible();
    }
}