package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MapEnemySpawner {

    private static final double ENEMY_SIZE = 40;

    private final Pane worldLayer;

    private final List<MapEnemyNode> activeEnemies;

    public MapEnemySpawner(
            Pane worldLayer) {

        this.worldLayer =
                Objects.requireNonNull(
                        worldLayer
                );

        this.activeEnemies =
                new ArrayList<>();
    }

    public void spawnForZone(
            ExplorationZone zone) {

        clear();

        switch (zone) {

            /*
             * A Fronteira Neutra é a região
             * de introdução e não possui inimigos.
             */
            case NEUTRAL_BORDER -> {
            }

            /*
             * Primeiros inimigos simples.
             */
            case INSULATOR_TRAIL -> {

                spawnEnemy(
                        new WanderingSpark(
                                201
                        ),
                        1250,
                        1400
                );

                spawnEnemy(
                        new UncontrolledCharge(
                                202,
                                ChargeType.NEGATIVE
                        ),
                        2200,
                        1050
                );
            }

            /*
             * A tensão entre as facções já acontece
             * em uma região afetada pela instabilidade.
             */
            case FACTION_CROSSROADS -> {

                spawnEnemy(
                        new UncontrolledCharge(
                                301,
                                ChargeType.POSITIVE
                        ),
                        1350,
                        1250
                );

                spawnEnemy(
                        new WanderingSpark(
                                302
                        ),
                        2350,
                        950
                );
            }

            /*
             * C1-044:
             * encontros médios no Vale de Coulomb.
             */
            case COULOMB_VALLEY -> {

                spawnEnemy(
                        new CorruptedDipole(
                                401,
                                ChargeType.POSITIVE
                        ),
                        1450,
                        1250
                );

                spawnEnemy(
                        new UnstableConductor(
                                402
                        ),
                        2300,
                        950
                );
            }

            case FIELD_PLATEAU -> {

                spawnEnemy(
                        new CorruptedDipole(
                                501,
                                ChargeType.NEGATIVE
                        ),
                        1400,
                        1250
                );

                spawnEnemy(
                        new UnstableConductor(
                                502
                        ),
                        2350,
                        1000
                );
            }

            case EQUIPOTENTIAL_RUINS -> {

                spawnEnemy(
                        new UnstableConductor(
                                601
                        ),
                        1400,
                        1150
                );

                spawnEnemy(
                        new CorruptedDipole(
                                602,
                                ChargeType.POSITIVE
                        ),
                        2350,
                        900
                );
            }

            /*
             * Na última região o encontro
             * é com o Guardião da Ruptura.
             *
             * O comportamento final das fases
             * ainda pertence à etapa do boss.
             */
            case FRAGMENT_CHAMBER -> {

                spawnEnemy(
                        new BossEnemy(
                                701,
                                "Guardião da Ruptura",
                                5,
                                100.0f
                        ),
                        1650,
                        1000
                );
            }
        }
    }

    private MapEnemyNode spawnEnemy(
            Enemy enemy,
            double x,
            double y) {

        Rectangle body =
                new Rectangle(
                        ENEMY_SIZE,
                        ENEMY_SIZE
                );

        body.setFill(
                getEnemyColor(
                        enemy
                )
        );

        body.setStroke(
                Color.BLACK
        );

        body.setStrokeWidth(
                2
        );

        Label name =
                new Label(
                        enemy.getName()
                );

        name.relocate(
                -30,
                -25
        );

        Pane visual =
                new Pane(
                        body,
                        name
                );

        visual.setPrefSize(
                ENEMY_SIZE,
                ENEMY_SIZE
        );

        visual.relocate(
                x,
                y
        );

        worldLayer
                .getChildren()
                .add(
                        visual
                );

        MapEnemyNode node =
                new MapEnemyNode(
                        enemy,
                        visual,
                        x,
                        y
                );

        activeEnemies.add(
                node
        );

        return node;
    }

    private Color getEnemyColor(
            Enemy enemy) {

        if (enemy instanceof BossEnemy) {

            return Color.DARKVIOLET;
        }

        if (enemy
                instanceof UnstableConductor) {

            return Color.DARKORANGE;
        }

        if (enemy
                instanceof CorruptedDipole) {

            return Color.MEDIUMPURPLE;
        }

        if (enemy
                instanceof UncontrolledCharge) {

            return Color.CRIMSON;
        }

        return Color.ORANGERED;
    }

    public Enemy findNearbyEnemy(
            Hero hero,
            double heroSize,
            double distanceThreshold) {

        for (MapEnemyNode node
                : activeEnemies) {

            if (!node.getEnemy()
                    .isAlive()) {

                continue;
            }

            if (node.isCloseTo(
                    hero,
                    heroSize,
                    distanceThreshold
            )) {

                return node.getEnemy();
            }
        }

        return null;
    }

    public void removeEnemy(
            Enemy enemy) {

        MapEnemyNode target =
                null;

        for (MapEnemyNode node
                : activeEnemies) {

            if (node.getEnemy()
                    == enemy) {

                target = node;

                break;
            }
        }

        if (target == null) {
            return;
        }

        worldLayer
                .getChildren()
                .remove(
                        target.getVisualNode()
                );

        activeEnemies.remove(
                target
        );
    }

    public void clear() {

        for (MapEnemyNode node
                : activeEnemies) {

            worldLayer
                    .getChildren()
                    .remove(
                            node.getVisualNode()
                    );
        }

        activeEnemies.clear();
    }

    public List<MapEnemyNode>
            getActiveEnemies() {

        return List.copyOf(
                activeEnemies
        );
    }

    public static class MapEnemyNode {

        private final Enemy enemy;

        private final Pane visualNode;

        private final double x;

        private final double y;

        public MapEnemyNode(
                Enemy enemy,
                Pane visualNode,
                double x,
                double y) {

            this.enemy = enemy;
            this.visualNode = visualNode;

            this.x = x;
            this.y = y;
        }

        public boolean isCloseTo(
                Hero hero,
                double heroSize,
                double distanceThreshold) {

            double heroCenterX =
                    hero.getX()
                    + heroSize / 2.0;

            double heroCenterY =
                    hero.getY()
                    + heroSize / 2.0;

            double enemyCenterX =
                    x
                    + ENEMY_SIZE / 2.0;

            double enemyCenterY =
                    y
                    + ENEMY_SIZE / 2.0;

            return Math.hypot(
                    heroCenterX
                    - enemyCenterX,

                    heroCenterY
                    - enemyCenterY
            ) <= distanceThreshold;
        }

        public Enemy getEnemy() {

            return enemy;
        }

        public Pane getVisualNode() {

            return visualNode;
        }
    }
}