package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Monta e controla o conteúdo das regiões de exploração.
 *
 * Cada região possui um mundo maior que a janela do jogo.
 */
public class ZoneManager {

    public static final double WORLD_WIDTH = 3200;
    public static final double WORLD_HEIGHT = 1800;

    private static final double TILE_SIZE = 40;

    private final Pane worldLayer;

    private final List<Interactable> interactables;

    private ExplorationZone currentZone;

    private Rectangle exitArea;

    public ZoneManager(Pane worldLayer) {

        this.worldLayer =
                Objects.requireNonNull(
                        worldLayer
                );

        this.interactables =
                new ArrayList<>();
    }

    public void loadZone(
            ExplorationZone zone) {

        if (zone == null) {

            throw new IllegalArgumentException(
                    "A região não pode ser nula."
            );
        }

        currentZone = zone;

        interactables.clear();

        exitArea = null;

        worldLayer
                .getChildren()
                .clear();

        worldLayer.setPrefSize(
                WORLD_WIDTH,
                WORLD_HEIGHT
        );

        /*
         * Placeholder visual enquanto os mapas
         * definitivos ainda não foram produzidos.
         */
        DebugGridRenderer.render(
                worldLayer,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                TILE_SIZE
        );

        switch (zone) {

            case NEUTRAL_BORDER ->
                buildNeutralBorder();

            case INSULATOR_TRAIL ->
                buildInsulatorTrail();

            case FACTION_CROSSROADS ->
                buildFactionCrossroads();
        }
    }

    private void buildNeutralBorder() {

        addZoneTitle(
                "FRONTEIRA NEUTRA"
        );

        NPC nilo =
                new NPC(
                        "Nilo",

                        600,
                        1650,

                        40,
                        50,

                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS,

                        "Aqui todos estão olhando "
                        + "para o símbolo no peito "
                        + "do outro e chamando isso "
                        + "de culpa.",

                        "Quando duas cargas possuem "
                        + "o mesmo sinal, elas tendem "
                        + "a se repelir.",

                        "Quando possuem sinais "
                        + "opostos, elas tendem "
                        + "a se atrair.",

                        "Positivo e negativo são "
                        + "sinais de uma propriedade "
                        + "física, não julgamentos."
                );

        addNpc(
                nilo,
                Color.GOLD
        );

        WorldObject sign =
                new WorldObject(
                        "Placa",

                        350,
                        1650,

                        50,
                        50,

                        "Fronteira Neutra. "
                        + "Pressione E próximo "
                        + "a elementos interativos."
                );

        addWorldObject(sign);

        createExitArea(
                "Trilha dos Isolantes",
                3000,
                1450,
                140,
                300
        );
    }

    private void buildInsulatorTrail() {

        addZoneTitle(
                "TRILHA DOS ISOLANTES"
        );

        NPC elia =
                new NPC(
                        "Élia",

                        650,
                        1650,

                        40,
                        50,

                        KnowledgeTopic
                                .KNOW_ELECTRIZATION,

                        "Nestes condutores, "
                        + "as cargas conseguem "
                        + "se rearranjar com "
                        + "mais facilidade.",

                        "Em materiais comuns, "
                        + "esse rearranjo costuma "
                        + "envolver elétrons.",

                        "Nos isolantes, as cargas "
                        + "ficam muito menos livres.",

                        "Uma carga próxima também "
                        + "pode induzir uma separação "
                        + "de cargas sem precisar "
                        + "encostar no condutor."
                );

        addNpc(
                elia,
                Color.LIGHTBLUE
        );

        WorldObject structure =
                new WorldObject(
                        "Estrutura",

                        1100,
                        1500,

                        70,
                        70,

                        "Uma parte da estrutura "
                        + "conduz cargas facilmente, "
                        + "enquanto outra atua "
                        + "como isolante."
                );

        addWorldObject(
                structure
        );

        createExitArea(
                "Encruzilhada das Facções",
                3000,
                1450,
                140,
                300
        );
    }

    private void buildFactionCrossroads() {

        addZoneTitle(
                "ENCRUZILHADA DAS FACÇÕES"
        );

        NPC mara =
                new NPC(
                        "Mara",

                        600,
                        1650,

                        40,
                        50,

                        "Os Prótons acreditam que "
                        + "os Elétrons sabem mais "
                        + "sobre estas anomalias "
                        + "do que dizem.",

                        "As descargas começaram "
                        + "a atingir nossa região "
                        + "e precisamos descobrir "
                        + "o que está causando isso."
                );

        NPC elia =
                new NPC(
                        "Élia",

                        950,
                        1650,

                        40,
                        50,

                        "Os Elétrons também "
                        + "registraram as anomalias "
                        + "no mesmo período.",

                        "Se os dois lados observaram "
                        + "o mesmo padrão, precisamos "
                        + "investigar antes de culpar "
                        + "uma das facções."
                );

        addNpc(
                mara,
                Color.LIGHTCORAL
        );

        addNpc(
                elia,
                Color.LIGHTBLUE
        );

        /*
         * A saída para o Vale de Coulomb será
         * adicionada quando a Zona 4 for criada.
         */
    }

    private void addZoneTitle(
            String text) {

        Label title =
                new Label(text);

        title.setStyle(
                "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
        );

        /*
         * Colocado próximo à região onde
         * o jogador começa.
         */
        title.relocate(
                80,
                1200
        );

        worldLayer
                .getChildren()
                .add(title);
    }

    private void addNpc(
            NPC npc,
            Color color) {

        interactables.add(npc);

        Rectangle npcView =
                new Rectangle(
                        npc.getWidth(),
                        npc.getHeight()
                );

        npcView.setFill(color);

        npcView.relocate(
                npc.getX(),
                npc.getY()
        );

        Label name =
                new Label(
                        npc.getName()
                );

        name.relocate(
                npc.getX(),
                npc.getY() - 25
        );

        worldLayer
                .getChildren()
                .addAll(
                        npcView,
                        name
                );
    }

    private void addWorldObject(
            WorldObject object) {

        interactables.add(object);

        Rectangle view =
                new Rectangle(
                        object.getWidth(),
                        object.getHeight()
                );

        view.setFill(
                Color.LIGHTGRAY
        );

        view.setStroke(
                Color.BLACK
        );

        view.relocate(
                object.getX(),
                object.getY()
        );

        Label name =
                new Label(
                        object.getInteractionName()
                );

        name.relocate(
                object.getX(),
                object.getY() - 25
        );

        worldLayer
                .getChildren()
                .addAll(
                        view,
                        name
                );
    }

    private void createExitArea(
            String destination,
            double x,
            double y,
            double width,
            double height) {

        exitArea =
                new Rectangle(
                        x,
                        y,
                        width,
                        height
                );

        /*
         * Placeholder visual.
         * No mapa final isso poderá ser uma estrada,
         * portal, ponte, porta etc.
         */
        exitArea.setFill(
                Color.rgb(
                        80,
                        200,
                        120,
                        0.35
                )
        );

        Label exitLabel =
                new Label(
                        "→ " + destination
                );

        exitLabel.relocate(
                x - 40,
                y - 35
        );

        worldLayer
                .getChildren()
                .addAll(
                        exitArea,
                        exitLabel
                );
    }

    public boolean intersectsExit(
            double x,
            double y,
            double width,
            double height) {

        if (exitArea == null) {
            return false;
        }

        double right =
                x + width;

        double bottom =
                y + height;

        double exitRight =
                exitArea.getX()
                + exitArea.getWidth();

        double exitBottom =
                exitArea.getY()
                + exitArea.getHeight();

        return right >= exitArea.getX()
                && x <= exitRight
                && bottom >= exitArea.getY()
                && y <= exitBottom;
    }

    public ExplorationZone getNextZone() {

        return currentZone.next();
    }

    public ExplorationZone getCurrentZone() {

        return currentZone;
    }

    public List<Interactable> getInteractables() {

        return List.copyOf(
                interactables
        );
    }
}