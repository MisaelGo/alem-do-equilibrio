package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
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

    private static final String HUD_CSS_PATH = "/css/hud.css";

    /*
     * POO 1.1 - Encapsulamento:
     * todos os componentes gráficos e referências internas são privados;
     * outras classes interagem apenas por métodos de apresentação.
     */
    private final Pane hudLayer;
    private final VBox statusCard;
    private final ProgressBar hpBar;
    private final Label hpValueLabel;
    private final HBox interactionPrompt;
    private final Label interactionLabel;
    private final VBox conceptBanner;
    private final Label conceptTitleLabel;

    private SequentialTransition conceptAnimation;

    /**
     * POO 3 - Construtores:
     * Construtor concreto que inicializa todos os elementos visuais do HUD,
     * registra as classes de estilo e aplica a folha CSS centralizada.
     *
     * @param hudLayer camada gráfica onde os componentes visuais são fixados
     * @param resourceManager gerenciador de recursos para obter o CSS compartilhado
     */
    public HUD(Pane hudLayer, ResourceManager resourceManager) {
        this.hudLayer = Objects.requireNonNull(
                hudLayer,
                "A camada hudLayer não pode ser nula."
        );
        Objects.requireNonNull(
                resourceManager,
                "O ResourceManager não pode ser nulo."
        );

        // Aplica folha de estilo CSS do HUD evitando setStyle inline
        String stylesheetUrl = resourceManager.getStylesheet(HUD_CSS_PATH);
        if (!hudLayer.getStylesheets().contains(stylesheetUrl)) {
            hudLayer.getStylesheets().add(stylesheetUrl);
        }

        // --- 1. Cartão de Status do Jogador (HP) ---
        Label playerTitle = new Label("Herói Neutro");
        playerTitle.getStyleClass().add("hud-player-title");

        Label hpIcon = new Label("HP");
        hpIcon.getStyleClass().add("hud-hp-label");

        this.hpBar = new ProgressBar(1.0);
        this.hpBar.getStyleClass().add("hud-progress-bar");

        this.hpValueLabel = new Label("100 / 100");
        this.hpValueLabel.getStyleClass().add("hud-hp-value");

        HBox hpBox = new HBox(hpIcon, hpBar, hpValueLabel);
        hpBox.getStyleClass().add("hud-hp-box");

        this.statusCard = new VBox(playerTitle, hpBox);
        this.statusCard.getStyleClass().add("hud-status-card");
        this.statusCard.relocate(20, 20);

        // --- 2. Prompt Dinâmico de Interação ([E] Interagir com ...) ---
        Label keyBadge = new Label("E");
        keyBadge.getStyleClass().add("hud-interaction-key");

        this.interactionLabel = new Label("Interagir");
        this.interactionLabel.getStyleClass().add("hud-interaction-text");

        this.interactionPrompt = new HBox(keyBadge, interactionLabel);
        this.interactionPrompt.getStyleClass().add("hud-interaction-prompt");
        this.interactionPrompt.setVisible(false);

        // Centraliza horizontalmente o prompt quando a largura for calculada
        this.interactionPrompt.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            interactionPrompt.setLayoutX((1280.0 - newBounds.getWidth()) / 2.0);
        });
        this.interactionPrompt.setLayoutY(500.0);

        // --- 3. Banner de Notificação de Conceito Aprendido ---
        Label bannerHeader = new Label("★ NOVO CONCEITO REGISTRADO ★");
        bannerHeader.getStyleClass().add("hud-concept-header");

        this.conceptTitleLabel = new Label("");
        this.conceptTitleLabel.getStyleClass().add("hud-concept-title");

        this.conceptBanner = new VBox(bannerHeader, conceptTitleLabel);
        this.conceptBanner.getStyleClass().add("hud-concept-banner");
        this.conceptBanner.setVisible(false);

        // Centraliza no topo da tela
        this.conceptBanner.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            conceptBanner.setLayoutX((1280.0 - newBounds.getWidth()) / 2.0);
        });
        this.conceptBanner.setLayoutY(30.0);

        // Adiciona à camada gráfica de interface
        this.hudLayer.getChildren().addAll(
                statusCard,
                interactionPrompt,
                conceptBanner
        );
    }

    /**
     * Atualiza os pontos de vida exibidos e a barra proporcional.
     *
     * @param currentHealth vida atual do herói
     * @param maxHealth vida máxima do herói
     */
    public void updateHealth(float currentHealth, float maxHealth) {
        float safeMax = maxHealth > 0.0f ? maxHealth : 100.0f;
        float clamped = Math.max(0.0f, Math.min(currentHealth, safeMax));
        double progress = clamped / (double) safeMax;

        hpBar.setProgress(progress);
        hpValueLabel.setText(String.format("%.0f / %.0f", clamped, safeMax));
    }

    /**
     * Exibe o aviso contextual de interação para o elemento próximo.
     *
     * @param targetName nome do personagem ou objeto interativo
     */
    public void showInteractionPrompt(String targetName) {
        String name = (targetName != null && !targetName.isBlank()) ? targetName : "Interagir";
        interactionLabel.setText("Conversar com " + name);
        interactionPrompt.setVisible(true);
    }

    /**
     * Oculta o aviso de interação.
     */
    public void hideInteractionPrompt() {
        interactionPrompt.setVisible(false);
    }

    /**
     * Exibe notificação visual de conceito de Física aprendido com animação.
     *
     * @param topic tópico de conhecimento aprendido
     */
    public void showConceptLearned(KnowledgeTopic topic) {
        if (topic == null) {
            return;
        }

        if (conceptAnimation != null) {
            conceptAnimation.stop();
        }

        conceptTitleLabel.setText(getTopicDisplayName(topic));
        conceptBanner.setOpacity(0.0);
        conceptBanner.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), conceptBanner);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition stay = new PauseTransition(Duration.seconds(3.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), conceptBanner);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> conceptBanner.setVisible(false));

        conceptAnimation = new SequentialTransition(fadeIn, stay, fadeOut);
        conceptAnimation.play();
    }

    /**
     * Traduz o enum de tópico para o título legível em português.
     */
    private String getTopicDisplayName(KnowledgeTopic topic) {
        return switch (topic) {
            case KNOW_CHARGE_SIGNS -> "Sinais das Cargas Elétricas (+ e −)";
            case KNOW_ELECTRIZATION -> "Processos de Eletrização";
            case KNOW_COULOMB -> "Lei de Coulomb e Força Eletrostática";
            case KNOW_FIELD -> "Campo Elétrico e Linhas de Força";
            case KNOW_POTENTIAL -> "Potencial Elétrico e Superfícies Equipotenciais";
        };
    }
}
