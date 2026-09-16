package com.alemdoequilibrio.game;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Utilitário temporário usado apenas para o teste de câmera (C1-010).
 *
 * Os elementos temporários são divididos entre:
 *
 * - elementos pertencentes ao mundo, que acompanham a câmera;
 * - elementos pertencentes ao HUD, que permanecem fixos na tela.
 *
 * Esta classe deverá ser removida quando os NPCs, símbolos
 * e HUD reais forem implementados.
 */
public final class CameraTestOverlay {

    /*
     * Cores da paleta funcional definida no Art Bible.
     */
    private static final String COLOR_PROTON = "#B4473D";
    private static final String COLOR_NEUTRON = "#607D72";

    /*
     * Classe utilitária: não deve ser instanciada.
     */
    private CameraTestOverlay() {
    }

    /**
     * Adiciona ao mundo os elementos temporários
     * que devem acompanhar o movimento da câmera.
     *
     * @param worldLayer camada que representa o mundo
     * @param tileSize tamanho de referência dos tiles
     */
    public static void renderWorld(
            Pane worldLayer,
            double tileSize) {

        addNpcPlaceholder(
                worldLayer,
                tileSize
        );

        addChargeSymbol(
                worldLayer,
                tileSize
        );
    }

    /**
     * Adiciona os elementos temporários do HUD.
     *
     * Esses elementos permanecem fixos na tela e não
     * acompanham o movimento da câmera.
     *
     * @param hudLayer camada da interface do jogador
     */
    public static void renderHud(Pane hudLayer) {

        addSimpleUi(hudLayer);
    }

    /*
     * NPC placeholder usado apenas para comparação
     * de tamanho e visual durante o teste.
     */
    private static void addNpcPlaceholder(
            Pane worldLayer,
            double tileSize) {

        Rectangle npcView =
                new Rectangle(tileSize, tileSize);

        npcView.setFill(
                Color.web(COLOR_NEUTRON)
        );

        npcView.relocate(
                tileSize * 6,
                tileSize * 3
        );

        worldLayer.getChildren().add(npcView);
    }

    /*
     * Símbolo temporário de carga elétrica.
     */
    private static void addChargeSymbol(
            Pane worldLayer,
            double tileSize) {

        Rectangle chargeBackground =
                new Rectangle(tileSize, tileSize);

        chargeBackground.setFill(
                Color.web(COLOR_PROTON)
        );

        chargeBackground.relocate(
                tileSize * 9,
                tileSize * 3
        );

        Text chargeSymbol = new Text("+");

        chargeSymbol.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        tileSize * 0.6
                )
        );

        chargeSymbol.setFill(Color.WHITE);

        /*
         * Centraliza o símbolo sobre
         * o retângulo que representa a carga.
         */
        chargeSymbol.relocate(
                tileSize * 9
                + (tileSize
                - chargeSymbol.getLayoutBounds().getWidth()) / 2,

                tileSize * 3
                + (tileSize
                - chargeSymbol.getLayoutBounds().getHeight()) / 2
        );

        worldLayer.getChildren().addAll(
                chargeBackground,
                chargeSymbol
        );
    }

    /*
     * Interface temporária usada somente
     * para validar o comportamento do HUD.
     */
    private static void addSimpleUi(Pane hudLayer) {

        Text hpLabel =
                new Text("HP: 100");

        hpLabel.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        18
                )
        );

        hpLabel.setFill(
                Color.web("#17324D")
        );

        hpLabel.relocate(10, 10);

        hudLayer.getChildren().add(hpLabel);
    }
}