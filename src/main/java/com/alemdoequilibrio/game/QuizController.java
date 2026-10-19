package com.alemdoequilibrio.game;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Controla a apresentação visual dos quizzes dentro da própria tela do jogo.
 *
 * Não contém regras de recompensa. Essas regras permanecem no Merchant.
 */
public class QuizController {

    private final VBox quizBox;

    private final Label titleLabel;

    private final Label progressLabel;

    private final Label questionLabel;

    private final Button[] optionButtons;

    private final Label feedbackLabel;

    private final Button continueButton;

    private List<PhysicsQuestion>
            currentQuestions;

    private int currentQuestionIndex;

    private int correctAnswers;

    private IntConsumer onQuizFinished;

    public QuizController(
            Pane hudLayer) {

        Objects.requireNonNull(
                hudLayer
        );

        titleLabel =
                new Label(
                        "TESTE DO ÍON"
                );

        titleLabel.setStyle(
                "-fx-font-size: 26px;"
                + "-fx-font-weight: bold;"
        );

        progressLabel =
                new Label("");

        progressLabel.setStyle(
                "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
        );

        questionLabel =
                new Label("");

        questionLabel.setWrapText(
                true
        );

        questionLabel.setMaxWidth(
                760
        );

        questionLabel.setStyle(
                "-fx-font-size: 21px;"
                + "-fx-font-weight: bold;"
        );

        optionButtons =
                new Button[4];

        VBox optionsBox =
                new VBox(10);

        optionsBox.setFillWidth(
                true
        );

        for (int i = 0;
                i < optionButtons.length;
                i++) {

            final int optionIndex =
                    i;

            Button button =
                    new Button();

            button.setMaxWidth(
                    Double.MAX_VALUE
            );

            button.setAlignment(
                    Pos.CENTER_LEFT
            );

            button.setWrapText(
                    true
            );

            button.setStyle(
                    "-fx-font-size: 17px;"
                    + "-fx-padding: 10px 14px;"
            );

            button.setOnAction(
                    event ->
                            answerQuestion(
                                    optionIndex
                            )
            );

            optionButtons[i] =
                    button;

            optionsBox
                    .getChildren()
                    .add(button);
        }

        feedbackLabel =
                new Label("");

        feedbackLabel.setWrapText(
                true
        );

        feedbackLabel.setMaxWidth(
                760
        );

        feedbackLabel.setStyle(
                "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
        );

        continueButton =
                new Button(
                        "Continuar"
                );

        continueButton.setStyle(
                "-fx-font-size: 17px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 9px 22px;"
        );

        continueButton.setVisible(
                false
        );

        continueButton.setManaged(
                false
        );

        continueButton.setOnAction(
                event ->
                        goToNextQuestion()
        );

        quizBox =
                new VBox(
                        16,
                        titleLabel,
                        progressLabel,
                        questionLabel,
                        optionsBox,
                        feedbackLabel,
                        continueButton
                );

        quizBox.setPadding(
                new Insets(
                        24
                )
        );

        quizBox.setPrefWidth(
                820
        );

        quizBox.setMaxWidth(
                820
        );

        quizBox.setStyle(
                "-fx-background-color: rgba(248,248,248,0.97);"
                + "-fx-border-color: black;"
                + "-fx-border-width: 6px;"
                + "-fx-background-radius: 2px;"
                + "-fx-border-radius: 2px;"
        );

        quizBox.relocate(
                230,
                100
        );

        quizBox.setVisible(
                false
        );

        hudLayer
                .getChildren()
                .add(
                        quizBox
                );
    }

    public void startQuiz(
            List<PhysicsQuestion> questions,
            IntConsumer onQuizFinished) {

        Objects.requireNonNull(
                questions
        );

        this.onQuizFinished =
                Objects.requireNonNull(
                        onQuizFinished
                );

        if (questions.size() < 4) {

            throw new IllegalArgumentException(
                    "O quiz precisa de pelo menos quatro questões."
            );
        }

        currentQuestions =
                List.copyOf(
                        questions
                );

        currentQuestionIndex = 0;

        correctAnswers = 0;

        quizBox.setVisible(
                true
        );

        showCurrentQuestion();
    }

    private void showCurrentQuestion() {

        PhysicsQuestion question =
                currentQuestions.get(
                        currentQuestionIndex
                );

        progressLabel.setText(
                "Pergunta "
                + (currentQuestionIndex + 1)
                + " de "
                + currentQuestions.size()
        );

        questionLabel.setText(
                question.getPrompt()
        );

        String[] options =
                question.getOptions();

        for (int i = 0;
                i < optionButtons.length;
                i++) {

            char letter =
                    (char) ('A' + i);

            optionButtons[i]
                    .setText(
                            letter
                            + ") "
                            + options[i]
                    );

            optionButtons[i]
                    .setDisable(
                            false
                    );

            optionButtons[i]
                    .setVisible(
                            true
                    );
        }

        feedbackLabel.setText(
                ""
        );

        continueButton.setVisible(
                false
        );

        continueButton.setManaged(
                false
        );
    }

    private void answerQuestion(
            int selectedIndex) {

        if (!isOpen()) {

            return;
        }

        PhysicsQuestion question =
                currentQuestions.get(
                        currentQuestionIndex
                );

        for (Button button
                : optionButtons) {

            button.setDisable(
                    true
            );
        }

        boolean correct =
                question.isCorrect(
                        selectedIndex
                );

        if (correct) {

            correctAnswers++;
        }

        String resultText =
                correct
                        ? "✓ Correto!\n\n"
                        : "✗ Incorreto.\n\n";

        feedbackLabel.setText(
                resultText
                + question
                        .getFeedbackForOption(
                                selectedIndex
                        )
        );

        continueButton.setText(
                currentQuestionIndex
                        == currentQuestions.size() - 1
                        ? "Ver resultado"
                        : "Continuar"
        );

        continueButton.setVisible(
                true
        );

        continueButton.setManaged(
                true
        );

        continueButton.requestFocus();
    }

    private void goToNextQuestion() {

        if (currentQuestionIndex
                < currentQuestions.size() - 1) {

            currentQuestionIndex++;

            showCurrentQuestion();

            return;
        }

        IntConsumer callback =
                onQuizFinished;

        int result =
                correctAnswers;

        closeQuiz();

        callback.accept(
                result
        );
    }

    public boolean isOpen() {

        return quizBox.isVisible();
    }

    public void closeQuiz() {

        quizBox.setVisible(
                false
        );

        currentQuestions = null;

        currentQuestionIndex = 0;

        correctAnswers = 0;

        onQuizFinished = null;
    }
}
