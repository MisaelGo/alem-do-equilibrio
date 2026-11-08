package com.alemdoequilibrio.game;

import java.util.List;
import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Interface de combate 2D Pixel Art sobre o próprio mapa de exploração.
 *
 * Inclui texturas pixeladas em todas as janelas, artes 2D em Pixel Art no menu
 * (visor dinâmico de campo eletrostático, brasão de duelo e ícones pixelados
 * nos comandos) e posicionamento espaçado entre o Herói e o Inimigo no mapa.
 */
public class BattleView {

    private static final double LOGICAL_WIDTH = 1280.0;
    private static final double LOGICAL_HEIGHT = 720.0;

    private static final float PLAYER_DAMAGE = 20.0f;
    private static final double HP_BAR_MAX_WIDTH = 190.0;

    /* Distância horizontal entre o Herói e o Inimigo a partir do ponto médio */
    private static final double HERO_STAGE_OFFSET_FROM_MID = -195.0;
    private static final double ENEMY_STAGE_OFFSET_FROM_MID = 175.0;

    /* Caminhos de Áudio e Efeitos Sonoros */
    private static final String MUSIC_BATTLE = "/audio/zone_neutral_suspense.mp3";
    private static final String SFX_HIT = "/audio/sfx_hit_player.wav";
    private static final String SFX_SHOCK = "/audio/electric_shock.mp3";
    private static final String SFX_DISCHARGE = "/audio/sfx_electric_discharge.wav";
    private static final String SFX_VICTORY = "/audio/sfx_quiz_correct.wav";
    private static final String SFX_MENU_SELECT = "/audio/sfx_menu_select.wav";
    private static final String SFX_THUNDER = "/audio/prologue_thunder.mp3";
    private static final String SFX_POSITIVE = "/audio/prologue_positive_theme.mp3";

    private static final String DEFAULT_ACTION_HINT =
            "Teclas [1-4] Acoes | [Q/E] Trocar Carga (- 0 +)";

    private enum PixelIconType {
        ATTACK,
        TECHNIQUES,
        ANALYZE,
        GROUNDING,
        PULSE,
        FIELD,
        SHIELD,
        BACK
    }

    private final BattleController battleController;
    private final ResourceManager resourceManager;

    private final Pane gameRoot;
    private final Pane arenaFloorLayer;
    private final Scene scene;
    private Pane hostGameRoot;
    private Pane mapWorldLayer;
    private Pane mapHudLayer;
    private Node mapHeroNode;
    private Node mapEnemyNode;
    private double heroBaseOffsetX;
    private double heroBaseOffsetY;
    private double enemyBaseOffsetX;
    private double enemyBaseOffsetY;
    private double worldBaseTranslateX;
    private double worldBaseTranslateY;
    private boolean overlayAttached;

    /* Cartão Superior Esquerdo: Herói */
    private final Label heroNameLabel;
    private final Label heroPolarityBadge;
    private final Rectangle heroHpBar;
    private final Label heroHpText;
    private final ImageView heroPortraitView;
    private final Button btnChargeNeg;
    private final Button btnChargeNeu;
    private final Button btnChargePos;

    /* Brasão 2D Pixel Art no Topo Central */
    private final ImageView topDuelCrestView;

    /* Cartão Superior Direito: Inimigo */
    private final Label enemyNameLabel;
    private final Label enemyPolarityBadge;
    private final Label enemyPhaseLabel;
    private final Rectangle enemyHpBar;
    private final Label enemyHpText;
    private final ImageView enemyPortraitView;

    /* Painel Inferior Esquerdo: Arte 2D do Campo + Mensagem e Leitura Física */
    private final ImageView battleMenuArtView;
    private final Label roundCounterLabel;
    private final Label stanceStatusLabel;
    private final Label telegraphTextLabel;
    private final Label physicsInsightLabel;

    /* Painel Inferior Direito: Grade 2x2 de Comandos com Ícones Pixel Art */
    private final Label commandHeader;
    private final GridPane mainCommandGrid;
    private final GridPane abilityCommandGrid;
    private final Button btnAttack;
    private final Button btnAbilities;
    private final Button btnAnalyze;
    private final Button btnGrounding;
    private final Label actionHintLabel;

    /* Efeitos de impacto visual e controle de áudio/estado */
    private final Rectangle damageFlash;
    private final MediaPlayer battleMusicPlayer;
    private final EventHandler<KeyEvent> battleKeyFilter;
    private float maxEnemyHealth = 50.0f;
    private final float maxHeroHealth;
    private boolean groundingActiveThisRound;
    private boolean endBannerShown;

    /*
     * POO 7.1 - Sobrecarga de construtores
     */
    public BattleView(BattleController battleController) {
        this(battleController, new ResourceManager(), null);
    }

    public BattleView(BattleController battleController, ResourceManager resourceManager) {
        this(battleController, resourceManager, null);
    }

    public BattleView(
            BattleController battleController,
            ResourceManager resourceManager,
            Scene explorationScene) {

        this.battleController = Objects.requireNonNull(battleController, "BattleController não pode ser nulo.");
        this.resourceManager = (resourceManager != null) ? resourceManager : new ResourceManager();

        maxEnemyHealth = Math.max(1.0f, battleController.getEnemy().getHealth());
        maxHeroHealth = Math.max(1.0f, battleController.getHero().getMaxHealth());

        gameRoot = new Pane();
        gameRoot.setPrefSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setMinSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        gameRoot.setMaxSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);

        arenaFloorLayer = new Pane();
        arenaFloorLayer.setMouseTransparent(true);

        // 1. PLACA PIXELADA DO HERÓI (Canto Superior Esquerdo)
        heroPortraitView = new ImageView();
        heroPortraitView.setFitWidth(56);
        heroPortraitView.setFitHeight(56);
        heroPortraitView.setPreserveRatio(true);
        heroPortraitView.setSmooth(false);
        loadHeroPortrait();

        StackPane heroPortraitFrame = new StackPane(heroPortraitView);
        heroPortraitFrame.setPrefSize(64, 64);
        heroPortraitFrame.setMinSize(64, 64);
        heroPortraitFrame.setMaxSize(64, 64);
        heroPortraitFrame.getStyleClass().add("pixel-portrait-frame");
        heroPortraitFrame.setBackground(createDarkInsetTexture(64, 64));

        heroNameLabel = new Label("Herói");
        heroNameLabel.getStyleClass().add("hero-name");

        heroPolarityBadge = new Label("0 NEUTRO");
        heroPolarityBadge.getStyleClass().addAll("polarity-badge", "polarity-neutral");

        HBox heroTopRow = new HBox(10, heroNameLabel, heroPolarityBadge);
        heroTopRow.setAlignment(Pos.CENTER_LEFT);

        Rectangle heroHpTrack = new Rectangle(HP_BAR_MAX_WIDTH + 4, 14);
        heroHpTrack.getStyleClass().add("hp-track");

        heroHpBar = new Rectangle(HP_BAR_MAX_WIDTH, 10);
        heroHpBar.getStyleClass().add("hp-fill-hero");

        StackPane heroHpStack = new StackPane(heroHpTrack, heroHpBar);
        heroHpStack.setAlignment(Pos.CENTER_LEFT);

        heroHpText = new Label("100 / 100");
        heroHpText.getStyleClass().add("hp-text");

        Label heroHpPrefix = new Label("HP");
        heroHpPrefix.getStyleClass().add("hp-prefix-label");

        HBox heroHpRow = new HBox(8, heroHpPrefix, heroHpStack, heroHpText);
        heroHpRow.setAlignment(Pos.CENTER_LEFT);

        btnChargeNeg = createChargeButton("[-] ELETRON", ChargeType.NEGATIVE);
        btnChargeNeu = createChargeButton("[0] NEUTRO", ChargeType.NEUTRAL);
        btnChargePos = createChargeButton("[+] PROTON", ChargeType.POSITIVE);

        HBox chargeButtonsRow = new HBox(6, btnChargeNeg, btnChargeNeu, btnChargePos);
        chargeButtonsRow.setAlignment(Pos.CENTER_LEFT);

        VBox heroInfoCol = new VBox(5, heroTopRow, heroHpRow, chargeButtonsRow);
        heroInfoCol.setAlignment(Pos.CENTER_LEFT);

        HBox heroHudCard = new HBox(12, heroPortraitFrame, heroInfoCol);
        heroHudCard.setAlignment(Pos.CENTER_LEFT);
        heroHudCard.getStyleClass().add("pixel-box");
        heroHudCard.setBackground(createCyanHudTexture(430, 96));
        heroHudCard.setLayoutX(24);
        heroHudCard.setLayoutY(14);
        heroHudCard.setPrefWidth(430);

        // 1.5. BRASÃO 2D PIXEL ART CENTRAL NO TOPO (Entre as duas barras de status)
        topDuelCrestView = new ImageView();
        topDuelCrestView.setFitWidth(136);
        topDuelCrestView.setFitHeight(60);
        topDuelCrestView.setSmooth(false);

        StackPane topCrestBox = new StackPane(topDuelCrestView);
        topCrestBox.setPrefSize(148, 72);
        topCrestBox.setBackground(createGoldMenuTexture(148, 72));
        topCrestBox.setLayoutX((LOGICAL_WIDTH - 148) / 2.0);
        topCrestBox.setLayoutY(22);

        // 2. PLACA PIXELADA DO INIMIGO (Canto Superior Direito)
        enemyPortraitView = new ImageView();
        enemyPortraitView.setFitWidth(52);
        enemyPortraitView.setFitHeight(52);
        enemyPortraitView.setPreserveRatio(true);
        enemyPortraitView.setSmooth(false);
        loadEnemyPortrait();

        StackPane enemyPortraitFrame = new StackPane(enemyPortraitView);
        enemyPortraitFrame.setPrefSize(64, 64);
        enemyPortraitFrame.setMinSize(64, 64);
        enemyPortraitFrame.setMaxSize(64, 64);
        enemyPortraitFrame.getStyleClass().add("pixel-portrait-frame");
        enemyPortraitFrame.setBackground(createDarkInsetTexture(64, 64));

        enemyNameLabel = new Label(battleController.getEnemy().getName());
        enemyNameLabel.getStyleClass().add("enemy-name");

        enemyPolarityBadge = createPolarityBadge(battleController.getEnemy());

        HBox enemyTopRow = new HBox(10, enemyNameLabel, enemyPolarityBadge);
        enemyTopRow.setAlignment(Pos.CENTER_LEFT);

        Rectangle enemyHpTrack = new Rectangle(HP_BAR_MAX_WIDTH + 4, 14);
        enemyHpTrack.getStyleClass().add("hp-track");

        enemyHpBar = new Rectangle(HP_BAR_MAX_WIDTH, 10);
        enemyHpBar.getStyleClass().add("hp-fill-enemy");

        StackPane enemyHpStack = new StackPane(enemyHpTrack, enemyHpBar);
        enemyHpStack.setAlignment(Pos.CENTER_LEFT);

        enemyHpText = new Label();
        enemyHpText.getStyleClass().add("hp-text");

        Label enemyHpPrefix = new Label("HP");
        enemyHpPrefix.getStyleClass().add("hp-prefix-label");

        HBox enemyHpRow = new HBox(8, enemyHpPrefix, enemyHpStack, enemyHpText);
        enemyHpRow.setAlignment(Pos.CENTER_LEFT);

        enemyPhaseLabel = new Label();
        enemyPhaseLabel.getStyleClass().add("command-hint-text");

        VBox enemyInfoCol = new VBox(5, enemyTopRow, enemyHpRow, enemyPhaseLabel);
        enemyInfoCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(enemyInfoCol, Priority.ALWAYS);

        HBox enemyHudCard = new HBox(12, enemyInfoCol, enemyPortraitFrame);
        enemyHudCard.setAlignment(Pos.CENTER_RIGHT);
        enemyHudCard.getStyleClass().add("pixel-box");
        enemyHudCard.setBackground(createCyanHudTexture(430, 96));
        enemyHudCard.setLayoutX(LOGICAL_WIDTH - 454);
        enemyHudCard.setLayoutY(14);
        enemyHudCard.setPrefWidth(430);

        // 3. PAINEL INFERIOR ESQUERDO: ARTE 2D PIXEL ART DO CAMPO + CAIXA DE MENSAGEM
        battleMenuArtView = new ImageView();
        battleMenuArtView.setFitWidth(132);
        battleMenuArtView.setFitHeight(132);
        battleMenuArtView.setSmooth(false);

        StackPane battleArtFrame = new StackPane(battleMenuArtView);
        battleArtFrame.setPrefSize(142, 142);
        battleArtFrame.setMinSize(142, 142);
        battleArtFrame.setMaxSize(142, 142);
        battleArtFrame.getStyleClass().add("pixel-portrait-frame");
        battleArtFrame.setBackground(createDarkInsetTexture(142, 142));

        roundCounterLabel = new Label("+ RODADA 1 :: SEU TURNO +");
        roundCounterLabel.getStyleClass().add("telegraph-title");

        stanceStatusLabel = new Label("POSTURA: NORMAL");
        stanceStatusLabel.getStyleClass().add("hero-condition-text");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox messageHeaderRow = new HBox(10, roundCounterLabel, headerSpacer, stanceStatusLabel);
        messageHeaderRow.setAlignment(Pos.CENTER_LEFT);

        telegraphTextLabel = new Label(buildInitialTelegraphText());
        telegraphTextLabel.getStyleClass().add("telegraph-text");
        telegraphTextLabel.setWrapText(true);
        VBox.setVgrow(telegraphTextLabel, Priority.ALWAYS);

        physicsInsightLabel = new Label(buildPhysicsInsightText());
        physicsInsightLabel.getStyleClass().add("command-hint-text");
        physicsInsightLabel.setWrapText(true);

        VBox messageTextCol = new VBox(6, messageHeaderRow, telegraphTextLabel, physicsInsightLabel);
        messageTextCol.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(messageTextCol, Priority.ALWAYS);

        HBox messageBox = new HBox(14, battleArtFrame, messageTextCol);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.getStyleClass().add("panel-box");
        messageBox.setBackground(createGoldMenuTexture(696, 176));
        messageBox.setLayoutX(24);
        messageBox.setLayoutY(524);
        messageBox.setPrefWidth(696);
        messageBox.setPrefHeight(176);

        // 4. PAINEL INFERIOR DIREITO: GRADE 2x2 COM TECLAS DOURADAS E ÍCONES PIXEL ART 2D
        commandHeader = new Label("+ ACOES DE COMBATE +");
        commandHeader.getStyleClass().add("panel-header");

        actionHintLabel = new Label(DEFAULT_ACTION_HINT);
        actionHintLabel.getStyleClass().add("command-hint-text");

        btnAttack = createKeyCommandButton(
                "1",
                "ATACAR",
                "Golpe direto no alvo (20 de dano base).",
                PixelIconType.ATTACK,
                false
        );
        btnAttack.setOnAction(e -> handlePlayerAttack());

        btnAbilities = createKeyCommandButton(
                "2",
                "TECNICAS",
                "Usa habilidades fisicas aprendidas com Ion.",
                PixelIconType.TECHNIQUES,
                false
        );
        btnAbilities.setOnAction(e -> showAbilitySubmenu());

        btnAnalyze = createKeyCommandButton(
                "3",
                "ANALISAR",
                "Inspeciona a carga e o campo do inimigo (livre).",
                PixelIconType.ANALYZE,
                false
        );
        btnAnalyze.setOnAction(e -> handleAnalyzeAction());

        btnGrounding = createKeyCommandButton(
                "4",
                "ATERRAR",
                "Escoa cargas para o solo e passa o turno.",
                PixelIconType.GROUNDING,
                false
        );
        btnGrounding.setOnAction(e -> handleGroundingAction());

        mainCommandGrid = create2x2Grid();
        mainCommandGrid.add(btnAttack, 0, 0);
        mainCommandGrid.add(btnAbilities, 1, 0);
        mainCommandGrid.add(btnAnalyze, 0, 1);
        mainCommandGrid.add(btnGrounding, 1, 1);
        VBox.setVgrow(mainCommandGrid, Priority.ALWAYS);

        abilityCommandGrid = create2x2Grid();
        abilityCommandGrid.setVisible(false);
        abilityCommandGrid.setManaged(false);
        VBox.setVgrow(abilityCommandGrid, Priority.ALWAYS);

        VBox commandBox = new VBox(6, commandHeader, mainCommandGrid, abilityCommandGrid, actionHintLabel);
        commandBox.getStyleClass().add("panel-box");
        commandBox.setBackground(createGoldMenuTexture(524, 176));
        commandBox.setLayoutX(732);
        commandBox.setLayoutY(524);
        commandBox.setPrefWidth(524);
        commandBox.setPrefHeight(176);

        // 5. Overlay de Flash de Dano
        damageFlash = new Rectangle(LOGICAL_WIDTH, LOGICAL_HEIGHT, Color.rgb(220, 20, 20, 0.0));
        damageFlash.setMouseTransparent(true);

        gameRoot.getChildren().addAll(
                arenaFloorLayer,
                heroHudCard,
                topCrestBox,
                enemyHudCard,
                messageBox,
                commandBox,
                damageFlash
        );

        battleKeyFilter = this::handleKeyPressed;

        if (explorationScene != null && attachToExplorationScene(explorationScene)) {
            this.scene = explorationScene;
            gameRoot.getStyleClass().add("battle-map-overlay");
        } else {
            gameRoot.getStyleClass().add("battle-root");
            buildStandaloneFallbackStage();
            Rectangle viewportClip = new Rectangle(LOGICAL_WIDTH, LOGICAL_HEIGHT);
            gameRoot.setClip(viewportClip);
            StackPane screenRoot = new StackPane(gameRoot);
            this.scene = new Scene(screenRoot, LOGICAL_WIDTH, LOGICAL_HEIGHT);

            NumberBinding scale = Bindings.min(
                    this.scene.widthProperty().divide(LOGICAL_WIDTH),
                    this.scene.heightProperty().divide(LOGICAL_HEIGHT)
            );
            gameRoot.scaleXProperty().bind(scale);
            gameRoot.scaleYProperty().bind(scale);
        }

        this.scene.addEventFilter(KeyEvent.KEY_PRESSED, battleKeyFilter);
        applyStylesheet();
        battleMusicPlayer = initBattleMusic();
        updateView();
        playEncounterIntroEffects();
    }

    /* =========================================================================
       ARTES 2D EM PIXEL ART PARA O MENU DE COMBATE
       ========================================================================= */

    /**
     * Desenha a ilustração 2D em Pixel Art exibida dentro do painel inferior do menu,
     * representando as bobinas/emissores do Herói e do Inimigo, o piso pixelado e
     * as linhas de força eletrostáticas ativas no turno.
     */
    private Image createBattleMenuPixelScene(Hero hero, Enemy enemy) {
        int gw = 66;
        int gh = 66;
        WritableImage img = new WritableImage(gw * 2, gh * 2);
        PixelWriter pw = img.getPixelWriter();

        Color skyDark = Color.web("#06111B");
        Color skyMid = Color.web("#091A28");
        Color skyGrid = Color.web("#0E2638");
        Color starColor = Color.web("#5CA6AE");
        Color floorTop = Color.web("#265373");
        Color floorBase = Color.web("#0D2233");
        Color floorTile = Color.web("#14324A");
        Color floorGold = Color.web("#C28216");

        Color heroColor = switch (hero.getChargeType()) {
            case POSITIVE -> Color.web("#FF6B6B");
            case NEGATIVE -> Color.web("#5CA6AE");
            case NEUTRAL -> Color.web("#F6C83E");
        };
        Color heroCore = Color.web("#FFF8E7");

        Color enemyColor = resolveEnemyThemeColor(enemy);

        // 1. Céu noturno com grade eletrostática e chão de ladrilhos pixelados
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                Color c;
                if (y >= 50) {
                    if (y == 50) {
                        c = floorTop;
                    } else if (y == 51 && x % 4 == 0) {
                        c = floorGold;
                    } else if ((x / 8 + (y - 50) / 4) % 2 == 0) {
                        c = floorTile;
                    } else {
                        c = floorBase;
                    }
                } else {
                    c = (y < 22) ? skyDark : skyMid;
                    if (x % 11 == 0 || y % 11 == 0) {
                        c = skyGrid;
                    }
                    if (((x * 17 + y * 31) % 53 == 0) && y < 38) {
                        c = starColor;
                    }
                }
                writeBlock2x2(pw, x, y, c);
            }
        }

        // 2. Bobina / Emissor Pixel Art do Herói (Esquerda, x = 8..18, y = 26..49)
        drawPixelTower(pw, 13, 49, heroColor, heroCore);
        drawPixelChargeSign(pw, 13, 16, hero.getChargeType(), heroColor);

        // Se o Escudo Equipotencial estiver ativo, desenha cúpula pixelada verde
        if (hero.isEquipotentialShieldActive()) {
            Color shieldC = Color.web("#6EE787");
            for (int sy = 22; sy <= 48; sy += 2) {
                writeBlock2x2(pw, 21, sy, shieldC);
                writeBlock2x2(pw, 22, sy + 1, shieldC);
            }
        }

        // 3. Bobina / Emissor Pixel Art do Inimigo (Direita, x = 52, y = 26..49)
        drawPixelTower(pw, 52, 49, enemyColor, heroCore);
        ChargeType enemyCharge = resolveEnemyChargeType(enemy);
        drawPixelChargeSign(pw, 52, 16, enemyCharge, enemyColor);

        // 4. Feixes e ondas de interação 2D no centro (x = 23..43, y = 34)
        boolean sameSign = hero.getChargeType() != ChargeType.NEUTRAL
                && hero.getChargeType() == enemyCharge;
        boolean oppositeSign = hero.getChargeType() != ChargeType.NEUTRAL
                && enemyCharge != ChargeType.NEUTRAL
                && hero.getChargeType() != enemyCharge;

        for (int x = 23; x <= 43; x++) {
            int waveY = 35 + (int) Math.round(Math.sin(x * 0.7) * 3.0);
            if (sameSign) {
                // Repulsão: choque no meio (x = 32..34) com arcos opostos
                if (x < 31) {
                    writeBlock2x2(pw, x, waveY, heroColor);
                } else if (x > 35) {
                    writeBlock2x2(pw, x, waveY, enemyColor);
                } else {
                    for (int ry = 27; ry <= 43; ry += 2) {
                        writeBlock2x2(pw, 33, ry, Color.web("#FFF2A6"));
                    }
                }
            } else if (oppositeSign) {
                // Atração: linha dupla contínua conectando os dois polos
                writeBlock2x2(pw, x, waveY, Color.web("#F6C83E"));
                writeBlock2x2(pw, x, waveY + 3, heroColor);
            } else {
                // Pulso padrão tracejado
                if (x % 2 == 0) {
                    writeBlock2x2(pw, x, waveY, (x < 33) ? heroColor : enemyColor);
                }
            }
        }

        // 5. Moldura interna dourada de canto na cena 2D
        Color gold = Color.web("#F6C83E");
        for (int i = 1; i <= 5; i++) {
            writeBlock2x2(pw, i, 1, gold);
            writeBlock2x2(pw, 1, i, gold);
            writeBlock2x2(pw, gw - 1 - i, 1, gold);
            writeBlock2x2(pw, gw - 2, i, gold);
            writeBlock2x2(pw, i, gh - 2, gold);
            writeBlock2x2(pw, 1, gh - 1 - i, gold);
            writeBlock2x2(pw, gw - 1 - i, gh - 2, gold);
            writeBlock2x2(pw, gw - 2, gh - 1 - i, gold);
        }

        return img;
    }

    private void drawPixelTower(PixelWriter pw, int centerX, int bottomY, Color mainColor, Color coreColor) {
        Color metalDark = Color.web("#16344B");
        Color metalLight = Color.web("#4CA0AB");

        // Pedestal
        for (int dx = -5; dx <= 5; dx++) {
            writeBlock2x2(pw, centerX + dx, bottomY, metalDark);
            writeBlock2x2(pw, centerX + dx, bottomY - 1, metalLight);
        }
        // Coluna com anéis condutores
        for (int y = bottomY - 16; y <= bottomY - 2; y++) {
            for (int dx = -2; dx <= 2; dx++) {
                Color col = (dx == -2 || dx == 2) ? metalDark : metalLight;
                if (y % 4 == 0) {
                    col = mainColor;
                }
                writeBlock2x2(pw, centerX + dx, y, col);
            }
        }
        // Esfera emissora no topo da bobina
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                if (dx * dx + dy * dy <= 18) {
                    Color c = (dx * dx + dy * dy <= 4) ? coreColor : mainColor;
                    writeBlock2x2(pw, centerX + dx, bottomY - 20 + dy, c);
                }
            }
        }
    }

    private void drawPixelChargeSign(PixelWriter pw, int cx, int cy, ChargeType type, Color color) {
        if (type == ChargeType.POSITIVE) {
            for (int i = -3; i <= 3; i++) {
                writeBlock2x2(pw, cx + i, cy, color);
                writeBlock2x2(pw, cx, cy + i, color);
            }
        } else if (type == ChargeType.NEGATIVE) {
            for (int i = -3; i <= 3; i++) {
                writeBlock2x2(pw, cx + i, cy, color);
            }
        } else {
            for (int i = -2; i <= 2; i++) {
                writeBlock2x2(pw, cx + i, cy - 3, color);
                writeBlock2x2(pw, cx + i, cy + 3, color);
                writeBlock2x2(pw, cx - 3, cy + i, color);
                writeBlock2x2(pw, cx + 3, cy + i, color);
            }
        }
    }

    /**
     * Desenha o brasão 2D em Pixel Art exibido no topo central entre as placas de HP.
     */
    private Image createTopDuelCrestImage(Hero hero, Enemy enemy) {
        int gw = 68;
        int gh = 30;
        WritableImage img = new WritableImage(gw * 2, gh * 2);
        PixelWriter pw = img.getPixelWriter();

        Color heroColor = switch (hero.getChargeType()) {
            case POSITIVE -> Color.web("#FF6B6B");
            case NEGATIVE -> Color.web("#5CA6AE");
            case NEUTRAL -> Color.web("#F6C83E");
        };
        Color enemyColor = resolveEnemyThemeColor(enemy);
        Color gold = Color.web("#F6C83E");
        Color white = Color.web("#FFF8E7");

        // Polo esquerdo (Herói) e feixe até o centro
        for (int y = 9; y <= 21; y++) {
            for (int x = 6; x <= 18; x++) {
                if ((x - 12) * (x - 12) + (y - 15) * (y - 15) <= 32) {
                    writeBlock2x2(pw, x, y, heroColor);
                }
            }
        }
        drawPixelChargeSign(pw, 12, 15, hero.getChargeType(), white);

        // Polo direito (Inimigo)
        for (int y = 9; y <= 21; y++) {
            for (int x = 50; x <= 62; x++) {
                if ((x - 56) * (x - 56) + (y - 15) * (y - 15) <= 32) {
                    writeBlock2x2(pw, x, y, enemyColor);
                }
            }
        }
        drawPixelChargeSign(pw, 56, 15, resolveEnemyChargeType(enemy), white);

        // Arcos elétricos convergindo no raio central
        for (int x = 19; x <= 49; x++) {
            int dy = (x % 3 == 0) ? -2 : ((x % 3 == 1) ? 2 : 0);
            writeBlock2x2(pw, x, 15 + dy, (x < 34) ? heroColor : enemyColor);
        }

        // Raio / Cristal central dourado em Pixel Art
        int cx = 34;
        for (int dy = -9; dy <= 9; dy++) {
            int span = 6 - (Math.abs(dy) / 2);
            for (int dx = -span; dx <= span; dx++) {
                Color c = (Math.abs(dx) <= 1 && Math.abs(dy) <= 4) ? white : gold;
                writeBlock2x2(pw, cx + dx, 15 + dy, c);
            }
        }

        return img;
    }

    /**
     * Cria um ícone 2D em Pixel Art (22x22 px) para os botões de comando e habilidades.
     */
    private static ImageView createCommandPixelIcon(PixelIconType type) {
        String[] map = switch (type) {
            case ATTACK -> new String[] {
                "......YY...",
                ".....YYY...",
                "....YYYY...",
                "...YYYY....",
                "..YYYYY....",
                ".YYYYYYYY..",
                "....YYYY...",
                "...YYYY....",
                "..YYYY.....",
                ".YYY.......",
                "YY........."
            };
            case TECHNIQUES -> new String[] {
                "...CCCCC...",
                "..CCWWWCC..",
                ".CCWCCCWCC.",
                "CCWCYYYCWCC",
                "CCWCYYYCWCC",
                "CCWCYYYCWCC",
                ".CCWCCCWCC.",
                "..CCWWWCC..",
                "...CCCCC...",
                "....YYY....",
                "...YYYYY..."
            };
            case ANALYZE -> new String[] {
                "..CCCCCC...",
                ".CCWWWWCC..",
                "CCW....WCC.",
                "CW..YY..WC.",
                "CW..YY..WC.",
                "CCW....WCC.",
                ".CCWWWWCC..",
                "..CCCCCCYY.",
                ".......YYY.",
                "........YYY",
                ".........YY"
            };
            case GROUNDING -> new String[] {
                ".....GG....",
                ".....GG....",
                ".....GG....",
                ".....GG....",
                ".GGGGGGGGGG",
                ".....GG....",
                "..GGGGGGGG.",
                ".....GG....",
                "...GGGGGG..",
                "...........",
                "....GGGG..."
            };
            case PULSE -> new String[] {
                ".RR.....RR.",
                "RR..RRR..RR",
                "R..RRRRR..R",
                "..RRWWWRR..",
                ".RRWWWWWRR.",
                ".RRWWWWWRR.",
                ".RRWWWWWRR.",
                "..RRWWWRR..",
                "R..RRRRR..R",
                "RR..RRR..RR",
                ".RR.....RR."
            };
            case FIELD -> new String[] {
                "....YY.....",
                ".....YY....",
                "YYYYYYYY...",
                "YYYYYYYYY..",
                ".....YY....",
                "....YY..YY.",
                "...YYYYYYYY",
                "...YYYYYYYY",
                "........YY.",
                ".......YY..",
                "..........."
            };
            case SHIELD -> new String[] {
                "..GGGGGGG..",
                ".GGWWWWWGG.",
                "GGWGGGGGWGG",
                "GGWGGGGGWGG",
                "GGWGGWGGWGG",
                "GGWGGGGGWGG",
                ".GGWGGGWGG.",
                "..GGWWWGG..",
                "...GGGGG...",
                "....GGG....",
                ".....G....."
            };
            case BACK -> new String[] {
                "....YY.....",
                "...YYY.....",
                "..YYYYYYYY.",
                ".YYYYYYYYYY",
                "..YYYYYYYY.",
                "...YYY..YY.",
                "....YY..YY.",
                "........YY.",
                "....YYYYYY.",
                "....YYYYY..",
                "..........."
            };
        };

        WritableImage img = new WritableImage(22, 22);
        PixelWriter pw = img.getPixelWriter();

        for (int y = 0; y < 11; y++) {
            String row = map[y];
            for (int x = 0; x < 11; x++) {
                char ch = row.charAt(x);
                Color c = switch (ch) {
                    case 'Y' -> Color.web("#F6C83E");
                    case 'C' -> Color.web("#5CA6AE");
                    case 'R' -> Color.web("#FF6B6B");
                    case 'G' -> Color.web("#6EE787");
                    case 'W' -> Color.web("#FFF8E7");
                    default -> Color.TRANSPARENT;
                };
                writeBlock2x2(pw, x, y, c);
            }
        }

        ImageView view = new ImageView(img);
        view.setFitWidth(22);
        view.setFitHeight(22);
        view.setSmooth(false);
        return view;
    }

    /**
     * Cria a arte 2D em Pixel Art para o topo do banner de Vitória ou Derrota.
     */
    private ImageView createEndBannerPixelArt(boolean victory) {
        int gw = 48;
        int gh = 22;
        WritableImage img = new WritableImage(gw * 2, gh * 2);
        PixelWriter pw = img.getPixelWriter();

        Color main = victory ? Color.web("#F6C83E") : Color.web("#FF6B6B");
        Color accent = victory ? Color.web("#5CA6AE") : Color.web("#8A242E");
        Color core = Color.web("#FFF8E7");

        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                int dx = Math.abs(x - 24);
                int dy = Math.abs(y - 11);
                if (dx + dy * 2 <= 16) {
                    if (!victory && (x + y) % 5 == 0 && dx <= 3) {
                        // Fenda de ruptura no cristal de derrota
                        writeBlock2x2(pw, x, y, Color.web("#140609"));
                    } else if (dx + dy <= 5) {
                        writeBlock2x2(pw, x, y, core);
                    } else {
                        writeBlock2x2(pw, x, y, main);
                    }
                } else if (y == 11 && dx >= 18 && dx <= 22) {
                    writeBlock2x2(pw, x, y, accent);
                }
            }
        }

        ImageView view = new ImageView(img);
        view.setFitWidth(96);
        view.setFitHeight(44);
        view.setSmooth(false);
        return view;
    }

    private Color resolveEnemyThemeColor(Enemy enemy) {
        if (enemy instanceof BossEnemy boss) {
            return switch (boss.getPhase()) {
                case POLARITY -> (boss.getChargeType() == ChargeType.POSITIVE)
                        ? Color.web("#FF6B6B") : Color.web("#5CA6AE");
                case FIELD -> Color.web("#F6C83E");
                case POTENTIAL -> Color.web("#B48EAD");
            };
        } else if (enemy instanceof CorruptedDipole dipole) {
            return (dipole.getChargeType() == ChargeType.POSITIVE)
                    ? Color.web("#FF6B6B") : Color.web("#5CA6AE");
        } else if (enemy instanceof ChargedEnemy charged) {
            return (charged.getChargeType() == ChargeType.POSITIVE)
                    ? Color.web("#FF6B6B") : Color.web("#5CA6AE");
        }
        return Color.web("#F6C83E");
    }

    private ChargeType resolveEnemyChargeType(Enemy enemy) {
        if (enemy instanceof BossEnemy boss && boss.getPhase() == BossEnemy.Phase.POLARITY) {
            return boss.getChargeType();
        } else if (enemy instanceof CorruptedDipole dipole) {
            return dipole.getChargeType();
        } else if (enemy instanceof ChargedEnemy charged) {
            return charged.getChargeType();
        }
        return ChargeType.NEUTRAL;
    }

    /* =========================================================================
       GERADOR DE TEXTURAS PIXEL ART (Compartilhado com HUD.java)
       ========================================================================= */

    static Background createCyanHudTexture(int width, int height) {
        return createPixelPanelBackground(
                width,
                height,
                Color.web("#041018"),
                Color.web("#68BEC8"),
                Color.web("#428891"),
                Color.web("#A8F0F8"),
                Color.web("#1D4E6B"),
                Color.web("#071520"),
                Color.web("#0A2233"),
                Color.web("#0F2D42"),
                Color.web("#153A52"),
                Color.web("#061724"),
                Color.web("#215675"),
                Color.web("#163E57")
        );
    }

    static Background createGoldMenuTexture(int width, int height) {
        return createPixelPanelBackground(
                width,
                height,
                Color.web("#160B03"),
                Color.web("#F6C83E"),
                Color.web("#C28216"),
                Color.web("#FFF2A6"),
                Color.web("#265373"),
                Color.web("#081521"),
                Color.web("#091B2B"),
                Color.web("#0E2538"),
                Color.web("#143149"),
                Color.web("#06121E"),
                Color.web("#234E6D"),
                Color.web("#183850")
        );
    }

    static Background createCrimsonDefeatTexture(int width, int height) {
        return createPixelPanelBackground(
                width,
                height,
                Color.web("#1E0608"),
                Color.web("#FF6B6B"),
                Color.web("#B83232"),
                Color.web("#FFD1D1"),
                Color.web("#5C2028"),
                Color.web("#140609"),
                Color.web("#1A0B11"),
                Color.web("#241018"),
                Color.web("#301520"),
                Color.web("#12060A"),
                Color.web("#4D1C26"),
                Color.web("#36131B")
        );
    }

    static Background createDarkInsetTexture(int width, int height) {
        int gridW = Math.max(12, width / 2);
        int gridH = Math.max(12, height / 2);
        WritableImage img = new WritableImage(gridW * 2, gridH * 2);
        PixelWriter pw = img.getPixelWriter();

        Color outerDark = Color.web("#040B11");
        Color rimTopLeft = Color.web("#1B435C");
        Color rimBottomRight = Color.web("#4CA0AB");
        Color bgBase = Color.web("#06131E");
        Color bgAlt = Color.web("#091B2A");
        Color bgDot = Color.web("#10293E");

        for (int gy = 0; gy < gridH; gy++) {
            for (int gx = 0; gx < gridW; gx++) {
                Color c;
                if (gx == 0 || gx == gridW - 1 || gy == 0 || gy == gridH - 1) {
                    c = outerDark;
                } else if (gx == 1 || gy == 1) {
                    c = rimTopLeft;
                } else if (gx == gridW - 2 || gy == gridH - 2) {
                    c = rimBottomRight;
                } else {
                    c = ((gx + gy) % 4 == 0) ? bgAlt : bgBase;
                    if (gx % 4 == 0 && gy % 4 == 0) {
                        c = bgDot;
                    }
                }
                writeBlock2x2(pw, gx, gy, c);
            }
        }
        return wrapImageAsBackground(img);
    }

    private static Background createCommandButtonTexture(
            int width,
            int height,
            boolean hover,
            boolean synergy) {

        int gridW = Math.max(16, width / 2);
        int gridH = Math.max(12, height / 2);
        WritableImage img = new WritableImage(gridW * 2, gridH * 2);
        PixelWriter pw = img.getPixelWriter();

        Color outer = Color.web("#050F17");
        Color borderLight = hover
                ? Color.web("#F8D348")
                : (synergy ? Color.web("#58D67C") : Color.web("#3B7A9E"));
        Color borderDark = hover
                ? Color.web("#C28216")
                : (synergy ? Color.web("#2D8A47") : Color.web("#1E4660"));
        Color cornerRivet = hover
                ? Color.web("#FFF2A6")
                : (synergy ? Color.web("#B0F8C4") : Color.web("#7CC8DC"));
        Color bgBase = hover
                ? Color.web("#173B56")
                : (synergy ? Color.web("#11342B") : Color.web("#10293E"));
        Color bgStripe = hover
                ? Color.web("#1E4868")
                : (synergy ? Color.web("#164236") : Color.web("#14334C"));
        Color bgShadow = hover
                ? Color.web("#102B40")
                : (synergy ? Color.web("#0B241E") : Color.web("#0B1E2E"));

        for (int gy = 0; gy < gridH; gy++) {
            for (int gx = 0; gx < gridW; gx++) {
                Color c;
                if ((gx == 0 || gx == gridW - 1) && (gy == 0 || gy == gridH - 1)) {
                    c = Color.TRANSPARENT;
                } else if (gx == 0 || gx == gridW - 1 || gy == 0 || gy == gridH - 1) {
                    c = outer;
                } else if (gx == 1 || gy == 1 || gx == gridW - 2 || gy == gridH - 2) {
                    boolean isCorner = (gx <= 2 || gx >= gridW - 3) && (gy <= 2 || gy >= gridH - 3);
                    if (isCorner) {
                        c = cornerRivet;
                    } else if (gy == 1 || gx == 1) {
                        c = borderLight;
                    } else {
                        c = borderDark;
                    }
                } else {
                    if ((gx + gy) % 5 == 0) {
                        c = bgStripe;
                    } else if ((gx + gy) % 5 == 2 && gx % 2 == 0) {
                        c = bgShadow;
                    } else {
                        c = bgBase;
                    }
                }
                writeBlock2x2(pw, gx, gy, c);
            }
        }
        return wrapImageAsBackground(img);
    }

    private static Background createPixelPanelBackground(
            int width,
            int height,
            Color outerDark,
            Color borderLight,
            Color borderDark,
            Color rivetHighlight,
            Color innerRimLight,
            Color innerRimDark,
            Color bgBase,
            Color bgStripe,
            Color bgHighlight,
            Color bgShadow,
            Color cornerDitherNear,
            Color cornerDitherFar) {

        int gridW = Math.max(24, width / 2);
        int gridH = Math.max(18, height / 2);
        WritableImage img = new WritableImage(gridW * 2, gridH * 2);
        PixelWriter pw = img.getPixelWriter();

        for (int gy = 0; gy < gridH; gy++) {
            for (int gx = 0; gx < gridW; gx++) {
                Color c;

                if ((gx == 0 || gx == gridW - 1) && (gy == 0 || gy == gridH - 1)) {
                    c = Color.TRANSPARENT;
                } else if (gx == 0 || gx == gridW - 1 || gy == 0 || gy == gridH - 1
                        || ((gx == 1 || gx == gridW - 2) && (gy == 1 || gy == gridH - 2))) {
                    c = outerDark;
                } else if (gx <= 2 || gx >= gridW - 3 || gy <= 2 || gy >= gridH - 3) {
                    boolean isCornerPlate = (gx <= 4 || gx >= gridW - 5) && (gy <= 4 || gy >= gridH - 5);
                    if (isCornerPlate && ((gx == 2 || gx == gridW - 3) && (gy == 2 || gy == gridH - 3))) {
                        c = rivetHighlight;
                    } else if (gy <= 2 || gx <= 2) {
                        c = borderLight;
                    } else {
                        c = borderDark;
                    }
                } else if (gx == 3 || gx == gridW - 4 || gy == 3 || gy == gridH - 4) {
                    c = (gy == 3 || gx == 3) ? innerRimLight : innerRimDark;
                } else {
                    int dx = Math.min(gx - 4, (gridW - 5) - gx);
                    int dy = Math.min(gy - 4, (gridH - 5) - gy);
                    int cornerDist = dx + dy;

                    if (cornerDist < 5 && ((gx + gy) % 2 == 0)) {
                        c = cornerDitherNear;
                    } else if (cornerDist < 9 && ((gx + gy) % 2 == 0) && (gx % 2 == 0)) {
                        c = cornerDitherFar;
                    } else if ((gx + gy) % 6 == 0) {
                        c = bgStripe;
                    } else if ((gx + gy) % 6 == 1 && (gx % 2 == 0)) {
                        c = bgHighlight;
                    } else if ((gx + gy) % 6 == 3 && (gy % 2 == 0)) {
                        c = bgShadow;
                    } else if (gx % 10 == 0 && gy % 6 == 0) {
                        c = bgHighlight;
                    } else {
                        c = bgBase;
                    }
                }

                writeBlock2x2(pw, gx, gy, c);
            }
        }

        return wrapImageAsBackground(img);
    }

    private static void writeBlock2x2(PixelWriter pw, int gx, int gy, Color color) {
        int px = gx * 2;
        int py = gy * 2;
        pw.setColor(px, py, color);
        pw.setColor(px + 1, py, color);
        pw.setColor(px, py + 1, color);
        pw.setColor(px + 1, py + 1, color);
    }

    private static Background wrapImageAsBackground(Image image) {
        BackgroundSize size = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage bgImg = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                size
        );
        return new Background(bgImg);
    }

    /**
     * Anexa a camada de batalha diretamente sobre a cena do mapa de exploração,
     * mantendo o mapa e os personagens visíveis ao fundo.
     */
    private boolean attachToExplorationScene(Scene explorationScene) {
        if (!(explorationScene.getRoot() instanceof StackPane rootStack)
                || rootStack.getChildren().isEmpty()
                || !(rootStack.getChildren().get(0) instanceof Pane foundGameRoot)
                || foundGameRoot.getChildren().size() < 2
                || !(foundGameRoot.getChildren().get(0) instanceof Pane foundWorldLayer)
                || !(foundGameRoot.getChildren().get(1) instanceof Pane foundHudLayer)) {
            return false;
        }

        this.hostGameRoot = foundGameRoot;
        this.mapWorldLayer = foundWorldLayer;
        this.mapHudLayer = foundHudLayer;
        this.worldBaseTranslateX = foundWorldLayer.getTranslateX();
        this.worldBaseTranslateY = foundWorldLayer.getTranslateY();

        this.mapHudLayer.setVisible(false);
        if (!this.hostGameRoot.getChildren().contains(gameRoot)) {
            this.hostGameRoot.getChildren().add(gameRoot);
        }
        this.overlayAttached = true;

        locateAndStageMapCombatants();
        return true;
    }

    /**
     * Localiza os nós do Herói e do Inimigo no worldLayer do mapa e os afasta
     * horizontalmente (~370px de distância um do outro) para dar espaço amplo
     * à arena de combate.
     */
    private void locateAndStageMapCombatants() {
        if (mapWorldLayer == null) {
            return;
        }

        Hero hero = battleController.getHero();
        Enemy enemy = battleController.getEnemy();
        double hx = hero.getX();
        double hy = hero.getY();

        double bestHeroDist = Double.MAX_VALUE;
        double bestEnemyDist = Double.MAX_VALUE;

        for (Node child : mapWorldLayer.getChildren()) {
            if (child instanceof ImageView imgView) {
                double dist = Math.hypot(imgView.getLayoutX() - hx, imgView.getLayoutY() - hy);
                if (dist < bestHeroDist) {
                    bestHeroDist = dist;
                    mapHeroNode = imgView;
                }
            } else if (child instanceof Pane enemyPane) {
                boolean matchesName = false;
                for (Node sub : enemyPane.getChildren()) {
                    if (sub instanceof Label lbl && enemy.getName().equalsIgnoreCase(lbl.getText())) {
                        matchesName = true;
                        break;
                    }
                }
                double dist = Math.hypot(enemyPane.getLayoutX() - hx, enemyPane.getLayoutY() - hy);
                if ((matchesName || dist <= 180.0) && dist < bestEnemyDist) {
                    bestEnemyDist = dist;
                    mapEnemyNode = enemyPane;
                }
            }
        }

        if (mapHeroNode instanceof ImageView heroImg) {
            heroImg.setSmooth(false);
            try {
                heroImg.setImage(resourceManager.getImage("/images/hero_idle_right.png"));
            } catch (Exception ignored) {
            }
        }

        if (mapHeroNode != null && mapEnemyNode != null) {
            double midX = (mapHeroNode.getLayoutX() + mapEnemyNode.getLayoutX()) / 2.0;
            double midY = (mapHeroNode.getLayoutY() + mapEnemyNode.getLayoutY()) / 2.0;

            double screenMidX = midX + worldBaseTranslateX;
            double targetScreenMidX = Math.max(430.0, Math.min(850.0, screenMidX));
            mapWorldLayer.setTranslateX(worldBaseTranslateX + (targetScreenMidX - screenMidX));

            double screenMidY = midY + worldBaseTranslateY;
            if (screenMidY > 385.0) {
                mapWorldLayer.setTranslateY(worldBaseTranslateY - (screenMidY - 295.0));
            } else if (screenMidY < 175.0) {
                mapWorldLayer.setTranslateY(worldBaseTranslateY + (240.0 - screenMidY));
            }

            heroBaseOffsetX = (midX + HERO_STAGE_OFFSET_FROM_MID) - mapHeroNode.getLayoutX();
            heroBaseOffsetY = midY - mapHeroNode.getLayoutY();

            enemyBaseOffsetX = (midX + ENEMY_STAGE_OFFSET_FROM_MID) - mapEnemyNode.getLayoutX();
            enemyBaseOffsetY = midY - mapEnemyNode.getLayoutY();

            mapHeroNode.setScaleX(1.65);
            mapHeroNode.setScaleY(1.65);
            mapEnemyNode.setScaleX(1.65);
            mapEnemyNode.setScaleY(1.65);

            TranslateTransition alignHero = new TranslateTransition(Duration.millis(220), mapHeroNode);
            alignHero.setToX(heroBaseOffsetX);
            alignHero.setToY(heroBaseOffsetY);

            TranslateTransition alignEnemy = new TranslateTransition(Duration.millis(220), mapEnemyNode);
            alignEnemy.setToX(enemyBaseOffsetX);
            alignEnemy.setToY(enemyBaseOffsetY);

            new ParallelTransition(alignHero, alignEnemy).play();
        }
    }

    /**
     * Caso a BattleView seja aberta fora da cena de exploração (ex.: testes),
     * posiciona sprites pixelados bem espaçados no centro da tela.
     */
    private void buildStandaloneFallbackStage() {
        ImageView standaloneHero = new ImageView();
        standaloneHero.setFitWidth(72);
        standaloneHero.setFitHeight(72);
        standaloneHero.setPreserveRatio(true);
        standaloneHero.setSmooth(false);
        try {
            standaloneHero.setImage(resourceManager.getImage("/images/hero_idle_right.png"));
        } catch (Exception ignored) {
        }
        standaloneHero.setLayoutX(380);
        standaloneHero.setLayoutY(270);

        ImageView standaloneEnemy = new ImageView();
        standaloneEnemy.setFitWidth(72);
        standaloneEnemy.setFitHeight(72);
        standaloneEnemy.setPreserveRatio(true);
        standaloneEnemy.setSmooth(false);
        try {
            standaloneEnemy.setImage(resourceManager.getImage(resolveEnemySpritePath()));
        } catch (Exception ignored) {
        }
        standaloneEnemy.setLayoutX(830);
        standaloneEnemy.setLayoutY(270);

        mapHeroNode = standaloneHero;
        mapEnemyNode = standaloneEnemy;
        arenaFloorLayer.getChildren().addAll(standaloneHero, standaloneEnemy);
    }

    private double getHeroScreenX() {
        if (mapHeroNode != null && mapWorldLayer != null) {
            return mapHeroNode.getLayoutX() + heroBaseOffsetX + mapWorldLayer.getTranslateX() + 20.0;
        }
        return 416.0;
    }

    private double getHeroScreenY() {
        if (mapHeroNode != null && mapWorldLayer != null) {
            return mapHeroNode.getLayoutY() + heroBaseOffsetY + mapWorldLayer.getTranslateY() + 20.0;
        }
        return 305.0;
    }

    private double getEnemyScreenX() {
        if (mapEnemyNode != null && mapWorldLayer != null) {
            return mapEnemyNode.getLayoutX() + enemyBaseOffsetX + mapWorldLayer.getTranslateX() + 20.0;
        }
        return 866.0;
    }

    private double getEnemyScreenY() {
        if (mapEnemyNode != null && mapWorldLayer != null) {
            return mapEnemyNode.getLayoutY() + enemyBaseOffsetY + mapWorldLayer.getTranslateY() + 20.0;
        }
        return 305.0;
    }

    private GridPane create2x2Grid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        row1.setVgrow(Priority.ALWAYS);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        row2.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(row1, row2);

        return grid;
    }

    private Button createChargeButton(String text, ChargeType targetCharge) {
        Button btn = new Button(text);
        btn.getStyleClass().add("charge-pixel-btn");
        btn.setOnAction(e -> {
            if (!battleController.isBattleFinished()
                    && battleController.getCurrentTurn() == BattleController.BattleTurn.PLAYER_TURN
                    && battleController.getHero().getChargeType() != targetCharge) {
                applyHeroChargeSelection(targetCharge);
            }
        });
        return btn;
    }

    private Button createKeyCommandButton(
            String keyText,
            String labelText,
            String hintText,
            PixelIconType iconType,
            boolean synergy) {

        Label keyBadge = new Label(keyText);
        keyBadge.getStyleClass().add("key-badge");

        ImageView pixelIcon = createCommandPixelIcon(iconType);

        HBox graphicBox = new HBox(6, keyBadge, pixelIcon);
        graphicBox.setAlignment(Pos.CENTER_LEFT);

        Background normalBg = createCommandButtonTexture(236, 52, false, synergy);
        Background hoverBg = createCommandButtonTexture(236, 52, true, synergy);

        Button btn = new Button(labelText, graphicBox);
        btn.setContentDisplay(ContentDisplay.LEFT);
        btn.setGraphicTextGap(8);
        btn.getStyleClass().add("command-btn");
        btn.setBackground(normalBg);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMaxHeight(Double.MAX_VALUE);
        btn.addEventHandler(javafx.event.ActionEvent.ACTION, e -> playSfx(SFX_MENU_SELECT));

        btn.setOnMouseEntered(e -> {
            btn.setBackground(hoverBg);
            if (hintText != null && !hintText.isBlank()) {
                actionHintLabel.setText(hintText);
            }
            playSfxModulated(SFX_MENU_SELECT, 0.08, -0.2, 1.45);
        });
        btn.setOnMouseExited(e -> {
            btn.setBackground(normalBg);
            actionHintLabel.setText(DEFAULT_ACTION_HINT);
        });
        btn.focusedProperty().addListener((obs, oldVal, focused) -> {
            btn.setBackground(focused ? hoverBg : normalBg);
            if (focused && hintText != null && !hintText.isBlank()) {
                actionHintLabel.setText(hintText);
            }
        });

        return btn;
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
            playSfxModulated(SFX_THUNDER, 0.20, 0.0, 1.0);
            playSfxModulated(SFX_SHOCK, 0.16, 0.35, 1.1);
            triggerScreenShake(6, 6);
        } else {
            playSfxModulated(SFX_DISCHARGE, 0.15, 0.30, 1.15);
        }
    }

    /**
     * Encerra o áudio de batalha e remove a camada sobreposta ao mapa.
     */
    public void stopAudio() {
        stopMusicOnly();
        cleanupMapOverlay();
    }

    private void stopMusicOnly() {
        if (battleMusicPlayer != null) {
            try {
                battleMusicPlayer.stop();
            } catch (Exception ignored) {
            }
        }
    }

    private void cleanupMapOverlay() {
        if (scene != null && battleKeyFilter != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, battleKeyFilter);
        }

        if (!overlayAttached) {
            return;
        }
        overlayAttached = false;

        if (mapHeroNode != null) {
            mapHeroNode.setTranslateX(0);
            mapHeroNode.setTranslateY(0);
            mapHeroNode.setScaleX(1.0);
            mapHeroNode.setScaleY(1.0);
            mapHeroNode.setOpacity(1.0);
        }
        if (mapEnemyNode != null) {
            mapEnemyNode.setTranslateX(0);
            mapEnemyNode.setTranslateY(0);
            mapEnemyNode.setScaleX(1.0);
            mapEnemyNode.setScaleY(1.0);
            mapEnemyNode.setOpacity(1.0);
        }
        if (mapWorldLayer != null) {
            mapWorldLayer.setTranslateX(worldBaseTranslateX);
            mapWorldLayer.setTranslateY(worldBaseTranslateY);
        }
        if (hostGameRoot != null) {
            hostGameRoot.getChildren().remove(gameRoot);
        }
        if (mapHudLayer != null) {
            mapHudLayer.setVisible(true);
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (battleController.isBattleFinished()
                || battleController.getCurrentTurn() != BattleController.BattleTurn.PLAYER_TURN) {
            event.consume();
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

        if (abilityCommandGrid.isVisible()) {
            List<Ability> learned = battleController.getHero().getAbilityBook().getAbilities();
            if ((code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) && learned.size() >= 1) {
                playSfx(SFX_MENU_SELECT);
                handlePlayerAbility(learned.get(0));
            } else if ((code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) && learned.size() >= 2) {
                playSfx(SFX_MENU_SELECT);
                handlePlayerAbility(learned.get(1));
            } else if ((code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) && learned.size() >= 3) {
                playSfx(SFX_MENU_SELECT);
                handlePlayerAbility(learned.get(2));
            } else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4
                    || code == KeyCode.ESCAPE || code == KeyCode.BACK_SPACE) {
                playSfx(SFX_MENU_SELECT);
                showMainMenu();
            }
            event.consume();
            return;
        }

        if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
            playSfx(SFX_MENU_SELECT);
            handlePlayerAttack();
        } else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
            playSfx(SFX_MENU_SELECT);
            showAbilitySubmenu();
        } else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
            playSfx(SFX_MENU_SELECT);
            handleAnalyzeAction();
        } else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) {
            playSfx(SFX_MENU_SELECT);
            handleGroundingAction();
        }
        event.consume();
    }

    private void cycleHeroCharge(int step) {
        ChargeType[] values = ChargeType.values();
        ChargeType current = battleController.getHero().getChargeType();
        int nextIndex = (current.ordinal() + step + values.length) % values.length;
        applyHeroChargeSelection(values[nextIndex]);
    }

    private void applyHeroChargeSelection(ChargeType selectedCharge) {
        battleController.setHeroChargeType(selectedCharge);
        updateHeroPolarityBadge(selectedCharge);
        updateMenuPixelArtScenes();
        if (abilityCommandGrid.isVisible()) {
            buildAbilitySubmenu();
        }
        double pitch = switch (selectedCharge) {
            case POSITIVE -> 1.25;
            case NEUTRAL -> 1.0;
            case NEGATIVE -> 0.82;
        };
        playSfxModulated(SFX_MENU_SELECT, 0.22, -0.35, pitch);
        playPolarityShiftPulse(selectedCharge);
        telegraphTextLabel.setText("Carga do Heroi alterada para "
                + selectedCharge.getDescription().toUpperCase() + " (" + selectedCharge.getSymbol() + ").");
    }

    private void playSfx(String path) {
        playSfxModulated(path, 0.25, 0.0, 1.0);
    }

    private void playSfxModulated(String path, double volume, double balance, double rate) {
        try {
            AudioClip clip = resourceManager.getAudioClip(path);
            clip.play(volume, balance, rate, 0.0, 0);
        } catch (Exception ignored) {
        }
    }

    private void applyStylesheet() {
        try {
            String css = resourceManager.getStylesheet("/css/battle.css");
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        } catch (Exception e) {
            var url = getClass().getResource("/css/battle.css");
            if (url != null && !scene.getStylesheets().contains(url.toExternalForm())) {
                scene.getStylesheets().add(url.toExternalForm());
            }
        }
    }

    private void loadHeroPortrait() {
        try {
            Image img = resourceManager.getImage("/images/hero_dialogue.png");
            heroPortraitView.setImage(img);
        } catch (Exception e) {
            try {
                heroPortraitView.setImage(resourceManager.getImage("/images/hero_idle_right.png"));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Usa os sprites 2D em Pixel Art nativos dos inimigos.
     */
    private String resolveEnemySpritePath() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof BossEnemy) {
            return "/images/enemy_map_boss.png";
        } else if (enemy instanceof CorruptedDipole) {
            return "/images/enemy_map_dipole.png";
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            return (uncharged.getChargeType() == ChargeType.POSITIVE)
                    ? "/images/enemy_map_charge_pos.png"
                    : "/images/enemy_map_charge_neg.png";
        } else if (enemy instanceof UnstableConductor) {
            return "/images/enemy_map_conductor.png";
        } else if (enemy instanceof ChargedEnemy charged) {
            return (charged.getChargeType() == ChargeType.POSITIVE)
                    ? "/images/enemy_map_charge_pos.png"
                    : "/images/enemy_map_charge_neg.png";
        }
        return "/images/enemy_map_wandering_spark.png";
    }

    private void loadEnemyPortrait() {
        try {
            Image img = resourceManager.getImage(resolveEnemySpritePath());
            enemyPortraitView.setImage(img);
        } catch (Exception ignored) {
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
                        badge.setText("- RUPTURA");
                        badge.getStyleClass().add("polarity-negative");
                    }
                }
                case FIELD -> {
                    String arrow = boss.getFieldDirection() == BossEnemy.FieldDirection.LEFT ? "<-" : "->";
                    badge.setText(arrow + " CAMPO");
                    badge.getStyleClass().add("polarity-rupture");
                }
                case POTENTIAL -> {
                    badge.setText(boss.isPotentialShieldActive() ? "[ESCUDO]" : "[POTENCIAL]");
                    badge.getStyleClass().add("polarity-rupture");
                }
            }
        } else if (enemy instanceof CorruptedDipole dipole) {
            if (dipole.getChargeType() == ChargeType.POSITIVE) {
                badge.setText("+ DIPOLO");
                badge.getStyleClass().add("polarity-positive");
            } else {
                badge.setText("- DIPOLO");
                badge.getStyleClass().add("polarity-negative");
            }
        } else if (enemy instanceof ChargedEnemy charged) {
            ChargeType type = charged.getChargeType();
            if (type == ChargeType.POSITIVE) {
                badge.setText("+ PROTON");
                badge.getStyleClass().add("polarity-positive");
            } else if (type == ChargeType.NEGATIVE) {
                badge.setText("- ELETRON");
                badge.getStyleClass().add("polarity-negative");
            } else {
                badge.setText("0 NEUTRO");
                badge.getStyleClass().add("polarity-neutral");
            }
        } else if (enemy instanceof UnstableConductor conductor) {
            if (conductor.hasStoredEnergy()) {
                badge.setText("SOBRECARGA");
                badge.getStyleClass().add("polarity-rupture");
            } else {
                badge.setText("CONDUTOR");
                badge.getStyleClass().add("polarity-neutral");
            }
        } else {
            badge.setText("OSCILANTE");
            badge.getStyleClass().add("polarity-neutral");
        }
    }

    private void updateHeroPolarityBadge(ChargeType type) {
        heroPolarityBadge.getStyleClass().removeAll("polarity-positive", "polarity-negative", "polarity-neutral");
        btnChargeNeg.getStyleClass().remove("charge-pixel-btn-active-neg");
        btnChargeNeu.getStyleClass().remove("charge-pixel-btn-active-neu");
        btnChargePos.getStyleClass().remove("charge-pixel-btn-active-pos");

        if (type == ChargeType.POSITIVE) {
            heroPolarityBadge.setText("+ PROTON");
            heroPolarityBadge.getStyleClass().add("polarity-positive");
            btnChargePos.getStyleClass().add("charge-pixel-btn-active-pos");
        } else if (type == ChargeType.NEGATIVE) {
            heroPolarityBadge.setText("- ELETRON");
            heroPolarityBadge.getStyleClass().add("polarity-negative");
            btnChargeNeg.getStyleClass().add("charge-pixel-btn-active-neg");
        } else {
            heroPolarityBadge.setText("0 NEUTRO");
            heroPolarityBadge.getStyleClass().add("polarity-neutral");
            btnChargeNeu.getStyleClass().add("charge-pixel-btn-active-neu");
        }
    }

    private String buildInitialTelegraphText() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof BossEnemy) {
            return "Guardiao da Ruptura bloqueia a Camara! Observe a fase antes de agir.";
        } else if (enemy instanceof WanderingSpark) {
            return "Faisca Errante surgiu no caminho! Escolha uma acao.";
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            String sign = (uncharged.getChargeType() == ChargeType.POSITIVE) ? "POSITIVA (+)" : "NEGATIVA (-)";
            return "Carga Descontrolada " + sign + " detectada! Ajuste sua polaridade.";
        } else if (enemy instanceof CorruptedDipole) {
            return "Dipolo Corrompido bloqueia a trilha! Ele alterna o sinal a cada turno.";
        } else if (enemy instanceof UnstableConductor) {
            return "Condutor Instavel no caminho! Ele acumula energia para descarregar.";
        }
        return enemy.getName() + " bloqueia o caminho!";
    }

    private String buildPhysicsInsightText() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof BossEnemy boss) {
            return switch (boss.getPhase()) {
                case POLARITY -> "LEITURA: Fase 1 (" + boss.getChargeType().getSymbol()
                        + "). Cargas iguais se repelem com Pulso de Repulsao.";
                case FIELD -> "LEITURA: Fase 2 ("
                        + (boss.getFieldDirection() == BossEnemy.FieldDirection.RIGHT ? "-> DIREITA" : "<- ESQUERDA")
                        + "). Impulso de Campo causa 26 de dano com campo -> Direita.";
                case POTENTIAL -> "LEITURA: Fase 3 ("
                        + (boss.isPotentialShieldActive() ? "ESCUDO ATIVO -50% DANO" : "ESCUDO INATIVO")
                        + "). Use Escudo Equipotencial para reduzir o ataque.";
            };
        } else if (enemy instanceof CorruptedDipole dipole) {
            return "LEITURA: Polo atual (" + dipole.getChargeType().getSymbol()
                    + "). Troca entre + e - apos cada ataque.";
        } else if (enemy instanceof ChargedEnemy charged) {
            if (charged.getChargeType() == ChargeType.POSITIVE) {
                return "LEITURA: Carga Positiva (+). Use carga (+) com Pulso de Repulsao para interromper.";
            } else if (charged.getChargeType() == ChargeType.NEGATIVE) {
                return "LEITURA: Carga Negativa (-). Sofre 26 de dano do Impulso de Campo.";
            }
        } else if (enemy instanceof UnstableConductor conductor) {
            if (conductor.hasStoredEnergy()) {
                return "LEITURA: SOBRECARREGADO! Vai descarregar dano duplo no proximo turno.";
            }
            return "LEITURA: Acumula carga em 1 turno e dispara no seguinte.";
        }
        return "LEITURA: Sem polaridade fixa. Ataques diretos causam 20 de dano.";
    }

    private void handlePlayerAttack() {
        if (battleController.isBattleFinished()) {
            return;
        }

        groundingActiveThisRound = false;
        setButtonsDisabled(true);

        playSfxModulated(SFX_DISCHARGE, 0.20, -0.40, 1.28);
        Timeline impactSfx = new Timeline(new KeyFrame(Duration.millis(140), ev -> {
            playSfxModulated(SFX_HIT, 0.26, 0.45, 1.0);
            playSfxModulated(SFX_SHOCK, 0.14, 0.45, 1.35);
        }));
        impactSfx.play();

        playHeroAttackAnimation();
        triggerScreenShake(5, 4);
        playEnemyHitReaction(false);

        float enemyHpBefore = battleController.getEnemy().getHealth();
        battleController.performPlayerAttack(PLAYER_DAMAGE);
        float damageDealt = Math.max(0.0f, enemyHpBefore - battleController.getEnemy().getHealth());
        showFloatingDamage(damageDealt, false, false);

        updateView();

        if (battleController.isBattleFinished()) {
            return;
        }

        telegraphTextLabel.setText("Heroi atacou! Causou " + Math.round(damageDealt) + " de dano.");

        Timeline enemyTurnTimer = new Timeline(new KeyFrame(Duration.millis(900), e -> {
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
            playSfxModulated(SFX_POSITIVE, 0.22, -0.35, 0.92);
            playSfxModulated(SFX_DISCHARGE, 0.16, -0.35, 0.80);
        } else if (ability instanceof RepulsionPulse) {
            playRepulsionPulseAnimation();
            playSfxModulated(SFX_DISCHARGE, 0.22, -0.25, 1.15);
            playSfxModulated(SFX_SHOCK, 0.18, 0.35, 1.20);
            playSfxModulated(SFX_HIT, 0.25, 0.45, 1.10);
            triggerScreenShake(6, 4);
            playEnemyHitReaction(true);
        } else if (ability instanceof FieldImpulse) {
            playFieldImpulseAnimation(isBonusHit);
            playSfxModulated(SFX_SHOCK, 0.22, 0.20, isBonusHit ? 0.95 : 1.15);
            playSfxModulated(SFX_HIT, 0.28, 0.45, isBonusHit ? 0.88 : 1.0);
            if (isBonusHit) {
                playSfxModulated(SFX_THUNDER, 0.18, 0.30, 1.35);
                triggerScreenShake(8, 6);
            } else {
                triggerScreenShake(6, 4);
            }
            playEnemyHitReaction(isBonusHit);
        } else {
            playAbilityAnimation();
            playSfxModulated(SFX_DISCHARGE, 0.22, 0.0, 1.0);
            triggerScreenShake(5, 4);
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

        telegraphTextLabel.setText("Usou " + ability.getName() + "! "
                + (damageDealt > 0
                        ? "Causou " + Math.round(damageDealt) + " de dano" + (isBonusHit ? " (BONUS DE CAMPO)!" : ".")
                        : "Escudo equipotencial ativado!"));

        Timeline enemyTurnTimer = new Timeline(new KeyFrame(Duration.millis(950), e -> {
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
                    + " INTERROMPIDO por repulsao de mesma carga!");
            playSfxModulated(SFX_DISCHARGE, 0.22, 0.35, 1.40);
        } else if (damageTaken > 0.0f) {
            boolean heavyAttack = damageTaken >= 10.0f;
            playEnemyAttackAnimation(heavyAttack);
            playSfxModulated(SFX_SHOCK, 0.20, 0.35, heavyAttack ? 0.82 : 1.05);
            Timeline heroHitSfx = new Timeline(new KeyFrame(Duration.millis(140), ev -> {
                playSfxModulated(SFX_HIT, 0.28, -0.45, heavyAttack ? 0.80 : 0.95);
                if (heavyAttack) {
                    playSfxModulated(SFX_THUNDER, 0.18, -0.35, 1.15);
                }
            }));
            heroHitSfx.play();
            playHeroHitEffects(heavyAttack);
            showFloatingDamage(damageTaken, true, heavyAttack);
            telegraphTextLabel.setText(enemy.getName() + " contra-atacou! Heroi recebeu "
                    + Math.round(damageTaken) + " de dano.");
        } else {
            playEnemyChargeAnimation();
            playSfxModulated(SFX_DISCHARGE, 0.18, 0.40, 0.72);
            if (enemy instanceof UnstableConductor conductor && conductor.hasStoredEnergy()) {
                telegraphTextLabel.setText(enemy.getName()
                        + " acumulou energia! Prepare-se para descarga dupla!");
            } else {
                telegraphTextLabel.setText(enemy.getName() + " alterou a configuracao do campo.");
            }
        }

        updateView();

        if (!battleController.isBattleFinished()) {
            setButtonsDisabled(false);
        }
    }

    private void handleAnalyzeAction() {
        if (battleController.isBattleFinished()) {
            return;
        }

        Enemy enemy = battleController.getEnemy();
        playAnalyzeAnimation();
        playSfxModulated(SFX_MENU_SELECT, 0.22, 0.30, 1.35);
        playSfxModulated(SFX_POSITIVE, 0.15, 0.35, 1.50);

        String analysis;
        if (enemy instanceof BossEnemy boss) {
            analysis = "ANALISE: Chefe Fase " + (boss.getPhase().ordinal() + 1) + "/3. "
                    + switch (boss.getPhase()) {
                        case POLARITY -> "Carga (" + boss.getChargeType().getSymbol() + "). Use Pulso com a mesma carga.";
                        case FIELD -> "Campo " + (boss.getFieldDirection() == BossEnemy.FieldDirection.RIGHT ? "-> Direita (Bonus 26 dano)" : "<- Esquerda") + ".";
                        case POTENTIAL -> boss.isPotentialShieldActive() ? "Escudo ativo (-50% dano)." : "Escudo desativado neste turno!";
                    };
        } else if (enemy instanceof CorruptedDipole dipole) {
            analysis = "ANALISE: Dipolo (" + dipole.getChargeType().getSymbol() + "). Alterna o sinal apos cada ataque.";
        } else if (enemy instanceof ChargedEnemy charged) {
            analysis = "ANALISE: Carga " + charged.getChargeType().getDescription()
                    + " (" + charged.getChargeType().getSymbol() + "). Sinais iguais se repelem.";
        } else if (enemy instanceof UnstableConductor conductor) {
            analysis = "ANALISE: Condutor " + (conductor.hasStoredEnergy() ? "SOBRECARREGADO (use Escudo!)." : "carregando energia.");
        } else {
            analysis = "ANALISE: Faisca sem polaridade fixa (HP " + Math.round(enemy.getHealth()) + ").";
        }

        telegraphTextLabel.setText(analysis);
    }

    private void handleGroundingAction() {
        if (battleController.isBattleFinished()) {
            return;
        }

        groundingActiveThisRound = true;
        stanceStatusLabel.setText("POSTURA: ATERRADO");
        telegraphTextLabel.setText("Heroi conectou aterramento ao solo e aguardou a acao inimiga.");
        playGroundingAnimation();
        playSfxModulated(SFX_DISCHARGE, 0.20, -0.35, 0.72);

        setButtonsDisabled(true);

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
        commandHeader.setText("+ TECNICAS [1-3] | [4/ESC] VOLTAR +");
        mainCommandGrid.setVisible(false);
        mainCommandGrid.setManaged(false);
        abilityCommandGrid.setVisible(true);
        abilityCommandGrid.setManaged(true);
    }

    private void buildAbilitySubmenu() {
        abilityCommandGrid.getChildren().clear();

        List<Ability> learnedAbilities = battleController.getHero().getAbilityBook().getAbilities();

        if (learnedAbilities.isEmpty()) {
            Label emptyNotice = new Label("Nenhuma tecnica aprendida.\nFale com NPCs e Ion!");
            emptyNotice.getStyleClass().add("command-hint-text");
            abilityCommandGrid.add(emptyNotice, 0, 0, 2, 1);
        } else {
            for (int i = 0; i < Math.min(3, learnedAbilities.size()); i++) {
                Ability ability = learnedAbilities.get(i);
                String synergyTag = evaluateAbilitySynergyTag(ability);
                boolean hasSynergy = !synergyTag.isEmpty();
                String shortName = shortenAbilityName(ability) + (hasSynergy ? " [+]" : "");
                String hint = ability.getDescription() + (hasSynergy ? " [" + synergyTag + "]" : "");

                Button btnSkill = createKeyCommandButton(
                        String.valueOf(i + 1),
                        shortName,
                        hint,
                        resolveAbilityIcon(ability),
                        hasSynergy
                );
                btnSkill.setOnAction(e -> handlePlayerAbility(ability));
                abilityCommandGrid.add(btnSkill, i % 2, i / 2);
            }
        }

        Button btnBack = createKeyCommandButton(
                "4",
                "VOLTAR",
                "Retorna aos comandos principais.",
                PixelIconType.BACK,
                false
        );
        btnBack.setOnAction(e -> showMainMenu());
        abilityCommandGrid.add(btnBack, 1, 1);
    }

    private PixelIconType resolveAbilityIcon(Ability ability) {
        if (ability instanceof RepulsionPulse) {
            return PixelIconType.PULSE;
        }
        if (ability instanceof FieldImpulse) {
            return PixelIconType.FIELD;
        }
        if (ability instanceof EquipotentialShield) {
            return PixelIconType.SHIELD;
        }
        return PixelIconType.TECHNIQUES;
    }

    private String shortenAbilityName(Ability ability) {
        if (ability instanceof RepulsionPulse) {
            return "PULSO REPULSAO";
        }
        if (ability instanceof FieldImpulse) {
            return "IMPULSO CAMPO";
        }
        if (ability instanceof EquipotentialShield) {
            return "ESCUDO EQUIP.";
        }
        return ability.getName().toUpperCase();
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
                return "INTERROMPE ATAQUE";
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
                return "BONUS 26 DANO";
            }
        } else if (ability instanceof EquipotentialShield) {
            if (hero.isUnstable()) {
                return "CURA INSTAVEL";
            }
            if ((enemy instanceof UnstableConductor conductor && conductor.hasStoredEnergy())
                    || (enemy instanceof BossEnemy boss && boss.getPhase() == BossEnemy.Phase.POTENTIAL)) {
                return "RECOMENDADO";
            }
        }
        return "";
    }

    private void showMainMenu() {
        commandHeader.setText("+ ACOES DE COMBATE +");
        actionHintLabel.setText(DEFAULT_ACTION_HINT);
        abilityCommandGrid.setVisible(false);
        abilityCommandGrid.setManaged(false);
        mainCommandGrid.setVisible(true);
        mainCommandGrid.setManaged(true);
    }

    /* =========================================================================
       ANIMAÇÕES 2D PIXEL ART DIRETO NOS PERSONAGENS DO MAPA
       ========================================================================= */

    private void animateHeroNode(double dx, double dy, double durationMs) {
        if (mapHeroNode == null) {
            return;
        }
        TranslateTransition lunge = new TranslateTransition(Duration.millis(durationMs), mapHeroNode);
        lunge.setFromX(heroBaseOffsetX);
        lunge.setFromY(heroBaseOffsetY);
        lunge.setToX(heroBaseOffsetX + dx);
        lunge.setToY(heroBaseOffsetY + dy);
        lunge.setAutoReverse(true);
        lunge.setCycleCount(2);
        lunge.play();
    }

    private void animateEnemyNode(double dx, double dy, double durationMs) {
        if (mapEnemyNode == null) {
            return;
        }
        TranslateTransition lunge = new TranslateTransition(Duration.millis(durationMs), mapEnemyNode);
        lunge.setFromX(enemyBaseOffsetX);
        lunge.setFromY(enemyBaseOffsetY);
        lunge.setToX(enemyBaseOffsetX + dx);
        lunge.setToY(enemyBaseOffsetY + dy);
        lunge.setAutoReverse(true);
        lunge.setCycleCount(2);
        lunge.play();
    }

    private void playHeroAttackAnimation() {
        animateHeroNode(85, 0, 130);
        double hx = getHeroScreenX() + 18;
        double hy = getHeroScreenY();
        double ex = getEnemyScreenX() - 12;
        double ey = getEnemyScreenY();
        playPixelBolt(hx, hy, ex, ey, Color.web("#F0B429"), false);
        playPixelSlashEffect(ex, ey, Color.web("#F0B429"));
    }

    private void playRepulsionPulseAnimation() {
        animateHeroNode(52, 0, 130);
        ChargeType charge = battleController.getHero().getChargeType();
        Color pulseColor = (charge == ChargeType.NEGATIVE) ? Color.web("#5CA6AE") : Color.web("#FF6B6B");

        double hx = getHeroScreenX();
        double hy = getHeroScreenY();
        double ex = getEnemyScreenX();
        double ey = getEnemyScreenY();

        playPixelExpandingBox(hx, hy, 90, pulseColor, 260);
        playPixelBolt(hx + 16, hy, ex - 12, ey, pulseColor, true);
    }

    private void playFieldImpulseAnimation(boolean isBonus) {
        animateHeroNode(92, 0, 140);
        Color fieldColor = isBonus ? Color.web("#F0B429") : Color.web("#5CA6AE");

        double hx = getHeroScreenX();
        double hy = getHeroScreenY();
        double ex = getEnemyScreenX();
        double ey = getEnemyScreenY();

        for (int i = -1; i <= 1; i++) {
            double yPos = hy + (i * 18);
            Line fieldLine = new Line(hx, yPos, ex, yPos);
            fieldLine.setStroke(fieldColor);
            fieldLine.setStrokeWidth(3.0);
            fieldLine.getStrokeDashArray().addAll(10.0, 6.0);
            fieldLine.setMouseTransparent(true);
            gameRoot.getChildren().add(fieldLine);

            FadeTransition fade = new FadeTransition(Duration.millis(340), fieldLine);
            fade.setFromValue(0.95);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> gameRoot.getChildren().remove(fieldLine));
            fade.play();
        }

        playPixelBolt(hx + 16, hy, ex - 12, ey, fieldColor, isBonus);
        if (isBonus) {
            playPixelSlashEffect(ex, ey, Color.web("#FFF8E7"));
        }
    }

    private void playAbilityAnimation() {
        animateHeroNode(60, 0, 140);
        double hx = getHeroScreenX();
        double hy = getHeroScreenY();
        double ex = getEnemyScreenX();
        double ey = getEnemyScreenY();
        playPixelExpandingBox(hx, hy, 80, Color.web("#5CA6AE"), 280);
        playPixelBolt(hx + 16, hy, ex - 12, ey, Color.web("#5CA6AE"), false);
    }

    private void playShieldAnimation() {
        double hx = getHeroScreenX();
        double hy = getHeroScreenY();

        Rectangle shieldBox = new Rectangle(64, 64, Color.rgb(70, 184, 102, 0.22));
        shieldBox.setStroke(Color.web("#6EE787"));
        shieldBox.setStrokeWidth(3.0);
        shieldBox.setLayoutX(hx - 32);
        shieldBox.setLayoutY(hy - 32);
        shieldBox.setMouseTransparent(true);
        gameRoot.getChildren().add(shieldBox);

        ScaleTransition scale = new ScaleTransition(Duration.millis(420), shieldBox);
        scale.setFromX(0.6);
        scale.setFromY(0.6);
        scale.setToX(1.25);
        scale.setToY(1.25);

        FadeTransition fade = new FadeTransition(Duration.millis(420), shieldBox);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(scale, fade);
        anim.setOnFinished(e -> gameRoot.getChildren().remove(shieldBox));
        anim.play();
    }

    private void playAnalyzeAnimation() {
        double ex = getEnemyScreenX();
        double ey = getEnemyScreenY();

        Rectangle scanFrame = new Rectangle(68, 68, Color.TRANSPARENT);
        scanFrame.setStroke(Color.web("#F0B429"));
        scanFrame.setStrokeWidth(2.5);
        scanFrame.getStrokeDashArray().addAll(8.0, 6.0);
        scanFrame.setLayoutX(ex - 34);
        scanFrame.setLayoutY(ey - 34);
        scanFrame.setMouseTransparent(true);
        gameRoot.getChildren().add(scanFrame);

        FadeTransition fade = new FadeTransition(Duration.millis(450), scanFrame);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> gameRoot.getChildren().remove(scanFrame));
        fade.play();
    }

    private void playGroundingAnimation() {
        double hx = getHeroScreenX();
        double hy = getHeroScreenY();
        playPixelSparks(hx, hy + 18, Color.web("#6EE787"), 6);
    }

    private void playPolarityShiftPulse(ChargeType selectedCharge) {
        Color color = switch (selectedCharge) {
            case POSITIVE -> Color.web("#FF6B6B");
            case NEGATIVE -> Color.web("#5CA6AE");
            case NEUTRAL -> Color.web("#F0B429");
        };
        playPixelExpandingBox(getHeroScreenX(), getHeroScreenY(), 56, color, 220);
    }

    private void playInterruptedAnimation() {
        double ex = getEnemyScreenX();
        double ey = getEnemyScreenY();
        animateEnemyNode(18, 0, 75);
        playPixelExpandingBox(ex, ey, 72, Color.web("#F0B429"), 300);
        playPixelSparks(ex, ey, Color.web("#F0B429"), 8);
    }

    private void playEnemyChargeAnimation() {
        double ex = getEnemyScreenX();
        double ey = getEnemyScreenY();
        playPixelExpandingBox(ex, ey, 64, Color.web("#F0B429"), 340);
        playPixelSparks(ex, ey, Color.web("#F0B429"), 6);
    }

    private void playEnemyAttackAnimation(boolean heavyAttack) {
        animateEnemyNode(-85, 0, 130);
        double ex = getEnemyScreenX() - 16;
        double ey = getEnemyScreenY();
        double hx = getHeroScreenX() + 12;
        double hy = getHeroScreenY();

        Color boltColor = heavyAttack ? Color.web("#F0B429") : Color.web("#FF6B6B");
        playPixelBolt(ex, ey, hx, hy, boltColor, heavyAttack);
        playPixelSlashEffect(hx, hy, boltColor);
    }

    private void playEnemyHitReaction(boolean criticalHit) {
        if (mapEnemyNode != null) {
            Timeline blink = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(mapEnemyNode.opacityProperty(), 0.25)),
                    new KeyFrame(Duration.millis(70), new KeyValue(mapEnemyNode.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(140), new KeyValue(mapEnemyNode.opacityProperty(), 0.25)),
                    new KeyFrame(Duration.millis(210), new KeyValue(mapEnemyNode.opacityProperty(), 1.0))
            );
            blink.play();
        }
        animateEnemyNode(criticalHit ? 22 : 12, 0, 65);
    }

    private void playHeroHitEffects(boolean heavyAttack) {
        Timeline flashTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(damageFlash.fillProperty(), Color.rgb(220, 38, 38, 0.0))),
                new KeyFrame(Duration.millis(65),
                        new KeyValue(damageFlash.fillProperty(), Color.rgb(220, 38, 38, heavyAttack ? 0.36 : 0.22))),
                new KeyFrame(Duration.millis(240),
                        new KeyValue(damageFlash.fillProperty(), Color.rgb(220, 38, 38, 0.0)))
        );
        flashTimeline.play();

        if (mapHeroNode != null) {
            Timeline blink = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(mapHeroNode.opacityProperty(), 0.25)),
                    new KeyFrame(Duration.millis(70), new KeyValue(mapHeroNode.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(140), new KeyValue(mapHeroNode.opacityProperty(), 0.25)),
                    new KeyFrame(Duration.millis(210), new KeyValue(mapHeroNode.opacityProperty(), 1.0))
            );
            blink.play();
        }

        triggerScreenShake(heavyAttack ? 8 : 5, heavyAttack ? 6 : 4);
    }

    private void triggerScreenShake(double intensity, int shakes) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(42), gameRoot);
        shake.setFromX(-intensity);
        shake.setToX(intensity);
        shake.setCycleCount(shakes);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> gameRoot.setTranslateX(0));
        shake.play();
    }

    private void playPixelBolt(
            double startX,
            double startY,
            double endX,
            double endY,
            Color color,
            boolean empowered) {

        double size = empowered ? 16 : 12;
        Rectangle outerBlock = new Rectangle(size, size, color);
        outerBlock.setStroke(Color.web("#06121C"));
        outerBlock.setStrokeWidth(2.0);

        Rectangle innerCore = new Rectangle(size * 0.5, size * 0.5, Color.WHITE);

        StackPane pixelBolt = new StackPane(outerBlock, innerCore);
        pixelBolt.setLayoutX(startX - size / 2.0);
        pixelBolt.setLayoutY(startY - size / 2.0);
        pixelBolt.setMouseTransparent(true);
        gameRoot.getChildren().add(pixelBolt);

        TranslateTransition move = new TranslateTransition(Duration.millis(165), pixelBolt);
        move.setByX(endX - startX);
        move.setByY(endY - startY);
        move.setInterpolator(Interpolator.LINEAR);
        move.setOnFinished(e -> {
            gameRoot.getChildren().remove(pixelBolt);
            playPixelSparks(endX, endY, color, empowered ? 10 : 6);
        });
        move.play();
    }

    private void playPixelSlashEffect(double x, double y, Color color) {
        Line slash1 = new Line(x - 20, y - 20, x + 20, y + 20);
        slash1.setStroke(Color.WHITE);
        slash1.setStrokeWidth(4.0);
        slash1.setMouseTransparent(true);

        Line slash2 = new Line(x - 20, y + 20, x + 20, y - 20);
        slash2.setStroke(color);
        slash2.setStrokeWidth(3.0);
        slash2.setMouseTransparent(true);

        gameRoot.getChildren().addAll(slash1, slash2);

        PauseTransition hold = new PauseTransition(Duration.millis(130));
        hold.setOnFinished(e -> gameRoot.getChildren().removeAll(slash1, slash2));
        hold.play();
    }

    private void playPixelExpandingBox(
            double centerX,
            double centerY,
            double targetSize,
            Color color,
            double durationMs) {

        Rectangle box = new Rectangle(20, 20, Color.TRANSPARENT);
        box.setStroke(color);
        box.setStrokeWidth(3.0);
        box.setLayoutX(centerX - 10);
        box.setLayoutY(centerY - 10);
        box.setMouseTransparent(true);
        gameRoot.getChildren().add(box);

        double ratio = targetSize / 20.0;
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), box);
        scale.setToX(ratio);
        scale.setToY(ratio);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), box);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(scale, fade);
        anim.setOnFinished(e -> gameRoot.getChildren().remove(box));
        anim.play();
    }

    private void playPixelSparks(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            double dist = 26.0 + ((i % 2) * 12.0);
            double dx = Math.cos(angle) * dist;
            double dy = Math.sin(angle) * dist;

            Rectangle pixel = new Rectangle(6, 6, (i % 2 == 0) ? Color.WHITE : color);
            pixel.setStroke(Color.web("#06121C"));
            pixel.setStrokeWidth(1.0);
            pixel.setLayoutX(x - 3);
            pixel.setLayoutY(y - 3);
            pixel.setMouseTransparent(true);
            gameRoot.getChildren().add(pixel);

            TranslateTransition move = new TranslateTransition(Duration.millis(260), pixel);
            move.setByX(dx);
            move.setByY(dy);
            move.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(260), pixel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition anim = new ParallelTransition(move, fade);
            anim.setOnFinished(e -> gameRoot.getChildren().remove(pixel));
            anim.play();
        }
    }

    private void showFloatingDamage(float damage, boolean onHero, boolean criticalOrHeavy) {
        if (damage <= 0.0f) {
            return;
        }

        String text = "-" + Math.round(damage) + (criticalOrHeavy && !onHero ? "!" : "");
        Label damageLabel = new Label(text);
        damageLabel.getStyleClass().add(onHero ? "damage-number-hero" : "damage-number-enemy");
        double targetX = onHero ? getHeroScreenX() - 18 : getEnemyScreenX() - 18;
        double targetY = (onHero ? getHeroScreenY() : getEnemyScreenY()) - 38;
        damageLabel.setLayoutX(targetX);
        damageLabel.setLayoutY(targetY);
        gameRoot.getChildren().add(damageLabel);

        TranslateTransition rise = new TranslateTransition(Duration.millis(750), damageLabel);
        rise.setByY(-36);
        FadeTransition fade = new FadeTransition(Duration.millis(750), damageLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        ParallelTransition animation = new ParallelTransition(rise, fade);
        animation.setOnFinished(event -> gameRoot.getChildren().remove(damageLabel));
        animation.play();
    }

    private void setButtonsDisabled(boolean disabled) {
        btnAttack.setDisable(disabled);
        btnAbilities.setDisable(disabled);
        btnAnalyze.setDisable(disabled);
        btnGrounding.setDisable(disabled);
        btnChargeNeg.setDisable(disabled);
        btnChargeNeu.setDisable(disabled);
        btnChargePos.setDisable(disabled);
    }

    private void updateHeroStanceStatus(Hero hero) {
        stanceStatusLabel.getStyleClass().removeAll(
                "hero-condition-text",
                "hero-condition-shield",
                "hero-condition-unstable"
        );

        if (hero.isEquipotentialShieldActive()) {
            stanceStatusLabel.setText("POSTURA: ESCUDO ATIVO (-50% DANO)");
            stanceStatusLabel.getStyleClass().add("hero-condition-shield");
        } else if (hero.isUnstable()) {
            stanceStatusLabel.setText("POSTURA: INSTAVEL (+2 DANO)");
            stanceStatusLabel.getStyleClass().add("hero-condition-unstable");
        } else if (groundingActiveThisRound) {
            stanceStatusLabel.setText("POSTURA: ATERRADO");
            stanceStatusLabel.getStyleClass().add("hero-condition-text");
        } else {
            stanceStatusLabel.setText("POSTURA: NORMAL");
            stanceStatusLabel.getStyleClass().add("hero-condition-text");
        }
    }

    private void updateMenuPixelArtScenes() {
        Hero hero = battleController.getHero();
        Enemy enemy = battleController.getEnemy();
        battleMenuArtView.setImage(createBattleMenuPixelScene(hero, enemy));
        topDuelCrestView.setImage(createTopDuelCrestImage(hero, enemy));
    }

    private void updateView() {
        Hero hero = battleController.getHero();
        Enemy enemy = battleController.getEnemy();

        // 1. Atualiza HP do Inimigo
        float enemyHp = Math.max(0.0f, enemy.getHealth());
        double enemyHpRatio = Math.max(0.0, Math.min(1.0, enemyHp / maxEnemyHealth));
        enemyHpBar.setWidth(HP_BAR_MAX_WIDTH * enemyHpRatio);
        enemyHpText.setText(Math.round(enemyHp) + " / " + Math.round(maxEnemyHealth));

        // 2. Atualiza HP do Herói
        float heroHp = Math.max(0.0f, hero.getHealth());
        double heroHpRatio = Math.max(0.0, Math.min(1.0, heroHp / maxHeroHealth));
        heroHpBar.setWidth(HP_BAR_MAX_WIDTH * heroHpRatio);
        heroHpText.setText(Math.round(heroHp) + " / " + Math.round(maxHeroHealth));

        // 3. Atualiza Polaridade, Postura e Artes 2D do Menu
        updateHeroPolarityBadge(hero.getChargeType());
        updateHeroStanceStatus(hero);
        updateMenuPixelArtScenes();

        // 4. Atualiza Contagem de Habilidades no Botão Principal
        int abilityCount = hero.getAbilityBook().getAbilities().size();
        btnAbilities.setText(abilityCount > 0
                ? "TECNICAS (" + abilityCount + ")"
                : "TECNICAS");

        // 5. Atualiza Polaridade, Fase e Insight Físico do Inimigo / Chefe
        applyEnemyPolarityBadgeState(enemyPolarityBadge, enemy);
        physicsInsightLabel.setText(buildPhysicsInsightText());

        if (enemy instanceof BossEnemy boss) {
            String phaseDescription = switch (boss.getPhase()) {
                case POLARITY -> "FASE 1/3 :: POLARIDADE (" + boss.getChargeType().getSymbol() + ")";
                case FIELD -> "FASE 2/3 :: CAMPO (" + (boss.getFieldDirection() == BossEnemy.FieldDirection.LEFT ? "<- ESQ" : "-> DIR") + ")";
                case POTENTIAL -> "FASE 3/3 :: POTENCIAL (" + (boss.isPotentialShieldActive() ? "ESCUDO ATIVO" : "VULNERAVEL") + ")";
            };
            enemyPhaseLabel.setText(phaseDescription);
        } else if (enemy instanceof ChargedEnemy chargedEnemy) {
            enemyPhaseLabel.setText("CARGA: " + chargedEnemy.getChargeType().getDescription().toUpperCase()
                    + " (" + chargedEnemy.getChargeType().getSymbol() + ")");
        } else if (enemy instanceof UnstableConductor conductor) {
            enemyPhaseLabel.setText(conductor.hasStoredEnergy() ? "ESTADO: PRONTO P/ DESCARGA" : "ESTADO: ACUMULANDO CARGA");
        } else {
            enemyPhaseLabel.setText("CARGA: INSTAVEL / OSCILANTE");
        }

        // 6. Atualiza Contador de Rodadas
        roundCounterLabel.setText("+ RODADA " + (battleController.getCompletedRounds() + 1)
                + " :: " + (battleController.getCurrentTurn() == BattleController.BattleTurn.PLAYER_TURN ? "SEU TURNO +" : "TURNO INIMIGO +"));

        // 7. Verifica Encerramento
        if (battleController.isBattleFinished()) {
            setButtonsDisabled(true);

            switch (battleController.getResult()) {
                case VICTORY -> {
                    roundCounterLabel.setText("+ EQUILIBRIO RESTAURADO +");
                    telegraphTextLabel.setText(enemy.getName() + " foi neutralizado! O caminho no mapa esta livre.");
                    if (mapEnemyNode != null) {
                        FadeTransition fadeOutEnemy = new FadeTransition(Duration.millis(450), mapEnemyNode);
                        fadeOutEnemy.setToValue(0.15);
                        fadeOutEnemy.play();
                    }
                    if (!endBannerShown) {
                        endBannerShown = true;
                        showBattleEndBanner(true);
                    }
                }
                case DEFEAT -> {
                    roundCounterLabel.setText("- COLAPSO DE EQUILIBRIO -");
                    telegraphTextLabel.setText("Suas forcas se esgotaram. Retornando ao ultimo checkpoint...");
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

    /**
     * Exibe o painel de resultado (Vitória ou Derrota) com ilustração 2D em Pixel Art
     * e a mesma arquitetura de caixas texturizadas do menu de combate.
     */
    private void showBattleEndBanner(boolean victory) {
        stopMusicOnly();

        Hero hero = battleController.getHero();
        Enemy enemy = battleController.getEnemy();

        ImageView emblemArt = createEndBannerPixelArt(victory);

        Label leftSign = new Label(victory ? "+" : "-");
        leftSign.getStyleClass().add("polarity-badge");
        leftSign.getStyleClass().add(victory ? "polarity-positive" : "polarity-negative");

        Label title = new Label(victory ? "EQUILIBRIO RESTAURADO" : "COLAPSO DE EQUILIBRIO");
        title.getStyleClass().add(victory ? "end-title-victory" : "end-title-defeat");

        Label rightSign = new Label(victory ? "+" : "-");
        rightSign.getStyleClass().add("polarity-badge");
        rightSign.getStyleClass().add(victory ? "polarity-positive" : "polarity-negative");

        HBox headerRow = new HBox(14, leftSign, title, rightSign);
        headerRow.setAlignment(Pos.CENTER);

        Label subtitle = new Label(victory
                ? "O campo eletrostatico local foi estabilizado com sucesso."
                : "A sobrecarga eletrica superou a resistencia do Heroi.");
        subtitle.getStyleClass().add("telegraph-text");

        Label targetStat = new Label("ALVO: " + enemy.getName().toUpperCase()
                + (victory ? " [NEUTRALIZADO]" : " [ATIVO]"));
        targetStat.getStyleClass().add("command-hint-text");

        Label heroStat = new Label("HP HEROI: " + Math.round(Math.max(0.0f, hero.getHealth()))
                + " / " + Math.round(maxHeroHealth)
                + "  |  RODADAS: " + (battleController.getCompletedRounds() + 1));
        heroStat.getStyleClass().add("hp-text");

        VBox statsBox = new VBox(4, targetStat, heroStat);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getStyleClass().add("end-stat-row");
        statsBox.setBackground(createDarkInsetTexture(460, 52));

        Label footerBadge = new Label(victory
                ? ">> RETORNANDO A EXPLORACAO DO MAPA <<"
                : ">> RETORNANDO AO ULTIMO CHECKPOINT <<");
        footerBadge.getStyleClass().add("end-footer-badge");

        VBox banner = new VBox(8, emblemArt, headerRow, subtitle, statsBox, footerBadge);
        banner.setAlignment(Pos.CENTER);
        banner.getStyleClass().add(victory ? "end-banner-victory" : "end-banner-defeat");
        banner.setBackground(victory
                ? createGoldMenuTexture(540, 248)
                : createCrimsonDefeatTexture(540, 248));
        banner.setLayoutX((LOGICAL_WIDTH - 540) / 2.0);
        banner.setLayoutY(134);
        banner.setPrefWidth(540);

        gameRoot.getChildren().add(banner);

        if (victory) {
            playSfxModulated(SFX_VICTORY, 0.32, 0.0, 1.0);
        } else {
            playSfxModulated(SFX_THUNDER, 0.24, 0.0, 0.85);
            playSfxModulated(SFX_SHOCK, 0.18, 0.0, 0.78);
        }
    }

    public Scene getScene() {
        return scene;
    }
}
