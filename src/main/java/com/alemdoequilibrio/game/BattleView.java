package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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
 * Inicialização robusta vinculada ao BattleController.
 *
 * POO 5 - Agregação:
 * Agrega BattleController e ResourceManager sem alterar suas regras internas.
 */
public class BattleView {

    private static final double LOGICAL_WIDTH = 1280;
    private static final double LOGICAL_HEIGHT = 720;
    private static final float PLAYER_DAMAGE = 20.0f;

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
    private final Rectangle heroHpBar;
    private final Label heroHpText;
    private final ImageView heroSpriteView;
    private final VBox heroStageBox;

    /* Componentes do palco de combate: Inimigo (Direita) */
    private final Label enemyNameLabel;
    private final Label enemyPolarityBadge;
    private final Rectangle enemyHpBar;
    private final Label enemyHpText;
    private final ImageView enemySpriteView;
    private final VBox enemyStageBox;

    /* Componentes do painel de comandos (Inferior Esquerdo) */
    private final VBox mainCommandList;
    private final VBox abilityCommandList;
    private final Button btnAttack;
    private final Button btnAbilities;
    private final Button btnAnalyze;
    private final Button btnGrounding;

    /* Componentes do painel de status/registro (Inferior Direito) */
    private final Label stanceStatusLabel;
    private final Label physicsInsightLabel;
    private final Label roundCounterLabel;

    /* Efeitos de impacto visual */
    private final Rectangle damageFlash;
    private float maxEnemyHealth = 50.0f;
    private final float maxHeroHealth = 100.0f;

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

        HBox heroHeaderRow = new HBox(12, heroNameLabel, heroPolarityBadge);
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

        HBox enemyHeaderRow = new HBox(12, enemyNameLabel, enemyPolarityBadge);
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
        Label commandHeader = new Label("> COMANDOS");
        commandHeader.getStyleClass().add("panel-header");

        btnAttack = createCommandButton("[1]  ⚔  ATACAR");
        btnAttack.setOnAction(e -> handlePlayerAttack());

        btnAbilities = createCommandButton("[2]  ⚡  MODULAÇÕES DE CAMPO");
        btnAbilities.setOnAction(e -> showAbilitySubmenu());

        btnAnalyze = createCommandButton("[3]  🔍  ANALISAR CAMPO");
        btnAnalyze.setOnAction(e -> handleAnalyzeAction());

        btnGrounding = createCommandButton("[4]  🛡  ATERRAMENTO CONDUTOR");
        btnGrounding.setOnAction(e -> handleGroundingAction());

        mainCommandList = new VBox(8, btnAttack, btnAbilities, btnAnalyze, btnGrounding);

        // Submenu de Habilidades
        Button btnSkillRepulsion = createCommandButton("⚡ Pulso de Repulsão (Coulomb)");
        btnSkillRepulsion.setOnAction(e -> handleAbilitySelect("Pulso de Repulsão", "Ainda bloqueada! Complete a prova de sinais de carga com Íon para desbloquear."));

        Button btnSkillField = createCommandButton("⚡ Impulso de Campo (Linhas E)");
        btnSkillField.setOnAction(e -> handleAbilitySelect("Impulso de Campo", "Ainda bloqueada! Domine Linhas de Campo Elétrico para canalizar."));

        Button btnSkillShield = createCommandButton("🛡 Escudo Equipotencial (Superfícies)");
        btnSkillShield.setOnAction(e -> handleAbilitySelect("Escudo Equipotencial", "Ainda bloqueada! Conquiste o conceito de Superfícies Equipotenciais com Íon."));

        Button btnBack = createCommandButton("↩ Retornar");
        btnBack.setOnAction(e -> showMainMenu());

        abilityCommandList = new VBox(8, btnSkillRepulsion, btnSkillField, btnSkillShield, btnBack);
        abilityCommandList.setVisible(false);
        abilityCommandList.setManaged(false);

        VBox commandBox = new VBox(10, commandHeader, mainCommandList, abilityCommandList);
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

        roundCounterLabel = new Label("Rodada: 1  |  Turno: SEU TURNO");
        roundCounterLabel.setStyle("-fx-text-fill: #D9A441; -fx-font-family: Consolas; -fx-font-size: 13px; -fx-font-weight: bold;");

        VBox statusBox = new VBox(10, statusHeader, stanceStatusLabel, physicsInsightLabel, roundCounterLabel);
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

        applyStylesheet();
        updateView();
    }

    private void playSfx(String path) {
        try {
            resourceManager.getAudioClip(path).play();
        } catch (Exception e) {
            // Silencioso se áudio não estiver disponível
        }
    }

    private Button createCommandButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("command-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(btn, Priority.ALWAYS);
        btn.addEventHandler(javafx.event.ActionEvent.ACTION, e -> playSfx("/audio/sfx_menu_select.wav"));
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

        if (enemy instanceof CorruptedDipole) {
            imagePath = "/images/enemy_battle_dipole.png";
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

        if (enemy instanceof ChargedEnemy charged) {
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
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            ChargeType type = uncharged.getChargeType();
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
        } else if (enemy instanceof CorruptedDipole) {
            badge.setText("± DIPOLO");
            badge.getStyleClass().add("polarity-negative");
        } else {
            badge.setText("~ OSCILANTE");
            badge.getStyleClass().add("polarity-neutral");
        }

        return badge;
    }

    private String buildInitialTelegraphText() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof WanderingSpark) {
            return "A Faísca Errante crepita com descargas desordenadas no ar. O ambiente ressoa com instabilidade dielétrica!";
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            String sign = (uncharged.getChargeType() == ChargeType.POSITIVE) ? "positivas (+)" : "negativas (−)";
            return "Uma concentração densa de cargas " + sign + " distorce o campo elétrico à sua frente!";
        } else if (enemy instanceof CorruptedDipole) {
            return "Dois polos opostos em colapso atraem e repelem o solo simultaneamente. As linhas de campo formam arcos perigosos!";
        }
        return "Uma presença anômala de matéria eletrizada barra o seu caminho. Prepare sua modulação de cargas!";
    }

    private String buildPhysicsInsightText() {
        Enemy enemy = battleController.getEnemy();
        if (enemy instanceof ChargedEnemy charged) {
            if (charged.getChargeType() == ChargeType.POSITIVE) {
                return "Carga Positiva (+): As linhas de força divergem radialmente do centro. Repele cargas de mesmo sinal com força de Coulomb inversamente proporcional a d².";
            } else if (charged.getChargeType() == ChargeType.NEGATIVE) {
                return "Carga Negativa (−): As linhas de força convergem para o centro. Possui excesso de elétrons livres.";
            }
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            if (uncharged.getChargeType() == ChargeType.POSITIVE) {
                return "Carga Positiva (+): Concentração protonada. Interage fortemente com qualquer condutor desbalanceado.";
            } else if (uncharged.getChargeType() == ChargeType.NEGATIVE) {
                return "Carga Negativa (−): Campo convergente. Pode atrair corpos neutros por polarização induzida.";
            }
        }
        return "Matéria Neutra / Oscilante: Cargas em equilíbrio ou transição rápida. Sofre forças apenas por gradientes de campo.";
    }

    private void handlePlayerAttack() {
        if (battleController.isBattleFinished()) {
            return;
        }

        setButtonsDisabled(true);
        triggerScreenShake();
        playEnemyHitFlash();
        playSfx("/audio/sfx_hit.wav");

        battleController.performPlayerAttack(PLAYER_DAMAGE);

        if (battleController.isBattleFinished()) {
            updateView();
            return;
        }

        telegraphTextLabel.setText("Você desfere um ataque de neutralização direta (-" + Math.round(PLAYER_DAMAGE) + " HP)! O inimigo prepara uma resposta de carga!");

        Timeline enemyTurnTimer = new Timeline(new KeyFrame(Duration.millis(900), e -> {
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

        triggerScreenShake();
        playPlayerDamageFlash();
        playSfx("/audio/sfx_hit.wav");

        telegraphTextLabel.setText(battleController.getEnemy().getName()
                + " descarrega seu potencial elétrico e causou "
                + Math.round(damageTaken) + " de dano!");

        updateView();
        setButtonsDisabled(false);
    }

    private void handleAnalyzeAction() {
        Enemy enemy = battleController.getEnemy();
        StringBuilder analysis = new StringBuilder("ANÁLISE DE CAMPO: ");

        if (enemy instanceof ChargedEnemy charged) {
            analysis.append("Carga ").append(charged.getChargeType().getDescription())
                    .append(" [").append(charged.getChargeType().getSymbol()).append("]. ")
                    .append("Cargas de mesmo sinal repelem; opostos atraem pela Lei de Coulomb.");
        } else if (enemy instanceof UncontrolledCharge uncharged) {
            analysis.append("Carga ").append(uncharged.getChargeType().getDescription())
                    .append(" [").append(uncharged.getChargeType().getSymbol()).append("]. ")
                    .append("Gradiente elétrico intenso. Cuidado com indução!");
        } else {
            analysis.append("Natureza oscilante. Não possui polaridade estável no momento.");
        }

        telegraphTextLabel.setText(analysis.toString());
    }

    private void handleGroundingAction() {
        if (battleController.isBattleFinished()) {
            return;
        }

        stanceStatusLabel.setText("Postura: Aterramento Condutor ao Solo");
        telegraphTextLabel.setText("Você estabelece um caminho condutor com o solo! As cargas excedentes encontram escoamento seguro enquanto você aguarda o impacto.");
        playSfx("/audio/sfx_electric_discharge.wav");

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
        mainCommandList.setVisible(false);
        mainCommandList.setManaged(false);
        abilityCommandList.setVisible(true);
        abilityCommandList.setManaged(true);
    }

    private void showMainMenu() {
        abilityCommandList.setVisible(false);
        abilityCommandList.setManaged(false);
        mainCommandList.setVisible(true);
        mainCommandList.setManaged(true);
    }

    private void handleAbilitySelect(String name, String message) {
        playSfx("/audio/sfx_electric_discharge.wav");
        telegraphTextLabel.setText("[" + name + "] " + message);
    }

    private void triggerScreenShake() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), gameRoot);
        shake.setFromX(-6);
        shake.setToX(6);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> gameRoot.setTranslateX(0));
        shake.play();
    }

    private void playPlayerDamageFlash() {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(damageFlash.fillProperty(), Color.rgb(180, 40, 40, 0.4))),
                new KeyFrame(Duration.millis(180), new KeyValue(damageFlash.fillProperty(), Color.rgb(180, 40, 40, 0.0)))
        );
        flash.play();
    }

    private void playEnemyHitFlash() {
        FadeTransition ft = new FadeTransition(Duration.millis(80), enemySpriteView);
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

    private void setButtonsDisabled(boolean disabled) {
        btnAttack.setDisable(disabled);
        btnAbilities.setDisable(disabled);
        btnAnalyze.setDisable(disabled);
        btnGrounding.setDisable(disabled);
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

        // 3. Atualiza Contador de Rodadas
        roundCounterLabel.setText("Rodada: " + (battleController.getCompletedRounds() + 1)
                + "  |  Turno: " + (battleController.getCurrentTurn() == BattleController.BattleTurn.PLAYER_TURN ? "SEU TURNO" : "TURNO DO INIMIGO"));

        // 4. Verifica Encerramento
        if (battleController.isBattleFinished()) {
            setButtonsDisabled(true);

            switch (battleController.getResult()) {
                case VICTORY -> {
                    telegraphTitleLabel.setText("> VITÓRIA ELETROSTÁTICA");
                    telegraphTextLabel.setText("A ameaça teve suas cargas dissipadas e o equilíbrio local foi restabelecido!");
                    showBattleEndBanner(true);
                }
                case DEFEAT -> {
                    telegraphTitleLabel.setText("> COLAPSO DE EQUILÍBRIO");
                    telegraphTextLabel.setText("Suas reservas de carga entraram em colapso irreversível...");
                    showBattleEndBanner(false);
                }
                case IN_PROGRESS -> {
                }
            }
        }
    }

    private void showBattleEndBanner(boolean victory) {
        VBox banner = new VBox(12);
        banner.setAlignment(Pos.CENTER);
        banner.getStyleClass().add(victory ? "end-banner-victory" : "end-banner-defeat");

        Label title = new Label(victory ? "VITÓRIA ELETROSTÁTICA" : "COLAPSO DE EQUILÍBRIO");
        title.getStyleClass().add(victory ? "end-title-victory" : "end-title-defeat");

        Label subtitle = new Label(victory
                ? "O campo eletrostático foi neutralizado com sucesso."
                : "A instabilidade elétrica superou suas forças.");
        subtitle.setStyle("-fx-text-fill: #DDD7CE; -fx-font-size: 15px; -fx-font-family: Georgia;");

        banner.getChildren().addAll(title, subtitle);
        banner.setLayoutX((LOGICAL_WIDTH - 520) / 2.0);
        banner.setLayoutY((LOGICAL_HEIGHT - 180) / 2.0);
        banner.setPrefWidth(520);

        gameRoot.getChildren().add(banner);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), banner);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        if (victory) {
            playSfx("/audio/sfx_battle_victory.wav");
        }
    }

    public Scene getScene() {
        return scene;
    }
}