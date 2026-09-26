package com.alemdoequilibrio.game;

import java.util.List;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Controla o modo de exploração.
 */
public class ExplorationController {

    private static final double LOGICAL_WIDTH = 1280;
    private static final double LOGICAL_HEIGHT = 720;

    private static final double WORLD_WIDTH = 3200;
    private static final double WORLD_HEIGHT = 1800;

    private static final double TILE_SIZE = 40;

    private static final double INTERACTION_RANGE = 90;

    private static final String HERO_SPRITE_PATH =
            "/images/hero_idle_01.png";

    private final ResourceManager resourceManager;

    private final Hero hero;
    private final ImageView heroView;

    private final Pane worldLayer;
    private final Pane hudLayer;
    private final Pane gameRoot;

    private final StackPane screenRoot;

    private final Scene scene;

    private final boolean[] keys;

    private final AnimationTimer gameLoop;

    private final List<Interactable> interactables;

    private final DialogueController dialogueController;

    private final Runnable onDialogueStarted;
    private final Runnable onDialogueFinished;

    private long lastTime;

    private double cameraX;
    private double cameraY;

    public ExplorationController(
            ResourceManager resourceManager,
            Runnable onDialogueStarted,
            Runnable onDialogueFinished) {

        this.resourceManager =
                Objects.requireNonNull(
                        resourceManager
                );

        this.onDialogueStarted =
                Objects.requireNonNull(
                        onDialogueStarted
                );

        this.onDialogueFinished =
                Objects.requireNonNull(
                        onDialogueFinished
                );

        this.hero =
                new Hero(
                        100,
                        WORLD_HEIGHT - 100,
                        200
                );

        Image heroSprite =
                resourceManager.getImage(
                        HERO_SPRITE_PATH
                );

        this.heroView =
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

        heroView.relocate(
                hero.getX(),
                hero.getY()
        );

        this.worldLayer =
                new Pane();

        worldLayer.setPrefSize(
                WORLD_WIDTH,
                WORLD_HEIGHT
        );

        DebugGridRenderer.render(
                worldLayer,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                TILE_SIZE
        );

        CameraTestOverlay.renderWorld(
                worldLayer,
                TILE_SIZE
        );

        /*
         * NPC de teste.
         */
        NPC villageNPC =
                new NPC(
                        "Nilo",
                        260,
                        WORLD_HEIGHT - 100,
                        40,
                        50,
                        "Olá, viajante.",
                        "Algo estranho está acontecendo "
                        + "com as cargas desta região."
                );

        Rectangle npcView =
                new Rectangle(
                        villageNPC.getWidth(),
                        villageNPC.getHeight()
                );

        npcView.setFill(
                Color.GOLD
        );

        npcView.relocate(
                villageNPC.getX(),
                villageNPC.getY()
        );

        /*
         * Objeto de teste.
         * Usa exatamente o mesmo fluxo de interação do NPC.
         */
        WorldObject testObject =
                new WorldObject(
                        "Placa",
                        450,
                        WORLD_HEIGHT - 100,
                        40,
                        40,
                        "Pressione E perto de elementos "
                        + "interativos."
                );

        Rectangle objectView =
                new Rectangle(
                        testObject.getWidth(),
                        testObject.getHeight()
                );

        objectView.setFill(
                Color.LIGHTBLUE
        );

        objectView.relocate(
                testObject.getX(),
                testObject.getY()
        );

        this.interactables =
                List.of(
                        villageNPC,
                        testObject
                );

        worldLayer.getChildren().addAll(
                heroView,
                npcView,
                objectView
        );

        this.hudLayer =
                new Pane();

        hudLayer.setPrefSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        CameraTestOverlay.renderHud(
                hudLayer
        );

        this.dialogueController =
                new DialogueController(
                        hudLayer
                );

        this.gameRoot =
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

        this.screenRoot =
                new StackPane(
                        gameRoot
                );

        this.scene =
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

        this.keys =
                new boolean[4];

        configureInput();

        this.gameLoop =
                createGameLoop();
    }

    public Scene getScene() {
        return scene;
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

        /*
         * Durante um diálogo, E avança a fala.
         */
        if (dialogueController.isOpen()) {

            boolean stillOpen =
                    dialogueController
                            .advanceDialogue();

            if (!stillOpen) {
                onDialogueFinished.run();
            }

            return;
        }

        Interactable interactable =
                findNearestInteractable();

        if (interactable == null) {
            return;
        }

        dialogueController.startDialogue(
                interactable
        );

        onDialogueStarted.run();
    }

    private Interactable findNearestInteractable() {

        Interactable nearest = null;

        double nearestDistance =
                Double.MAX_VALUE;

        for (Interactable interactable
                : interactables) {

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
                        WORLD_WIDTH
                        - TILE_SIZE,
                        WORLD_HEIGHT
                        - TILE_SIZE
                );

                heroView.relocate(
                        hero.getX(),
                        hero.getY()
                );

                updateCamera();
            }
        };
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
                                WORLD_WIDTH
                                - LOGICAL_WIDTH
                        )
                );

        cameraY =
                Math.max(
                        0,
                        Math.min(
                                targetY,
                                WORLD_HEIGHT
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

    public void start() {

        lastTime = 0;

        updateCamera();

        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }

    public void closeDialogue() {
        dialogueController.closeDialogue();
    }
}