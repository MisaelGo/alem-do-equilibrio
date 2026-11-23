package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;

/**
 * Geometria compartilhada da Trilha dos Isolantes.
 *
 * Os corredores seguem o croqui: cerâmica segura sobre o solo de madeira
 * encharcada. Nesta etapa os materiais são apenas visuais; dano ambiental
 * e posicionamento de placas serão adicionados em seguida.
 *
 * Coordenadas dos caminhos e paredes em tiles de 40 x 40 px.
 * Esta classe centraliza os dados usados no desenho e nas colisões.
 */
final class InsulatorTrailLayout {

    static final int TILE_SIZE = 40;

    /** Interação nas extremidades da passarela sem colisão adicional. */
    static final String CERAMIC_PILE_INTERACTION = "CERAMIC_PILE_PICKUP";
    static final String CERAMIC_BRIDGE_INTERACTION = "CERAMIC_BRIDGE_PLACE";


    /*
     * Mesmo vão da saída leste da Fronteira Neutra: 4 tiles (160 px).
     * Fronteira: linhas 15..18; Trilha: 36..39.
     * Diferença de 21 tiles = 840 px entre os limites inferiores dos mapas.
     * Assim, as duas paredes ficam na mesma altura quando a câmera
     * mostra a extremidade inferior das respectivas regiões.
     */
    static final int WEST_GATE_FIRST_ROW = 36;
    static final int WEST_GATE_LAST_ROW = 39;

    // Saída na extremidade superior direita (4 tiles = 160 px).
    static final int EAST_GATE_FIRST_ROW = 9;
    static final int EAST_GATE_LAST_ROW = 12;

    static final double HOUSE_X = 175;
    static final double HOUSE_Y = 185;
    static final double HOUSE_WIDTH = 400;
    static final double HOUSE_HEIGHT = 220;

    static final double CERAMIC_PILE_X = 220;
    // Encostada à borda superior do corredor inferior, sem invadi-lo.
    static final double CERAMIC_PILE_Y = 1340;
    static final double CERAMIC_PILE_WIDTH = 180;
    static final double CERAMIC_PILE_HEIGHT = 110;

    // Mantém o centro original e ajusta a colisão à imagem vertical.
    static final double MONOLITH_X = 2761;
    static final double MONOLITH_Y = 1330;
    static final double MONOLITH_WIDTH = 128;
    static final double MONOLITH_HEIGHT = 185;

    // Respawn seguro na base de cerâmica, ao sul do monólito.
    static final double MONOLITH_CHECKPOINT_X =
            MONOLITH_X + (MONOLITH_WIDTH - TILE_SIZE) / 2.0;
    static final double MONOLITH_CHECKPOINT_Y =
            MONOLITH_Y + MONOLITH_HEIGHT + 8;

    // Paredes interiores do desenho: coluna esquerda, divisão e bloqueio leste.
    // Intervalos [coluna inicial, final, linha inicial, final] inclusivos.
    private static final int[][] INNER_WALLS = {
        {25, 27, 3, 9},
        {0, 29, 16, 18},
        {61, 79, 22, 24}
    };

    /*
     * Traçado conectado: a ramificação inferior termina junto ao monólito,
     * sem uma segunda rota que contorne o monumento pela direita. O jogador
     * deve voltar pelo mesmo caminho diagonal para seguir até a saída.
     */
    private static final double[][][] ROUTES = {
        // Entrada oeste e desvio diagonal que termina diante do monólito.
        { {3, 38}, {11, 38}, {19, 38}, {27, 34}, {35, 31},
          {43, 28}, {49, 27}, {57, 30}, {62, 33}, {65, 35},
          {66, 37} },

        // Passagem superior sob a divisória rochosa (colunas 25-27).
        // Centralizada na linha 13 para não ficar cortada pela parede.
        { {3, 13}, {11, 13}, {19, 13}, {24, 13}, {28, 13},
          {32, 13}, {36, 14}, {41, 18}, {45, 18}, {53, 14},
          {62, 8}, {70, 10}, {79, 11} },

        // Ligação central que permite sair da área do monólito e avançar.
        { {43, 28}, {45, 24}, {46, 20}, {45, 18} }
    };

    // Meia-largura dos corredores, na mesma ordem de ROUTES.
    private static final double[] ROUTE_RADII = {3.1, 2.5, 2.4};

    // As distâncias são medidas a partir dos blocos reais da passarela.
    private static final List<List<Rectangle2D>> BRIDGE_SECTION_TILES =
            buildBridgeSectionTiles();

    private InsulatorTrailLayout() {
    }

    /**
     * Retângulos das nove placas visíveis, usados tanto na renderização
     * quanto nas colisões. Não cria colisão no espaço vazio da pilha.
     */
    static List<Rectangle2D> getCeramicPilePlates() {
        List<Rectangle2D> plates = new ArrayList<>();

        for (int layer = 0; layer < 3; layer++) {
            int count = 4 - layer;
            double layerX = CERAMIC_PILE_X + layer * (TILE_SIZE / 2.0);
            double layerY = CERAMIC_PILE_Y + (2 - layer) * 28;

            for (int col = 0; col < count; col++) {
                plates.add(new Rectangle2D(
                        layerX + col * TILE_SIZE,
                        layerY,
                        TILE_SIZE,
                        TILE_SIZE
                ));
            }
        }

        return plates;
    }

    static boolean isWallTile(int col, int row, int columns, int rows) {
        if (col < 0 || row < 0 || col >= columns || row >= rows) {
            return true;
        }
        if (row <= 2 || row >= rows - 2) {
            return true;
        }
        // A abertura oeste permite voltar à Fronteira Neutra.
        if (col == 0
                && (row < WEST_GATE_FIRST_ROW
                    || row > WEST_GATE_LAST_ROW)) {
            return true;
        }
        // Abertura superior direita para a Encruzilhada das Facções.
        if (col >= columns - 2
                && (row < EAST_GATE_FIRST_ROW
                    || row > EAST_GATE_LAST_ROW)) {
            return true;
        }
        for (int[] wall : INNER_WALLS) {
            if (col >= wall[0] && col <= wall[1]
                    && row >= wall[2] && row <= wall[3]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Piso metálico que substitui a cerâmica na parte diagonal da trilha.
     * Usa os mesmos limites do caminho, sem sobrepor uma ponte visual.
     */
    static boolean isMetalBridgeTile(int col, int row) {
        if (!isPathTile(col, row)) {
            return false;
        }

        double x = col + 0.5;
        double y = row + 0.5;
        double[] start = ROUTES[0][2]; // (19, 38)
        double[] end = ROUTES[0][4];   // (35, 31)
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double lengthSquared = dx * dx + dy * dy;
        double t = ((x - start[0]) * dx + (y - start[1]) * dy)
                / lengthSquared;

        return t >= 0.0 && t <= 1.0
                && distanceToSegment(x, y, start, end) <= ROUTE_RADII[0];
    }

    /**
     * Divide toda a parte metálica em nove faixas da entrada à saída.
     * Cada viagem instala a cerâmica em uma faixa consecutiva.
     * Retorna -1 para terrenos que não fazem parte da passarela.
     */
    static int getBridgeSectionForTile(int col, int row) {
        if (!isMetalBridgeTile(col, row)) {
            return -1;
        }

        double[] start = ROUTES[0][2];
        double[] end = ROUTES[0][4];
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];

        double t = ((col + 0.5 - start[0]) * dx
                + (row + 0.5 - start[1]) * dy)
                / (dx * dx + dy * dy);

        return Math.min(ChapterProgress.CERAMIC_BRIDGE_PLATES - 1,
                Math.max(0, (int) (t * ChapterProgress.CERAMIC_BRIDGE_PLATES)));
    }

    /** Blocos reais de uma etapa da passarela, sempre na ordem de instalação. */
    static List<Rectangle2D> getBridgeSectionTiles(int section) {
        if (section < 0 || section >= BRIDGE_SECTION_TILES.size()) {
            return List.of();
        }
        return BRIDGE_SECTION_TILES.get(section);
    }

    private static List<List<Rectangle2D>> buildBridgeSectionTiles() {
        List<List<Rectangle2D>> sections = new ArrayList<>();
        for (int i = 0; i < ChapterProgress.CERAMIC_BRIDGE_PLATES; i++) {
            sections.add(new ArrayList<>());
        }

        int columns = (int) (ZoneManager.WORLD_WIDTH / TILE_SIZE);
        int rows = (int) (ZoneManager.WORLD_HEIGHT / TILE_SIZE);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int section = getBridgeSectionForTile(col, row);
                if (section >= 0) {
                    sections.get(section).add(new Rectangle2D(
                            col * TILE_SIZE, row * TILE_SIZE,
                            TILE_SIZE, TILE_SIZE
                    ));
                }
            }
        }

        List<List<Rectangle2D>> immutable = new ArrayList<>();
        for (List<Rectangle2D> section : sections) {
            immutable.add(List.copyOf(section));
        }
        return List.copyOf(immutable);
    }

    static boolean isCeramicTile(int col, int row) {
        return isPathTile(col, row) && !isMetalBridgeTile(col, row);
    }

    /** Retorna todos os tiles transitáveis, independentemente do material. */
    private static boolean isPathTile(int col, int row) {
        // Evita considerar paredes como parte de um piso seguro.
        if (isWallTile(col, row,
                (int) (ZoneManager.WORLD_WIDTH / TILE_SIZE),
                (int) (ZoneManager.WORLD_HEIGHT / TILE_SIZE))) {
            return false;
        }

        /*
         * O corredor de entrada mantém exatamente os quatro tiles da
         * saída da Fronteira Neutra até a curva começar. Evita o
         * alargamento logo após atravessar de uma zona para a outra.
         */
        if (col <= 18 && row >= 35) {
            return row >= WEST_GATE_FIRST_ROW
                    && row <= WEST_GATE_LAST_ROW;
        }

        // O trecho aberto na borda leste deve ser todo de cerâmica.
        if (col >= (int) (ZoneManager.WORLD_WIDTH / TILE_SIZE) - 2
                && row >= EAST_GATE_FIRST_ROW
                && row <= EAST_GATE_LAST_ROW) {
            return true;
        }

        // Pequena base de cerâmica ligada à trilha sob o monólito.
        // O cálculo usa sua posição atual e acompanha futuros ajustes.
        if (isMonolithBaseTile(col, row)) {
            return true;
        }

        double x = col + 0.5;
        double y = row + 0.5;

        for (int route = 0; route < ROUTES.length; route++) {
            double[][] points = ROUTES[route];
            for (int segment = 1; segment < points.length; segment++) {
                if (distanceToSegment(x, y, points[segment - 1],
                        points[segment]) <= ROUTE_RADII[route]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Base de dois tiles de profundidade abaixo do monólito isolante,
     * com um pequeno avanço lateral. Não altera a colisão do monólito.
     */
    private static boolean isMonolithBaseTile(int col, int row) {
        int firstCol = (int) Math.floor(
                (MONOLITH_X - TILE_SIZE) / TILE_SIZE
        );
        int lastCol = (int) Math.floor(
                (MONOLITH_X + MONOLITH_WIDTH + TILE_SIZE / 2.0)
                        / TILE_SIZE
        );
        int firstRow = (int) Math.ceil(
                (MONOLITH_Y + MONOLITH_HEIGHT) / TILE_SIZE
        );

        return col >= firstCol && col <= lastCol
                && row >= firstRow && row <= firstRow + 1;
    }

    private static double distanceToSegment(
            double x, double y, double[] start, double[] end) {
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0.0) {
            return Math.hypot(x - start[0], y - start[1]);
        }
        double projection = ((x - start[0]) * dx + (y - start[1]) * dy)
                / lengthSquared;
        double t = Math.max(0.0, Math.min(1.0, projection));
        return Math.hypot(x - (start[0] + t * dx),
                y - (start[1] + t * dy));
    }
}
