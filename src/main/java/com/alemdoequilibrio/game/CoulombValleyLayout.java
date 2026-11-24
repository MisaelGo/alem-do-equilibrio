package com.alemdoequilibrio.game;

/**
 * Geometria e coordenadas compartilhadas do Vale de Coulomb.
 *
 * O vale é um desfiladeiro monumental esculpido por forças eletrostáticas,
 * conectando a Encruzilhada das Facções ao Planalto do Campo.
 * Contém o acampamento de medição do sábio Vetor, a histórica Balança de
 * Torção de Coulomb, obeliscos de polaridade (+ e -), as estações de
 * calibração de distância (d, 2d, 3d) e o Monólito Eletrostático de Checkpoint.
 */
final class CoulombValleyLayout {

    static final int TILE_SIZE = 40;

    static final int WEST_GATE_FIRST_ROW = 36;
    static final int WEST_GATE_LAST_ROW = 41;

    static final int EAST_GATE_FIRST_ROW = 36;
    static final int EAST_GATE_LAST_ROW = 41;

    static final double VETOR_X = 520;
    static final double VETOR_Y = 1520;

    static final double TORSION_BALANCE_X = 850;
    static final double TORSION_BALANCE_Y = 1460;
    static final double TORSION_BALANCE_WIDTH = 80;
    static final double TORSION_BALANCE_HEIGHT = 80;
    static final String TORSION_BALANCE_INTERACTION = "COULOMB_TORSION_BALANCE";

    static final double PYLON_POS_X = 1100;
    static final double PYLON_POS_Y = 1160;
    static final double PYLON_POS_WIDTH = 80;
    static final double PYLON_POS_HEIGHT = 120;
    static final String PYLON_POS_INTERACTION = "COULOMB_PYLON_POS";

    static final double PYLON_NEG_X = 1400;
    static final double PYLON_NEG_Y = 1160;
    static final double PYLON_NEG_WIDTH = 80;
    static final double PYLON_NEG_HEIGHT = 120;
    static final String PYLON_NEG_INTERACTION = "COULOMB_PYLON_NEG";

    static final double MARKER_D_X = 1250;
    static final double MARKER_D_Y = 1440;
    static final String MARKER_D_INTERACTION = "COULOMB_MARKER_D";

    static final double MARKER_2D_X = 1800;
    static final double MARKER_2D_Y = 1440;
    static final String MARKER_2D_INTERACTION = "COULOMB_MARKER_2D";

    static final double MARKER_3D_X = 2400;
    static final double MARKER_3D_Y = 1440;
    static final String MARKER_3D_INTERACTION = "COULOMB_MARKER_3D";

    static final double MARKER_WIDTH = 70;
    static final double MARKER_HEIGHT = 80;

    static final double MONOLITH_X = 2700;
    static final double MONOLITH_Y = 1260;
    static final double MONOLITH_WIDTH = 128;
    static final double MONOLITH_HEIGHT = 185;
    static final String MONOLITH_INTERACTION = "COULOMB_MONOLITH_INTERACTION";
    static final String MONOLITH_REPEAT_INTERACTION = "COULOMB_MONOLITH_REPEAT";

    static final double MONOLITH_CHECKPOINT_X =
            MONOLITH_X + (MONOLITH_WIDTH - TILE_SIZE) / 2.0;
    static final double MONOLITH_CHECKPOINT_Y =
            MONOLITH_Y + MONOLITH_HEIGHT + 10;

    // Obstáculos rochosos do desfiladeiro [colInit, colEnd, rowInit, rowEnd]
    private static final int[][] CANYON_CLIFFS = {
        {12, 18, 14, 28},
        {32, 42, 8, 22},
        {50, 58, 12, 26},
        {68, 76, 6, 20}
    };

    private CoulombValleyLayout() {
    }

    static boolean isPathTile(int col, int row) {
        // Entrada oeste (conexão com Encruzilhada)
        if (col >= 0 && col <= 24 && row >= 36 && row <= 41) {
            return true;
        }
        // Curva e desfiladeiro central passando pelas estações de distância d, 2d, 3d
        if (col >= 22 && col <= 66 && row >= 32 && row <= 38) {
            return true;
        }
        // Acesso norte para os obeliscos gêmeos (+ e -)
        if (col >= 26 && col <= 38 && row >= 28 && row <= 33) {
            return true;
        }
        // Saída leste conduzindo ao monólito e ao Planalto do Campo
        if (col >= 64 && col <= 79 && row >= 34 && row <= 41) {
            return true;
        }
        return false;
    }

    static boolean isWallTile(int col, int row, int columns, int rows) {
        // Paredes norte e sul
        if (row <= 2 || row >= rows - 2) {
            return true;
        }
        // Parede oeste (deixa aberta a passagem de retorno nas linhas 36..41)
        if (col <= 1 && (row < WEST_GATE_FIRST_ROW || row > WEST_GATE_LAST_ROW)) {
            return true;
        }
        // Parede leste (deixa aberta a saída nas linhas 36..41)
        if (col >= columns - 2 && (row < EAST_GATE_FIRST_ROW || row > EAST_GATE_LAST_ROW)) {
            return true;
        }
        // Penhascos internos do desfiladeiro
        for (int[] cliff : CANYON_CLIFFS) {
            if (col >= cliff[0] && col <= cliff[1] && row >= cliff[2] && row <= cliff[3]) {
                return true;
            }
        }
        return false;
    }
}
