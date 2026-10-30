package com.alemdoequilibrio.game;

import java.util.List;
import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Interface gráfica de batalha inspirada no estilo Fear & Hunger.
 *
 * Apresenta o confronto clássico com o Herói posicionado à esquerda e o
 * Inimigo à direita, acompanhados de telegrafia narrativa superior, caixas
 * austeras de comandos e monitoramento de cargas com acessibilidade visual.
 *
 * POO 1.1 - Encapsulamento:
 * Todos os nós visuais, animações e controles internos são privados.
 *
 * POO 3 - Construtores:
 * Inicialização robusta vinculada ao BattleController e ResourceManager.
 *
 * POO 5 - Agregação:
 * Agrega BattleController e ResourceManager sem alterar suas regras internas.
 */
public class BattleView {

    private static final double LOGICAL_WIDTH = 1280;
    private static final double LOGICAL_HEIGHT = 720;
    private static final float PLAYER_DAMAGE = 20.0f;
    private static final double HERO_CENTER_X = 300.0;
    private static final double HERO_CENTER_Y = 278.0;
    private static final double ENEMY_CENTER_X = 980.0;
    private static final double ENEMY_CENTER_Y = 285.0;

    private static final String DEFAULT_ACTION_HINT =
            "Atalhos: [1-4] Comandos  |  [Q/E] Alternar Polaridade (livre)  |  Passe o mouse para detalhes";

    /* Caminhos de áudio disponíveis em resources */
    private static final String SFX_MENU_SELECT = "/audio/sfx_menu_select.wav";
    private static final String SFX_HIT = "/audio/sfx_hit.wav";
    private static final String SFX_DISCHARGE = "/audio/sfx_electric_discharge.wav";
    private static final String SFX_SHOCK = "/audio/electric_shock.mp3";
    private static final String SFX_THUNDER = "/audio/estrondo_thunder.mp3";
    private static final String SFX_POSITIVE = "/audio/sfx_quiz_correct.wav";
    private static final String SFX_VICTORY = "/audio/sfx_battle_victory.wav";
    private static final String MUSIC_BATTLE = "/audio/zone_neutral_suspense.mp3";

    /*
     * POO 5 - Agregação
     */
    private final BattleController battleController;
    private final ResourceManager resourceManager;

    private final Pane gameRoot;
    private final StackPane screenRoot;
    private final Scene scene;

    /* Componentes da telegrafia superior */
    private final Label telegraphTitleLabel;
    private final Label telegraphTextLabel;

    /* Componentes do palco de combate: Herói (Esquerda) */
    private final Label heroNameLabel;
    private final Label heroPolarityBadge;
    private final ComboBox<ChargeType> chargeSelector;
    private final Rectangle heroHpBar;
    private final Label heroHpText;
    private final ImageView heroSpriteView;
    private final VBox heroStageBox;

    /* Componentes do palco de combate: Inimigo (Direita) */
    private final Label enemyNameLabel;
    private final Label enemyPolarityBadge;
    private final Label enemyPhaseLabel;
    private final Rectangle enemyHpBar;
    private final Label enemyHpText;
    private final ImageView enemySpriteView;
    private final VBox enemyStageBox;

    /* Componentes do painel de comandos (Inferior Esquerdo) */
    private final Label commandHeader;
    private final VBox mainCommandList;
    private final VBox abilityCommandList;
    private final Button btnAttack;
    private final Button btnAbilities;
    private final Button btnAnalyze;
    private final Button btnGrounding;
    private final Label actionHintLabel;

    /* Componentes do painel de status/registro (Inferior Direito) */
    private final Label stanceStatusLabel;
    private final Label physicsInsightLabel;
    private final Label roundCounterLabel;

    /* Efeitos de impacto visual e controle de áudio/estado */
    private final Rectangle damageFlash;
    private final MediaPlayer battleMusicPlayer;
    private float maxEnemyHealth = 50.0f;
    private final float maxHeroHealth;
    private boolean groundingActiveThisRound;
    private boolean endBannerShown;

    /*
     * POO 7.1 - Sobrecarga de construtores
     */
    public BattleView(BattleController battleController) {
        this(battleController, new ResourceManager());
    }

    public BattleView(BattleController battleController, ResourceManager resourceManager) {
        this.battleController = Objects.requireNonNull(battleController, "BattleController não pode ser nulo.");
        this.resourceManager = (resourceManager != null) ? resourceManager : new ResourceManager();

        maxEnemyHealth = Math.max(1.0f, battleController.getEnemy().getHealth());
        maxHeroHealth = Math.max(1.0f, battleController.getHero().getMaxHealth());

        gameRoot = new Pane();
        gameRoot.setPrefSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setMinSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setMaxSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.getStyleClass().add("battle-root");

        // 1. Caixa de Telegrafia e Narrativa Superior (Fear & Hunger Style)
        telegraphTitleLabel = new Label("> REGISTRO DE CAMPO ELETROSTÁTICO");
        telegraphTitleLabel.getStyleClass().add("telegraph-title");

        telegraphTextLabel = new Label(buildInitialTelegraphText());
        telegraphTextLabel.getStyleClass().add("telegraph-text");
        telegraphTextLabel.setWrapText(true);

        VBox telegraphBox = new VBox(6, telegraphTitleLabel, telegraphTextLabel);
        telegraphBox.getStyleClass().add("telegraph-box");
        telegraphBox.setLayoutX(40);
        telegraphBox.setLayoutY(20);
        telegraphBox.setPrefWidth(LOGICAL_WIDTH - 80);
        telegraphBox.setPrefHeight(88);

        // 2. PALCO DE CONFRONTO: Herói à Esquerda, Inimigo à Direita

        // --- LADO DO HERÓI (ESQUERDA) ---
        heroNameLabel = new Label("HERÓI");
        heroNameLabel.getStyleClass().add("hero-name");

        heroPolarityBadge = new Label("0 NEUTRO");
        heroPolarityBadge.getStyleClass().addAll("polarity-badge", "polarity-neutral");

        chargeSelector = new ComboBox<>();
        chargeSelector.getItems().addAll(ChargeType.values());
        chargeSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ChargeType chargeType) {
                return chargeType == null ? "" : chargeType.getDescription()
                        + " (" + chargeType.getSymbol() + ")";
            }

            @Override
            public ChargeType fromString(String value) {
                for (ChargeType chargeType : ChargeType.values()) {
                    if (toString(chargeType).equals(value)) {
                        return chargeType;
                    }
                }
                throw new IllegalArgumentException("Carga desconhecida: " + value);
            }
        });
        chargeSelector.setValue(battleController.getHero().getChargeType());
        chargeSelector.getStyleClass().add("charge-combo");
        chargeSelector.setOnAction(event -> {
            ChargeType selectedCharge = chargeSelector.getValue();
            if (selectedCharge != null
                    && selectedCharge != battleController.getHero().getChargeType()
                    && !battleController.isBattleFinished()
                    && battleController.getCurrentTurn() == BattleController.BattleTurn.PLAYER_TURN) {
                applyHeroChargeSelection(selectedCharge);
            }
        });

        HBox heroHeaderRow = new HBox(10, heroNameLabel, heroPolarityBadge, chargeSelector);
        heroHeaderRow.setAlignment(Pos.CENTER);

        heroHpBar = new Rectangle(240, 14);
        heroHpBar.getStyleClass().add("hp-fill-hero");

        Rectangle heroHpTrack = new Rectangle(244, 18);
        heroHpTrack.getStyleClass().add("hp-track");

        StackPane heroHpStack = new StackPane(heroHpTrack, heroHpBar);
        heroHpStack.setAlignment(Pos.CENTER_LEFT);

        heroHpText = new Label("100 / 100");
        heroHpText.getStyleClass().add("hp-text");

        HBox heroHpRow = new HBox(10, heroHpStack, heroHpText);
        heroHpRow.setAlignment(Pos.CENTER);

        heroSpriteView = new ImageView();
        heroSpriteView.setFitWidth(150);
        heroSpriteView.setFitHeight(150);
        heroSpriteView.setPreserveRatio(true);
        loadHeroSprite();

        // Animação de respiração/idle sutil do herói
        TranslateTransition heroBobbing = new TranslateTransition(Duration.seconds(1.4), heroSpriteView);
        heroBobbing.setFromY(-4);
        heroBobbing.setToY(4);
        heroBobbing.setAutoReverse(true);
        heroBobbing.setCycleCount(TranslateTransition.INDEFINITE);
        heroBobbing.setInterpolator(Interpolator.EASE_BOTH);
        heroBobbing.play();

        heroStageBox = new VBox(8, heroHeaderRow, heroHpRow, heroSpriteView);
        heroStageBox.setAlignment(Pos.CENTER);
        heroStageBox.setLayoutX(80);
        heroStageBox.setLayoutY(130);
        heroStageBox.setPrefWidth(440);

        // --- TENSÃO DE CAMPO CENTRAL (VS / CAMPO) ---
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-text-fill: #4a4556; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: Georgia;");
        Label vsLines = new Label("⚡  E  ⚡");
        vsLines.setStyle("-fx-text-fill: #D9A441; -fx-font-size: 13px; -fx-font-family: Consolas;");
        VBox clashBox = new VBox(4, vsLabel, vsLines);
        clashBox.setAlignment(Pos.CENTER);
        clashBox.setLayoutX((LOGICAL_WIDTH - 120) / 2.0);
        clashBox.setLayoutY(220);
        clashBox.setPrefWidth(120);

        // --- LADO DO INIMIGO (DIREITA) ---
        enemyNameLabel = new Label(battleController.getEnemy().getName());
        enemyNameLabel.getStyleClass().add("enemy-name");

        enemyPolarityBadge = createPolarityBadge(battleController.getEnemy());

        enemyPhaseLabel = new Label();
        enemyPhaseLabel.setStyle("-fx-text-fill: #D9A441; -fx-font-size: 11px; -fx-font-family: Consolas;");

        HBox enemyHeaderRow = new HBox(10, enemyNameLabel, enemyPolarityBadge, enemyPhaseLabel);
        enemyHeaderRow.setAlignment(Pos.CENTER);

        enemyHpBar = new Rectangle(240, 14);
        enemyHpBar.getStyleClass().add("hp-fill-enemy");

        Rectangle enemyHpTrack = new Rectangle(244, 18);
        enemyHpTrack.getStyleClass().add("hp-track");

        StackPane enemyHpStack = new StackPane(enemyHpTrack, enemyHpBar);
        enemyHpStack.setAlignment(Pos.CENTER_LEFT);

        enemyHpText = new Label();
        enemyHpText.getStyleClass().add("hp-text");

        HBox enemyHpRow = new HBox(10, enemyHpStack, enemyHpText);
        enemyHpRow.setAlignment(Pos.CENTER);

        enemySpriteView = new ImageView();
        enemySpriteView.setFitWidth(180);
        enemySpriteView.setFitHeight(180);
        enemySpriteView.setPreserveRatio(true);
        loadEnemySprite();

        // Animação de flutuação sutil do inimigo
        TranslateTransition enemyFloating = new TranslateTransition(Duration.seconds(1.8), enemySpriteView);
        enemyFloating.setFromY(-6);
        enemyFloating.setToY(6);
        enemyFloating.setAutoReverse(true);
        enemyFloating.setCycleCount(TranslateTransition.INDEFINITE);
        enemyFloating.setInterpolator(Interpolator.EASE_BOTH);
        enemyFloating.play();

        enemyStageBox = new VBox(8, enemyHeaderRow, enemyHpRow, enemySpriteView);
        enemyStageBox.setAlignment(Pos.CENTER);
        enemyStageBox.setLayoutX(LOGICAL_WIDTH - 520);
        enemyStageBox.setLayoutY(130);
        enemyStageBox.setPrefWidth(440);

        // 3. PAINEL DE COMANDOS (Inferior Esquerdo)
        commandHeader = new Label("> COMANDOS DE COMBATE");
        commandHeader.getStyleClass().add("panel-header");

        actionHintLabel = new Label(DEFAULT_ACTION_HINT);
        actionHintLabel.getStyleClass().add("command-hint-text");
        actionHintLabel.setWrapText(true);

        btnAttack = createCommandButton(
                "[1]  ⚔  ATACAR",
                "Desfere um golpe de neutralização direta causando 20 de dano base."
        );
        btnAttack.setOnAction(e -> handlePlayerAttack());

        btnAbilities = createCommandButton(
                "[2]  ⚡  MODULAÇÕES DE CAMPO",
                "Abre o grimório de habilidades eletrostáticas aprendidas com o mercador Íon."
        );
        btnAbilities.setOnAction(e -> showAbilitySubmenu());

        btnAnalyze = createCommandButton(
                "[3]  🔍  ANALISAR CAMPO",
                "Examina a carga, o campo e as vulnerabilidades atuais do inimigo (não consome turno)."
        );
        btnAnalyze.setOnAction(e -> handleAnalyzeAction());

        btnGrounding = createCommandButton(
                "[4]  🛡  ATERRAMENTO CONDUTOR",
                "Postura defensiva: conecta-se ao solo para escoar cargas e aguarda a ação inimiga."
        );
        btnGrounding.setOnAction(e -> handleGroundingAction());

        mainCommandList = new VBox(6, btnAttack, btnAbilities, btnAnalyze, btnGrounding);

        // Submenu de Habilidades
        abilityCommandList = new VBox(6);
        abilityCommandList.setVisible(false);
        abilityCommandList.setManaged(false);

        VBox commandBox = new VBox(6, commandHeader, mainCommandList, abilityCommandList, actionHintLabel);
        commandBox.getStyleClass().add("panel-box");
        commandBox.setLayoutX(40);
        commandBox.setLayoutY(470);
        commandBox.setPrefWidth(585);
        commandBox.setPrefHeight(225);

        // 4. PAINEL DE ANÁLISE E ESTADO (Inferior Direito)
        Label statusHeader = new Label("> ANÁLISE DE ESTADO E FÍSICA");
        statusHeader.getStyleClass().add("panel-header");

        stanceStatusLabel = new Label("Postura: Equilíbrio Eletrostático Normal");
        stanceStatusLabel.getStyleClass().add("hero-condition-text");

        physicsInsightLabel = new Label(buildPhysicsInsightText());
        physicsInsightLabel.setStyle("-fx-text-fill: #CFC9BE; -fx-font-family: Georgia; -fx-font-size: 14px;");
        physicsInsightLabel.setWrapText(true);
        VBox.setVgrow(physicsInsightLabel, Priority.ALWAYS);

        roundCounterLabel = new Label("Rodada: 1  |  Turno: SEU TURNO");
        roundCounterLabel.setStyle("-fx-text-fill: #D9A441; -fx-font-family: Consolas; -fx-font-size: 13px; -fx-font-weight: bold;");

        VBox statusBox = new VBox(8, statusHeader, stanceStatusLabel, physicsInsightLabel, roundCounterLabel);
        statusBox.getStyleClass().add("panel-box");
        statusBox.setLayoutX(655);
        statusBox.setLayoutY(470);
        statusBox.setPrefWidth(585);
        statusBox.setPrefHeight(225);

        // 5. Overlay de Flash de Dano (Screen Flash)
        damageFlash = new Rectangle(LOGICAL_WIDTH, LOGICAL_HEIGHT, Color.rgb(220, 20, 20, 0.0));
        damageFlash.setMouseTransparent(true);

        gameRoot.getChildren().addAll(
                telegraphBox,
                heroStageBox,
                clashBox,
                enemyStageBox,
                commandBox,
                statusBox,
                damageFlash
        );

        // Viewport e binding responsivo
        Rectangle viewportClip = new Rectangle(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setClip(viewportClip);

        screenRoot = new StackPane(gameRoot);
        scene = new Scene(screenRoot, LOGICAL_WIDTH, LOGICAL_HEIGHT);

        NumberBinding scale = Bindings.min(
                scene.widthProperty().divide(LOGICAL_WIDTH),
                scene.heightProperty().divide(LOGICAL_HEIGHT)
        );

        gameRoot.scaleXProperty().bind(scale);
        gameRoot.scaleYProperty().bind(scale);

        battleMusicPlayer = initBattleMusic();
        setupKeyboardShortcuts();
        applyStylesheet();
        updateView();
        playEncounterIntroEffects();
    }

    private MediaPlayer initBattleMusic() {
        try {
            Media media = resourceManager.getMedia(MUSIC_BATTLE);
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            boolean isBoss = battleController.getEnemy() instanceof BossEnemy;
            player.setVolume(isBoss ? 0.36 : 0.24);
            player.setRate(isBoss ? 1.06 : 1.0);
            player.play();
            return player;
        } catch (Exception e) {
            return null;
        }
    }

    private void playEncounterIntroEffects() {
        if (battleController.getEnemy() instanceof BossEnemy) {
            playSfxModulated(SFX_THUNDER, 0.55, 0.0, 1.0);
            playSfxModulated(SFX_SHOCK, 0.45, 0.35, 1.1);
            triggerScreenShake(8, 6);
        } else {
            playSfxModulated(SFX_DISCHARGE, 0.40, 0.30, 1.15);
        }
    }

    /**
     * Encerra a trilha sonora de batalha ao retornar para a exploração.
     */
    public void stopAudio() {
        if (battleMusicPlayer != null) {
            try {
                battleMusicPlayer.stop();
            } catch (Exception e) {
                // Ignora falhas de encerramento de áudio
            }
        }
    }

    private void setupKeyboardShortcuts() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (battleController.isBattleFinished()
                    || battleController.getCurrentTurn() != BattleController.BattleTurn.PLAYER_TURN) {
                return;
            }

            KeyCode code = event.getCode();

            if (code == KeyCode.Q) {
                cycleHeroCharge(-1);
                event.consume();
                return;
            }
            if (code == KeyCode.E) {
                cycleHeroCharge(1);
                event.consume();
                return;
            }

            if (abilityCommandList.isVisible()) {
                List<Ability> learned = battleController.getHero().getAbilityBook().getAbilities();
                if ((code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) && learned.size() >= 1) {
                    playSfx(SFX_MENU_SELECT);
                    handlePlayerAbility(learned.get(0));
                    event.consume();
                } else if ((code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) && learned.size() >= 2) {
                    playSfx(SFX_MENU_SELECT);
                    handlePlayerAbility(learned.get(1));
                    event.consume();
                } else if ((code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) && learned.size() >= 3) {
                    playSfx(SFX_MENU_SELECT);
                    handlePlayerAbility(learned.get(2));
                    event.consume();
                } else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4
                        || code == KeyCode.ESCAPE || code == KeyCode.BACK_SPACE) {
                    playSfx(SFX_MENU_SELECT);
                    showMainMenu();
                    event.consume();
                }
            } else {
                if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                    playSfx(SFX_MENU_SELECT);
                    handlePlayerAttack();
                    event.consume();
                } else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                    playSfx(SFX_MENU_SELECT);
                    showAbilitySubmenu();
                    event.consume();
                } else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                    playSfx(SFX_MENU_SELECT);
                    handleAnalyzeAction();
                    event.consume();
                } else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) {
                    playSfx(SFX_MENU_SELECT);
                    handleGroundingAction();
                    event.consume();
                }
            }
        });
    }

    private void cycleHeroCharge(int step) {
        ChargeType[] values = ChargeType.values();
        ChargeType current = battleController.getHero().getChargeType();
        int nextIndex = (current.ordinal() + step + values.length) % values.length;
        applyHeroChargeSelection(values[nextIndex]);
    }

    private void applyHeroChargeSelection(ChargeType selectedCharge) {
        battleController.setHeroChargeType(selectedCharge);
        if (chargeSelector.getValue() != selectedCharge) {
            chargeSelector.setValue(selectedCharge);
        }
        updateHeroPolarityBadge(selectedCharge);
        if (abilityCommandList.isVisible()) {
            buildAbilitySubmenu();
        }
        double pitch = switch (selectedCharge) {
            case POSITIVE -> 1.25;
            case NEUTRAL -> 1.0;
            case NEGATIVE -> 0.82;
        };
        playSfxModulated(SFX_MENU_SELECT, 0.75, -0.35, pitch);
        playPolarityShiftPulse(selectedCharge);
        telegraphTextLabel.setText("Polaridade do Herói reconfigurada para "
                + selectedCharge.getDescription() + " (" + selectedCharge.getSymbol() + ").");
    }

    private void playSfx(String path) {
        playSfxModulated(path, 0.85, 0.0, 1.0);
    }

    private void playSfxModulated(String path, double volume, double balance, double rate) {
        try {
            AudioClip clip = resourceManager.getAudioClip(path);
            clip.play(volume, balance, rate, 0.0, 0);
        } catch (Exception e) {
            // Silencioso se áudio não estiver disponível
        }
    }

    private Button createCommandButton(String text, String hintText) {
        Button btn = new Button(text);
        btn.getStyleClass().add("command-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(btn, Priority.ALWAYS);
        btn.addEventHandler(javafx.event.ActionEvent.ACTION, e -> playSfx(SFX_MENU_SELECT));
        if (hintText != null && !hintText.isBlank()) {
            btn.setOnMouseEntered(e -> {
                actionHintLabel.setText(hintText);
                playSfxModulated(SFX_MENU_SELECT, 0.22, -0.2, 1.45);
            });
            btn.setOnMouseExited(e -> actionHintLabel.setText(DEFAULT_ACTION_HINT));
            btn.focusedProperty().addListener((obs, oldVal, focused) -> {
                if (focused) {
                    actionHintLabel.setText(hintText);
                }
            });
        }
        return btn;
    }

    private void applyStylesheet() {
        try {
            String css = resourceManager.getStylesheet("/css/battle.css");
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar battle.css pelo ResourceManager: " + e.getMessage());
            var url = getClass().getResource("/css/battle.css");
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            }
        }
    }

    private void loadHeroSprite() {
        try {
            Image img = resourceManager.getImage("/images/hero_idle_right.png");
            heroSpriteView.setImage(img);
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar sprite do herói: " + e.getMessage());
        }
    }

    private void loadEnemySprite() {
        String imagePath;
        Enemy enemy = battleController.getEnemy();

        if (enemy instanceof BossEnemy) {
            imagePath = "/images/enemy_battle_boss.png";
        } else if (enemy instanceof CorruptedDipole) {
            imagePath = "/images/enemy_battle_dipole.png";
        } else if (enemy instanceof UnstableConductor) {
            imagePath = "/images/enemy_battle_conductor.png";
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            imagePath = (uncharged.getChargeType() == ChargeType.POSITIVE)
                    ? "/images/enemy_battle_charge_pos.png"
                    : "/images/enemy_battle_charge_neg.png";
        } else if (enemy instanceof ChargedEnemy charged) {
            imagePath = (charged.getChargeType() == ChargeType.POSITIVE)
                    ? "/images/enemy_battle_charge_pos.png"
                    : "/images/enemy_battle_charge_neg.png";
        } else {
            imagePath = "/images/enemy_battle_wandering_spark.png";
        }

        try {
            Image img = resourceManager.getImage(imagePath);
            enemySpriteView.setImage(img);
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar sprite do inimigo: " + e.getMessage());
        }
    }

    private Label createPolarityBadge(Enemy enemy) {
        Label badge = new Label();
        badge.getStyleClass().add("polarity-badge");
        applyEnemyPolarityBadgeState(badge, enemy);
        return badge;
    }

    private void applyEnemyPolarityBadgeState(Label badge, Enemy enemy) {
        badge.getStyleClass().removeAll(
                "polarity-positive",
                "polarity-negative",
                "polarity-neutral",
                "polarity-rupture"
        );

        if (enemy instanceof BossEnemy boss) {
            switch (boss.getPhase()) {
                case POLARITY -> {
                    if (boss.getChargeType() == ChargeType.POSITIVE) {
                        badge.setText("+ RUPTURA");
                        badge.getStyleClass().add("polarity-positive");
                    } else {
                        badge.setText("− RUPTURA");
                        badge.getStyleClass().add("polarity-negative");
                    }
                }
                case FIELD -> {
                    String arrow = boss.getFieldDirection() == BossEnemy.FieldDirection.LEFT ? "←" : "→";
                    badge.setText(arrow + " CAMPO");
                    badge.getStyleClass().add("polarity-rupture");
                }
                case POTENTIAL -> {
                    badge.setText(boss.isPotentialShieldActive() ? "◎ EQUIPOTENCIAL" : "✦ POTENCIAL");
                    badge.getStyleClass().add("polarity-rupture");
                }
            }
        } else if (enemy instanceof CorruptedDipole dipole) {
            if (dipole.getChargeType() == ChargeType.POSITIVE) {
                badge.setText("+ DIPOLO (±)");
                badge.getStyleClass().add("polarity-positive");
            } else {
                badge.setText("− DIPOLO (±)");
                badge.getStyleClass().add("polarity-negative");
            }
        } else if (enemy instanceof ChargedEnemy charged) {
            ChargeType type = charged.getChargeType();
            if (type == ChargeType.POSITIVE) {
                badge.setText("+ PRÓTON");
                badge.getStyleClass().add("polarity-positive");
            } else if (type == ChargeType.NEGATIVE) {
                badge.setText("− ELÉTRON");
                badge.getStyleClass().add("polarity-negative");
            } else {
                badge.setText("0 NEUTRO");
                badge.getStyleClass().add("polarity-neutral");
            }
        } else if (enemy instanceof UnstableConductor conductor) {
            if (conductor.hasStoredEnergy()) {
                badge.setText("⚡ SOBRECARGA");
                badge.getStyleClass().add("polarity-rupture");
            } else {
                badge.setText("0 CONDUTOR");
                badge.getStyleClass().add("polarity-neutral");
            }
        } else {
            badge.setText("~ OSCILANTE");
            badge.getStyleClass().add("polarity-neutral");
        }
    }

    private void updateHeroPolarityBadge(ChargeType type) {
        heroPolarityBadge.getStyleClass().removeAll("polarity-positive", "polarity-negative", "polarity-neutral");
        if (type == ChargeType.POSITIVE) {
            heroPolarityBadge.setText("+ PRÓTON");
            heroPolarityBadge.getStyleClass().add("polarity-positive");
        } else if (type == ChargeType.NEGATIVE) {
            heroPolarityBadge.setText("− ELÉTRON");
            heroPolarityBadge.getStyleClass().add("polarity-negative");
        } else {
            heroPolarityBadge.setText("0 NEUTRO");
            heroPolarityBadge.getStyleClass().add("polarity-neutral");
        }
    }

    private String buildInitialTelegraphText() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof BossEnemy) {
            return "O Guardião da Ruptura desperta! As singularidades opostas colapsam o equilíbrio e o ar é saturado de alta voltagem!";
        } else if (enemy instanceof WanderingSpark) {
            return "A Faísca Errante crepita com descargas desordenadas no ar. O ambiente ressoa com instabilidade dielétrica!";
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            String sign = (uncharged.getChargeType() == ChargeType.POSITIVE) ? "positivas (+)" : "negativas (−)";
            return "Uma concentração densa de cargas " + sign + " distorce o campo elétrico à sua frente!";
        } else if (enemy instanceof CorruptedDipole) {
            return "Dois polos opostos em colapso atraem e repelem o solo simultaneamente. As linhas de campo formam arcos perigosos!";
        } else if (enemy instanceof UnstableConductor) {
            return "O Condutor Instável absorve elétrons livres do solo, alternando entre ciclos de indução e descargas violentas!";
        }
        return "Uma presença anômala de matéria eletrizada barra o seu caminho. Prepare sua modulação de cargas!";
    }

    private String buildPhysicsInsightText() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof BossEnemy boss) {
            return switch (boss.getPhase()) {
                case POLARITY -> "Fase 1 — Polaridade (" + boss.getChargeType().getSymbol()
                        + "): Cargas de mesmo sinal se repelem. Use Pulso de Repulsão com a mesma polaridade para interromper o ataque!";
                case FIELD -> "Fase 2 — Campo Elétrico ("
                        + (boss.getFieldDirection() == BossEnemy.FieldDirection.RIGHT ? "→ Favorável" : "← Oposto")
                        + "): O Impulso de Campo causa 26 de dano quando alinhado ao vetor (→ Direita)!";
                case POTENTIAL -> "Fase 3 — Potencial Elétrico: "
                        + (boss.isPotentialShieldActive()
                                ? "Superfície equipotencial ativa (reduz dano recebido em 50%). Estabilize-se com Escudo Equipotencial!"
                                : "Escudo do Guardião dissipado neste turno! Aproveite para atacar com força total!");
            };
        } else if (enemy instanceof CorruptedDipole dipole) {
            return "Dipolo Corrompido (Polo atual: " + dipole.getChargeType().getSymbol()
                    + "): Alterna entre + e − após cada ataque. Sincronize sua polaridade antes de usar Pulso de Repulsão.";
        } else if (enemy instanceof ChargedEnemy charged) {
            if (charged.getChargeType() == ChargeType.POSITIVE) {
                return "Carga Positiva (+): As linhas de força divergem radialmente. Use polaridade Positiva (+) com Pulso de Repulsão para repelir seu avanço.";
            } else if (charged.getChargeType() == ChargeType.NEGATIVE) {
                return "Carga Negativa (−): As linhas de força convergem para o centro. Vulnerável ao bônus de 26 de dano do Impulso de Campo!";
            }
        } else if (enemy instanceof UnstableConductor conductor) {
            if (conductor.hasStoredEnergy()) {
                return "Condutor Sobrecarregado (⚡): Energia acumulada! Descarregará o dobro de dano e causará Instabilidade. Use Escudo Equipotencial!";
            }
            return "Condutor Instável: Acumula energia em um turno e libera uma descarga dupla no turno seguinte.";
        }
        return "Matéria Neutra / Oscilante: Cargas em equilíbrio ou transição rápida. Ataques diretos dissipam sua coesão.";
    }

    private void handlePlayerAttack() {
        if (battleController.isBattleFinished()) {
            return;
        }

        groundingActiveThisRound = false;
        setButtonsDisabled(true);

        // Som de disparo elétrico partindo da esquerda (Herói) e impacto à direita (Inimigo)
        playSfxModulated(SFX_DISCHARGE, 0.70, -0.40, 1.28);
        Timeline impactSfx = new Timeline(new KeyFrame(Duration.millis(140), ev -> {
            playSfxModulated(SFX_HIT, 0.95, 0.45, 1.0);
            playSfxModulated(SFX_SHOCK, 0.45, 0.45, 1.35);
        }));
        impactSfx.play();

        playHeroAttackAnimation();
        triggerScreenShake(7, 4);
        playEnemyHitReaction(false);

        float enemyHpBefore = battleController.getEnemy().getHealth();
        battleController.performPlayerAttack(PLAYER_DAMAGE);
        float damageDealt = Math.max(0.0f, enemyHpBefore - battleController.getEnemy().getHealth());
        showFloatingDamage(damageDealt, false, false);

        updateView();

        if (battleController.isBattleFinished()) {
            return;
        }

        telegraphTextLabel.setText("Você desfere um ataque de neutralização direta (-" + Math.round(damageDealt) + " HP)! O inimigo prepara uma resposta de carga!");

        Timeline enemyTurnTimer = new Timeline(new KeyFrame(Duration.millis(950), e -> {
            executeEnemyTurn();
        }));
        enemyTurnTimer.play();
    }

    private void handlePlayerAbility(Ability ability) {
        if (battleController.isBattleFinished()) {
            return;
        }

        groundingActiveThisRound = false;
        battleController.getHero().getAbilityBook().equipAbility(ability);

        setButtonsDisabled(true);

        float enemyHpBefore = battleController.getEnemy().getHealth();
        battleController.performPlayerAbility();
        float damageDealt = Math.max(0.0f, enemyHpBefore - battleController.getEnemy().getHealth());
        boolean isBonusHit = damageDealt >= 20.0f;

        if (ability instanceof EquipotentialShield) {
            playShieldAnimation();
            playSfxModulated(SFX_POSITIVE, 0.78, -0.35, 0.92);
            playSfxModulated(SFX_DISCHARGE, 0.50, -0.35, 0.80);
        } else if (ability instanceof RepulsionPulse) {
            playRepulsionPulseAnimation();
            playSfxModulated(SFX_DISCHARGE, 0.85, -0.25, 1.15);
            playSfxModulated(SFX_SHOCK, 0.60, 0.35, 1.20);
            playSfxModulated(SFX_HIT, 0.85, 0.45, 1.10);
            triggerScreenShake(8, 4);
            playEnemyHitReaction(true);
        } else if (ability instanceof FieldImpulse) {
            playFieldImpulseAnimation(isBonusHit);
            playSfxModulated(SFX_SHOCK, 0.90, 0.20, isBonusHit ? 0.95 : 1.15);
            playSfxModulated(SFX_HIT, 1.0, 0.45, isBonusHit ? 0.88 : 1.0);
            if (isBonusHit) {
                playSfxModulated(SFX_THUNDER, 0.55, 0.30, 1.35);
                triggerScreenShake(11, 6);
            } else {
                triggerScreenShake(8, 4);
            }
            playEnemyHitReaction(isBonusHit);
        } else {
            playAbilityAnimation();
            playSfxModulated(SFX_DISCHARGE, 0.85, 0.0, 1.0);
            triggerScreenShake(7, 4);
            playEnemyHitReaction(false);
        }

        if (damageDealt > 0.0f) {
            showFloatingDamage(damageDealt, false, isBonusHit);
        }

        showMainMenu();
        updateView();

        if (battleController.isBattleFinished()) {
            return;
        }

        telegraphTextLabel.setText("Você canaliza [" + ability.getName() + "]"
                + (damageDealt > 0
                        ? (isBonusHit ? " em RESSONÂNCIA DE CAMPO causando -" : " causando -") + Math.round(damageDealt) + " HP!"
                        : " estabilizando o potencial ao seu redor!"));

        Timeline enemyTurnTimer = new Timeline(new KeyFrame(Duration.millis(980), e -> {
            executeEnemyTurn();
        }));
        enemyTurnTimer.play();
    }

    private void executeEnemyTurn() {
        if (battleController.isBattleFinished()) {
            return;
        }

        float hpBefore = battleController.getHero().getHealth();

        battleController.performEnemyTurn();

        float hpAfter = battleController.getHero().getHealth();
        float damageTaken = Math.max(0.0f, hpBefore - hpAfter);
        Enemy enemy = battleController.getEnemy();

        if (battleController.wasLastEnemyTurnInterrupted()) {
            playInterruptedAnimation();
            telegraphTextLabel.setText("Ataque de " + enemy.getName()
                    + " foi INTERROMPIDO pela força de repulsão eletrostática de mesma polaridade!");
            playSfxModulated(SFX_POSITIVE, 0.90, 0.35, 1.12);
            playSfxModulated(SFX_DISCHARGE, 0.65, 0.35, 1.40);
        } else if (damageTaken <= 0.0f && enemy instanceof UnstableConductor conductor && conductor.hasStoredEnergy()) {
            playEnemyChargeUpAnimation();
            playSfxModulated(SFX_SHOCK, 0.70, 0.45, 0.82);
            playSfxModulated(SFX_DISCHARGE, 0.65, 0.45, 0.75);
            telegraphTextLabel.setText(enemy.getName()
                    + " acumulou cargas elétricas em sua superfície! Prepare-se para uma descarga intensa no próximo turno!");
        } else {
            boolean heavyHit = damageTaken >= 10.0f || enemy instanceof BossEnemy;
            playSfxModulated(SFX_DISCHARGE, 0.75, 0.45, 0.92);
            Timeline hitSfx = new Timeline(new KeyFrame(Duration.millis(150), ev -> {
                playSfxModulated(SFX_HIT, 0.95, -0.45, heavyHit ? 0.85 : 1.0);
                playSfxModulated(SFX_SHOCK, heavyHit ? 0.75 : 0.45, -0.40, 1.05);
                if (heavyHit) {
                    playSfxModulated(SFX_THUNDER, 0.42, -0.25, 1.25);
                }
            }));
            hitSfx.play();

            playEnemyAttackAnimation(heavyHit);
            triggerScreenShake(heavyHit ? 10 : 7, heavyHit ? 6 : 4);
            playPlayerDamageFlash();
            playHeroHitReaction();

            showFloatingDamage(damageTaken, true, heavyHit);

            telegraphTextLabel.setText(enemy.getName()
                    + " descarrega seu potencial elétrico e causou "
                    + Math.round(damageTaken) + " de dano!");
        }

        groundingActiveThisRound = false;
        updateView();
        if (!battleController.isBattleFinished()) {
            setButtonsDisabled(false);
        }
    }

    private void handleAnalyzeAction() {
        Enemy enemy = battleController.getEnemy();
        playAnalyzeAnimation();
        playSfxModulated(SFX_MENU_SELECT, 0.75, 0.30, 1.35);
        playSfxModulated(SFX_POSITIVE, 0.45, 0.35, 1.50);

        StringBuilder analysis = new StringBuilder("ANÁLISE DE CAMPO: ");

        if (enemy instanceof BossEnemy boss) {
            analysis.append("Chefe Ruptura [Fase ").append(boss.getPhase().ordinal() + 1).append("]. ")
                    .append(boss.getPhase() == BossEnemy.Phase.POLARITY
                            ? "Carga atual (" + boss.getChargeType().getSymbol() + "). Iguale sua polaridade e use Pulso de Repulsão! "
                            : "")
                    .append(boss.getPhase() == BossEnemy.Phase.FIELD
                            ? "Campo apontando para " + (boss.getFieldDirection() == BossEnemy.FieldDirection.RIGHT ? "→ Direita (Impulso de Campo causa 26 de dano!)" : "← Esquerda") + ". "
                            : "")
                    .append(boss.getPhase() == BossEnemy.Phase.POTENTIAL
                            ? (boss.isPotentialShieldActive() ? "Escudo equipotencial ativo (50% menos dano)! " : "Escudo enfraquecido neste turno! ")
                            : "");
        } else if (enemy instanceof CorruptedDipole dipole) {
            analysis.append("Dipolo Corrompido [Polo atual: ").append(dipole.getChargeType().getSymbol()).append("]. ")
                    .append("Inverte o sinal após cada turno. Combine sua carga ou use Impulso quando estiver negativo (−).");
        } else if (enemy instanceof ChargedEnemy charged) {
            analysis.append("Carga ").append(charged.getChargeType().getDescription())
                    .append(" [").append(charged.getChargeType().getSymbol()).append("]. ")
                    .append("Cargas de mesmo sinal repelem (Pulso de Repulsão); cargas negativas sofrem 26 de dano do Impulso de Campo.");
        } else if (enemy instanceof UnstableConductor conductor) {
            analysis.append("Condutor Instável [")
                    .append(conductor.hasStoredEnergy() ? "SOBRECARREGADO — Descarga iminente!" : "Acumulando carga")
                    .append("]. Use Escudo Equipotencial antes da descarga para reduzir o dano pela metade e evitar Instabilidade.");
        } else {
            analysis.append("Natureza oscilante. Não possui polaridade estável no momento. Ataques diretos são eficazes.");
        }

        telegraphTextLabel.setText(analysis.toString());
    }

    private void handleGroundingAction() {
        if (battleController.isBattleFinished()) {
            return;
        }

        groundingActiveThisRound = true;
        stanceStatusLabel.setText("Postura: Aterramento Condutor ao Solo");
        telegraphTextLabel.setText("Você estabelece um caminho condutor com o solo! As cargas excedentes encontram escoamento seguro enquanto você aguarda o impacto.");
        playGroundingAnimation();
        playSfxModulated(SFX_DISCHARGE, 0.75, -0.35, 0.72);

        setButtonsDisabled(true);

        // Avança o turno no BattleController com 0 de dano (postura puramente defensiva)
        battleController.performPlayerAttack(0.0f);

        if (battleController.isBattleFinished()) {
            updateView();
            return;
        }

        Timeline enemyTurnTimer = new Timeline(new KeyFrame(Duration.millis(900), e -> {
            executeEnemyTurn();
        }));
        enemyTurnTimer.play();
    }

    private void showAbilitySubmenu() {
        buildAbilitySubmenu();
        commandHeader.setText("> MODULAÇÕES DE CAMPO (ESC PARA VOLTAR)");
        mainCommandList.setVisible(false);
        mainCommandList.setManaged(false);
        abilityCommandList.setVisible(true);
        abilityCommandList.setManaged(true);
    }

    private void buildAbilitySubmenu() {
        abilityCommandList.getChildren().clear();

        List<Ability> learnedAbilities = battleController.getHero().getAbilityBook().getAbilities();

        if (learnedAbilities.isEmpty()) {
            Label emptyNotice = new Label("Nenhuma habilidade aprendida ainda.\nConverse com os NPCs e realize as provas com o Mercador Íon!");
            emptyNotice.setStyle("-fx-text-fill: #DDD7CE; -fx-font-family: Georgia; -fx-font-size: 13px;");
            abilityCommandList.getChildren().add(emptyNotice);
        } else {
            for (int i = 0; i < learnedAbilities.size(); i++) {
                Ability ability = learnedAbilities.get(i);
                String synergyTag = evaluateAbilitySynergyTag(ability);
                String buttonLabel = "[" + (i + 1) + "]  ⚡  " + ability.getName()
                        + (synergyTag.isEmpty() ? "" : "   " + synergyTag);
                String hint = ability.getDescription()
                        + (synergyTag.isEmpty() ? "" : "  (" + synergyTag + ")");

                Button btnSkill = createCommandButton(buttonLabel, hint);
                if (!synergyTag.isEmpty()) {
                    btnSkill.getStyleClass().add("command-btn-synergy");
                }
                btnSkill.setOnAction(e -> handlePlayerAbility(ability));
                abilityCommandList.getChildren().add(btnSkill);
            }
        }

        Button btnBack = createCommandButton(
                "[ESC]  ↩  Retornar aos Comandos Principais",
                "Volta para a lista principal de ações sem consumir o seu turno."
        );
        btnBack.setOnAction(e -> showMainMenu());
        abilityCommandList.getChildren().add(btnBack);
    }

    private String evaluateAbilitySynergyTag(Ability ability) {
        Enemy enemy = battleController.getEnemy();
        Hero hero = battleController.getHero();

        if (ability instanceof RepulsionPulse) {
            ChargeType heroCharge = hero.getChargeType();
            ChargeType pulseCharge = (heroCharge == ChargeType.NEUTRAL) ? ChargeType.POSITIVE : heroCharge;
            boolean matchesCharged = enemy instanceof ChargedEnemy charged
                    && charged.getChargeType() == pulseCharge;
            boolean matchesBoss = enemy instanceof BossEnemy boss
                    && boss.getPhase() == BossEnemy.Phase.POLARITY
                    && boss.getChargeType() == pulseCharge;
            if (matchesCharged || matchesBoss) {
                return "★ SINERGIA: REPELIR (" + pulseCharge.getSymbol() + ")";
            }
        } else if (ability instanceof FieldImpulse) {
            boolean negativeTarget = (enemy instanceof ChargedEnemy charged
                    && charged.getChargeType() == ChargeType.NEGATIVE)
                    || (enemy instanceof BossEnemy boss
                            && boss.getPhase() == BossEnemy.Phase.POLARITY
                            && boss.getChargeType() == ChargeType.NEGATIVE);
            boolean favorableField = enemy instanceof BossEnemy boss
                    && boss.getPhase() == BossEnemy.Phase.FIELD
                    && boss.getFieldDirection() == BossEnemy.FieldDirection.RIGHT;
            if (negativeTarget || favorableField) {
                return "★ BÔNUS DE CAMPO (26 DANO)";
            }
        } else if (ability instanceof EquipotentialShield) {
            if (hero.isUnstable()) {
                return "★ REMOVE INSTABILIDADE";
            }
            if ((enemy instanceof UnstableConductor conductor && conductor.hasStoredEnergy())
                    || (enemy instanceof BossEnemy boss && boss.getPhase() == BossEnemy.Phase.POTENTIAL)) {
                return "★ PROTEÇÃO RECOMENDADA";
            }
        }
        return "";
    }

    private void showMainMenu() {
        commandHeader.setText("> COMANDOS DE COMBATE");
        actionHintLabel.setText(DEFAULT_ACTION_HINT);
        abilityCommandList.setVisible(false);
        abilityCommandList.setManaged(false);
        mainCommandList.setVisible(true);
        mainCommandList.setManaged(true);
    }

    /* =========================================================================
       SISTEMA DE ANIMAÇÕES DE COMBATE E EFEITOS VISUAIS (VFX)
       ========================================================================= */

    private void triggerScreenShake(double amplitude, int cycles) {
        TranslateTransition shakeX = new TranslateTransition(Duration.millis(42), gameRoot);
        shakeX.setFromX(-amplitude);
        shakeX.setToX(amplitude);
        shakeX.setCycleCount(cycles);
        shakeX.setAutoReverse(true);
        shakeX.setOnFinished(e -> gameRoot.setTranslateX(0));
        shakeX.play();
    }

    private void playPlayerDamageFlash() {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(damageFlash.fillProperty(), Color.rgb(195, 35, 35, 0.42))),
                new KeyFrame(Duration.millis(210), new KeyValue(damageFlash.fillProperty(), Color.rgb(195, 35, 35, 0.0)))
        );
        flash.play();
    }

    private void playHeroHitReaction() {
        TranslateTransition knockback = new TranslateTransition(Duration.millis(110), heroSpriteView);
        knockback.setFromX(0);
        knockback.setToX(-26);
        knockback.setAutoReverse(true);
        knockback.setCycleCount(2);

        RotateTransition tilt = new RotateTransition(Duration.millis(110), heroSpriteView);
        tilt.setFromAngle(0);
        tilt.setToAngle(-9);
        tilt.setAutoReverse(true);
        tilt.setCycleCount(2);

        FadeTransition blink = new FadeTransition(Duration.millis(75), heroSpriteView);
        blink.setFromValue(1.0);
        blink.setToValue(0.35);
        blink.setCycleCount(4);
        blink.setAutoReverse(true);

        ParallelTransition hit = new ParallelTransition(knockback, tilt, blink);
        hit.setOnFinished(e -> {
            heroSpriteView.setTranslateX(0);
            heroSpriteView.setRotate(0);
            heroSpriteView.setOpacity(1.0);
        });
        hit.play();
    }

    private void playEnemyHitReaction(boolean strongKnockback) {
        double pushDistance = strongKnockback ? 42 : 26;
        TranslateTransition knockback = new TranslateTransition(Duration.millis(120), enemySpriteView);
        knockback.setFromX(0);
        knockback.setToX(pushDistance);
        knockback.setAutoReverse(true);
        knockback.setCycleCount(2);

        RotateTransition tilt = new RotateTransition(Duration.millis(120), enemySpriteView);
        tilt.setFromAngle(0);
        tilt.setToAngle(strongKnockback ? 11 : 7);
        tilt.setAutoReverse(true);
        tilt.setCycleCount(2);

        ScaleTransition squash = new ScaleTransition(Duration.millis(110), enemySpriteView);
        squash.setFromX(1.0);
        squash.setFromY(1.0);
        squash.setToX(0.88);
        squash.setToY(1.10);
        squash.setAutoReverse(true);
        squash.setCycleCount(2);

        FadeTransition ft = new FadeTransition(Duration.millis(75), enemySpriteView);
        ft.setFromValue(1.0);
        ft.setToValue(0.28);
        ft.setCycleCount(4);
        ft.setAutoReverse(true);

        ParallelTransition reaction = new ParallelTransition(knockback, tilt, squash, ft);
        reaction.setOnFinished(e -> {
            enemySpriteView.setTranslateX(0);
            enemySpriteView.setRotate(0);
            enemySpriteView.setScaleX(1.0);
            enemySpriteView.setScaleY(1.0);
            enemySpriteView.setOpacity(1.0);
        });
        reaction.play();
    }

    private Color resolveHeroChargeColor() {
        return switch (battleController.getHero().getChargeType()) {
            case POSITIVE -> Color.web("#E27B72");
            case NEGATIVE -> Color.web("#6EA3D1");
            case NEUTRAL -> Color.web("#D9A441");
        };
    }

    private void playPolarityShiftPulse(ChargeType chargeType) {
        Color pulseColor = switch (chargeType) {
            case POSITIVE -> Color.web("#B4473D");
            case NEGATIVE -> Color.web("#356D9A");
            case NEUTRAL -> Color.web("#607D72");
        };
        playImpactBurst(HERO_CENTER_X, HERO_CENTER_Y, pulseColor);
    }

    private void playHeroAttackAnimation() {
        Color attackColor = resolveHeroChargeColor();

        // 1. Preparação (wind-up) + Bote rápido (lunge) + Retorno
        TranslateTransition windUp = new TranslateTransition(Duration.millis(65), heroSpriteView);
        windUp.setFromX(0);
        windUp.setToX(-14);

        TranslateTransition lunge = new TranslateTransition(Duration.millis(115), heroSpriteView);
        lunge.setFromX(-14);
        lunge.setToX(68);
        lunge.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition recover = new TranslateTransition(Duration.millis(180), heroSpriteView);
        recover.setFromX(68);
        recover.setToX(0);
        recover.setInterpolator(Interpolator.EASE_BOTH);

        RotateTransition lungeTilt = new RotateTransition(Duration.millis(115), heroSpriteView);
        lungeTilt.setFromAngle(0);
        lungeTilt.setToAngle(7);
        lungeTilt.setAutoReverse(true);
        lungeTilt.setCycleCount(2);

        Glow glow = new Glow(0.65);
        heroSpriteView.setEffect(glow);

        SequentialTransition bodyMotion = new SequentialTransition(
                windUp,
                new ParallelTransition(lunge, lungeTilt),
                recover
        );
        bodyMotion.setOnFinished(e -> {
            heroSpriteView.setTranslateX(0);
            heroSpriteView.setRotate(0);
            heroSpriteView.setEffect(null);
        });
        bodyMotion.play();

        // 2. Raio elétrico em zig-zag + Corte diagonal + Explosão de faíscas no inimigo
        playLightningBolt(true, attackColor, Color.WHITE);
        playImpactSlash(ENEMY_CENTER_X - 30, ENEMY_CENTER_Y, attackColor);
        playImpactBurst(ENEMY_CENTER_X - 30, ENEMY_CENTER_Y, attackColor);
        playSparksExplosion(ENEMY_CENTER_X - 30, ENEMY_CENTER_Y, attackColor, 9);
    }

    private void playEnemyAttackAnimation(boolean heavyHit) {
        Color attackColor = heavyHit ? Color.web("#A488C2") : Color.web("#E05353");

        TranslateTransition windUp = new TranslateTransition(Duration.millis(70), enemySpriteView);
        windUp.setFromX(0);
        windUp.setToX(16);

        TranslateTransition lunge = new TranslateTransition(Duration.millis(125), enemySpriteView);
        lunge.setFromX(16);
        lunge.setToX(-68);
        lunge.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition recover = new TranslateTransition(Duration.millis(190), enemySpriteView);
        recover.setFromX(-68);
        recover.setToX(0);
        recover.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition motion = new SequentialTransition(windUp, lunge, recover);
        motion.setOnFinished(e -> enemySpriteView.setTranslateX(0));
        motion.play();

        playLightningBolt(false, attackColor, Color.web("#FFD6D6"));
        playImpactSlash(HERO_CENTER_X + 35, HERO_CENTER_Y, attackColor);
        playImpactBurst(HERO_CENTER_X + 35, HERO_CENTER_Y, attackColor);
        playSparksExplosion(HERO_CENTER_X + 35, HERO_CENTER_Y, attackColor, heavyHit ? 12 : 8);
    }

    private void playRepulsionPulseAnimation() {
        ChargeType heroCharge = battleController.getHero().getChargeType();
        Color pulseColor = (heroCharge == ChargeType.NEGATIVE)
                ? Color.web("#6EA3D1")
                : Color.web("#E27B72");

        playAbilityAnimation();

        // Anéis divergentes (Regra 5.2 do GDD: VFX de repulsão devem divergir)
        for (int i = 0; i < 3; i++) {
            Circle ring = new Circle(22, Color.TRANSPARENT);
            ring.setStroke(pulseColor);
            ring.setStrokeWidth(3.5);
            ring.setLayoutX(HERO_CENTER_X + 80 + (i * 150));
            ring.setLayoutY(HERO_CENTER_Y);
            ring.setMouseTransparent(true);
            gameRoot.getChildren().add(ring);

            ScaleTransition expand = new ScaleTransition(Duration.millis(420), ring);
            expand.setFromX(0.3);
            expand.setFromY(0.3);
            expand.setToX(2.8);
            expand.setToY(2.8);

            FadeTransition fade = new FadeTransition(Duration.millis(420), ring);
            fade.setFromValue(0.95);
            fade.setToValue(0.0);

            ParallelTransition anim = new ParallelTransition(expand, fade);
            anim.setDelay(Duration.millis(i * 75));
            anim.setOnFinished(e -> gameRoot.getChildren().remove(ring));
            anim.play();
        }

        playLightningBolt(true, pulseColor, Color.WHITE);
        playSparksExplosion(ENEMY_CENTER_X - 30, ENEMY_CENTER_Y, pulseColor, 12);
    }

    private void playFieldImpulseAnimation(boolean bonusActive) {
        Color fieldColor = bonusActive ? Color.web("#D9A441") : Color.web("#6EA3D1");

        playAbilityAnimation();

        // Vetores direcionais de linhas de campo (Regra 5.2 do GDD: Linhas de Campo)
        for (int row = -1; row <= 1; row++) {
            double y = HERO_CENTER_Y + (row * 44);
            Line vectorLine = new Line(0, 0, 95, 0);
            vectorLine.setStroke(fieldColor);
            vectorLine.setStrokeWidth(bonusActive ? 4.0 : 2.5);
            vectorLine.setStrokeLineCap(StrokeLineCap.ROUND);
            vectorLine.setLayoutX(HERO_CENTER_X + 70);
            vectorLine.setLayoutY(y);
            vectorLine.setMouseTransparent(true);
            gameRoot.getChildren().add(vectorLine);

            TranslateTransition flow = new TranslateTransition(Duration.millis(320), vectorLine);
            flow.setByX(430);
            FadeTransition fade = new FadeTransition(Duration.millis(320), vectorLine);
            fade.setFromValue(0.95);
            fade.setToValue(0.0);

            ParallelTransition vectorAnim = new ParallelTransition(flow, fade);
            vectorAnim.setOnFinished(e -> gameRoot.getChildren().remove(vectorLine));
            vectorAnim.play();
        }

        playLightningBolt(true, fieldColor, Color.WHITE);
        playImpactSlash(ENEMY_CENTER_X - 25, ENEMY_CENTER_Y, fieldColor);
        playImpactBurst(ENEMY_CENTER_X - 25, ENEMY_CENTER_Y, fieldColor);
        playSparksExplosion(ENEMY_CENTER_X - 25, ENEMY_CENTER_Y, fieldColor, bonusActive ? 15 : 10);
    }

    private void playAbilityAnimation() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(240), heroSpriteView);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.18);
        scale.setToY(1.18);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        Glow glow = new Glow(0.0);
        heroSpriteView.setEffect(glow);
        Timeline energy = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
                new KeyFrame(Duration.millis(180), new KeyValue(glow.levelProperty(), 0.9)),
                new KeyFrame(Duration.millis(520), new KeyValue(glow.levelProperty(), 0.0))
        );
        energy.setOnFinished(event -> heroSpriteView.setEffect(null));
        new ParallelTransition(scale, energy).play();
        playAbilityWave(Color.web("#6EA3D1"));
    }

    private void playShieldAnimation() {
        Glow glow = new Glow(0.0);
        heroSpriteView.setEffect(glow);

        ScaleTransition scale = new ScaleTransition(Duration.millis(420), heroSpriteView);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.12);
        scale.setToY(1.12);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        Timeline shield = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
                new KeyFrame(Duration.millis(220), new KeyValue(glow.levelProperty(), 1.0)),
                new KeyFrame(Duration.millis(700), new KeyValue(glow.levelProperty(), 0.25))
        );
        shield.setOnFinished(event -> heroSpriteView.setEffect(null));
        new ParallelTransition(scale, shield).play();

        // Curvas equipotenciais fechadas ao redor do Herói (Regra 5.2 do GDD)
        for (int ringIdx = 0; ringIdx < 2; ringIdx++) {
            Circle equipotentialRing = new Circle(68 + (ringIdx * 22), Color.rgb(63, 173, 109, 0.10));
            equipotentialRing.setStroke(Color.web("#66D18F"));
            equipotentialRing.setStrokeWidth(3.0);
            equipotentialRing.setLayoutX(HERO_CENTER_X);
            equipotentialRing.setLayoutY(HERO_CENTER_Y);
            equipotentialRing.setMouseTransparent(true);
            gameRoot.getChildren().add(equipotentialRing);

            ScaleTransition pulse = new ScaleTransition(Duration.millis(620), equipotentialRing);
            pulse.setFromX(0.65);
            pulse.setFromY(0.65);
            pulse.setToX(1.25);
            pulse.setToY(1.25);

            FadeTransition fade = new FadeTransition(Duration.millis(620), equipotentialRing);
            fade.setFromValue(0.95);
            fade.setToValue(0.0);

            ParallelTransition ringAnim = new ParallelTransition(pulse, fade);
            ringAnim.setDelay(Duration.millis(ringIdx * 110));
            ringAnim.setOnFinished(e -> gameRoot.getChildren().remove(equipotentialRing));
            ringAnim.play();
        }
    }

    private void playEnemyChargeUpAnimation() {
        ScaleTransition swell = new ScaleTransition(Duration.millis(240), enemySpriteView);
        swell.setFromX(1.0);
        swell.setFromY(1.0);
        swell.setToX(1.16);
        swell.setToY(1.16);
        swell.setAutoReverse(true);
        swell.setCycleCount(2);
        swell.play();

        playImpactBurst(ENEMY_CENTER_X, ENEMY_CENTER_Y, Color.web("#A488C2"));
        playSparksExplosion(ENEMY_CENTER_X, ENEMY_CENTER_Y, Color.web("#D9A441"), 10);
    }

    private void playAnalyzeAnimation() {
        Line scanBeam = new Line(-95, 0, 95, 0);
        scanBeam.setStroke(Color.web("#D9A441"));
        scanBeam.setStrokeWidth(3.0);
        scanBeam.setLayoutX(ENEMY_CENTER_X);
        scanBeam.setLayoutY(ENEMY_CENTER_Y - 80);
        scanBeam.setMouseTransparent(true);
        gameRoot.getChildren().add(scanBeam);

        TranslateTransition sweep = new TranslateTransition(Duration.millis(420), scanBeam);
        sweep.setByY(160);
        FadeTransition fade = new FadeTransition(Duration.millis(420), scanBeam);
        fade.setFromValue(0.95);
        fade.setToValue(0.15);

        ParallelTransition scan = new ParallelTransition(sweep, fade);
        scan.setOnFinished(e -> gameRoot.getChildren().remove(scanBeam));
        scan.play();
    }

    private void playGroundingAnimation() {
        for (int i = -1; i <= 1; i++) {
            Line groundLine = new Line(0, 0, i * 28, 65);
            groundLine.setStroke(Color.web("#92B0A5"));
            groundLine.setStrokeWidth(3.0);
            groundLine.setLayoutX(HERO_CENTER_X + (i * 18));
            groundLine.setLayoutY(HERO_CENTER_Y + 20);
            groundLine.setMouseTransparent(true);
            gameRoot.getChildren().add(groundLine);

            FadeTransition fade = new FadeTransition(Duration.millis(460), groundLine);
            fade.setFromValue(0.95);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> gameRoot.getChildren().remove(groundLine));
            fade.play();
        }
        playImpactBurst(HERO_CENTER_X, HERO_CENTER_Y + 70, Color.web("#607D72"));
    }

    private void playInterruptedAnimation() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), enemySpriteView);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(0.84);
        pulse.setToY(0.84);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();

        playImpactBurst(790, 285, Color.web("#6EA3D1"));
        playSparksExplosion(790, 285, Color.web("#6EA3D1"), 10);
    }

    /**
     * Desenha um raio elétrico em zig-zag com núcleo brilhante que cruza a arena.
     */
    private void playLightningBolt(boolean fromHero, Color outerColor, Color coreColor) {
        double startX = fromHero ? 390 : 890;
        double direction = fromHero ? 1.0 : -1.0;

        Polyline outerBolt = new Polyline(
                0.0, 0.0,
                direction * 35.0, -18.0,
                direction * 70.0, 14.0,
                direction * 110.0, -22.0,
                direction * 145.0, 12.0,
                direction * 185.0, -10.0,
                direction * 220.0, 0.0
        );
        outerBolt.setStroke(outerColor);
        outerBolt.setStrokeWidth(6.5);
        outerBolt.setStrokeLineCap(StrokeLineCap.ROUND);
        outerBolt.setEffect(new DropShadow(14, outerColor));
        outerBolt.setLayoutX(startX);
        outerBolt.setLayoutY(285);
        outerBolt.setMouseTransparent(true);

        Polyline coreBolt = new Polyline(outerBolt.getPoints().stream().mapToDouble(Double::doubleValue).toArray());
        coreBolt.setStroke(coreColor);
        coreBolt.setStrokeWidth(2.2);
        coreBolt.setStrokeLineCap(StrokeLineCap.ROUND);
        coreBolt.setLayoutX(startX);
        coreBolt.setLayoutY(285);
        coreBolt.setMouseTransparent(true);

        gameRoot.getChildren().addAll(outerBolt, coreBolt);

        TranslateTransition travelOuter = new TranslateTransition(Duration.millis(210), outerBolt);
        travelOuter.setByX(direction * 310);
        TranslateTransition travelCore = new TranslateTransition(Duration.millis(210), coreBolt);
        travelCore.setByX(direction * 310);

        FadeTransition fadeOuter = new FadeTransition(Duration.millis(240), outerBolt);
        fadeOuter.setFromValue(1.0);
        fadeOuter.setToValue(0.0);
        FadeTransition fadeCore = new FadeTransition(Duration.millis(240), coreBolt);
        fadeCore.setFromValue(1.0);
        fadeCore.setToValue(0.0);

        ParallelTransition boltAnim = new ParallelTransition(travelOuter, travelCore, fadeOuter, fadeCore);
        boltAnim.setOnFinished(event -> gameRoot.getChildren().removeAll(outerBolt, coreBolt));
        boltAnim.play();
    }

    /**
     * Cria um corte cruzado luminoso sobre o alvo no momento do impacto.
     */
    private void playImpactSlash(double x, double y, Color color) {
        Line slash1 = new Line(-34, -34, 34, 34);
        slash1.setStroke(Color.WHITE);
        slash1.setStrokeWidth(4.0);
        slash1.setStrokeLineCap(StrokeLineCap.ROUND);
        slash1.setEffect(new DropShadow(12, color));
        slash1.setLayoutX(x);
        slash1.setLayoutY(y);
        slash1.setMouseTransparent(true);

        Line slash2 = new Line(-28, 28, 28, -28);
        slash2.setStroke(color);
        slash2.setStrokeWidth(3.5);
        slash2.setStrokeLineCap(StrokeLineCap.ROUND);
        slash2.setLayoutX(x);
        slash2.setLayoutY(y);
        slash2.setMouseTransparent(true);

        gameRoot.getChildren().addAll(slash1, slash2);

        ScaleTransition scale1 = new ScaleTransition(Duration.millis(220), slash1);
        scale1.setFromX(0.4);
        scale1.setFromY(0.4);
        scale1.setToX(1.35);
        scale1.setToY(1.35);

        FadeTransition fade1 = new FadeTransition(Duration.millis(220), slash1);
        fade1.setFromValue(1.0);
        fade1.setToValue(0.0);

        FadeTransition fade2 = new FadeTransition(Duration.millis(220), slash2);
        fade2.setFromValue(0.95);
        fade2.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(scale1, fade1, fade2);
        anim.setOnFinished(e -> gameRoot.getChildren().removeAll(slash1, slash2));
        anim.play();
    }

    /**
     * Dispara partículas de faísca radialmente a partir do ponto de impacto.
     */
    private void playSparksExplosion(double x, double y, Color color, int sparkCount) {
        for (int i = 0; i < sparkCount; i++) {
            double angle = (2.0 * Math.PI * i) / sparkCount;
            double distance = 42.0 + ((i % 3) * 18.0);
            double dx = Math.cos(angle) * distance;
            double dy = Math.sin(angle) * distance;

            Circle spark = new Circle((i % 2 == 0) ? 4.0 : 2.8, (i % 3 == 0) ? Color.WHITE : color);
            spark.setEffect(new DropShadow(8, color));
            spark.setLayoutX(x);
            spark.setLayoutY(y);
            spark.setMouseTransparent(true);
            gameRoot.getChildren().add(spark);

            TranslateTransition move = new TranslateTransition(Duration.millis(320), spark);
            move.setByX(dx);
            move.setByY(dy);
            move.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(320), spark);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition sparkAnim = new ParallelTransition(move, fade);
            sparkAnim.setOnFinished(e -> gameRoot.getChildren().remove(spark));
            sparkAnim.play();
        }
    }

    private void playImpactBurst(double x, double y, Color color) {
        Circle burst = new Circle(14, Color.TRANSPARENT);
        burst.setStroke(color);
        burst.setStrokeWidth(4.5);
        burst.setOpacity(0.95);
        burst.setLayoutX(x);
        burst.setLayoutY(y);
        burst.setMouseTransparent(true);
        gameRoot.getChildren().add(burst);

        ScaleTransition expand = new ScaleTransition(Duration.millis(300), burst);
        expand.setFromX(0.35);
        expand.setFromY(0.35);
        expand.setToX(2.5);
        expand.setToY(2.5);
        FadeTransition fade = new FadeTransition(Duration.millis(300), burst);
        fade.setFromValue(0.95);
        fade.setToValue(0.0);
        ParallelTransition animation = new ParallelTransition(expand, fade);
        animation.setOnFinished(event -> gameRoot.getChildren().remove(burst));
        animation.play();
    }

    private void playAbilityWave(Color color) {
        Circle wave = new Circle(42, Color.TRANSPARENT);
        wave.setStroke(color);
        wave.setStrokeWidth(3.5);
        wave.setOpacity(0.8);
        wave.setLayoutX(640);
        wave.setLayoutY(285);
        wave.setMouseTransparent(true);
        gameRoot.getChildren().add(wave);

        ScaleTransition expand = new ScaleTransition(Duration.millis(520), wave);
        expand.setFromX(0.25);
        expand.setFromY(0.25);
        expand.setToX(2.6);
        expand.setToY(2.6);
        FadeTransition fade = new FadeTransition(Duration.millis(520), wave);
        fade.setFromValue(0.8);
        fade.setToValue(0.0);
        ParallelTransition animation = new ParallelTransition(expand, fade);
        animation.setOnFinished(event -> gameRoot.getChildren().remove(wave));
        animation.play();
    }

    private void showFloatingDamage(float damage, boolean onHero, boolean criticalOrHeavy) {
        if (damage <= 0.0f) {
            return;
        }

        String text = (criticalOrHeavy && !onHero ? "⚡ -" : "-") + Math.round(damage) + " HP";
        Label damageLabel = new Label(text);
        damageLabel.getStyleClass().add(onHero ? "damage-number-hero" : "damage-number-enemy");
        damageLabel.setLayoutX(onHero ? 265 : 935);
        damageLabel.setLayoutY(242);
        gameRoot.getChildren().add(damageLabel);

        ScaleTransition pop = new ScaleTransition(Duration.millis(180), damageLabel);
        pop.setFromX(0.7);
        pop.setFromY(0.7);
        pop.setToX(criticalOrHeavy ? 1.28 : 1.08);
        pop.setToY(criticalOrHeavy ? 1.28 : 1.08);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);

        TranslateTransition rise = new TranslateTransition(Duration.millis(880), damageLabel);
        rise.setByY(-62);
        FadeTransition fade = new FadeTransition(Duration.millis(880), damageLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        ParallelTransition animation = new ParallelTransition(pop, rise, fade);
        animation.setOnFinished(event -> gameRoot.getChildren().remove(damageLabel));
        animation.play();
    }

    private void setButtonsDisabled(boolean disabled) {
        btnAttack.setDisable(disabled);
        btnAbilities.setDisable(disabled);
        btnAnalyze.setDisable(disabled);
        btnGrounding.setDisable(disabled);
        chargeSelector.setDisable(disabled);
    }

    private void updateHeroStanceStatus(Hero hero) {
        stanceStatusLabel.getStyleClass().removeAll(
                "hero-condition-text",
                "hero-condition-shield",
                "hero-condition-unstable"
        );

        if (hero.isEquipotentialShieldActive()) {
            stanceStatusLabel.setText("Postura: 🛡 Escudo Equipotencial Ativo (−50% no próximo dano)");
            stanceStatusLabel.getStyleClass().add("hero-condition-shield");
        } else if (hero.isUnstable()) {
            stanceStatusLabel.setText("Postura: ⚠ Instabilidade Elétrica (+2 de dano no próximo golpe)");
            stanceStatusLabel.getStyleClass().add("hero-condition-unstable");
        } else if (groundingActiveThisRound) {
            stanceStatusLabel.setText("Postura: Aterramento Condutor ao Solo");
            stanceStatusLabel.getStyleClass().add("hero-condition-text");
        } else {
            stanceStatusLabel.setText("Postura: Equilíbrio Eletrostático Normal");
            stanceStatusLabel.getStyleClass().add("hero-condition-text");
        }
    }

    private void updateView() {
        Hero hero = battleController.getHero();
        Enemy enemy = battleController.getEnemy();

        // 1. Atualiza HP do Inimigo
        float enemyHp = Math.max(0.0f, enemy.getHealth());
        double enemyHpRatio = Math.max(0.0, Math.min(1.0, enemyHp / maxEnemyHealth));
        enemyHpBar.setWidth(240.0 * enemyHpRatio);
        enemyHpText.setText(Math.round(enemyHp) + " / " + Math.round(maxEnemyHealth));

        // 2. Atualiza HP do Herói
        float heroHp = Math.max(0.0f, hero.getHealth());
        double heroHpRatio = Math.max(0.0, Math.min(1.0, heroHp / maxHeroHealth));
        heroHpBar.setWidth(240.0 * heroHpRatio);
        heroHpText.setText(Math.round(heroHp) + " / " + Math.round(maxHeroHealth));

        // 3. Atualiza Polaridade e Postura do Herói
        updateHeroPolarityBadge(hero.getChargeType());
        if (chargeSelector.getValue() != hero.getChargeType()) {
            chargeSelector.setValue(hero.getChargeType());
        }
        updateHeroStanceStatus(hero);

        // 4. Atualiza Contagem de Habilidades no Botão Principal
        int abilityCount = hero.getAbilityBook().getAbilities().size();
        btnAbilities.setText(abilityCount > 0
                ? "[2]  ⚡  MODULAÇÕES DE CAMPO (" + abilityCount + ")"
                : "[2]  ⚡  MODULAÇÕES DE CAMPO");

        // 5. Atualiza Polaridade, Fase e Insight Físico do Inimigo / Chefe
        applyEnemyPolarityBadgeState(enemyPolarityBadge, enemy);
        physicsInsightLabel.setText(buildPhysicsInsightText());

        if (enemy instanceof BossEnemy boss) {
            String phaseDescription = switch (boss.getPhase()) {
                case POLARITY -> "F1 - Polaridade: " + boss.getChargeType().getSymbol();
                case FIELD -> "F2 - Campo: " + (boss.getFieldDirection() == BossEnemy.FieldDirection.LEFT ? "← Esq" : "→ Dir");
                case POTENTIAL -> "F3 - Potencial: " + (boss.isPotentialShieldActive() ? "🛡 Escudo Ativo" : "Escudo Inativo");
            };
            enemyPhaseLabel.setText(phaseDescription);
        } else if (enemy instanceof ChargedEnemy chargedEnemy) {
            enemyPhaseLabel.setText("[" + chargedEnemy.getChargeType().getSymbol() + "]");
        } else if (enemy instanceof UnstableConductor conductor) {
            enemyPhaseLabel.setText(conductor.hasStoredEnergy() ? "[⚡ Pronta p/ Descarga]" : "[Carregando]");
        } else {
            enemyPhaseLabel.setText("");
        }

        // 6. Atualiza Contador de Rodadas
        roundCounterLabel.setText("Rodada: " + (battleController.getCompletedRounds() + 1)
                + "  |  Turno: " + (battleController.getCurrentTurn() == BattleController.BattleTurn.PLAYER_TURN ? "SEU TURNO" : "TURNO DO INIMIGO"));

        // 7. Verifica Encerramento
        if (battleController.isBattleFinished()) {
            setButtonsDisabled(true);

            switch (battleController.getResult()) {
                case VICTORY -> {
                    telegraphTitleLabel.setText("> EQUILÍBRIO RESTAURADO");
                    telegraphTextLabel.setText("A ameaça teve suas cargas dissipadas e o equilíbrio local foi restabelecido!");
                    if (!endBannerShown) {
                        endBannerShown = true;
                        showBattleEndBanner(true);
                    }
                }
                case DEFEAT -> {
                    telegraphTitleLabel.setText("> COLAPSO DE EQUILÍBRIO");
                    telegraphTextLabel.setText("Suas reservas de carga entraram em colapso irreversível...");
                    if (!endBannerShown) {
                        endBannerShown = true;
                        showBattleEndBanner(false);
                    }
                }
                case IN_PROGRESS -> {
                }
            }
        }
    }

    private void showBattleEndBanner(boolean victory) {
        stopAudio();

        VBox banner = new VBox(12);
        banner.setAlignment(Pos.CENTER);
        banner.getStyleClass().add(victory ? "end-banner-victory" : "end-banner-defeat");

        Label title = new Label(victory ? "EQUILÍBRIO RESTAURADO" : "COLAPSO DE EQUILÍBRIO");
        title.getStyleClass().add(victory ? "end-title-victory" : "end-title-defeat");

        Label subtitle = new Label(victory
                ? "O campo eletrostático foi neutralizado com sucesso."
                : "A instabilidade elétrica superou suas forças.");
        subtitle.setStyle("-fx-text-fill: #DDD7CE; -fx-font-size: 15px; -fx-font-family: Georgia;");

        Label transitionHint = new Label(victory
                ? "Retornando ao fluxo da aventura..."
                : "Retornando ao último checkpoint...");
        transitionHint.setStyle("-fx-text-fill: #9D9585; -fx-font-size: 12px; -fx-font-family: Consolas;");

        banner.getChildren().addAll(title, subtitle, transitionHint);
        banner.setLayoutX((LOGICAL_WIDTH - 520) / 2.0);
        banner.setLayoutY((LOGICAL_HEIGHT - 180) / 2.0);
        banner.setPrefWidth(520);

        gameRoot.getChildren().add(banner);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), banner);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), banner);
        scaleIn.setFromX(0.88);
        scaleIn.setFromY(0.88);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        new ParallelTransition(fadeIn, scaleIn).play();

        if (victory) {
            playSfxModulated(SFX_VICTORY, 0.90, 0.0, 1.0);
        } else {
            playSfxModulated(SFX_THUNDER, 0.72, 0.0, 0.85);
            playSfxModulated(SFX_SHOCK, 0.55, 0.0, 0.78);
        }
    }

    public Scene getScene() {
        return scene;
    }
}
