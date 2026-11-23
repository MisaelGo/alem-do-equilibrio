package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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

    private static final String HERO_PORTRAIT_IMAGE =
            "/images/hero_dialogue.png";

    private static final String PRESS_E_HUD_IMAGE =
            "/images/press_E_hud.png";

    private static final String CONCEPT_IMAGE =
            "/images/conceito_registrado.png";

    private static final String CHECK_SOUND =
            "/audio/check.mp3";

    private static final double LOGICAL_WIDTH =
            1280.0;

    private static final double HP_BAR_MAX_WIDTH =
            190.0;

    private final Pane hudLayer;

    private final ResourceManager
            resourceManager;

    private final Pane statusCard;

    private final Rectangle hpBar;

    private final Label hpValueLabel;

    private final Pane interactionPrompt;

    private final Label interactionLabel;

    private final Pane conceptBanner;

    private final Label conceptTitleLabel;

    private final Label ceramicProgressLabel;

    private final Label ceramicActionLabel;

    private PauseTransition ceramicActionTimer;

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
         * HUD de vida no mapa com a mesma arte e textura pixelada
         * da placa do Herói no menu de combate (BattleView).
         */
        ImageView heroPortraitView =
                new ImageView();

        heroPortraitView.setFitWidth(
                48
        );

        heroPortraitView.setFitHeight(
                48
        );

        heroPortraitView.setPreserveRatio(
                true
        );

        heroPortraitView.setSmooth(
                false
        );

        try {
            heroPortraitView.setImage(
                    resourceManager.getImage(
                            HERO_PORTRAIT_IMAGE
                    )
            );
        } catch (Exception e) {
            try {
                heroPortraitView.setImage(
                        resourceManager.getImage(
                                "/images/hero_idle_right.png"
                        )
                );
            } catch (Exception ignored) {
            }
        }

        StackPane portraitFrame =
                new StackPane(
                        heroPortraitView
                );

        portraitFrame.setPrefSize(
                56,
                56
        );

        portraitFrame.setMinSize(
                56,
                56
        );

        portraitFrame.setMaxSize(
                56,
                56
        );

        portraitFrame.getStyleClass().add(
                "hud-portrait-frame"
        );

        portraitFrame.setBackground(
                BattleView.createDarkInsetTexture(
                        56,
                        56
                )
        );

        Label heroTitleLabel =
                new Label(
                        "Herói"
                );

        heroTitleLabel.getStyleClass().add(
                "hud-player-title"
        );

        Label polarityBadge =
                new Label(
                        "0 NEUTRO"
                );

        polarityBadge.getStyleClass().add(
                "hud-polarity-badge"
        );

        HBox topRow =
                new HBox(
                        10,
                        heroTitleLabel,
                        polarityBadge
                );

        topRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label hpPrefix =
                new Label(
                        "HP"
                );

        hpPrefix.getStyleClass().add(
                "hud-hp-label"
        );

        Rectangle hpTrack =
                new Rectangle(
                        HP_BAR_MAX_WIDTH + 4,
                        14
                );

        hpTrack.getStyleClass().add(
                "hud-hp-track"
        );

        hpBar =
                new Rectangle(
                        HP_BAR_MAX_WIDTH,
                        10
                );

        hpBar.getStyleClass().add(
                "hud-hp-fill"
        );

        StackPane hpStack =
                new StackPane(
                        hpTrack,
                        hpBar
                );

        hpStack.setAlignment(
                Pos.CENTER_LEFT
        );

        hpValueLabel =
                new Label(
                        "100 / 100"
                );

        hpValueLabel.getStyleClass().add(
                "hud-hp-value"
        );

        HBox hpRow =
                new HBox(
                        8,
                        hpPrefix,
                        hpStack,
                        hpValueLabel
                );

        hpRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox infoCol =
                new VBox(
                        6,
                        topRow,
                        hpRow
                );

        infoCol.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox cardBox =
                new HBox(
                        12,
                        portraitFrame,
                        infoCol
                );

        cardBox.setAlignment(
                Pos.CENTER_LEFT
        );

        cardBox.getStyleClass().add(
                "hud-status-card"
        );

        cardBox.setPrefWidth(
                406
        );

        cardBox.setBackground(
                BattleView.createCyanHudTexture(
                        406,
                        80
                )
        );

        statusCard = cardBox;

        statusCard.relocate(
                24,
                14
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
         * A arte contém o título fixo e os símbolos + laterais.
         * Somente o nome do conceito é desenhado dinamicamente.
         * O viewport remove as margens transparentes do PNG.
         */
        Image conceptImage = resourceManager.getImage(CONCEPT_IMAGE);
        // Próximo dos 280 px do aviso "Conversar com Nilo".
        double conceptWidth = 380;
        double conceptHeight = 86;

        ImageView conceptBackground = new ImageView(conceptImage);
        conceptBackground.setViewport(new Rectangle2D(
                conceptImage.getWidth() * 0.04,
                conceptImage.getHeight() * 0.20,
                conceptImage.getWidth() * 0.92,
                conceptImage.getHeight() * 0.61
        ));
        conceptBackground.setFitWidth(conceptWidth);
        conceptBackground.setFitHeight(conceptHeight);
        conceptBackground.setPreserveRatio(false);
        conceptBackground.setSmooth(false);

        conceptTitleLabel = new Label("");
        conceptTitleLabel.getStyleClass().add("hud-concept-title");
        conceptTitleLabel.setAlignment(javafx.geometry.Pos.CENTER);
        conceptTitleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        conceptTitleLabel.setWrapText(true);
        // Área central preserva os símbolos + das laterais da arte.
        conceptTitleLabel.setPrefSize(conceptWidth * 0.82, conceptHeight * 0.50);
        conceptTitleLabel.relocate(conceptWidth * 0.09, conceptHeight * 0.38);

        conceptBanner = new Pane(conceptBackground, conceptTitleLabel);
        conceptBanner.setPrefSize(conceptWidth, conceptHeight);
        conceptBanner.relocate((LOGICAL_WIDTH - conceptWidth) / 2.0, 25);
        conceptBanner.setVisible(false);

        ceramicProgressLabel = new Label();
        ceramicProgressLabel.setStyle(
                "-fx-background-color: rgba(24, 31, 40, 0.90);"
                + "-fx-text-fill: #F9E6AE;"
                + "-fx-border-color: #C4A76A; -fx-border-width: 2px;"
                + "-fx-font-family: 'Consolas'; -fx-font-size: 16px;"
                + "-fx-font-weight: bold; -fx-padding: 9px 12px;");
        // Alinhado ao topo do HUD de vida e próximo à borda direita.
        ceramicProgressLabel.relocate(950, 10);
        ceramicProgressLabel.setVisible(false);

        ceramicActionLabel = new Label();
        ceramicActionLabel.setStyle(
                "-fx-background-color: rgba(24, 31, 40, 0.94);"
                + "-fx-text-fill: white;"
                + "-fx-border-color: #C4A76A; -fx-border-width: 2px;"
                + "-fx-font-family: 'Consolas'; -fx-font-size: 19px;"
                + "-fx-font-weight: bold; -fx-padding: 10px 14px;");
        ceramicActionLabel.setWrapText(true);
        ceramicActionLabel.setMaxWidth(580);
        ceramicActionLabel.relocate(360, 115);
        ceramicActionLabel.setVisible(false);

        this.hudLayer
                .getChildren()
                .addAll(
                        statusCard,
                        interactionPrompt,
                        conceptBanner,
                        ceramicProgressLabel,
                        ceramicActionLabel
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

        hpBar.setWidth(
                HP_BAR_MAX_WIDTH * progress
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

    /** Indicador persistente das nove viagens de instalação. */
    public void showCeramicProgress(int placed, int total, boolean carrying) {
        ceramicProgressLabel.setText(
                "CERÂMICA: faltam " + (total - placed)
                + (carrying ? " | Com placa" : "")
        );
        ceramicProgressLabel.setVisible(true);
    }

    public void hideCeramicProgress() {
        ceramicProgressLabel.setVisible(false);
        if (ceramicActionTimer != null) {
            ceramicActionTimer.stop();
        }
        ceramicActionLabel.setVisible(false);
    }

    /** Mensagem curta de coleta, instalação ou tentativa inválida. */
    public void showCeramicAction(String message) {
        if (ceramicActionTimer != null) {
            ceramicActionTimer.stop();
        }
        ceramicActionLabel.setText(message);
        ceramicActionLabel.setVisible(true);
        ceramicActionTimer = new PauseTransition(Duration.seconds(3.2));
        ceramicActionTimer.setOnFinished(event ->
                ceramicActionLabel.setVisible(false));
        ceramicActionTimer.play();
    }

    /**
     * Reproduz o efeito de conquista para conceitos, habilidades,
     * checkpoints e a última placa da passarela.
     */
    public void playCompletionSound() {
        try {
            resourceManager.getAudioClip(CHECK_SOUND).play(0.70);
        } catch (Exception exception) {
            // A conquista continua mesmo se o áudio falhar.
        }
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

        // O conhecimento só é registrado uma vez; usa o check.
        playCompletionSound();

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
