package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Controla exploração, movimento, câmera,
 * interação e mudança entre regiões.
 */
public class ExplorationController {

    private static final double LOGICAL_WIDTH =
            1280;

    private static final double LOGICAL_HEIGHT =
            720;

    private static final double TILE_SIZE =
            40;

    private static final double INTERACTION_RANGE =
            90;

    private static final double HERO_START_X =
            100;

    private static final double HERO_START_Y =
            ZoneManager.WORLD_HEIGHT - 100;

    private static final String HERO_SPRITE_PATH =
            "/images/hero_idle_01.png";

    private final Hero hero;

    private final ImageView heroView;

    private final Pane worldLayer;

    private final Pane hudLayer;

    private final HUD hud;

    private final Pane gameRoot;

    private final StackPane screenRoot;

    private final Scene scene;

    private final boolean[] keys;

    private final AnimationTimer gameLoop;

    private final ZoneManager zoneManager;

    private final ChapterProgress chapterProgress;

    private final DialogueController
            dialogueController;

    private final Runnable
            onDialogueStarted;

    private final Runnable
            onDialogueFinished;

    private long lastTime;

    private double cameraX;

    private double cameraY;

    public ExplorationController(
            ResourceManager resourceManager,
            ChapterProgress chapterProgress,
            Runnable onDialogueStarted,
            Runnable onDialogueFinished) {

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

        hero =
                new Hero(
                        HERO_START_X,
                        HERO_START_Y,
                        200
                );

        Image heroSprite =
                resourceManager.getImage(
                        HERO_SPRITE_PATH
                );

        heroView =
                new ImageView(
                        heroSprite
                );

        heroView.setFitWidth(
                TILE_SIZE
        );

        heroView.setFitHeight(
                TILE_SIZE
        );

        heroView.setPreserveRatio(
                false
        );

        worldLayer =
                new Pane();

        worldLayer.setPrefSize(
                ZoneManager.WORLD_WIDTH,
                ZoneManager.WORLD_HEIGHT
        );

        zoneManager =
                new ZoneManager(
                        worldLayer
                );

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
                100.0f
        );

        DialogueRepository dialogueRepository =
        new DialogueRepository();

        dialogueController =
        new DialogueController(
                hudLayer,
                dialogueRepository,
                this::completeInteraction
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
                ExplorationZone.NEUTRAL_BORDER
        );
    }

    public Scene getScene() {

        return scene;
    }

    /**
     * Carrega uma nova região mantendo a mesma
     * estrutura de mundo grande + câmera.
     */
    private void loadZone(
            ExplorationZone zone) {

        zoneManager.loadZone(
                zone
        );

        hero.setPosition(
                HERO_START_X,
                HERO_START_Y
        );

        heroView.relocate(
                hero.getX(),
                hero.getY()
        );

        /*
         * ZoneManager limpa o worldLayer.
         * Por isso o Hero precisa ser recolocado.
         */
        worldLayer
                .getChildren()
                .add(heroView);

        resetKeys();

        updateCamera();
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

        if (dialogueController.isOpen()) {

            boolean stillOpen =
                    dialogueController
                            .advanceDialogue();

            if (!stillOpen) {

                onDialogueFinished.run();
                updateInteractionPrompt();
            }

            return;
        }

        Interactable interactable =
                findNearestInteractable();

        if (interactable == null) {
            return;
        }

        dialogueController
                .startDialogue(
                        interactable
                );

        if (dialogueController.isOpen()) {

            hud.hideInteractionPrompt();
            onDialogueStarted.run();
        }
    }

    private Interactable
            findNearestInteractable() {

        Interactable nearest =
                null;

        double nearestDistance =
                Double.MAX_VALUE;

        for (Interactable interactable
                : zoneManager.getInteractables()) {

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

        double targetCenterX =
                interactable.getX()
                + interactable.getWidth()
                / 2.0;

        double targetCenterY =
                interactable.getY()
                + interactable.getHeight()
                / 2.0;

        return Math.hypot(

                heroCenterX
                - targetCenterX,

                heroCenterY
                - targetCenterY
        );
    }

    private void completeInteraction(
            Interactable interactable) {

        if (!(interactable
                instanceof NPC npc)) {

            return;
        }

        KnowledgeTopic topic =
                npc.getKnowledgeTopic();

        if (topic == null) {
            return;
        }

        if (!chapterProgress
                .hasLearned(topic)) {

            chapterProgress.learn(
                    topic
            );

            hud.showConceptLearned(
                    topic
            );

            System.out.println(
                    "Tópico aprendido: "
                    + topic
            );
        }
    }

    private AnimationTimer createGameLoop() {

        return new AnimationTimer() {

            @Override
            public void handle(long now) {

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

                hero.updateMovement(

                        directionX,
                        directionY,

                        deltaTime,

                        ZoneManager.WORLD_WIDTH
                        - TILE_SIZE,

                        ZoneManager.WORLD_HEIGHT
                        - TILE_SIZE
                );

                heroView.relocate(
                        hero.getX(),
                        hero.getY()
                );

                updateInteractionPrompt();

                /*
                 * Primeiro verifica se o personagem
                 * entrou numa área de mudança de mapa.
                 */
                if (handleZoneTransition()) {
                    return;
                }

                /*
                 * Depois move a câmera para acompanhar
                 * a posição atual do herói.
                 */
                updateCamera();
            }
        };
    }

    private boolean handleZoneTransition() {

        boolean reachedExit =
                zoneManager.intersectsExit(

                        hero.getX(),
                        hero.getY(),

                        TILE_SIZE,
                        TILE_SIZE
                );

        if (!reachedExit) {
            return false;
        }

        ExplorationZone nextZone =
                zoneManager.getNextZone();

        if (nextZone == null) {

            return false;
        }

        loadZone(
                nextZone
        );

        return true;
    }

    /**
     * Centraliza a câmera no herói sem permitir
     * que ela ultrapasse os limites do mapa.
     */
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

                                ZoneManager.WORLD_WIDTH
                                - LOGICAL_WIDTH
                        )
                );

        cameraY =
                Math.max(
                        0,
                        Math.min(
                                targetY,

                                ZoneManager.WORLD_HEIGHT
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

    public void start() {

        lastTime = 0;

        updateCamera();

        gameLoop.start();
    }

    public void stop() {

        gameLoop.stop();
    }

    public void closeDialogue() {

        dialogueController
                .closeDialogue();

        updateInteractionPrompt();
    }

    private void updateInteractionPrompt() {

        if (dialogueController.isOpen()) {
            hud.hideInteractionPrompt();
            return;
        }

        Interactable nearest =
                findNearestInteractable();

        if (nearest != null) {
            hud.showInteractionPrompt(
                    nearest.getInteractionName()
            );
        } else {
            hud.hideInteractionPrompt();
        }
    }

    public ExplorationZone getCurrentZone() {

        return zoneManager
                .getCurrentZone();
    }
}