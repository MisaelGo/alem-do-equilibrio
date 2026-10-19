package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Cena introdutoria exibida antes da primeira chegada a Eletéria.
 *
 * O prólogo usa apenas fundo preto, texto e áudio para manter
 * a abertura simples e atmosférica.
 */
public class PrologueController {

    private static final double
            WIDTH = 1280;

    private static final double
            HEIGHT = 720;

    private static final String
            RAIN_AUDIO =
                    "/audio/rain.mp3";

    private static final String
            THUNDER_AUDIO =
                    "/audio/estrondo_thunder.mp3";

    private static final String
            ELECTRIC_SHOCK_AUDIO =
                    "/audio/electric_shock.mp3";

    private static final String[] LINES = {
        "Era uma noite comum.",
        "Chovia muito. Os trovões pareciam cada vez mais próximos.",
        "Eu estava em casa quando ouvi um barulho vindo do lado de fora.",
        "No começo, achei que fosse apenas mais um trovão.",
        "Mas aquele som havia vindo de muito perto.",
        "Levantei e fui até a porta.",
        "Quando abri, tentei descobrir o que havia acontecido.",
        "Foi quando tudo aconteceu.",
        "Um estrondo.",
        "Um clarão.",
        "Não consegui nem reagir.",
        "O raio me atingiu.",
        "Depois disso...",
        "...eu não me lembro de mais nada."
    };

    private final Scene scene;

    private final Label textLabel;

    private final MediaPlayer rainPlayer;

    private final AudioClip thunderSound;

    private final AudioClip electricShockSound;

    private final Runnable onFinished;

    private SequentialTransition sequence;

    public PrologueController(
            ResourceManager resourceManager,
            Runnable onFinished) {

        Objects.requireNonNull(
                resourceManager
        );

        this.onFinished =
                Objects.requireNonNull(
                        onFinished
                );

        textLabel =
                new Label();

        textLabel.setTextFill(
                Color.web(
                        "#F4F4F4"
                )
        );

        textLabel.setWrapText(
                true
        );

        textLabel.setTextAlignment(
                TextAlignment.CENTER
        );

        textLabel.setAlignment(
                Pos.CENTER
        );

        textLabel.setMaxWidth(
                920
        );

        textLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace;"
                + "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
        );

        textLabel.setOpacity(
                0.0
        );

        StackPane root =
                new StackPane(
                        textLabel
                );

        root.setAlignment(
                Pos.CENTER
        );

        root.setPrefSize(
                WIDTH,
                HEIGHT
        );

        root.setStyle(
                "-fx-background-color: #000000;"
        );

        scene =
                new Scene(
                        root,
                        WIDTH,
                        HEIGHT,
                        Color.BLACK
                );

        rainPlayer =
                new MediaPlayer(
                        resourceManager.getMedia(
                                RAIN_AUDIO
                        )
                );

        rainPlayer.setCycleCount(
                MediaPlayer.INDEFINITE
        );

        rainPlayer.setVolume(
                0.38
        );

        thunderSound =
                resourceManager.getAudioClip(
                        THUNDER_AUDIO
                );

        thunderSound.setVolume(
                0.85
        );

        electricShockSound =
                resourceManager.getAudioClip(
                        ELECTRIC_SHOCK_AUDIO
                );

        electricShockSound.setVolume(
                0.85
        );

        sequence =
                createSequence();
    }

    public Scene getScene() {

        return scene;
    }

    public void start() {

        stop();

        textLabel.setText(
                ""
        );

        textLabel.setOpacity(
                0.0
        );

        rainPlayer.play();

        sequence =
                createSequence();

        sequence.playFromStart();
    }

    public void stop() {

        if (sequence != null) {

            sequence.stop();
        }

        rainPlayer.stop();

        thunderSound.stop();

        electricShockSound.stop();
    }

    private SequentialTransition
            createSequence() {

        SequentialTransition result =
                new SequentialTransition();

        result.getChildren().add(
                new PauseTransition(
                        Duration.seconds(
                                1.2
                        )
                )
        );

        for (int i = 0;
                i < LINES.length;
                i++) {

            final int lineIndex =
                    i;

            PauseTransition prepareLine =
                    new PauseTransition(
                            Duration.millis(
                                    1
                            )
                    );

            prepareLine.setOnFinished(
                    event -> showLine(
                            lineIndex
                    )
            );

            FadeTransition fadeIn =
                    new FadeTransition(
                            Duration.millis(
                                    450
                            ),
                            textLabel
                    );

            fadeIn.setFromValue(
                    0.0
            );

            fadeIn.setToValue(
                    1.0
            );

            PauseTransition hold =
                    new PauseTransition(
                            Duration.millis(
                                    getHoldDuration(
                                            LINES[i]
                                    )
                            )
                    );

            FadeTransition fadeOut =
                    new FadeTransition(
                            Duration.millis(
                                    450
                            ),
                            textLabel
                    );

            fadeOut.setFromValue(
                    1.0
            );

            fadeOut.setToValue(
                    0.0
            );

            result.getChildren().addAll(
                    prepareLine,
                    fadeIn,
                    hold,
                    fadeOut
            );
        }

        result.getChildren().add(
                new PauseTransition(
                        Duration.seconds(
                                1.0
                        )
                )
        );

        result.setOnFinished(
                event -> {

                    stop();

                    onFinished.run();
                }
        );

        return result;
    }

    private void showLine(
            int index) {

        textLabel.setText(
                LINES[index]
        );

        if (index == 8) {

            thunderSound.play();
        }

        if (index == 11) {

            electricShockSound.play();
        }

        if (index == 12) {

            rainPlayer.stop();
        }
    }

    private double getHoldDuration(
            String text) {

        return Math.max(
                1900,
                Math.min(
                        3600,
                        1500
                        + text.length()
                        * 24.0
                )
        );
    }
}
