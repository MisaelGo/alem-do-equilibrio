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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Cena introdutoria exibida antes da primeira chegada a Eletéria.
 *
 * A introdução combina texto, chuva, trovões, um breve clarão
 * e silêncio. O fenômeno que transportou o herói segue misterioso.
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

    /*
     * Quatro momentos: tempestade, suspeita, clarão e silêncio.
     * A origem da descarga permanece desconhecida para o jogador.
     */
    private static final String[] LINES = {
        "Aquela parecia ser apenas mais uma noite de tempestade.",
        "A chuva não dava trégua, e os trovões pareciam cada vez mais próximos.",
        "Eu estava em casa quando ouvi um barulho estranho.",
        "Não parecia um trovão.",
        "Vinha de algum lugar lá fora.",

        "Esperei alguns segundos, tentando entender o que havia escutado.",
        "Então ouvi novamente.",
        "Dessa vez, tive certeza de que vinha de perto da porta.",
        "Não sei por que decidi investigar.",
        "Talvez devesse ter ignorado.",

        "Levantei e caminhei até a entrada.",
        "Assim que abri a porta, uma luz intensa surgiu diante de mim.",
        "Por um instante, tudo pareceu parar.",
        "Não consegui me mover.",

        "Não senti dor.",
        "Não senti mais nada.",
        "Só me lembro daquela luz...",
        "...e da sensação de estar caindo.",
        "Depois disso, tudo ficou escuro.",

        "Quando abri os olhos novamente, já não estava em casa."
    };

    private static final int DISTANT_THUNDER_LINE = 6;
    private static final int RAIN_INTENSIFIES_LINE = 10;
    private static final int IMPACT_AFTER_LINE = 13;
    private static final int FINAL_REVEAL_LINE = LINES.length - 1;

    private final Scene scene;

    private final Label textLabel;

    private final Rectangle flashOverlay;

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

        // Clarão breve sobre o fundo preto, sem utilizar novas imagens.
        flashOverlay = new Rectangle(WIDTH, HEIGHT, Color.WHITE);
        flashOverlay.setOpacity(0.0);
        flashOverlay.setMouseTransparent(true);

        StackPane root =
                new StackPane(
                        textLabel,
                        flashOverlay
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
                0.22
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

        flashOverlay.setOpacity(0.0);
        rainPlayer.setVolume(0.22);
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

            if (i == FINAL_REVEAL_LINE) {
                // Três segundos de silêncio antes da última revelação.
                result.getChildren().add(
                        new PauseTransition(Duration.seconds(3.0))
                );
            }

            result.getChildren().addAll(
                    prepareLine,
                    fadeIn,
                    hold,
                    fadeOut
            );

            if (i == IMPACT_AFTER_LINE) {
                result.getChildren().addAll(
                        createImpactCue(),
                        new PauseTransition(Duration.seconds(2.0))
                );
            }
        }

        result.getChildren().add(
                new PauseTransition(Duration.seconds(1.0))
        );

        result.setOnFinished(
                event -> {

                    stop();

                    onFinished.run();
                }
        );

        return result;
    }

    /** Descarga repentina, clarão rápido e corte imediato da chuva. */
    private SequentialTransition createImpactCue() {
        PauseTransition trigger =
                new PauseTransition(Duration.millis(1));

        trigger.setOnFinished(event -> {
            rainPlayer.stop();
            thunderSound.stop();
            thunderSound.play(0.95);
            electricShockSound.play();
            flashOverlay.setOpacity(0.90);
        });

        FadeTransition fadeFlash =
                new FadeTransition(
                        Duration.millis(480),
                        flashOverlay
                );
        fadeFlash.setFromValue(0.90);
        fadeFlash.setToValue(0.0);

        return new SequentialTransition(trigger, fadeFlash);
    }

    private void showLine(int index) {
        textLabel.setText(LINES[index]);

        if (index == DISTANT_THUNDER_LINE) {
            // O primeiro trovão deve soar distante.
            thunderSound.play(0.40);
        }

        if (index == RAIN_INTENSIFIES_LINE) {
            rainPlayer.setVolume(0.48);
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
