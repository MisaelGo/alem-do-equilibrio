package com.alemdoequilibrio.game;

import java.util.Objects;
import java.util.Random;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * Renderizador de mapas baseado em tiles e imagens.
 *
 * Constrói o cenário da exploração desenhando os tiles de terreno, trilha e
 * rochas sobre um Canvas de alto desempenho, eliminando a grade temporária de
 * debug e consumindo as texturas cacheadas pelo ResourceManager.
 */
public final class TileMapRenderer {

    private static final String TILE_GROUND_01 = "/images/tile_neutral_ground_01.png";
    private static final String TILE_GROUND_02 = "/images/tile_neutral_ground_02.png";
    private static final String TILE_PATH = "/images/tile_neutral_path.png";
    private static final String TILE_ROCK = "/images/tile_neutral_rock.png";

    // Zona 2: Trilha dos Isolantes
    private static final String TILE_INSULATOR_GROUND = "/images/tile_insulator_ground.png";
    private static final String TILE_INSULATOR_CERAMIC = "/images/tile_insulator_ceramic.png";
    private static final String TILE_INSULATOR_ROCK = "/images/tile_insulator_rock.png";

    // Zona 3: Encruzilhada das Facções
    private static final String TILE_CROSSROADS_DIRT = "/images/tile_crossroads_dirt.png";
    private static final String TILE_CROSSROADS_BRICK = "/images/tile_crossroads_brick.png";
    private static final String TILE_CROSSROADS_METAL = "/images/tile_crossroads_metal.png";

    // Zona 4: Vale de Coulomb
    private static final String TILE_COULOMB_GROUND = "/images/tile_coulomb_ground.png";
    private static final String TILE_COULOMB_PATH = "/images/tile_coulomb_path.png";
    private static final String TILE_COULOMB_CLIFF = "/images/tile_coulomb_cliff.png";

    // Zona 5: Planalto do Campo
    private static final String TILE_PLATEAU_GROUND = "/images/tile_field_plateau_ground.png";
    private static final String TILE_PLATEAU_PATH = "/images/tile_field_plateau_path.png";
    private static final String TILE_PLATEAU_ROCK = "/images/tile_field_plateau_rock.png";

    // Zona 6: Ruínas Equipotenciais
    private static final String TILE_RUINS_SLAB = "/images/tile_ruins_slab.png";
    private static final String TILE_RUINS_GOLD = "/images/tile_ruins_gold_rune.png";
    private static final String TILE_RUINS_WALL = "/images/tile_ruins_wall.png";

    // Zona 7: Câmara do Fragmento
    private static final String TILE_CHAMBER_OBSIDIAN = "/images/tile_chamber_obsidian.png";
    private static final String TILE_CHAMBER_CORE = "/images/tile_chamber_core_floor.png";
    private static final String TILE_CHAMBER_WALL = "/images/tile_chamber_wall.png";

    /*
     * Classe utilitária: construtor privado para impedir instanciação.
     */
    private TileMapRenderer() {
    }

    /**
     * Renderiza o mapa completo correspondente à zona de exploração ativa.
     *
     * @param root contêiner do mundo onde o mapa é inserido
     * @param zone zona do capítulo atual
     * @param resourceManager gerenciador central para cache de imagens
     * @param width largura total do mundo em pixels
     * @param height altura total do mundo em pixels
     * @param tileSize tamanho de cada tile
     */
    public static void renderZone(
            Pane root,
            ExplorationZone zone,
            ResourceManager resourceManager,
            double width,
            double height,
            double tileSize) {

        Objects.requireNonNull(root, "O Pane root não pode ser nulo.");
        Objects.requireNonNull(zone, "A zona não pode ser nula.");
        Objects.requireNonNull(resourceManager, "O ResourceManager não pode ser nulo.");

        if (zone == ExplorationZone.NEUTRAL_BORDER) {
            renderNeutralBorder(root, resourceManager, width, height, tileSize);
            return;
        }

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int columns = (int) (width / tileSize);
        int rows = (int) (height / tileSize);

        ZoneTheme theme = getThemeForZone(zone, resourceManager);
        Random rand = new Random(101 + zone.ordinal());

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                double x = col * tileSize;
                double y = row * tileSize;

                // 1. Limites do cenário
                boolean isNorthWall = row <= 2;
                boolean isSouthWall = row >= rows - 2;
                boolean isWestWall = col == 0;
                // Saída no leste apenas para zonas 1 a 6; Câmara do Fragmento (7) é arena fechada
                boolean hasEastExit = (zone != ExplorationZone.FRAGMENT_CHAMBER);
                boolean isEastWall = (col >= columns - 2) && (!hasEastExit || (row < 35 || row > 42));

                boolean isWall = zone == ExplorationZone.INSULATOR_TRAIL
                        ? InsulatorTrailLayout.isWallTile(col, row, columns, rows)
                        : isNorthWall || isSouthWall || isWestWall || isEastWall;
                if (isWall) {
                    gc.drawImage(theme.wall, x, y, tileSize, tileSize);
                    continue;
                }

                // 2. Trilhas e estruturas específicas da zona
                if (isZonePathTile(zone, col, row)) {
                    gc.drawImage(theme.path, x, y, tileSize, tileSize);
                    continue;
                }

                // 3. Solo padrão da zona
                gc.drawImage(theme.ground, x, y, tileSize, tileSize);
            }
        }

        root.getChildren().add(0, canvas);
    }

    /**
     * Renderiza o mapa da Zona 1 (Fronteira Neutra).
     *
     * Constrói as montanhas de borda, o chão neutro de musgo/terra verde-acinzentado (#607D72)
     * e a trilha de terra batida que conduz o jogador do início até a saída para a Zona 2.
     *
     * @param root contêiner do mundo onde o mapa é inserido
     * @param resourceManager gerenciador central para cache de imagens
     * @param width largura total do mundo em pixels
     * @param height altura total do mundo em pixels
     * @param tileSize tamanho de cada tile (40px)
     */
    public static void renderNeutralBorder(
            Pane root,
            ResourceManager resourceManager,
            double width,
            double height,
            double tileSize) {

        Objects.requireNonNull(root, "O Pane root não pode ser nulo.");
        Objects.requireNonNull(resourceManager, "O ResourceManager não pode ser nulo.");

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image ground01 = resourceManager.getImage(TILE_GROUND_01);
        Image ground02 = resourceManager.getImage(TILE_GROUND_02);
        Image pathImg = resourceManager.getImage(TILE_PATH);
        Image rockImg = resourceManager.getImage(TILE_ROCK);

        int columns = (int) (width / tileSize);
        int rows = (int) (height / tileSize);

        // Semente fixa para que a distribuição de variações de terreno seja determinística
        Random rand = new Random(101);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                double x = col * tileSize;
                double y = row * tileSize;

                /*
                 * Calcula primeiro se o tile faz parte da estrada.
                 * A borda leste usa essa informação para deixar
                 * aberta somente a passagem exata do caminho.
                 */
                boolean isPath =
                        isNeutralBorderPathTile(
                                col,
                                row,
                                columns,
                                rows
                        );

                // 1. Paredes e montanhas rochosas de limite do cenário
                if (isNeutralBorderWallTile(
                        col,
                        row,
                        columns,
                        rows
                )) {
                    gc.drawImage(rockImg, x, y, tileSize, tileSize);
                    continue;
                }

                // 2. Trilha original, comprimida para o novo tamanho do mapa
                if (isPath) {
                    gc.drawImage(pathImg, x, y, tileSize, tileSize);
                    continue;
                }

                // 3. Solo neutro com variação natural de musgo e cascalho
                Image groundTile = (rand.nextInt(10) < 3) ? ground02 : ground01;
                gc.drawImage(groundTile, x, y, tileSize, tileSize);
            }
        }

        // Insere o canvas no fundo da camada do mundo (índice 0)
        root.getChildren().add(0, canvas);
    }

    /**
     * Define se a célula atual faz parte do percurso ou destaque temático da zona.
     */
    private static boolean isZonePathTile(ExplorationZone zone, int col, int row) {
        return switch (zone) {
            case NEUTRAL_BORDER -> false;

            case INSULATOR_TRAIL ->
                InsulatorTrailLayout.isCeramicTile(col, row);

            case FACTION_CROSSROADS ->
                // Encruzilhada: via horizontal principal e grande avenida perpendicular
                (row >= 39 && row <= 42 && col >= 2 && col <= 77)
                || (col >= 38 && col <= 42 && row >= 4 && row <= 42);

            case COULOMB_VALLEY ->
                // Desfiladeiro de Coulomb: caminho passando pelos marcos d (x=1200), 2d (x=1700), 3d (x=2400)
                (row >= 31 && row <= 34 && col >= 25 && col <= 65)
                || (col >= 2 && col <= 30 && row >= 38 && row <= 41)
                || (col >= 60 && col <= 77 && row >= 35 && row <= 39);

            case FIELD_PLATEAU ->
                // Planalto do Campo: pista elevada acompanhando os vetores de campo
                (row >= 37 && row <= 40 && col >= 2 && col <= 25)
                || (row >= 28 && row <= 32 && col >= 26 && col <= 60)
                || (row >= 35 && row <= 39 && col >= 58 && col <= 77)
                || (col >= 24 && col <= 28 && row >= 30 && row <= 39);

            case EQUIPOTENTIAL_RUINS ->
                // Ruínas: calçada rúnica de ouro e círculos equipotenciais concêntricos em torno de (col 42, row 26)
                (row >= 38 && row <= 41 && col >= 2 && col <= 77)
                || isEquipotentialCircle(col, row, 42, 26, 6)
                || isEquipotentialCircle(col, row, 42, 26, 10);

            case FRAGMENT_CHAMBER ->
                // Câmara do Fragmento: corredor de acesso e grande arena circular central
                (row >= 38 && row <= 41 && col >= 2 && col <= 28)
                || (Math.hypot(col - 42, row - 22) <= 12);
        };
    }

    private static boolean isEquipotentialCircle(int col, int row, int centerCol, int centerRow, int radius) {
        double dist = Math.hypot(col - centerCol, row - centerRow);
        return Math.abs(dist - radius) < 0.9;
    }

    /**
     * Define se o tile pertence às paredes da Fronteira Neutra.
     *
     * A borda superior possui duas fileiras, a inferior duas,
     * a esquerda uma coluna e a direita uma coluna. Na borda
     * direita, os tiles da estrada permanecem livres.
     */
    static boolean isNeutralBorderWallTile(
            int col,
            int row,
            int columns,
            int rows) {

        boolean isPath =
                isNeutralBorderPathTile(
                        col,
                        row,
                        columns,
                        rows
                );

        boolean isNorthWall =
                row <= 1;

        boolean isSouthWall =
                row >= rows - 2;

        boolean isWestWall =
                col == 0;

        boolean isEastWall =
                col == columns - 1
                && !isPath;

        return isNorthWall
                || isSouthWall
                || isWestWall
                || isEastWall;
    }

    /**
     * Define o traçado da trilha de terra batida da Fronteira Neutra.
     *
     * O desenho preserva a sequência visual do mapa anterior:
     * começa junto à borda inferior, sobe suavemente depois do Nilo
     * e termina alinhado à abertura leste.
     */
    static boolean isNeutralBorderPathTile(
            int col,
            int row,
            int columns,
            int rows) {

        int startCenterRow =
                rows - 4;

        int firstSectionEnd =
                Math.max(
                        12,
                        (int) Math.round(
                                columns * 0.26
                        )
                );

        int middleSectionEnd =
                Math.max(
                        firstSectionEnd + 1,
                        (int) Math.round(
                                columns * 0.64
                        )
                );

        int lastPathColumn =
                columns - 1;

        /*
         * Trecho inicial:
         * faixa horizontal próxima ao ponto de chegada.
         */
        if (col >= 2
                && col <= firstSectionEnd
                && row >= startCenterRow - 1
                && row <= startCenterRow + 1) {

            return true;
        }

        /*
         * Trecho central:
         * mantém a subida suave da estrada original.
         */
        if (col > firstSectionEnd
                && col <= middleSectionEnd) {

            double progress =
                    (col - firstSectionEnd)
                    / (double) (
                            middleSectionEnd
                            - firstSectionEnd
                    );

            int targetRow =
                    (int) Math.round(
                            startCenterRow
                            - 4 * progress
                    );

            return row >= targetRow - 1
                    && row <= targetRow + 1;
        }

        /*
         * Trecho final:
         * atravessa a passagem da parede leste até o fim do mapa.
         */
        if (col > middleSectionEnd
                && col <= lastPathColumn) {

            int finalCenterRow =
                    rows - 8;

            return row >= finalCenterRow - 1
                    && row <= finalCenterRow + 2;
        }

        return false;
    }

    /**
     * Mapeia os conjuntos de texturas visuais para cada zona.
     */
    private static ZoneTheme getThemeForZone(ExplorationZone zone, ResourceManager rm) {
        return switch (zone) {
            case NEUTRAL_BORDER -> new ZoneTheme(
                    rm.getImage(TILE_GROUND_01),
                    rm.getImage(TILE_PATH),
                    rm.getImage(TILE_ROCK)
            );
            case INSULATOR_TRAIL -> new ZoneTheme(
                    rm.getImage(TILE_INSULATOR_GROUND),
                    rm.getImage(TILE_INSULATOR_CERAMIC),
                    rm.getImage(TILE_INSULATOR_ROCK)
            );
            case FACTION_CROSSROADS -> new ZoneTheme(
                    rm.getImage(TILE_CROSSROADS_DIRT),
                    rm.getImage(TILE_CROSSROADS_BRICK),
                    rm.getImage(TILE_CROSSROADS_METAL)
            );
            case COULOMB_VALLEY -> new ZoneTheme(
                    rm.getImage(TILE_COULOMB_GROUND),
                    rm.getImage(TILE_COULOMB_PATH),
                    rm.getImage(TILE_COULOMB_CLIFF)
            );
            case FIELD_PLATEAU -> new ZoneTheme(
                    rm.getImage(TILE_PLATEAU_GROUND),
                    rm.getImage(TILE_PLATEAU_PATH),
                    rm.getImage(TILE_PLATEAU_ROCK)
            );
            case EQUIPOTENTIAL_RUINS -> new ZoneTheme(
                    rm.getImage(TILE_RUINS_SLAB),
                    rm.getImage(TILE_RUINS_GOLD),
                    rm.getImage(TILE_RUINS_WALL)
            );
            case FRAGMENT_CHAMBER -> new ZoneTheme(
                    rm.getImage(TILE_CHAMBER_OBSIDIAN),
                    rm.getImage(TILE_CHAMBER_CORE),
                    rm.getImage(TILE_CHAMBER_WALL)
            );
        };
    }

    private record ZoneTheme(Image ground, Image path, Image wall) {
    }
}
