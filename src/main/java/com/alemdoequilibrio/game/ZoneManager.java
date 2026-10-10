package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class ZoneManager {

    public static final double WORLD_WIDTH =
            3200;

    public static final double WORLD_HEIGHT =
            1800;

    private static final double TILE_SIZE =
            40;

    private final Pane worldLayer;

    private final ResourceManager
            resourceManager;

    private final List<Interactable>
            interactables;

    private ExplorationZone currentZone;

    private Rectangle exitArea;

    private Rectangle checkpointArea;

    public ZoneManager(
            Pane worldLayer,
            ResourceManager resourceManager) {

        this.worldLayer =
                Objects.requireNonNull(
                        worldLayer
                );

        this.resourceManager =
                Objects.requireNonNull(
                        resourceManager
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
        checkpointArea = null;

        worldLayer
                .getChildren()
                .clear();

        worldLayer.setPrefSize(
                WORLD_WIDTH,
                WORLD_HEIGHT
        );

        if (zone
                == ExplorationZone
                        .NEUTRAL_BORDER) {

            TileMapRenderer
                    .renderNeutralBorder(
                            worldLayer,
                            resourceManager,
                            WORLD_WIDTH,
                            WORLD_HEIGHT,
                            TILE_SIZE
                    );

        } else {

            DebugGridRenderer.render(
                    worldLayer,
                    WORLD_WIDTH,
                    WORLD_HEIGHT,
                    TILE_SIZE
            );
        }

        switch (zone) {

            case NEUTRAL_BORDER ->
                buildNeutralBorder();

            case INSULATOR_TRAIL ->
                buildInsulatorTrail();

            case FACTION_CROSSROADS ->
                buildFactionCrossroads();

            case COULOMB_VALLEY ->
                buildCoulombValley();

            case FIELD_PLATEAU ->
                buildFieldPlateau();

            case EQUIPOTENTIAL_RUINS ->
                buildEquipotentialRuins();

            case FRAGMENT_CHAMBER ->
                buildFragmentChamber();
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
                        "NILO_CHARGE_SIGNS"
                );

        addNpc(
                nilo,
                Color.GOLD
        );

        addDecoration(
                "Entrada da Fronteira",
                350,
                1650
        );

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
                        "ELIA_ELECTRIZATION"
                );

        addNpc(
                elia,
                Color.LIGHTBLUE
        );

        addDecoration(
                "Estrutura condutora / isolante",
                1100,
                1500
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

        createExitArea(
                "Vale de Coulomb",
                3000,
                1450,
                140,
                300
        );
    }

    /*
     * C1-044 - Vale de Coulomb.
     */
    private void buildCoulombValley() {

        addZoneTitle(
                "VALE DE COULOMB"
        );

        NPC vector =
                new NPC(
                        "Vetor",
                        600,
                        1650,
                        40,
                        50,
                        KnowledgeTopic
                                .KNOW_COULOMB,
                        "VECTOR_COULOMB"
                );

        addNpc(
                vector,
                Color.LIGHTGREEN
        );

        /*
         * Marcos visuais de distância.
         */
        addDistanceMarker(
                "d",
                1200,
                1300
        );

        addDistanceMarker(
                "2d",
                1700,
                1300
        );

        addDistanceMarker(
                "3d",
                2400,
                1300
        );

        Label hint =
                new Label(
                        "Compare as interações "
                        + "nos diferentes marcos "
                        + "de distância."
                );

        hint.relocate(
                1050,
                1100
        );

        worldLayer
                .getChildren()
                .add(hint);

        createExitArea(
                "Planalto do Campo",
                3000,
                1450,
                140,
                300
        );
    }

    /*
     * C1-046 - Planalto do Campo.
     */
    private void buildFieldPlateau() {

        addZoneTitle(
                "PLANALTO DO CAMPO"
        );

        NPC vector =
                new NPC(
                        "Vetor",
                        600,
                        1650,
                        40,
                        50,
                        KnowledgeTopic
                                .KNOW_FIELD,
                        "VECTOR_FIELD"
                );

        addNpc(
                vector,
                Color.LIGHTGREEN
        );

        /*
         * A direção é indicada pela própria
         * geometria das setas e pelo símbolo E,
         * não apenas por cor.
         */
        addFieldArrow(
                1200,
                1400,
                180,
                -100
        );

        addFieldArrow(
                1550,
                1200,
                220,
                0
        );

        addFieldArrow(
                1950,
                1000,
                160,
                130
        );

        addFieldArrow(
                2350,
                1350,
                -180,
                80
        );

        createExitArea(
                "Ruínas Equipotenciais",
                3000,
                1450,
                140,
                300
        );
    }

    /*
     * C1-049 - Ruínas Equipotenciais.
     */
    private void buildEquipotentialRuins() {

        addZoneTitle(
                "RUÍNAS EQUIPOTENCIAIS"
        );

        NPC sera =
                new NPC(
                        "Sera",
                        650,
                        1650,
                        40,
                        50,
                        KnowledgeTopic
                                .KNOW_POTENTIAL,
                        "SERA_POTENTIAL"
                );

        addNpc(
                sera,
                Color.PLUM
        );

        addEquipotentialRing(
                1700,
                1050,
                140
        );

        addEquipotentialRing(
                1700,
                1050,
                260
        );

        addEquipotentialRing(
                1700,
                1050,
                380
        );

        Label ringLabel =
                new Label(
                        "Linhas equipotenciais"
                );

        ringLabel.relocate(
                1550,
                600
        );

        worldLayer
                .getChildren()
                .add(ringLabel);

        createCheckpointArea(
                2450,
                1300,
                220,
                220
        );

        createExitArea(
                "Câmara do Fragmento",
                3000,
                1450,
                140,
                300
        );
    }

    private void buildFragmentChamber() {

        addZoneTitle(
                "CÂMARA DO FRAGMENTO"
        );

        Label label =
                new Label(
                        "Entrada do boss — integração "
                        + "final prevista para a S4."
                );

        label.relocate(
                500,
                1500
        );

        worldLayer
                .getChildren()
                .add(label);
    }

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

    private void addNpc(
            NPC npc,
            Color color) {

        interactables.add(
                npc
        );

        Node visualNode;
        String spritePath = getNpcSpritePath(npc.getName());
        try {
            Image sprite = resourceManager.getImage(spritePath);
            ImageView imageView = new ImageView(sprite);
            imageView.setFitWidth(npc.getWidth());
            imageView.setFitHeight(npc.getHeight());
            imageView.setPreserveRatio(true);
            imageView.relocate(
                    npc.getX(),
                    npc.getY()
            );
            visualNode = imageView;
        } catch (Exception e) {
            Rectangle body =
                    new Rectangle(
                            npc.getWidth(),
                            npc.getHeight()
                    );
            body.setFill(color);
            body.relocate(
                    npc.getX(),
                    npc.getY()
            );
            visualNode = body;
        }

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
                        visualNode,
                        name
                );
    }

    private String getNpcSpritePath(String name) {
        if (name == null) {
            return "/images/npc_scholar.png";
        }
        return switch (name.trim().toLowerCase()) {
            case "nilo" -> "/images/npc_nilo.png";
            case "élia", "elia" -> "/images/npc_elia.png";
            case "mara" -> "/images/npc_mara.png";
            case "vetor" -> "/images/npc_vetor.png";
            case "sera" -> "/images/npc_sera.png";
            case "íon", "ion" -> "/images/npc_merchant_ion.png";
            default -> "/images/npc_scholar.png";
        };
    }

    private void addDecoration(
            String text,
            double x,
            double y) {

        Rectangle body =
                new Rectangle(
                        70,
                        70
                );

        body.setFill(
                Color.LIGHTGRAY
        );

        body.setStroke(
                Color.DARKGRAY
        );

        body.relocate(
                x,
                y
        );

        Label label =
                new Label(text);

        label.relocate(
                x - 20,
                y - 25
        );

        worldLayer
                .getChildren()
                .addAll(
                        body,
                        label
                );
    }

    private void addDistanceMarker(
            String labelText,
            double x,
            double y) {

        Line marker =
                new Line(
                        x,
                        y - 120,
                        x,
                        y + 120
                );

        marker.setStrokeWidth(
                5
        );

        marker.setStroke(
                Color.DARKORANGE
        );

        Label label =
                new Label(
                        labelText
                );

        label.setStyle(
                "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
        );

        label.relocate(
                x - 10,
                y - 160
        );

        worldLayer
                .getChildren()
                .addAll(
                        marker,
                        label
                );
    }

    private void addFieldArrow(
            double x,
            double y,
            double dx,
            double dy) {

        double endX =
                x + dx;

        double endY =
                y + dy;

        Line line =
                new Line(
                        x,
                        y,
                        endX,
                        endY
                );

        line.setStroke(
                Color.DARKBLUE
        );

        line.setStrokeWidth(
                6
        );

        double angle =
                Math.atan2(
                        dy,
                        dx
                );

        double length =
                20;

        double spread =
                Math.toRadians(
                        28
                );

        double x1 =
                endX
                - length
                * Math.cos(
                        angle - spread
                );

        double y1 =
                endY
                - length
                * Math.sin(
                        angle - spread
                );

        double x2 =
                endX
                - length
                * Math.cos(
                        angle + spread
                );

        double y2 =
                endY
                - length
                * Math.sin(
                        angle + spread
                );

        Polygon arrowHead =
                new Polygon(
                        endX,
                        endY,
                        x1,
                        y1,
                        x2,
                        y2
                );

        arrowHead.setFill(
                Color.DARKBLUE
        );

        Label label =
                new Label("E");

        label.setStyle(
                "-fx-font-size: 18px;"
                + "-fx-font-weight: bold;"
        );

        label.relocate(
                x + dx / 2.0,
                y + dy / 2.0
        );

        worldLayer
                .getChildren()
                .addAll(
                        line,
                        arrowHead,
                        label
                );
    }

    private void addEquipotentialRing(
            double centerX,
            double centerY,
            double radius) {

        Circle ring =
                new Circle(
                        centerX,
                        centerY,
                        radius
                );

        ring.setFill(
                Color.TRANSPARENT
        );

        ring.setStroke(
                Color.DARKVIOLET
        );

        ring.setStrokeWidth(
                5
        );

        worldLayer
                .getChildren()
                .add(ring);
    }

    private void createCheckpointArea(
            double x,
            double y,
            double width,
            double height) {

        checkpointArea =
                new Rectangle(
                        x,
                        y,
                        width,
                        height
                );

        checkpointArea.setFill(
                Color.rgb(
                        70,
                        130,
                        220,
                        0.25
                )
        );

        checkpointArea.setStroke(
                Color.DODGERBLUE
        );

        Label label =
                new Label(
                        "CHECKPOINT"
                );

        label.relocate(
                x + 45,
                y - 30
        );

        worldLayer
                .getChildren()
                .addAll(
                        checkpointArea,
                        label
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

        return intersects(
                exitArea,
                x,
                y,
                width,
                height
        );
    }

    public boolean intersectsCheckpoint(
            double x,
            double y,
            double width,
            double height) {

        return intersects(
                checkpointArea,
                x,
                y,
                width,
                height
        );
    }

    private boolean intersects(
            Rectangle area,
            double x,
            double y,
            double width,
            double height) {

        if (area == null) {
            return false;
        }

        double right =
                x + width;

        double bottom =
                y + height;

        double areaRight =
                area.getX()
                + area.getWidth();

        double areaBottom =
                area.getY()
                + area.getHeight();

        return right >= area.getX()
                && x <= areaRight
                && bottom >= area.getY()
                && y <= areaBottom;
    }

    public ExplorationZone getNextZone() {

        return currentZone.next();
    }

    public ExplorationZone getCurrentZone() {

        return currentZone;
    }

    public List<Interactable>
            getInteractables() {

        return List.copyOf(
                interactables
        );
    }
}