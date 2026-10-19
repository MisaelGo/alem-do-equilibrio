package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Interface de exibição visual (HUD) do jogador.
 *
 * Responsável pela apresentação de pontos de vida (HP), avisos contextuais de
 * interação e notificações visuais de conceitos de Física aprendidos durante
 * a exploração. Não armazena nem altera regras centrais de jogo.
 */
public class HUD {

    private static final String HUD_CSS_PATH =
            "/css/hud.css";

    private static final String HERO_LIFE_HUD_IMAGE =
            "/images/hero_life_hud.png";

    private static final String PRESS_E_HUD_IMAGE =
            "/images/press_E_hud.png";

    private static final double LOGICAL_WIDTH =
            1280.0;

    private final Pane hudLayer;

    private final ResourceManager
            resourceManager;

    private final Pane statusCard;

    private final ProgressBar hpBar;

    private final Label hpValueLabel;

    private final Pane interactionPrompt;

    private final Label interactionLabel;

    private final VBox conceptBanner;

    private final Label conceptTitleLabel;

    private SequentialTransition
            conceptAnimation;

    public HUD(
            Pane hudLayer,
            ResourceManager resourceManager) {

        this.hudLayer =
                Objects.requireNonNull(
                        hudLayer,
                        "A camada hudLayer não pode ser nula."
                );

        this.resourceManager =
                Objects.requireNonNull(
                        resourceManager,
                        "O ResourceManager não pode ser nulo."
                );

        String stylesheetUrl =
                resourceManager.getStylesheet(
                        HUD_CSS_PATH
                );

        if (!hudLayer
                .getStylesheets()
                .contains(
                        stylesheetUrl
                )) {

            hudLayer
                    .getStylesheets()
                    .add(
                            stylesheetUrl
                    );
        }

        /*
         * HUD de vida.
         *
         * A arte já contém "Herói Neutro" e "HP".
         * A barra e o valor permanecem componentes reais para
         * acompanhar dinamicamente a vida do personagem.
         */
        Image statusImage =
                resourceManager.getImage(
                        HERO_LIFE_HUD_IMAGE
                );

        double statusWidth =
                310;

        double statusHeight =
                statusWidth
                * statusImage.getHeight()
                / statusImage.getWidth();

        ImageView statusBackground =
                new ImageView(
                        statusImage
                );

        statusBackground.setFitWidth(
                statusWidth
        );

        statusBackground.setPreserveRatio(
                true
        );

        statusBackground.setSmooth(
                false
        );

        hpBar =
                new ProgressBar(
                        1.0
                );

        hpBar.getStyleClass().add(
                "hud-progress-bar"
        );

        hpBar.setPrefSize(
                statusWidth * 0.50,
                14
        );

        hpBar.relocate(
                statusWidth * 0.232,
                statusHeight * 0.604
        );

        hpValueLabel =
                new Label(
                        "100 / 100"
                );

        hpValueLabel.getStyleClass().add(
                "hud-hp-value"
        );

        hpValueLabel.relocate(
                statusWidth * 0.765,
                statusHeight * 0.583
        );

        statusCard =
                new Pane(
                        statusBackground,
                        hpBar,
                        hpValueLabel
                );

        statusCard.setPrefSize(
                statusWidth,
                statusHeight
        );

        statusCard.relocate(
                10,
                10
        );

        /*
         * Prompt de interação.
         *
         * A tecla E faz parte da própria arte. O programa escreve
         * apenas a ação e o alvo, permitindo reutilizar o mesmo HUD
         * para conversar, examinar e futuras interações.
         */
        Image interactionImage =
                resourceManager.getImage(
                        PRESS_E_HUD_IMAGE
                );

        double interactionWidth =
                280;

        double interactionHeight =
                interactionWidth
                * interactionImage.getHeight()
                / interactionImage.getWidth();

        ImageView interactionBackground =
                new ImageView(
                        interactionImage
                );

        interactionBackground.setFitWidth(
                interactionWidth
        );

        interactionBackground.setPreserveRatio(
                true
        );

        interactionBackground.setSmooth(
                false
        );

        interactionLabel =
                new Label(
                        "Interagir"
                );

        interactionLabel.getStyleClass().add(
                "hud-interaction-text"
        );

        interactionLabel.setMaxWidth(
                interactionWidth * 0.64
        );

        interactionLabel.relocate(
                interactionWidth * 0.287,
                interactionHeight * 0.37
        );

        interactionPrompt =
                new Pane(
                        interactionBackground,
                        interactionLabel
                );

        interactionPrompt.setPrefSize(
                interactionWidth,
                interactionHeight
        );

        interactionPrompt.setLayoutX(
                (LOGICAL_WIDTH
                        - interactionWidth)
                        / 2.0
        );

        interactionPrompt.setLayoutY(
                500
        );

        interactionPrompt.setVisible(
                false
        );

        /*
         * Banner de conceito aprendido.
         */
        Label bannerHeader =
                new Label(
                        "★ NOVO CONCEITO REGISTRADO ★"
                );

        bannerHeader.getStyleClass().add(
                "hud-concept-header"
        );

        conceptTitleLabel =
                new Label("");

        conceptTitleLabel.getStyleClass().add(
                "hud-concept-title"
        );

        conceptBanner =
                new VBox(
                        bannerHeader,
                        conceptTitleLabel
                );

        conceptBanner.getStyleClass().add(
                "hud-concept-banner"
        );

        conceptBanner.setVisible(
                false
        );

        conceptBanner
                .layoutBoundsProperty()
                .addListener(
                        (obs, oldBounds, newBounds) ->
                                conceptBanner.setLayoutX(
                                        (LOGICAL_WIDTH
                                                - newBounds.getWidth())
                                                / 2.0
                                )
                );

        conceptBanner.setLayoutY(
                30.0
        );

        this.hudLayer
                .getChildren()
                .addAll(
                        statusCard,
                        interactionPrompt,
                        conceptBanner
                );
    }

    /**
     * Atualiza os pontos de vida exibidos e a barra proporcional.
     */
    public void updateHealth(
            float currentHealth,
            float maxHealth) {

        float safeMax =
                maxHealth > 0.0f
                        ? maxHealth
                        : 100.0f;

        float clamped =
                Math.max(
                        0.0f,
                        Math.min(
                                currentHealth,
                                safeMax
                        )
                );

        double progress =
                clamped
                / (double) safeMax;

        hpBar.setProgress(
                progress
        );

        hpValueLabel.setText(
                String.format(
                        "%.0f / %.0f",
                        clamped,
                        safeMax
                )
        );
    }

    /**
     * Exibe o aviso contextual para conversar com um personagem.
     */
    public void showInteractionPrompt(
            String targetName) {

        showInteractionPrompt(
                "Conversar com",
                targetName
        );
    }

    /**
     * Exibe o aviso contextual usando a mesma arte para qualquer ação.
     */
    public void showInteractionPrompt(
            String action,
            String targetName) {

        String safeAction =
                action != null
                && !action.isBlank()
                        ? action
                        : "Interagir com";

        String name =
                targetName != null
                && !targetName.isBlank()
                        ? targetName
                        : "objeto";

        interactionLabel.setText(
                safeAction
                + " "
                + name
        );

        interactionPrompt.setVisible(
                true
        );
    }

    public void hideInteractionPrompt() {

        interactionPrompt.setVisible(
                false
        );
    }

    public void showConceptLearned(
            KnowledgeTopic topic) {

        if (topic == null) {

            return;
        }

        if (conceptAnimation != null) {

            conceptAnimation.stop();
        }

        conceptTitleLabel.setText(
                getTopicDisplayName(
                        topic
                )
        );

        conceptBanner.setOpacity(
                0.0
        );

        conceptBanner.setVisible(
                true
        );

        try {

            resourceManager
                    .getAudioClip(
                            "/audio/sfx_quiz_correct.wav"
                    )
                    .play();

        } catch (Exception e) {

            // Se áudio estiver indisponível, a notificação segue normalmente.
        }

        FadeTransition fadeIn =
                new FadeTransition(
                        Duration.millis(
                                350
                        ),
                        conceptBanner
                );

        fadeIn.setFromValue(
                0.0
        );

        fadeIn.setToValue(
                1.0
        );

        PauseTransition stay =
                new PauseTransition(
                        Duration.seconds(
                                3.5
                        )
                );

        FadeTransition fadeOut =
                new FadeTransition(
                        Duration.millis(
                                600
                        ),
                        conceptBanner
                );

        fadeOut.setFromValue(
                1.0
        );

        fadeOut.setToValue(
                0.0
        );

        fadeOut.setOnFinished(
                e ->
                        conceptBanner.setVisible(
                                false
                        )
        );

        conceptAnimation =
                new SequentialTransition(
                        fadeIn,
                        stay,
                        fadeOut
                );

        conceptAnimation.play();
    }

    private String getTopicDisplayName(
            KnowledgeTopic topic) {

        return switch (topic) {

            case KNOW_CHARGE_SIGNS ->
                    "Sinais das Cargas Elétricas (+ e −)";

            case KNOW_ELECTRIZATION ->
                    "Processos de Eletrização";

            case KNOW_FACTION_CONFLICT ->
                    "Conflito das Facções e Propriedades da Carga";

            case KNOW_COULOMB ->
                    "Lei de Coulomb e Força Eletrostática";

            case KNOW_FIELD ->
                    "Campo Elétrico e Linhas de Força";

            case KNOW_POTENTIAL ->
                    "Potencial Elétrico e Superfícies Equipotenciais";
        };
    }
}
