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
 * Adiciona ao Pane os elementos exigidos pelo critério de aceite da
 * tarefa: um NPC placeholder, um símbolo de carga (+/-) e um texto
 * simples representando UI (ex.: HP), tudo na mesma escala do tile
 * definido em ExplorationController.
 *
 * Assim como o DebugGridRenderer, esta classe é descartável: deve
 * ser removida quando os NPCs, símbolos e HUD reais entrarem no jogo
 * (fora do escopo do C1-010).
 */
public final class CameraTestOverlay {

    /*
     * Cores da paleta funcional definida no Art Bible.
     * Usadas aqui só para já testar a leitura visual nessa escala,
     * não como implementação final da paleta.
     */
    private static final String COLOR_PROTON = "#B4473D";
    private static final String COLOR_NEUTRON = "#607D72";

    /*
     * Classe utilitária: não deve ser instanciada.
     */
    private CameraTestOverlay() {
    }

    /**
     * Adiciona os elementos de teste ao Pane informado.
     *
     * @param root      contêiner onde os elementos serão adicionados
     * @param tileSize  tamanho de tile usado como referência de escala
     */
    public static void render(Pane root, double tileSize) {

        addNpcPlaceholder(root, tileSize);
        addChargeSymbol(root, tileSize);
        addSimpleUi(root);
    }

    /*
     * NPC placeholder: um retângulo de cor diferente do herói,
     * parado em uma posição fixa do grid, só para comparar tamanho
     * e distinguir visualmente "personagem controlável" de "NPC".
     */
    private static void addNpcPlaceholder(Pane root, double tileSize) {

        Rectangle npcView = new Rectangle(tileSize, tileSize);
        npcView.setFill(Color.web(COLOR_NEUTRON));
        npcView.relocate(tileSize * 6, tileSize * 3);

        root.getChildren().add(npcView);
    }

    /*
     * Símbolo de carga: testa se um "+" fica legível no tamanho
     * de tile escolhido, sobre um fundo colorido da paleta oficial.
     * Regra do Art Bible: nunca depender só de cor, por isso o
     * símbolo textual é obrigatório junto do retângulo.
     */
    private static void addChargeSymbol(Pane root, double tileSize) {

        Rectangle chargeBackground = new Rectangle(tileSize, tileSize);
        chargeBackground.setFill(Color.web(COLOR_PROTON));
        chargeBackground.relocate(tileSize * 9, tileSize * 3);

        Text chargeSymbol = new Text("+");
        chargeSymbol.setFont(Font.font(
                "System",
                FontWeight.BOLD,
                tileSize * 0.6
        ));
        chargeSymbol.setFill(Color.WHITE);

        /*
         * Posiciona o símbolo centralizado sobre o retângulo,
         * usando o próprio tamanho do texto para calcular o ajuste.
         */
        chargeSymbol.relocate(
                tileSize * 9 + (tileSize - chargeSymbol.getLayoutBounds().getWidth()) / 2,
                tileSize * 3 + (tileSize - chargeSymbol.getLayoutBounds().getHeight()) / 2
        );

        root.getChildren().addAll(chargeBackground, chargeSymbol);
    }

    /*
     * UI simples: um texto de status no canto da tela, só para
     * validar que informação de HUD é legível nessa escala.
     * Não é o HUD final — isso é responsabilidade do M5 em uma
     * sprint posterior (C1-020 / C1-030).
     */
    private static void addSimpleUi(Pane root) {

        Text hpLabel = new Text("HP: 100");
        hpLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        hpLabel.setFill(Color.web("#17324D"));
        hpLabel.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
        hpLabel.relocate(10, 10);

        root.getChildren().add(hpLabel);
    }
}