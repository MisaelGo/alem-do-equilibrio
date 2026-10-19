package com.alemdoequilibrio.game;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Controla a sequência e a exibição dos diálogos.
 */
public class DialogueController {

    private static final String HERO_PORTRAIT =
            "/images/hero_dialogue.png";

    private static final String NILO_PORTRAIT =
            "/images/nilo_dialogue.png";

    private static final String ION_PORTRAIT =
            "/images/ion_dialogue.png";

    private final Label speakerLabel;

    private final Label dialogueLabel;

    private final Label continueLabel;

    private final ImageView portraitView;

    private final StackPane portraitFrame;

    private final StackPane dialogueBox;

    private final ResourceManager
            resourceManager;

    private final DialogueRepository
            dialogueRepository;

    private final Consumer<Interactable>
            onDialogueCompleted;

    private Interactable currentInteractable;

    private List<DialogueLine> currentLines;

    private int currentLineIndex;

    public DialogueController(
            Pane hudLayer,
            ResourceManager resourceManager,
            DialogueRepository dialogueRepository,
            Consumer<Interactable>
                    onDialogueCompleted) {

        Objects.requireNonNull(
                hudLayer
        );

        this.resourceManager =
                Objects.requireNonNull(
                        resourceManager
                );

        this.dialogueRepository =
                Objects.requireNonNull(
                        dialogueRepository
                );

        this.onDialogueCompleted =
                Objects.requireNonNull(
                        onDialogueCompleted
                );

        portraitView =
                new ImageView();

        portraitView.setFitWidth(
                108
        );

        portraitView.setFitHeight(
                108
        );

        portraitView.setPreserveRatio(
                true
        );

        portraitView.setSmooth(
                false
        );

        portraitFrame =
                new StackPane(
                        portraitView
                );

        portraitFrame.setPrefSize(
                118,
                118
        );

        portraitFrame.setMinSize(
                118,
                118
        );

        portraitFrame.setMaxSize(
                118,
                118
        );

        portraitFrame.setStyle(
                "-fx-background-color: #20262B;"
                + "-fx-border-color: #000000;"
                + "-fx-border-width: 4px;"
        );

        speakerLabel =
                new Label("");

        speakerLabel.setTextFill(
                Color.BLACK
        );

        speakerLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
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
                Double.MAX_VALUE
        );

        dialogueLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace;"
                + "-fx-font-size: 19px;"
                + "-fx-font-weight: bold;"
        );

        VBox textArea =
                new VBox(
                        5,
                        speakerLabel,
                        dialogueLabel
                );

        textArea.setAlignment(
                Pos.TOP_LEFT
        );

        textArea.setPadding(
                new Insets(
                        0,
                        0,
                        28,
                        0
                )
        );

        HBox.setHgrow(
                textArea,
                Priority.ALWAYS
        );

        HBox dialogueContent =
                new HBox(
                        18,
                        portraitFrame,
                        textArea
                );

        dialogueContent.setAlignment(
                Pos.TOP_LEFT
        );

        continueLabel =
                new Label(
                        "Pressione E para continuar"
                );

        continueLabel.setTextFill(
                Color.BLACK
        );

        continueLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );

        StackPane.setAlignment(
                dialogueContent,
                Pos.TOP_LEFT
        );

        StackPane.setAlignment(
                continueLabel,
                Pos.BOTTOM_RIGHT
        );

        dialogueBox =
                new StackPane(
                        dialogueContent,
                        continueLabel
                );

        dialogueBox.setPrefSize(
                1050,
                170
        );

        dialogueBox.setPadding(
                new Insets(
                        15,
                        24,
                        15,
                        20
                )
        );

        dialogueBox.setStyle(
                "-fx-background-color: #F8F8F8;"
                + "-fx-border-color: #000000;"
                + "-fx-border-width: 6px;"
        );

        dialogueBox.relocate(
                115,
                530
        );

        dialogueBox.setVisible(
                false
        );

        hidePortrait();

        hudLayer.getChildren().add(
                dialogueBox
        );
    }

    /**
     * Inicia um diálogo associado
     * a um objeto interagível.
     */
    public void startDialogue(
            Interactable interactable) {

        Objects.requireNonNull(
                interactable
        );

        currentInteractable =
                interactable;

        startDialogueById(
                interactable
                        .getDialogueId()
        );
    }

    /**
     * Inicia um diálogo diretamente pelo ID.
     *
     * É usado em cenas que não dependem
     * de interação com NPC, como a introdução.
     */
    public void startDialogue(
            String dialogueId) {

        currentInteractable =
                null;

        startDialogueById(
                dialogueId
        );
    }

    private void startDialogueById(
            String dialogueId) {

        if (dialogueId == null
                || dialogueId.isBlank()) {

            throw new IllegalArgumentException(
                    "O ID do diálogo não pode ser vazio."
            );
        }

        currentLines =
                dialogueRepository
                        .getDialogue(
                                dialogueId
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

        String speaker =
                line.getSpeaker();

        /*
         * Narração continua sem nome e sem retrato.
         */
        if ("Narração".equalsIgnoreCase(
                speaker
        )) {

            speakerLabel.setVisible(
                    false
            );

            speakerLabel.setManaged(
                    false
            );

            hidePortrait();

            dialogueLabel.setText(
                    line.getText()
            );

            return;
        }

        speakerLabel.setText(
                speaker
        );

        speakerLabel.setVisible(
                true
        );

        speakerLabel.setManaged(
                true
        );

        updatePortrait(
                speaker
        );

        dialogueLabel.setText(
                line.getText()
        );
    }

    private void updatePortrait(
            String speaker) {

        String portraitPath =
                getPortraitPath(
                        speaker
                );

        if (portraitPath == null) {

            hidePortrait();

            return;
        }

        portraitView.setImage(
                resourceManager.getImage(
                        portraitPath
                )
        );

        portraitFrame.setVisible(
                true
        );

        portraitFrame.setManaged(
                true
        );
    }

    private String getPortraitPath(
            String speaker) {

        if (speaker == null) {

            return null;
        }

        String normalized =
                speaker
                        .trim()
                        .toLowerCase();

        return switch (normalized) {

            case "herói",
                 "heroi" ->
                    HERO_PORTRAIT;

            case "nilo" ->
                    NILO_PORTRAIT;

            case "íon",
                 "ion" ->
                    ION_PORTRAIT;

            default ->
                    null;
        };
    }

    private void hidePortrait() {

        portraitView.setImage(
                null
        );

        portraitFrame.setVisible(
                false
        );

        portraitFrame.setManaged(
                false
        );
    }

    public void closeDialogue() {

        dialogueBox.setVisible(
                false
        );

        hidePortrait();

        currentInteractable =
                null;

        currentLines =
                null;

        currentLineIndex = 0;
    }

    public boolean isOpen() {

        return dialogueBox.isVisible();
    }
}
