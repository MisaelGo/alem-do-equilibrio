package com.alemdoequilibrio.game;

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

    /*
     * Mesmo vão da saída leste da Fronteira Neutra: 4 tiles (160 px).
     * Fronteira: linhas 15..18; Trilha: 36..39.
     * Diferença de 21 tiles = 840 px entre os limites inferiores dos mapas.
     * Assim, as duas paredes ficam na mesma altura quando a câmera
     * mostra a extremidade inferior das respectivas regiões.
     */
    static final int WEST_GATE_FIRST_ROW = 36;
    static final int WEST_GATE_LAST_ROW = 39;

    static final double HOUSE_X = 175;
    static final double HOUSE_Y = 185;
    static final double HOUSE_WIDTH = 455;
    static final double HOUSE_HEIGHT = 250;

    static final double CERAMIC_PILE_X = 220;
    static final double CERAMIC_PILE_Y = 1110;
    static final double CERAMIC_PILE_WIDTH = 180;
    static final double CERAMIC_PILE_HEIGHT = 110;

    static final double MONOLITH_X = 2600;
    static final double MONOLITH_Y = 1530;
    static final double MONOLITH_WIDTH = 190;
    static final double MONOLITH_HEIGHT = 185;

    // Paredes interiores do desenho: coluna esquerda, divisão e bloqueio leste.
    // Intervalos [coluna inicial, final, linha inicial, final] inclusivos.
    private static final int[][] INNER_WALLS = {
        {25, 27, 3, 9},
        {0, 29, 16, 18},
        {61, 79, 22, 24}
    };

    /*
     * Trilhas sinuosas do croqui, representadas por eixos em tiles.
     * Cada eixo recebe uma largura própria, formando corredores e bifurcações.
     * O renderizador colore os tiles resultantes com a textura de cerâmica.
     */
    private static final double[][][] ROUTES = {
        { {3, 38}, {11, 38}, {19, 38}, {27, 34}, {35, 31},
          {43, 28}, {49, 27}, {57, 30}, {65, 33}, {72, 38}, {78, 38} },

        { {3, 31}, {13, 29}, {22, 26}, {31, 23}, {37, 21},
          {35, 18}, {29, 15}, {20, 14}, {12, 13}, {3, 13} },

        { {3, 10}, {11, 11}, {19, 10}, {24, 10}, {32, 11},
          {36, 14}, {41, 18}, {45, 18}, {53, 14}, {62, 8},
          {70, 3} },

        { {76, 11}, {70, 11}, {64, 15}, {59, 20}, {58, 25},
          {63, 27}, {74, 27}, {76, 30}, {76, 38} }
    };

    // Meia-largura de cada caminho, em tiles.
    private static final double[] ROUTE_RADII = {3.1, 2.7, 2.5, 2.5};

    private InsulatorTrailLayout() {
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
        // A abertura leste coincide com a saída para a Encruzilhada.
        if (col >= columns - 2 && (row < 35 || row > 42)) {
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

    static boolean isCeramicTile(int col, int row) {
        // Evita considerar paredes como parte de um piso seguro.
        if (isWallTile(col, row,
                (int) (ZoneManager.WORLD_WIDTH / TILE_SIZE),
                (int) (ZoneManager.WORLD_HEIGHT / TILE_SIZE))) {
            return false;
        }

        /*
         * Nos 12 tiles iniciais, o corredor inferior tem exatamente a
         * largura da abertura da Fronteira Neutra. Mais adiante,
         * ele volta a seguir o traçado sinuoso normal.
         * Limita apenas a faixa inferior, sem apagar os demais caminhos.
         */
        if (col <= 11 && row >= 35) {
            return row >= WEST_GATE_FIRST_ROW
                    && row <= WEST_GATE_LAST_ROW;
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
