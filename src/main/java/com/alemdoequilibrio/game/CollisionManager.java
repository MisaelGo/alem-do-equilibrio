package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.geometry.Rectangle2D;

/**
 * Gerencia as areas solidas da zona atual.
 *
 * A logica de movimento nao precisa conhecer
 * individualmente pedras, paredes, NPCs ou construcoes.
 * Todos sao tratados como retangulos de colisao.
 */
public class CollisionManager {

    private final List<Rectangle2D>
            obstacles;

    public CollisionManager() {

        obstacles =
                new ArrayList<>();
    }

    /**
     * Reconstroi as colisoes ao entrar em uma nova zona.
     */
    public void loadZone(
            ExplorationZone zone,
            List<Interactable> interactables) {

        Objects.requireNonNull(
                zone
        );

        Objects.requireNonNull(
                interactables
        );

        obstacles.clear();

        /*
         * NPCs e comerciantes passam a possuir
         * colisao automaticamente.
         */
        for (Interactable interactable
                : interactables) {

            addObstacle(
                    interactable.getX(),
                    interactable.getY(),
                    interactable.getWidth(),
                    interactable.getHeight()
            );
        }

        /*
         * Obstaculos fisicos ja existentes
         * nos mapas atuais.
         *
         * Conforme cada mapa for polido,
         * novas areas serao adicionadas aqui.
         */
        switch (zone) {

            case NEUTRAL_BORDER -> {

                addObstacle(
                        ZoneManager.NEUTRAL_HOUSE_X,
                        ZoneManager.NEUTRAL_HOUSE_Y,
                        ZoneManager.NEUTRAL_HOUSE_WIDTH,
                        ZoneManager.NEUTRAL_HOUSE_HEIGHT
                );

                addNeutralBorderSideCollisions();
            }

            case INSULATOR_TRAIL ->
                addObstacle(
                        1100,
                        1500,
                        70,
                        70
                );

            case FACTION_CROSSROADS,
                 COULOMB_VALLEY,
                 FIELD_PLATEAU,
                 EQUIPOTENTIAL_RUINS,
                 FRAGMENT_CHAMBER -> {
            }
        }
    }

    /**
     * Adiciona colisão em todos os blocos da lateral direita
     * da Fronteira Neutra, deixando livre somente a passagem
     * onde a estrada atravessa a borda.
     */
    private void addNeutralBorderSideCollisions() {

        double tileSize =
                40;

        int columns =
                (int) (
                        ZoneManager.NEUTRAL_BORDER_WIDTH
                        / tileSize
                );

        int rows =
                (int) (
                        ZoneManager.NEUTRAL_BORDER_HEIGHT
                        / tileSize
                );

        int rightColumn =
                columns - 1;

        double x =
                rightColumn
                * tileSize;

        for (int row = 0;
                row < rows;
                row++) {

            boolean isPath =
                    TileMapRenderer
                            .isNeutralBorderPathTile(
                                    rightColumn,
                                    row,
                                    columns,
                                    rows
                            );

            if (isPath) {

                continue;
            }

            addObstacle(
                    x,
                    row * tileSize,
                    tileSize,
                    tileSize
            );
        }
    }

    public void addObstacle(
            double x,
            double y,
            double width,
            double height) {

        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(width)
                || !Double.isFinite(height)
                || width <= 0
                || height <= 0) {

            throw new IllegalArgumentException(
                    "Area de colisao invalida."
            );
        }

        obstacles.add(
                new Rectangle2D(
                        x,
                        y,
                        width,
                        height
                )
        );
    }

    public boolean collides(
            double x,
            double y,
            double width,
            double height) {

        Rectangle2D heroArea =
                new Rectangle2D(
                        x,
                        y,
                        width,
                        height
                );

        for (Rectangle2D obstacle
                : obstacles) {

            if (heroArea.intersects(
                    obstacle
            )) {

                return true;
            }
        }

        return false;
    }

    public int getObstacleCount() {

        return obstacles.size();
    }
}