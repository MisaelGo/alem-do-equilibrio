package com.alemdoequilibrio.game;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class MapEnemySpawner {

    private final Pane worldLayer;
    private final List<MapEnemyNode> activeEnemies = new ArrayList<>();

    public static class MapEnemyNode {
        private final Enemy enemy;
        private final Rectangle visualNode; // Substituído de ImageView para Rectangle

        public MapEnemyNode(Enemy enemy, Rectangle visualNode) {
            this.enemy = enemy;
            this.visualNode = visualNode;
        }

        public Enemy getEnemy() {
            return enemy;
        }

        public Rectangle getVisualNode() {
            return visualNode;
        }

        // Verifica a proximidade utilizando a coordenada do círculo
        public boolean isCloseTo(Hero hero, double distanceThreshold) {
            double deltaX = visualNode.getX() - hero.getX(); // Ajuste getX() para o getter do Hero
            double deltaY = visualNode.getY() - hero.getY(); // Ajuste getY() para o getter do Hero
            return Math.hypot(deltaX, deltaY) <= distanceThreshold;
        }
    }

    public MapEnemySpawner(Pane worldLayer) {
        this.worldLayer = worldLayer;
    }

    /**
     * Spawna um inimigo representado por um círculo no mapa.
     * @param id
     * @param name
     * @param level
     * @param health
     * @param x
     * @param y
     * @return 
     */
    public MapEnemyNode spawnEnemy(int id, String name, int level, float health, double x, double y) {
        Enemy enemy = new Enemy(id, name, level, health);

        // Representação visual provisória
        Rectangle rectangle = new Rectangle(x, y, 30, 30);
        rectangle.setFill(Color.RED);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(2);

        worldLayer.getChildren().add(rectangle);

        MapEnemyNode node = new MapEnemyNode(enemy, rectangle);
        activeEnemies.add(node);
        return node;
    }

    public List<MapEnemyNode> getActiveEnemies() {
        return activeEnemies;
    }
}