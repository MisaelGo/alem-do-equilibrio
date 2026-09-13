package com.alemdoequilibrio.game;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Utilitário temporário usado apenas para o teste de câmera (C1-010).
 *
 * Desenha uma grade de retângulos sobre um Pane, permitindo validar
 * o tamanho de tile escolhido e a legibilidade dos elementos antes
 * de o tileset final entrar no jogo.
 *
 * Esta classe deve ser removida (ou seu uso comentado) assim que o
 * TileMapRenderer definitivo, baseado em imagens, estiver pronto.
 * Por isso fica isolada: não deve se misturar com a lógica real de
 * exploração em ExplorationController.
 */
public final class DebugGridRenderer {

    /*
     * Classe utilitária: não deve ser instanciada.
     */
    private DebugGridRenderer() {
    }

    /**
     * Desenha a grade de debug dentro do Pane informado.
     *
     * @param root      contêiner onde os tiles de debug serão adicionados
     * @param width     largura total da área a ser coberta pela grade
     * @param height    altura total da área a ser coberta pela grade
     * @param tileSize  tamanho (em pixels) de cada tile da grade
     */
    public static void render(Pane root, double width, double height, double tileSize) {

        int columns = (int) (width / tileSize);
        int rows = (int) (height / tileSize);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {

                Rectangle tile = new Rectangle(tileSize, tileSize);
                tile.relocate(col * tileSize, row * tileSize);

                boolean isEven = (row + col) % 2 == 0;
                tile.setFill(isEven
                        ? Color.web("#E8E8E8")
                        : Color.web("#D6D6D6"));

                root.getChildren().add(tile);
            }
        }
    }
}