package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Monta e controla o conteúdo das regiões de exploração.
 *
 * Cada região possui um mundo maior que a janela do jogo.
 *
 * Os textos dos diálogos não ficam nesta classe.
 * Aqui são armazenados apenas os IDs dos diálogos,
 * que posteriormente são carregados pelo DialogueRepository.
 */
public class ZoneManager {

    public static final double WORLD_WIDTH = 3200;
    public static final double WORLD_HEIGHT = 1800;

    private static final double TILE_SIZE = 40;

    private final Pane worldLayer;

    private final List<Interactable> interactables;

    private ExplorationZone currentZone;

    private Rectangle exitArea;

    private final ResourceManager resourceManager;

    public ZoneManager(Pane worldLayer) {

        this(
                worldLayer,
                new ResourceManager()
        );
    }

    public ZoneManager(
            Pane worldLayer,
            ResourceManager resourceManager) {

        this.worldLayer =
                Objects.requireNonNull(
                        worldLayer
                );

        this.resourceManager =
                resourceManager != null
                        ? resourceManager
                        : new ResourceManager();

        this.interactables =
                new ArrayList<>();
    }

    /**
     * Carrega uma região e remove o conteúdo
     * visual da região anterior.
     */
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

        switch (zone) {

            case NEUTRAL_BORDER -> {
                TileMapRenderer.renderNeutralBorder(
                        worldLayer,
                        resourceManager,
                        WORLD_WIDTH,
                        WORLD_HEIGHT,
                        TILE_SIZE
                );
                buildNeutralBorder();
            }

            case INSULATOR_TRAIL -> {
                DebugGridRenderer.render(
                        worldLayer,
                        WORLD_WIDTH,
                        WORLD_HEIGHT,
                        TILE_SIZE
                );
                buildInsulatorTrail();
            }

            case FACTION_CROSSROADS -> {
                DebugGridRenderer.render(
                        worldLayer,
                        WORLD_WIDTH,
                        WORLD_HEIGHT,
                        TILE_SIZE
                );
                buildFactionCrossroads();
            }
        }
    }

    /**
     * Zona 1 - Fronteira Neutra.
     */
    private void buildNeutralBorder() {

        addZoneTitle(
                "FRONTEIRA NEUTRA"
        );

        /*
         * O conteúdo da conversa está no arquivo
         * dialogues.csv com o ID NILO_CHARGE_SIGNS.
         */
        NPC nilo =
                new NPC(
                        "Nilo",

                        600,
                        1650,

                        40,
                        50,

                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS,

                        "NILO_CHARGE_SIGNS"
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

                        "FRONTIER_SIGN"
                );

        addWorldObject(
                sign
        );

        createExitArea(
                "Trilha dos Isolantes",

                3000,
                1450,

                140,
                300
        );
    }

    /**
     * Zona 2 - Trilha dos Isolantes.
     */
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

                        "ELIA_ELECTRIZATION"
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

                        "INSULATOR_STRUCTURE"
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

    /**
     * Zona 3 - Encruzilhada das Facções.
     */
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

                        "MARA_CROSSROADS"
                );

        NPC elia =
                new NPC(
                        "Élia",

                        950,
                        1650,

                        40,
                        50,

                        "ELIA_CROSSROADS"
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
         * A saída para o Vale de Coulomb
         * será adicionada quando a Zona 4
         * for implementada.
         */
    }

    /**
     * Adiciona o nome provisório da região
     * dentro do mundo.
     */
    private void addZoneTitle(
            String text) {

        Label title =
                new Label(text);

        title.setStyle(
                "-fx-font-size: 28px;"
                + "-fx-font-weight: bold;"
        );

        title.relocate(
                80,
                1200
        );

        worldLayer
                .getChildren()
                .add(title);
    }

    /**
     * Adiciona um NPC ao mundo e ao sistema
     * genérico de interação.
     */
    private void addNpc(
            NPC npc,
            Color color) {

        interactables.add(
                npc
        );

        Rectangle npcView =
                new Rectangle(
                        npc.getWidth(),
                        npc.getHeight()
                );

        npcView.setFill(
                color
        );

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

    /**
     * Adiciona um objeto interativo ao mundo.
     */
    private void addWorldObject(
            WorldObject object) {

        interactables.add(
                object
        );

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

    /**
     * Cria a região que leva para o próximo mapa.
     */
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
         *
         * Futuramente poderá representar
         * uma estrada, ponte, porta,
         * portal, caverna etc.
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

    /**
     * Verifica se o personagem entrou
     * na área de saída da região.
     */
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

    /**
     * Retorna a próxima região.
     */
    public ExplorationZone getNextZone() {

        return currentZone.next();
    }

    /**
     * Retorna a região atualmente carregada.
     */
    public ExplorationZone getCurrentZone() {

        return currentZone;
    }

    /**
     * Retorna os elementos interativos
     * da região atual.
     */
    public List<Interactable> getInteractables() {

        return List.copyOf(
                interactables
        );
    }
}