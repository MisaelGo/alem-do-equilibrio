package com.alemdoequilibrio.core;

import java.util.Objects;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Menu inicial temático de Além do Equilíbrio - Capítulo 1: A Terra das Cargas.
 *
 * Apresenta o cenário atmosférico do mundo de Eletéria, emblema de equilíbrio
 * eletrostático, botões com feedback sonoro e atalhos rápidos de navegação.
 *
 * POO 1.1 - Encapsulamento:
 * Todos os nós visuais, animações e manipuladores são privados.
 *
 * POO 5 - Agregação:
 * Agrega o ResourceManager para acesso seguro a imagens e áudio em cache.
 *
 * POO 7.1 - Sobrecarga de construtores:
 * Suporta inicialização padrão ou com injeção de dependência do ResourceManager.
 */
public final class MainMenuController {

    private static final double LOGICAL_WIDTH = 1280.0;
    private static final double LOGICAL_HEIGHT = 720.0;

    private static final String BACKGROUND_IMAGE = "/images/menu_background.png";
    private static final String SFX_SELECT = "/audio/sfx_menu_select.wav";

    private final ResourceManager resourceManager;
    private final Scene scene;

    /**
     * Construtor padrão que inicializa com nova instância de ResourceManager.
     *
     * @param onPlay ação disparada ao clicar em Jogar
     * @param onRestart ação disparada ao clicar em Reiniciar
     * @param onExit ação disparada ao clicar em Sair
     */
    public MainMenuController(
            Runnable onPlay,
            Runnable onRestart,
            Runnable onExit) {
        this(onPlay, onRestart, onExit, new ResourceManager());
    }

    /**
     * Construtor com injeção explícita do ResourceManager.
     *
     * @param onPlay ação disparada ao clicar em Jogar
     * @param onRestart ação disparada ao clicar em Reiniciar
     * @param onExit ação disparada ao clicar em Sair
     * @param resourceManager gerenciador central de assets
     */
    public MainMenuController(
            Runnable onPlay,
            Runnable onRestart,
            Runnable onExit,
            ResourceManager resourceManager) {

        Objects.requireNonNull(onPlay, "A ação de Jogar não pode ser nula.");
        Objects.requireNonNull(onRestart, "A ação de Reiniciar não pode ser nula.");
        Objects.requireNonNull(onExit, "A ação de Sair não pode ser nula.");
        this.resourceManager = (resourceManager != null) ? resourceManager : new ResourceManager();

        Pane gameRoot = new Pane();
        gameRoot.setPrefSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setMinSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setMaxSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);

        // 1. Camada de Cenário de Fundo (Arte Temática de Eletéria)
        ImageView backgroundView = new ImageView();
        backgroundView.setFitWidth(LOGICAL_WIDTH);
        backgroundView.setFitHeight(LOGICAL_HEIGHT);
        backgroundView.setPreserveRatio(false);
        backgroundView.setSmooth(true);
        loadBackgroundImage(backgroundView);

        // Película semitransparente para realce e foco no conteúdo central
        Rectangle vignette = new Rectangle(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        vignette.setFill(Color.rgb(8, 14, 18, 0.45));

        // 2. Painel Central com Título e Ações
        VBox contentBox = new VBox(18);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPrefWidth(LOGICAL_WIDTH);
        contentBox.setLayoutY(110);

        // Título Principal com Brilho Dourado / Sombra Épica
        Label titleLabel = new Label("ALÉM DO EQUILÍBRIO");
        titleLabel.setStyle(
                "-fx-text-fill: #F5DA93;"
                + "-fx-font-family: 'Georgia', serif;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 48px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.95), 14, 0.4, 0, 4);"
        );

        // 3. Caixa de Opções / Botões
        Button btnPlay = createThemedButton("JOGAR", onPlay, true);
        Button btnRestart = createThemedButton("REINICIAR", onRestart, false);
        Button btnExit = createThemedButton("SAIR", onExit, false);

        VBox menuButtons = new VBox(14, btnPlay, btnRestart, btnExit);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.setPadding(new Insets(10, 0, 0, 0));

        // 4. Rodapé Informativo (Atalhos e Contexto Acadêmico)
        Label hintLabel = new Label("Atalhos: [Enter] Iniciar  |  [Esc] Sair  •  Física III — Eletrostática");
        hintLabel.setStyle(
                "-fx-text-fill: #9BB0A8;"
                + "-fx-font-family: 'Consolas', monospace;"
                + "-fx-font-size: 12px;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.9), 4, 0, 0, 1);"
        );
        hintLabel.setPadding(new Insets(16, 0, 0, 0));

        contentBox.getChildren().addAll(
                titleLabel,
                menuButtons,
                hintLabel
        );

        gameRoot.getChildren().addAll(backgroundView, vignette, contentBox);

        // Viewport responsivo para manter proporção 16:9
        Rectangle clip = new Rectangle(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setClip(clip);

        StackPane screenRoot = new StackPane(gameRoot);
        screenRoot.setStyle("-fx-background-color: #061117;");
        scene = new Scene(screenRoot, LOGICAL_WIDTH, LOGICAL_HEIGHT);

        NumberBinding scale = Bindings.min(
                scene.widthProperty().divide(LOGICAL_WIDTH),
                scene.heightProperty().divide(LOGICAL_HEIGHT)
        );
        gameRoot.scaleXProperty().bind(scale);
        gameRoot.scaleYProperty().bind(scale);

        // Atalhos de teclado
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                playSfx(SFX_SELECT);
                onPlay.run();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                playSfx(SFX_SELECT);
                onExit.run();
                event.consume();
            }
        });
    }

    private Button createThemedButton(String text, Runnable action, boolean isPrimary) {
        Button button = new Button(text);
        button.setPrefSize(320, 52);
        button.setFocusTraversable(true);
        button.setStyle(buttonStyle(false, isPrimary));

        button.setOnMouseEntered(event -> {
            button.setStyle(buttonStyle(true, isPrimary));
            playSfx(SFX_SELECT);
        });

        button.setOnMouseExited(event -> button.setStyle(buttonStyle(false, isPrimary)));

        button.setOnAction(event -> {
            playSfx(SFX_SELECT);
            action.run();
        });

        return button;
    }

    private static String buttonStyle(boolean hovered, boolean isPrimary) {
        String baseBg = isPrimary ? (hovered ? "#406363;" : "#27454A;") : (hovered ? "#2D3F43;" : "#1A2B2F;");
        String borderColor = isPrimary ? (hovered ? "#F5DA93;" : "#D9A441;") : (hovered ? "#BA9350;" : "#755B32;");
        String textColor = hovered ? "#FFFFFF;" : (isPrimary ? "#F5DA93;" : "#DDD7CE;");
        int borderWidth = hovered ? 3 : 2;

        return "-fx-background-color: " + baseBg
                + "-fx-text-fill: " + textColor
                + "-fx-border-color: " + borderColor
                + "-fx-border-width: " + borderWidth + "px;"
                + "-fx-border-radius: 4px;"
                + "-fx-background-radius: 4px;"
                + "-fx-font-family: 'Consolas', monospace;"
                + "-fx-font-size: 21px;"
                + "-fx-font-weight: bold;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 6, 0, 0, 2);";
    }

    private void loadBackgroundImage(ImageView view) {
        try {
            Image img = resourceManager.getImage(BACKGROUND_IMAGE);
            view.setImage(img);
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar " + BACKGROUND_IMAGE + ": " + e.getMessage());
        }
    }

    private void playSfx(String path) {
        try {
            resourceManager.getAudioClip(path).play();
        } catch (Exception ignored) {
        }
    }

    public Scene getScene() {
        return scene;
    }
}
