package com.alemdoequilibrio.game;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;

public class ExplorationController {

    private static final double
            LOGICAL_WIDTH = 1280;

    private static final double
            LOGICAL_HEIGHT = 720;

    private static final double
            TILE_SIZE = 40;

    private static final double
            INTERACTION_RANGE = 30;

    private static final double
            ENEMY_RANGE = 70;

    private static final double
            HERO_START_X = 176;

    private static final double
            HERO_START_Y = 832;

    private static final String
            NEUTRAL_BORDER_MUSIC =
                    "/audio/zone_neutral_suspense.mp3";

    private static final String
            NEUTRAL_ELECTRIC_SFX =
                    "/audio/electric_shock.mp3";

    private final Hero hero;

    private final HeroView heroView;

    private final Pane worldLayer;

    private final Pane hudLayer;

    private final HUD hud;

    private final Pane gameRoot;

    private final StackPane screenRoot;

    private final Scene scene;

    private final boolean[] keys;

    private final AnimationTimer
            gameLoop;

    private final ZoneManager
            zoneManager;

    private final MapEnemySpawner
            mapEnemySpawner;

    private final CollisionManager
            collisionManager;

    private final ChapterProgress
            chapterProgress;

    private final DialogueController
            dialogueController;

    private final QuizController
            quizController;

    private final MediaPlayer
            neutralBorderMusicPlayer;

    private final AudioClip
            neutralElectricShock;

    private final Runnable
            onDialogueStarted;

    private final Runnable
            onDialogueFinished;

    private final Runnable
            onQuizStarted;

    private final Runnable
            onQuizFinished;

    private final Consumer<Enemy>
            onEnemyEncounter;

    private final Runnable
            onProgressChanged;

    private long lastTime;

    private double cameraX;

    private double cameraY;
    
    public ExplorationController(
            ResourceManager resourceManager,
            ChapterProgress chapterProgress,
            Runnable onDialogueStarted,
            Runnable onDialogueFinished,
            Runnable onQuizStarted,
            Runnable onQuizFinished,
            Consumer<Enemy> onEnemyEncounter,
            Runnable onProgressChanged) {

        Objects.requireNonNull(
                resourceManager
        );

        this.chapterProgress =
                Objects.requireNonNull(
                        chapterProgress
                );

        this.onDialogueStarted =
                Objects.requireNonNull(
                        onDialogueStarted
                );

        this.onDialogueFinished =
                Objects.requireNonNull(
                        onDialogueFinished
                );

        this.onQuizStarted =
                Objects.requireNonNull(
                        onQuizStarted
                );

        this.onQuizFinished =
                Objects.requireNonNull(
                        onQuizFinished
                );

        this.onEnemyEncounter =
                Objects.requireNonNull(
                        onEnemyEncounter
                );

        this.onProgressChanged =
                Objects.requireNonNull(
                        onProgressChanged
                );

        hero =
                new Hero(
                        HERO_START_X,
                        HERO_START_Y,
                        200
                );

        heroView =
                new HeroView(
                        resourceManager,
                        TILE_SIZE
                );

        heroView.setPosition(
            HERO_START_X,
            HERO_START_Y
        );

        worldLayer =
                new Pane();

        worldLayer.setPrefSize(
                ZoneManager.NEUTRAL_BORDER_WIDTH,
                ZoneManager.NEUTRAL_BORDER_HEIGHT
        );

        QuestionBank questionBank =
                new QuestionBank();

        questionBank
                .loadQuestionsFromFile(
                        "/questions/questions.csv"
                );

        zoneManager =
                new ZoneManager(
                        worldLayer,
                        resourceManager,
                        questionBank
                );

        mapEnemySpawner =
                new MapEnemySpawner(
                        worldLayer
                );

        collisionManager =
                new CollisionManager();

        hudLayer =
                new Pane();

        hudLayer.setPrefSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        hud =
                new HUD(
                        hudLayer,
                        resourceManager
                );

        hud.updateHealth(
                hero.getHealth(),
                hero.getMaxHealth()
        );

        DialogueRepository
                dialogueRepository =
                new DialogueRepository();

        dialogueController =
                new DialogueController(
                        hudLayer,
                        resourceManager,
                        dialogueRepository,
                        this::completeInteraction
                );

        quizController =
                new QuizController(
                        hudLayer
                );

        neutralBorderMusicPlayer =
                new MediaPlayer(
                        resourceManager.getMedia(
                                NEUTRAL_BORDER_MUSIC
                        )
                );

        neutralBorderMusicPlayer.setCycleCount(
                MediaPlayer.INDEFINITE
        );

        neutralBorderMusicPlayer.setVolume(
                0.35
        );

        neutralElectricShock =
                resourceManager.getAudioClip(
                        NEUTRAL_ELECTRIC_SFX
                );

        neutralElectricShock.setVolume(
                0.55
        );

        gameRoot =
                new Pane();

        gameRoot.setPrefSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        gameRoot.setMinSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        gameRoot.setMaxSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        gameRoot.getChildren().addAll(
                worldLayer,
                hudLayer
        );

        Rectangle viewportClip =
                new Rectangle(
                        LOGICAL_WIDTH,
                        LOGICAL_HEIGHT
                );

        gameRoot.setClip(
                viewportClip
        );

        screenRoot =
                new StackPane(
                        gameRoot
                );

        scene =
                new Scene(
                        screenRoot,
                        LOGICAL_WIDTH,
                        LOGICAL_HEIGHT
                );

        NumberBinding scale =
                Bindings.min(
                        scene.widthProperty()
                                .divide(
                                        LOGICAL_WIDTH
                                ),

                        scene.heightProperty()
                                .divide(
                                        LOGICAL_HEIGHT
                                )
                );

        gameRoot
                .scaleXProperty()
                .bind(scale);

        gameRoot
                .scaleYProperty()
                .bind(scale);

        keys =
                new boolean[4];
        
        configureInput();

        gameLoop =
                createGameLoop();

        loadZone(
                ExplorationZone
                        .NEUTRAL_BORDER
        );
    }

    public Scene getScene() {

        return scene;
    }

    private void loadZone(
            ExplorationZone zone) {

        mapEnemySpawner.clear();

        zoneManager.loadZone(
                zone
        );

        /*
         * Se o boss ja foi derrotado,
         * ele nao deve reaparecer ao carregar
         * a Camara do Fragmento.
         */
        if (!(zone
                == ExplorationZone
                        .FRAGMENT_CHAMBER
                && chapterProgress
                        .isBossDefeated())) {

            mapEnemySpawner
                    .spawnForZone(
                            zone
                    );
        }

        collisionManager
                .loadZone(
                        zone,
                        zoneManager
                                .getInteractables()
                );

        positionHeroAtZoneStart(
            zone
        );

        heroView.setPosition(
                hero.getX(),
                hero.getY()
        );

        worldLayer
                .getChildren()
                .add(
                        heroView.getNode()
                );

        resetKeys();

        neutralElectricShock.stop();

        updateCamera();
    }
    
    private void updateZoneMusic(
            ExplorationZone zone) {

        if (zone
                == ExplorationZone
                        .NEUTRAL_BORDER) {

            neutralBorderMusicPlayer.play();

            return;
        }

        neutralBorderMusicPlayer.stop();
    }

    private void positionHeroAtZoneStart(
        ExplorationZone zone) {

        switch (zone) {

            case NEUTRAL_BORDER ->
                hero.setPosition(
                        176,
                        832
                );

            case INSULATOR_TRAIL ->
                hero.setPosition(
                        100,
                        1700
                );

            case FACTION_CROSSROADS ->
                hero.setPosition(
                        100,
                        1700
                );

            case COULOMB_VALLEY ->
                hero.setPosition(
                        100,
                        1700
                );

            case FIELD_PLATEAU ->
                hero.setPosition(
                        100,
                        1700
                );

            case EQUIPOTENTIAL_RUINS ->
                hero.setPosition(
                        100,
                        1700
                );

            case FRAGMENT_CHAMBER ->
                hero.setPosition(
                        100,
                        1700
                );
        }
    }

    private void configureInput() {

        scene.setOnKeyPressed(
                event -> {

            if (event.getCode()
                    == KeyCode.W
                    || event.getCode()
                    == KeyCode.UP) {

                keys[0] = true;
            }

            if (event.getCode()
                    == KeyCode.S
                    || event.getCode()
                    == KeyCode.DOWN) {

                keys[1] = true;
            }

            if (event.getCode()
                    == KeyCode.A
                    || event.getCode()
                    == KeyCode.LEFT) {

                keys[2] = true;
            }

            if (event.getCode()
                    == KeyCode.D
                    || event.getCode()
                    == KeyCode.RIGHT) {

                keys[3] = true;
            }

            if (event.getCode()
                    == KeyCode.E) {

                handleInteraction();
            }
        });

        scene.setOnKeyReleased(
                event -> {

            if (event.getCode()
                    == KeyCode.W
                    || event.getCode()
                    == KeyCode.UP) {

                keys[0] = false;
            }

            if (event.getCode()
                    == KeyCode.S
                    || event.getCode()
                    == KeyCode.DOWN) {

                keys[1] = false;
            }

            if (event.getCode()
                    == KeyCode.A
                    || event.getCode()
                    == KeyCode.LEFT) {

                keys[2] = false;
            }

            if (event.getCode()
                    == KeyCode.D
                    || event.getCode()
                    == KeyCode.RIGHT) {

                keys[3] = false;
            }
        });
    }

    private void handleInteraction() {

        if (quizController
                .isOpen()) {

            return;
        }

        if (dialogueController
                .isOpen()) {

            boolean stillOpen =
                    dialogueController
                            .advanceDialogue();

            if (!stillOpen) {

                if (quizController
                        .isOpen()) {

                    onQuizStarted
                            .run();

                } else {

                    onDialogueFinished
                            .run();
                }

                updateInteractionPrompt();
            }

            return;
        }

        Interactable interactable =
                findNearestInteractable();

        if (interactable == null) {

            return;
        }

        startInteractionDialogue(
                interactable
        );

        if (dialogueController
                .isOpen()) {

            hud.hideInteractionPrompt();

            onDialogueStarted
                    .run();
        }
    }

    private void startInteractionDialogue(
            Interactable interactable) {

        if (zoneManager.getCurrentZone()
                == ExplorationZone.NEUTRAL_BORDER
                && interactable
                        instanceof SceneryInteractable) {

            try {

                neutralElectricShock.play();

            } catch (Exception exception) {

                // A interação continua mesmo se o áudio estiver indisponível.
            }
        }

        if (zoneManager.getCurrentZone()
                == ExplorationZone.NEUTRAL_BORDER
                && interactable
                        instanceof NPC npc) {

            String name =
                    npc.getName();

            if ("Nilo".equalsIgnoreCase(
                    name
            )
                    && chapterProgress
                            .hasLearned(
                                    KnowledgeTopic
                                            .KNOW_CHARGE_SIGNS
                            )) {

                dialogueController
                        .startDialogue(
                                "NILO_REPEAT"
                        );

                return;
            }

            if (("Íon".equalsIgnoreCase(
                    name
            )
                    || "Ion".equalsIgnoreCase(
                            name
                    ))
                    && chapterProgress
                            .hasMetNeutralIon()) {

                dialogueController
                        .startDialogue(
                                "ION_NEUTRAL_REPEAT"
                        );

                return;
            }
        }

        dialogueController
                .startDialogue(
                        interactable
                );
    }

    private void startMerchantQuiz(
            Merchant merchant) {

        List<PhysicsQuestion> questions =
                merchant.startQuiz(
                        chapterProgress,
                        4
                );

        if (questions.size() < 4) {

            return;
        }

        hud.hideInteractionPrompt();

        quizController.startQuiz(
                questions,
                correctAnswers ->
                        finishMerchantQuiz(
                                merchant,
                                correctAnswers
                        )
        );
    }

    private void finishMerchantQuiz(
            Merchant merchant,
            int correctAnswers) {

        boolean success =
                merchant.completeQuiz(
                        hero,
                        chapterProgress,
                        correctAnswers
                );

        if (success) {

            onProgressChanged
                    .run();
        }

        String resultDialogueId =
                success
                        ? merchant
                                .getSuccessDialogueId()
                        : merchant
                                .getFailureDialogueId();

        if (resultDialogueId != null
                && !resultDialogueId.isBlank()) {

            dialogueController
                    .startDialogue(
                            resultDialogueId
                    );

            if (dialogueController
                    .isOpen()) {

                onDialogueStarted
                        .run();

                return;
            }
        }

        onQuizFinished
                .run();
    }

    private Interactable
            findNearestInteractable() {

        Interactable nearest =
                null;

        double nearestDistance =
                Double.MAX_VALUE;

        for (Interactable interactable
                : zoneManager
                        .getInteractables()) {

            double distance =
                    distanceTo(
                            interactable
                    );

            if (distance
                    <= INTERACTION_RANGE
                    && distance
                    < nearestDistance) {

                nearest =
                        interactable;

                nearestDistance =
                        distance;
            }
        }

        return nearest;
    }

    private double distanceTo(
            Interactable interactable) {

        double heroCenterX =
                hero.getX()
                + TILE_SIZE / 2.0;

        double heroCenterY =
                hero.getY()
                + TILE_SIZE / 2.0;

        double targetLeft =
                interactable.getX();

        double targetTop =
                interactable.getY();

        double targetRight =
                targetLeft
                + interactable.getWidth();

        double targetBottom =
                targetTop
                + interactable.getHeight();

        double nearestX =
                Math.max(
                        targetLeft,
                        Math.min(
                                heroCenterX,
                                targetRight
                        )
                );

        double nearestY =
                Math.max(
                        targetTop,
                        Math.min(
                                heroCenterY,
                                targetBottom
                        )
                );

        return Math.hypot(
                heroCenterX
                        - nearestX,

                heroCenterY
                        - nearestY
        );
    }

    private void completeInteraction(
            Interactable interactable) {

        if (interactable
                instanceof Merchant merchant) {

            startMerchantQuiz(
                    merchant
            );

            return;
        }

        if (!(interactable
                instanceof NPC npc)) {

            return;
        }

        if (zoneManager.getCurrentZone()
                == ExplorationZone.NEUTRAL_BORDER
                && ("Íon".equalsIgnoreCase(
                        npc.getName()
                )
                    || "Ion".equalsIgnoreCase(
                            npc.getName()
                    ))
                && !chapterProgress
                        .hasMetNeutralIon()) {

            chapterProgress
                    .markNeutralIonMet();

            onProgressChanged
                    .run();
        }

        KnowledgeTopic topic =
                npc.getKnowledgeTopic();

        if (topic == null) {

            return;
        }

        if (!chapterProgress
                .hasLearned(
                        topic
                )) {

            chapterProgress.learn(
                    topic
            );

            hud.showConceptLearned(
                    topic
            );

            System.out.println(
                    "Topico aprendido: "
                    + topic
            );

            onProgressChanged
                    .run();
        }
    }

    private AnimationTimer
            createGameLoop() {

        return new AnimationTimer() {

            @Override
            public void handle(
                    long now) {

                if (lastTime == 0) {

                    lastTime = now;

                    return;
                }

                double deltaTime =
                        (now - lastTime)
                        / 1_000_000_000.0;

                lastTime = now;

                double directionX = 0;

                double directionY = 0;

                if (keys[0]) {

                    directionY -= 1;
                }

                if (keys[1]) {

                    directionY += 1;
                }

                if (keys[2]) {

                    directionX -= 1;
                }

                if (keys[3]) {

                    directionX += 1;
                }

                moveHeroWithCollisions(
                        directionX,
                        directionY,
                        deltaTime
                );

                heroView.update(
                        directionX,
                        directionY,
                        deltaTime,
                        hero.getX(),
                        hero.getY()
                );

                updateInteractionPrompt();

                handleCheckpoint();

                if (handleEnemyEncounter()) {

                    return;
                }

                if (handleZoneTransition()) {

                    return;
                }

                updateCamera();
            }
        };
    }

    /**
     * Movimento com colisao separada por eixo.
     *
     * Isso permite que o Hero deslize ao longo
     * de uma parede sem atravessa-la.
     */
    private void moveHeroWithCollisions(
            double directionX,
            double directionY,
            double deltaTime) {

        double length =
                Math.hypot(
                        directionX,
                        directionY
                );

        if (length == 0) {

            return;
        }

        double normalizedX =
                directionX
                / length;

        double normalizedY =
                directionY
                / length;

        if (normalizedX != 0) {

            double previousX =
                    hero.getX();

            double previousY =
                    hero.getY();

            hero.updateMovement(
                normalizedX,
                0,
                deltaTime * Math.abs(
                    normalizedX
                ),
                zoneManager
                        .getCurrentWorldWidth()
                    - TILE_SIZE,
                zoneManager
                        .getCurrentWorldHeight()
                    - TILE_SIZE
            );

            if (collisionManager
                    .collides(
                            hero.getX(),
                            hero.getY(),
                            TILE_SIZE,
                            TILE_SIZE
                    )) {

                hero.setPosition(
                        previousX,
                        previousY
                );
            }
        }

        if (normalizedY != 0) {

            double previousX =
                    hero.getX();

            double previousY =
                    hero.getY();

            hero.updateMovement(
                    0,
                    normalizedY,
                    deltaTime
                    * Math.abs(
                            normalizedY
                    ),
                    zoneManager
                            .getCurrentWorldWidth()
                            - TILE_SIZE,
                    zoneManager
                            .getCurrentWorldHeight()
                            - TILE_SIZE
            );

            if (collisionManager
                    .collides(
                            hero.getX(),
                            hero.getY(),
                            TILE_SIZE,
                            TILE_SIZE
                    )) {

                hero.setPosition(
                        previousX,
                        previousY
                );
            }
        }
    }

    private boolean
            handleEnemyEncounter() {

        Enemy enemy =
                mapEnemySpawner
                        .findNearbyEnemy(
                                hero,
                                TILE_SIZE,
                                ENEMY_RANGE
                        );

        if (enemy == null) {

            return false;
        }

        onEnemyEncounter.accept(
                enemy
        );

        return true;
    }

    private void handleCheckpoint() {

        if (!zoneManager
                .intersectsCheckpoint(
                        hero.getX(),
                        hero.getY(),
                        TILE_SIZE,
                        TILE_SIZE
                )) {

            return;
        }

        ExplorationZone zone =
                zoneManager
                        .getCurrentZone();

        if (chapterProgress
                .reachCheckpoint(
                        zone
                )) {

            hero.restoreHealth();

            hud.updateHealth(
                    hero.getHealth(),
                    hero.getMaxHealth()
            );

            System.out.println(
                    "Checkpoint alcancado: "
                    + zone
            );

            onProgressChanged
                    .run();
        }
    }

    private boolean
            handleZoneTransition() {

        if (!zoneManager
                .intersectsExit(
                        hero.getX(),
                        hero.getY(),
                        TILE_SIZE,
                        TILE_SIZE
                )) {

            return false;
        }

        if (zoneManager.getCurrentZone()
                == ExplorationZone.NEUTRAL_BORDER) {

            if (!chapterProgress
                    .hasLearned(
                            KnowledgeTopic
                                    .KNOW_CHARGE_SIGNS
                    )) {

                return blockNeutralBorderExit(
                        "NEUTRAL_EXIT_NEED_NILO"
                );
            }

            if (!chapterProgress
                    .hasMetNeutralIon()) {

                return blockNeutralBorderExit(
                        "NEUTRAL_EXIT_NEED_ION"
                );
            }
        }

        ExplorationZone nextZone =
                zoneManager
                        .getNextZone();

        if (nextZone == null) {

            return false;
        }

        loadZone(
                nextZone
        );

        updateZoneMusic(
                nextZone
        );

        return true;
    }

    private boolean blockNeutralBorderExit(
            String dialogueId) {

        hero.setPosition(
                1550,
                hero.getY()
        );

        heroView.setPosition(
                hero.getX(),
                hero.getY()
        );

        hud.hideInteractionPrompt();

        dialogueController
                .startDialogue(
                        dialogueId
                );

        if (dialogueController
                .isOpen()) {

            onDialogueStarted
                    .run();
        }

        updateCamera();

        return true;
    }

    private void updateCamera() {

        double targetX =
                hero.getX()
                + TILE_SIZE / 2.0
                - LOGICAL_WIDTH / 2.0;

        double targetY =
                hero.getY()
                + TILE_SIZE / 2.0
                - LOGICAL_HEIGHT / 2.0;

        cameraX =
                Math.max(
                        0,
                        Math.min(
                                targetX,
                                zoneManager
                                        .getCurrentWorldWidth()
                                        - LOGICAL_WIDTH
                        )
                );

        cameraY =
                Math.max(
                        0,
                        Math.min(
                                targetY,
                                zoneManager
                                        .getCurrentWorldHeight()
                                        - LOGICAL_HEIGHT
                        )
                );

        worldLayer.setTranslateX(
                -cameraX
        );

        worldLayer.setTranslateY(
                -cameraY
        );
    }

    private void resetKeys() {

        for (int i = 0;
                i < keys.length;
                i++) {

            keys[i] = false;
        }
    }

    private void updateInteractionPrompt() {

        if (dialogueController
                .isOpen()
                || quizController
                        .isOpen()) {

            hud.hideInteractionPrompt();

            return;
        }

        Interactable nearest =
                findNearestInteractable();

        if (nearest != null) {

            if (nearest
                    instanceof SceneryInteractable) {

                hud.showInteractionPrompt(
                        "Examinar",
                        nearest
                                .getInteractionName()
                );

            } else {

                hud.showInteractionPrompt(
                        nearest
                                .getInteractionName()
                );
            }

        } else {

            hud.hideInteractionPrompt();
        }
    }
    
    private boolean
        shouldStartNeutralBorderIntro() {

        return !chapterProgress.hasSeenNeutralBorderIntro()
                && zoneManager.getCurrentZone()
                        == ExplorationZone
                                .NEUTRAL_BORDER
                && !chapterProgress.hasLearned(
                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS
                );
    }

    private void startNeutralBorderIntro() {

        /*
        * Marcamos antes de mudar para DIALOGUE.
        *
        * Quando o diálogo terminar, o jogo volta
        * para EXPLORATION e start() será chamado
        * novamente. Assim a introdução não reinicia.
        */
        chapterProgress.markNeutralBorderIntroSeen();
        onProgressChanged.run();

        dialogueController
                .startDialogue(
                        "INTRO_NEUTRAL_BORDER"
                );

        if (dialogueController
                .isOpen()) {
            
                hud.hideInteractionPrompt();
                
                onDialogueStarted
                        .run();
        }
    }

    public void start() {

        resetKeys();

        lastTime = 0;

        hud.updateHealth(
                hero.getHealth(),
                hero.getMaxHealth()
        );

        updateCamera();

        updateZoneMusic(
                zoneManager
                        .getCurrentZone()
        );

        /*
        * Na primeira chegada à Fronteira Neutra,
        * a cena inicial acontece antes de liberar
        * o movimento do jogador.
        */
        if (shouldStartNeutralBorderIntro()) {
        
            startNeutralBorderIntro();

            return;
        }

        gameLoop.start();
    }

    public void stop() {

        gameLoop.stop();

        resetKeys();
    }

    public void closeDialogue() {

        dialogueController
                .closeDialogue();

        updateInteractionPrompt();
    }

    public void closeQuiz() {

        quizController
                .closeQuiz();

        updateInteractionPrompt();
    }

    public void removeDefeatedEnemy(
            Enemy enemy) {

        mapEnemySpawner
                .removeEnemy(
                        enemy
                );
    }

    public void respawnAtCheckpoint() {

        hero.restoreHealth();

        loadZone(
                chapterProgress
                        .getCheckpoint()
        );

        hud.updateHealth(
                hero.getHealth(),
                hero.getMaxHealth()
        );
    }

    /**
     * Utilizado apos carregar um save.
     */
    public void restoreAtCheckpoint() {

        loadZone(
                chapterProgress
                        .getCheckpoint()
        );

        hud.updateHealth(
                hero.getHealth(),
                hero.getMaxHealth()
        );
    }

    public Hero getHero() {

        return hero;
    }

    public ExplorationZone
            getCurrentZone() {

        return zoneManager
                .getCurrentZone();
    }
}