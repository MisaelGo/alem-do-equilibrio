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

        loadZone(zone, interactables,
                ChapterProgress.CERAMIC_BRIDGE_PLATES, 0);
    }

    /** Reconstrói colisões para o estoque e as etapas atuais da ponte. */
    public void loadZone(
            ExplorationZone zone,
            List<Interactable> interactables,
            int remainingPlates,
            int installedPlates) {

        Objects.requireNonNull(
                zone
        );

        Objects.requireNonNull(
                interactables
        );

        obstacles.clear();

        /*
         * Todos os elementos interagíveis da zona
         * passam a possuir colisão automaticamente.
         */
        for (Interactable interactable
                : interactables) {

            // Áreas de pegar/instalar são marcadores, não objetos sólidos.
            if (zone == ExplorationZone.INSULATOR_TRAIL
                    && (InsulatorTrailLayout.CERAMIC_PILE_INTERACTION
                                .equals(interactable.getDialogueId())
                        || InsulatorTrailLayout.CERAMIC_BRIDGE_INTERACTION
                                .equals(interactable.getDialogueId()))) {
                continue;
            }

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
                addNeutralBorderWallCollisions();
                addNeutralBushCollisions();
            }

            case INSULATOR_TRAIL -> {
                addInsulatorTrailWallCollisions();
                // As partes metálicas ainda descobertas bloqueiam a travessia.
                // Quando cobertas, tornam-se transitáveis imediatamente.
                addUncoveredBridgeCollisions(installedPlates);

                addObstacle(
                        InsulatorTrailLayout.HOUSE_X,
                        InsulatorTrailLayout.HOUSE_Y,
                        InsulatorTrailLayout.HOUSE_WIDTH,
                        InsulatorTrailLayout.HOUSE_HEIGHT
                );
                // Colisão em cada uma das placas: o herói pode
                // chegar até o contorno real da pilha.
                List<Rectangle2D> plates =
                        InsulatorTrailLayout.getCeramicPilePlates();
                for (int i = 0; i < Math.min(remainingPlates, plates.size()); i++) {
                    Rectangle2D plate = plates.get(i);
                    addObstacle(
                            plate.getMinX(),
                            plate.getMinY(),
                            plate.getWidth(),
                            plate.getHeight()
                    );
                }
                addObstacle(
                        InsulatorTrailLayout.MONOLITH_X,
                        InsulatorTrailLayout.MONOLITH_Y,
                        InsulatorTrailLayout.MONOLITH_WIDTH,
                        InsulatorTrailLayout.MONOLITH_HEIGHT
                );
            }

            case FACTION_CROSSROADS,
                 COULOMB_VALLEY,
                 FIELD_PLATEAU,
                 EQUIPOTENTIAL_RUINS,
                 FRAGMENT_CHAMBER -> {
            }
        }
    }

    /**
     * O herói não atravessa o metal exposto até instalar cerâmica
     * sobre a respectiva faixa.
     */
    private void addUncoveredBridgeCollisions(int installedPlates) {
        int columns = (int) (ZoneManager.WORLD_WIDTH
                / InsulatorTrailLayout.TILE_SIZE);
        int rows = (int) (ZoneManager.WORLD_HEIGHT
                / InsulatorTrailLayout.TILE_SIZE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int section = InsulatorTrailLayout.getBridgeSectionForTile(col, row);
                if (section >= installedPlates) {
                    addObstacle(
                            col * InsulatorTrailLayout.TILE_SIZE,
                            row * InsulatorTrailLayout.TILE_SIZE,
                            InsulatorTrailLayout.TILE_SIZE,
                            InsulatorTrailLayout.TILE_SIZE
                    );
                }
            }
        }
    }

    /**
     * Colisões idênticas às paredes desenhadas pelo TileMapRenderer.
     * Evita permitir que o herói atravesse as divisórias escuras.
     */
    private void addInsulatorTrailWallCollisions() {
        int columns = (int) (ZoneManager.WORLD_WIDTH
                / InsulatorTrailLayout.TILE_SIZE);
        int rows = (int) (ZoneManager.WORLD_HEIGHT
                / InsulatorTrailLayout.TILE_SIZE);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (InsulatorTrailLayout.isWallTile(col, row,
                        columns, rows)) {
                    addObstacle(
                            col * InsulatorTrailLayout.TILE_SIZE,
                            row * InsulatorTrailLayout.TILE_SIZE,
                            InsulatorTrailLayout.TILE_SIZE,
                            InsulatorTrailLayout.TILE_SIZE
                    );
                }
            }
        }
    }

    private void addNeutralBushCollisions() {

        addObstacle(
                ZoneManager.NEUTRAL_BUSH_1_X,
                ZoneManager.NEUTRAL_BUSH_1_Y,
                ZoneManager.NEUTRAL_BUSH_WIDTH,
                ZoneManager.NEUTRAL_BUSH_HEIGHT
        );

        addObstacle(
                ZoneManager.NEUTRAL_BUSH_2_X,
                ZoneManager.NEUTRAL_BUSH_2_Y,
                ZoneManager.NEUTRAL_BUSH_WIDTH,
                ZoneManager.NEUTRAL_BUSH_HEIGHT
        );
    }

    /**
     * Adiciona colisão a todos os tiles de parede
     * da Fronteira Neutra.
     *
     * A mesma regra usada para desenhar as bordas
     * é reutilizada aqui para manter visual e física
     * sempre sincronizados.
     */
    private void addNeutralBorderWallCollisions() {

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

        for (int row = 0;
                row < rows;
                row++) {

            for (int col = 0;
                    col < columns;
                    col++) {

                boolean isWall =
                        TileMapRenderer
                                .isNeutralBorderWallTile(
                                        col,
                                        row,
                                        columns,
                                        rows
                                );

                if (!isWall) {

                    continue;
                }

                addObstacle(
                        col * tileSize,
                        row * tileSize,
                        tileSize,
                        tileSize
                );
            }
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