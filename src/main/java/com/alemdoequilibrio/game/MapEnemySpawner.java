package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Gerencia o posicionamento e a renderização dos inimigos no mapa de exploração.
 *
 * Utiliza sprites pixel art dedicados e acessíveis com fallback visual robusto.
 *
 * POO 1.1 - Encapsulamento:
 * Lista de nós ativos e referências internas são privadas.
 *
 * POO 5 - Agregação:
 * Agrega ResourceManager e instâncias polimórficas de Enemy.
 *
 * POO 7.1 - Sobrecarga de construtores:
 * Permite inicialização padrão ou com injeção de ResourceManager.
 */
public class MapEnemySpawner {

    private static final double ENEMY_SIZE = 40;

    private final Pane worldLayer;
    private final ResourceManager resourceManager;
    private final List<MapEnemyNode> activeEnemies;

    /**
     * Construtor padrão para compatibilidade com inicializações sem ResourceManager.
     */
    public MapEnemySpawner(Pane worldLayer) {
        this(worldLayer, new ResourceManager());
    }

    /**
     * Construtor com injeção do gerenciador central de recursos.
     */
    public MapEnemySpawner(Pane worldLayer, ResourceManager resourceManager) {
        this.worldLayer = Objects.requireNonNull(worldLayer, "worldLayer não pode ser nulo.");
        this.resourceManager = (resourceManager != null) ? resourceManager : new ResourceManager();
        this.activeEnemies = new ArrayList<>();
    }

    public void spawnForZone(ExplorationZone zone) {
        clear();

        switch (zone) {
            /*
             * A Fronteira Neutra é a região
             * de introdução e não possui inimigos.
             */
            case NEUTRAL_BORDER -> {
            }

            /*
             * Primeiros inimigos simples na Trilha dos Isolantes.
             */
            case INSULATOR_TRAIL -> {
                spawnEnemy(
                        new WanderingSpark(201),
                        1250,
                        1400
                );

                spawnEnemy(
                        new UncontrolledCharge(202, ChargeType.NEGATIVE),
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
                        new UncontrolledCharge(301, ChargeType.POSITIVE),
                        1350,
                        1250
                );

                spawnEnemy(
                        new WanderingSpark(302),
                        2350,
                        950
                );
            }

            /*
             * C1-044: Encontros no Vale de Coulomb.
             */
            case COULOMB_VALLEY -> {
                spawnEnemy(
                        new CorruptedDipole(401, ChargeType.POSITIVE),
                        1450,
                        1250
                );

                spawnEnemy(
                        new UnstableConductor(402),
                        2300,
                        950
                );
            }

            case FIELD_PLATEAU -> {
                spawnEnemy(
                        new CorruptedDipole(501, ChargeType.NEGATIVE),
                        1400,
                        1250
                );

                spawnEnemy(
                        new UnstableConductor(502),
                        2350,
                        1000
                );
            }

            case EQUIPOTENTIAL_RUINS -> {
                spawnEnemy(
                        new UnstableConductor(601),
                        1400,
                        1150
                );

                spawnEnemy(
                        new CorruptedDipole(602, ChargeType.POSITIVE),
                        2350,
                        900
                );
            }

            /*
             * Na última região o confronto é com o Guardião da Ruptura.
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

    private MapEnemyNode spawnEnemy(Enemy enemy, double x, double y) {
        double width = getEnemyWidth(enemy);
        double height = getEnemyHeight(enemy);

        Node visualBody;
        try {
            String spritePath = getEnemySpritePath(enemy);
            Image sprite = resourceManager.getImage(spritePath);
            ImageView imageView = new ImageView(sprite);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(false);
            visualBody = imageView;

            // Animação de flutuação / hovering suave
            TranslateTransition hover = new TranslateTransition(
                    Duration.seconds(1.2 + (enemy.getEnemyId() % 4) * 0.2),
                    imageView
            );
            hover.setFromY(-3);
            hover.setToY(3);
            hover.setAutoReverse(true);
            hover.setCycleCount(TranslateTransition.INDEFINITE);
            hover.setInterpolator(Interpolator.EASE_BOTH);
            hover.play();
        } catch (Exception e) {
            Rectangle body = new Rectangle(width, height);
            body.setFill(getEnemyColor(enemy));
            body.setStroke(Color.BLACK);
            body.setStrokeWidth(2);
            visualBody = body;
        }

        Label name = new Label(enemy.getName());
        name.setStyle(
                "-fx-text-fill: #F5DA93;"
                + "-fx-font-family: 'Consolas', monospace;"
                + "-fx-font-size: 11px;"
                + "-fx-font-weight: bold;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 3, 0, 0, 1);"
        );
        name.setPrefWidth(120);
        name.setAlignment(Pos.CENTER);
        name.relocate((width - 120) / 2.0, -22);

        Pane visual = new Pane(visualBody, name);
        visual.setPrefSize(width, height);
        visual.relocate(x, y);

        worldLayer.getChildren().add(visual);

        MapEnemyNode node = new MapEnemyNode(
                enemy,
                visual,
                x,
                y,
                width,
                height
        );

        activeEnemies.add(node);
        return node;
    }

    private String getEnemySpritePath(Enemy enemy) {
        if (enemy instanceof BossEnemy) {
            return "/images/enemy_map_boss.png";
        }
        if (enemy instanceof UnstableConductor) {
            return "/images/enemy_map_conductor.png";
        }
        if (enemy instanceof CorruptedDipole) {
            return "/images/enemy_map_dipole.png";
        }
        if (enemy instanceof UncontrolledCharge charge) {
            return charge.getChargeType() == ChargeType.POSITIVE
                    ? "/images/enemy_map_charge_pos.png"
                    : "/images/enemy_map_charge_neg.png";
        }
        if (enemy instanceof ChargedEnemy charged) {
            return charged.getChargeType() == ChargeType.POSITIVE
                    ? "/images/enemy_map_charge_pos.png"
                    : "/images/enemy_map_charge_neg.png";
        }
        return "/images/enemy_map_wandering_spark.png";
    }

    private double getEnemyWidth(Enemy enemy) {
        if (enemy instanceof BossEnemy) {
            return 72.0;
        }
        if (enemy instanceof CorruptedDipole) {
            return 48.0;
        }
        return ENEMY_SIZE;
    }

    private double getEnemyHeight(Enemy enemy) {
        if (enemy instanceof BossEnemy) {
            return 72.0;
        }
        return ENEMY_SIZE;
    }

    private Color getEnemyColor(Enemy enemy) {
        if (enemy instanceof BossEnemy) {
            return Color.DARKVIOLET;
        }
        if (enemy instanceof UnstableConductor) {
            return Color.DARKORANGE;
        }
        if (enemy instanceof CorruptedDipole) {
            return Color.MEDIUMPURPLE;
        }
        if (enemy instanceof UncontrolledCharge) {
            return Color.CRIMSON;
        }
        return Color.ORANGERED;
    }

    public Enemy findNearbyEnemy(Hero hero, double heroSize, double distanceThreshold) {
        for (MapEnemyNode node : activeEnemies) {
            if (!node.getEnemy().isAlive()) {
                continue;
            }

            if (node.isCloseTo(hero, heroSize, distanceThreshold)) {
                return node.getEnemy();
            }
        }

        return null;
    }

    public void removeEnemy(Enemy enemy) {
        MapEnemyNode target = null;
        for (MapEnemyNode node : activeEnemies) {
            if (node.getEnemy() == enemy) {
                target = node;
                break;
            }
        }

        if (target == null) {
            return;
        }

        worldLayer.getChildren().remove(target.getVisualNode());
        activeEnemies.remove(target);
    }

    public void clear() {
        for (MapEnemyNode node : activeEnemies) {
            worldLayer.getChildren().remove(node.getVisualNode());
        }
        activeEnemies.clear();
    }

    public List<MapEnemyNode> getActiveEnemies() {
        return List.copyOf(activeEnemies);
    }

    public static class MapEnemyNode {

        private final Enemy enemy;
        private final Pane visualNode;
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        public MapEnemyNode(
                Enemy enemy,
                Pane visualNode,
                double x,
                double y,
                double width,
                double height) {

            this.enemy = enemy;
            this.visualNode = visualNode;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public MapEnemyNode(
                Enemy enemy,
                Pane visualNode,
                double x,
                double y) {
            this(enemy, visualNode, x, y, ENEMY_SIZE, ENEMY_SIZE);
        }

        public boolean isCloseTo(
                Hero hero,
                double heroSize,
                double distanceThreshold) {

            double heroCenterX = hero.getX() + heroSize / 2.0;
            double heroCenterY = hero.getY() + heroSize / 2.0;
            double enemyCenterX = x + width / 2.0;
            double enemyCenterY = y + height / 2.0;

            return Math.hypot(
                    heroCenterX - enemyCenterX,
                    heroCenterY - enemyCenterY
            ) <= distanceThreshold;
        }

        public Enemy getEnemy() {
            return enemy;
        }

        public Pane getVisualNode() {
            return visualNode;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }
    }
}
