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

    /*
     * Classe utilitária: construtor privado para impedir instanciação.
     */
    private TileMapRenderer() {
    }

    /**
     * Renderiza o mapa da Zona 1 (Fronteira Neutra).
     *
     * Constrói as montanhas de borda, o chão neutro de musgo/terra verde-acinzentado (#607D72)
     * e a trilha de terra batida que conduz o jogador do início até a saída para a Zona 2.
     *
     * @param root contêiner do mundo onde o mapa é inserido
     * @param resourceManager gerenciador central para cache de imagens
     * @param width largura total do mundo em pixels (3200px)
     * @param height altura total do mundo em pixels (1800px)
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

                // 1. Paredes e montanhas rochosas de limite do cenário
                boolean isNorthWall = row <= 2;
                boolean isSouthWall = row >= rows - 2;
                boolean isWestWall = col == 0;
                // Deixa uma passagem na borda leste exatamente onde fica a área de saída (y: 1450..1750)
                boolean isEastWall = (col >= columns - 2) && (row < 35 || row > 42);

                if (isNorthWall || isSouthWall || isWestWall || isEastWall) {
                    gc.drawImage(rockImg, x, y, tileSize, tileSize);
                    continue;
                }

                // 2. Trilha que guia o jogador desde o início até a saída
                if (isPathTile(col, row)) {
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
     * Define o traçado da trilha de terra batida da Fronteira Neutra.
     */
    private static boolean isPathTile(int col, int row) {
        // Início (spawn do herói e placa): col 2 a 18 na altura das linhas 40 a 42
        if (row >= 40 && row <= 42 && col >= 2 && col <= 18) {
            return true;
        }

        // Passa pelo Nilo (col 15..25) e começa a subir suavemente
        if (col > 18 && col <= 45) {
            int targetRow = 41 - (int) ((col - 18) * 0.12);
            if (row >= targetRow - 1 && row <= targetRow + 1) {
                return true;
            }
        }

        // Continua em direção à saída no extremo leste da fase (col 46 a 76 nas linhas 37 a 39)
        if (col > 45 && col <= 76 && row >= 36 && row <= 39) {
            return true;
        }

        return false;
    }
}
