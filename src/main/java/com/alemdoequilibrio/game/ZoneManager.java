package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.scene.Group;
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

    public static final double NEUTRAL_BORDER_WIDTH =
            1600;

    public static final double NEUTRAL_BORDER_HEIGHT =
            960;

    private static final double TILE_SIZE =
            40;

    static final double NEUTRAL_HOUSE_X =
            450;

    static final double NEUTRAL_HOUSE_Y =
            420;

    static final double NEUTRAL_HOUSE_WIDTH =
            280;

    static final double NEUTRAL_HOUSE_HEIGHT =
            280;

    private static final String NEUTRAL_HOUSE_IMAGE =
            "/images/house.png";

    static final double NEUTRAL_TREE_X =
            1250;

    static final double NEUTRAL_TREE_Y =
            150;

    static final double NEUTRAL_TREE_WIDTH =
            240;

    static final double NEUTRAL_TREE_HEIGHT =
            240;

    private static final String NEUTRAL_TREE_IMAGE =
            "/images/tree.png";

    private static final String NEUTRAL_BUSH_IMAGE =
            "/images/arbusto.png";

    static final double NEUTRAL_BUSH_WIDTH =
            80;

    static final double NEUTRAL_BUSH_HEIGHT =
            120;

    static final double NEUTRAL_BUSH_1_X =
            210;

    static final double NEUTRAL_BUSH_1_Y =
            170;

    static final double NEUTRAL_BUSH_2_X =
            900;

    static final double NEUTRAL_BUSH_2_Y =
            350;

    private final Pane worldLayer;

    private final ResourceManager
            resourceManager;

    private final QuestionBank questionBank;

    private final List<Interactable>
            interactables;

    private ExplorationZone currentZone;

    private double currentWorldWidth;

    private double currentWorldHeight;

    private Rectangle exitArea;

    private Rectangle returnArea;

    private Rectangle checkpointArea;

    public ZoneManager(
            Pane worldLayer,
            ResourceManager resourceManager,
            QuestionBank questionBank) {

        this.worldLayer =
                Objects.requireNonNull(
                        worldLayer
                );

        this.resourceManager =
                Objects.requireNonNull(
                        resourceManager
                );

        this.questionBank = Objects.requireNonNull(questionBank);

        this.interactables =
                new ArrayList<>();

        currentWorldWidth =
                WORLD_WIDTH;

        currentWorldHeight =
                WORLD_HEIGHT;
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
        returnArea = null;
        checkpointArea = null;

        worldLayer
                .getChildren()
                .clear();

        currentWorldWidth =
                zone == ExplorationZone.NEUTRAL_BORDER
                        ? NEUTRAL_BORDER_WIDTH
                        : WORLD_WIDTH;

        currentWorldHeight =
                zone == ExplorationZone.NEUTRAL_BORDER
                        ? NEUTRAL_BORDER_HEIGHT
                        : WORLD_HEIGHT;

        worldLayer.setPrefSize(
                currentWorldWidth,
                currentWorldHeight
        );

        TileMapRenderer.renderZone(
                worldLayer,
                zone,
                resourceManager,
                currentWorldWidth,
                currentWorldHeight,
                TILE_SIZE
        );

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

        addNeutralHouse();

        addNeutralTree();

        addNeutralBush(
                NEUTRAL_BUSH_1_X,
                NEUTRAL_BUSH_1_Y
        );

        addNeutralBush(
                NEUTRAL_BUSH_2_X,
                NEUTRAL_BUSH_2_Y
        );

        NPC nilo =
                new NPC(
                        "Nilo",
                        400,
                        824,
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

        NPC ion =
                new NPC(
                        "Íon",
                        1024,
                        720,
                        40,
                        50,
                        "ION_FIRST_MEETING"
                );

        addNpc(
                ion,
                Color.GOLDENROD
        );

        /*addDecoration(
                "Entrada da Fronteira",
                350,
                1650
        );*/

        /*
         * Gatilho invisível de transição.
         * Cobre toda a abertura de quatro tiles na borda leste.
         */
        createExitArea(
                "Trilha dos Isolantes",
                1595,
                600,
                5,
                160
        );
    }

    private void buildInsulatorTrail() {
        addZoneTitle("TRILHA DOS ISOLANTES");

        // Elementos da etapa visual: paredes e cerâmica estão no tilemap.
        addInsulatorHouse();
        addCeramicPile();
        addMetalBridge();
        addInsulatorMonolith();

        NPC elia = new NPC(
                "Élia",
                500,
                1580,
                40,
                50,
                KnowledgeTopic.KNOW_ELECTRIZATION,
                "ELIA_ELECTRIZATION"
        );
        addNpc(elia, Color.LIGHTBLUE);

        // Passagem oeste sem colisão: permite retornar à Fronteira Neutra.
        createReturnArea(
                0,
                InsulatorTrailLayout.WEST_GATE_FIRST_ROW
                        * InsulatorTrailLayout.TILE_SIZE,
                5,
                (InsulatorTrailLayout.WEST_GATE_LAST_ROW
                        - InsulatorTrailLayout.WEST_GATE_FIRST_ROW + 1)
                        * InsulatorTrailLayout.TILE_SIZE
        );

        // Saída existente: a área será ligada à conclusão do puzzle depois.
        createExitArea(
                "Encruzilhada das Facções",
                3000,
                1450,
                140,
                300
        );
    }

    /** Depósito estilizado, mantendo a identidade pixel art do cenário. */
    private void addInsulatorHouse() {
        double x = InsulatorTrailLayout.HOUSE_X;
        double y = InsulatorTrailLayout.HOUSE_Y;

        Rectangle base = new Rectangle(x, y + 65, 455, 185);
        base.setFill(Color.web("#C4A77D"));
        base.setStroke(Color.web("#564331"));
        base.setStrokeWidth(8);

        Rectangle roof = new Rectangle(x - 15, y + 15, 485, 65);
        roof.setFill(Color.web("#435C70"));
        roof.setStroke(Color.web("#233442"));
        roof.setStrokeWidth(8);

        Rectangle door = new Rectangle(x + 325, y + 145, 75, 105);
        door.setFill(Color.web("#394D58"));
        door.setStroke(Color.web("#1D2A32"));
        door.setStrokeWidth(5);

        Rectangle window = new Rectangle(x + 45, y + 110, 130, 70);
        window.setFill(Color.web("#7CA7A8"));
        window.setStroke(Color.web("#3E5255"));
        window.setStrokeWidth(6);

        Label sign = new Label("DEPÓSITO DE ISOLANTES");
        sign.setStyle("-fx-font-family: 'Consolas';"
                + "-fx-font-size: 20px; -fx-font-weight: bold;"
                + "-fx-text-fill: #1B2732;");
        sign.relocate(x + 35, y + 205);

        worldLayer.getChildren().addAll(
                base, roof, door, window, sign
        );
    }

    /** Placas preparadas para o futuro sistema de coleta. */
    private void addCeramicPile() {
        double x = InsulatorTrailLayout.CERAMIC_PILE_X;
        double y = InsulatorTrailLayout.CERAMIC_PILE_Y;
        Group pile = new Group();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Rectangle plate = new Rectangle(
                        x + col * 58 + (row % 2) * 4,
                        y + row * 33,
                        54,
                        28
                );
                plate.setFill(Color.web(row % 2 == 0
                        ? "#D6E1DD" : "#A5C2BE"));
                plate.setStroke(Color.web("#527878"));
                plate.setStrokeWidth(4);
                pile.getChildren().add(plate);
            }
        }

        Label label = new Label("PLACAS DE CERÂMICA");
        label.setStyle("-fx-font-family: 'Consolas';"
                + "-fx-font-size: 19px; -fx-font-weight: bold;"
                + "-fx-text-fill: white; -fx-effect:"
                + " dropshadow(gaussian, black, 3, 0.9, 1, 1);");
        label.relocate(x - 10, y - 38);
        worldLayer.getChildren().addAll(pile, label);
    }

    /**
     * Estrutura metálica em diagonal, inspirada no croqui.
     * Visual por enquanto; na próxima etapa receberá zonas de perigo
     * e espaços para posicionar placas isolantes.
     */
    private void addMetalBridge() {
        Group bridge = new Group();

        Rectangle deck = new Rectangle(0, 0, 760, 170);
        deck.setFill(Color.web("#58636B"));
        deck.setStroke(Color.web("#26353D"));
        deck.setStrokeWidth(12);
        bridge.getChildren().add(deck);

        for (int i = 0; i < 15; i++) {
            Rectangle panel = new Rectangle(
                    20 + i * 49, 18, 42, 134
            );
            panel.setFill(Color.web(i % 2 == 0
                    ? "#778B91" : "#64767E"));
            panel.setStroke(Color.web("#39474F"));
            panel.setStrokeWidth(3);
            bridge.getChildren().add(panel);
        }

        for (int i = 0; i < 4; i++) {
            Line arc = new Line(
                    140 + i * 135, 18,
                    155 + i * 135, 150
            );
            arc.setStroke(Color.web("#F6D55C"));
            arc.setStrokeWidth(6);
            bridge.getChildren().add(arc);
        }

        bridge.setLayoutX(650);
        bridge.setLayoutY(1170);
        bridge.setRotate(-20);
        worldLayer.getChildren().add(bridge);

        Label warning = new Label("PASSARELA ENERGIZADA");
        warning.setStyle("-fx-font-family: 'Consolas';"
                + "-fx-font-size: 20px; -fx-font-weight: bold;"
                + "-fx-text-fill: #F6D55C;");
        warning.relocate(790, 1070);
        worldLayer.getChildren().add(warning);
    }

    /** Marco isolante próximo à saída, ainda sem ativação de checkpoint. */
    private void addInsulatorMonolith() {
        double x = InsulatorTrailLayout.MONOLITH_X;
        double y = InsulatorTrailLayout.MONOLITH_Y;

        Rectangle plinth = new Rectangle(x - 18, y + 145, 225, 40);
        plinth.setFill(Color.web("#3D6466"));
        plinth.setStroke(Color.web("#213D42"));
        plinth.setStrokeWidth(5);

        Rectangle body = new Rectangle(x + 22, y, 146, 155);
        body.setFill(Color.web("#86C1BF"));
        body.setStroke(Color.web("#294C52"));
        body.setStrokeWidth(9);

        Rectangle emblem = new Rectangle(x + 61, y + 52, 68, 44);
        emblem.setFill(Color.web("#D6F1EA"));
        emblem.setStroke(Color.web("#3D7774"));
        emblem.setStrokeWidth(5);

        Label label = new Label("MONÓLITO ISOLANTE");
        label.setStyle("-fx-font-family: 'Consolas';"
                + "-fx-font-size: 18px; -fx-font-weight: bold;"
                + "-fx-text-fill: white; -fx-effect:"
                + " dropshadow(gaussian, black, 3, 0.9, 1, 1);");
        label.relocate(x - 15, y - 42);

        worldLayer.getChildren().addAll(
                plinth, body, emblem, label
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

        Merchant ion =
                new Merchant(
                        "Íon",
                        1250,
                        1650,
                        40,
                        50,
                        "ION_QUIZ_I",
                        questionBank,
                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS,
                        AbilityRegistry
                                .REPULSION_PULSE_ID,
                        "ION_SUCCESS_I",
                        "ION_FAILURE_I",
                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS,
                        KnowledgeTopic
                                .KNOW_ELECTRIZATION
                );

        addNpc(
                ion,
                Color.GOLDENROD
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

        Merchant ion =
                new Merchant(
                        "Íon",
                        1250,
                        1650,
                        40,
                        50,
                        "ION_INTRO_II",
                        questionBank,
                        KnowledgeTopic
                                .KNOW_FIELD,
                        AbilityRegistry
                                .FIELD_IMPULSE_ID,
                        "",
                        "",
                        KnowledgeTopic
                                .KNOW_COULOMB,
                        KnowledgeTopic
                                .KNOW_FIELD
                );

        addNpc(
                ion,
                Color.GOLDENROD
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

        Merchant ion =
                new Merchant(
                        "Íon",
                        1250,
                        1650,
                        40,
                        50,
                        "ION_INTRO_III",
                        questionBank,
                        KnowledgeTopic
                                .KNOW_POTENTIAL,
                        AbilityRegistry
                                .EQUIPOTENTIAL_SHIELD_ID,
                        "",
                        "",
                        KnowledgeTopic
                                .KNOW_POTENTIAL
                );

        addNpc(
                ion,
                Color.GOLDENROD
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
                        "O Guardião da Ruptura aguarda no centro da câmara."
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
                currentZone == ExplorationZone.NEUTRAL_BORDER
                        ? 400
                        : 1200
        );

        worldLayer
                .getChildren()
                .add(title);
    }

    private void addNeutralHouse() {

        Image houseImage =
                resourceManager.getImage(
                        NEUTRAL_HOUSE_IMAGE
                );

        ImageView houseView =
                new ImageView(
                        houseImage
                );

        houseView.setFitWidth(
                NEUTRAL_HOUSE_WIDTH
        );

        houseView.setFitHeight(
                NEUTRAL_HOUSE_HEIGHT
        );

        houseView.setPreserveRatio(
                true
        );

        houseView.setSmooth(
                false
        );

        houseView.relocate(
                NEUTRAL_HOUSE_X,
                NEUTRAL_HOUSE_Y
        );

        worldLayer
                .getChildren()
                .add(
                        houseView
                );

        interactables.add(
                new SceneryInteractable(
                        "a casa",
                        "HOUSE_NEUTRAL_INTERACTION",
                        NEUTRAL_HOUSE_X,
                        NEUTRAL_HOUSE_Y,
                        NEUTRAL_HOUSE_WIDTH,
                        NEUTRAL_HOUSE_HEIGHT
                )
        );
    }

    private void addNeutralTree() {

        Image treeImage =
                resourceManager.getImage(
                        NEUTRAL_TREE_IMAGE
                );

        ImageView treeView =
                new ImageView(
                        treeImage
                );

        treeView.setFitWidth(
                NEUTRAL_TREE_WIDTH
        );

        treeView.setFitHeight(
                NEUTRAL_TREE_HEIGHT
        );

        treeView.setPreserveRatio(
                true
        );

        treeView.setSmooth(
                false
        );

        treeView.relocate(
                NEUTRAL_TREE_X,
                NEUTRAL_TREE_Y
        );

        worldLayer
                .getChildren()
                .add(
                        treeView
                );

        interactables.add(
                new SceneryInteractable(
                        "a árvore",
                        "TREE_NEUTRAL_INTERACTION",
                        NEUTRAL_TREE_X,
                        NEUTRAL_TREE_Y,
                        NEUTRAL_TREE_WIDTH,
                        NEUTRAL_TREE_HEIGHT
                )
        );
    }

    private void addNeutralBush(
            double x,
            double y) {

        Image bushImage =
                resourceManager.getImage(
                        NEUTRAL_BUSH_IMAGE
                );

        ImageView bushView =
                new ImageView(
                        bushImage
                );

        bushView.setFitWidth(
                NEUTRAL_BUSH_WIDTH
        );

        bushView.setFitHeight(
                NEUTRAL_BUSH_HEIGHT
        );

        bushView.setPreserveRatio(
                true
        );

        bushView.setSmooth(
                false
        );

        bushView.relocate(
                x,
                y
        );

        worldLayer
                .getChildren()
                .add(
                        bushView
                );
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

        if (shouldShowNpcName(
                npc.getName()
        )) {

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

            return;
        }

        worldLayer
                .getChildren()
                .add(
                        visualNode
                );
    }

    private boolean shouldShowNpcName(
            String name) {

        if (name == null) {

            return true;
        }

        String normalized =
                name
                        .trim()
                        .toLowerCase();

        return !normalized.equals(
                "nilo"
        )
                && !normalized.equals(
                        "íon"
                )
                && !normalized.equals(
                        "ion"
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
                Color.TRANSPARENT
        );

        Label exitLabel =
                new Label(
                        "→ " + destination
                );

        exitLabel.relocate(
                x - 40,
                y - 35
        );

        if (currentZone
                == ExplorationZone
                        .NEUTRAL_BORDER) {

            worldLayer
                    .getChildren()
                    .add(
                            exitArea
                    );

            return;
        }

        worldLayer
                .getChildren()
                .addAll(
                        exitArea,
                        exitLabel
                );
    }

    /** Gatilho exclusivo da passagem oeste da Trilha dos Isolantes. */
    private void createReturnArea(
            double x, double y, double width, double height) {

        returnArea = new Rectangle(x, y, width, height);
        returnArea.setFill(Color.TRANSPARENT);

        Label label = new Label("← FRONTEIRA NEUTRA");
        label.setStyle("-fx-font-family: 'Consolas';"
                + "-fx-font-weight: bold; -fx-font-size: 18px;"
                + "-fx-text-fill: #f4f4f4;"
                + "-fx-effect: dropshadow(gaussian, black, 4, 0.9, 1, 1);");
        label.relocate(65, y - 35);

        worldLayer.getChildren().addAll(returnArea, label);
    }

    public boolean intersectsReturn(
            double x, double y, double width, double height) {
        return intersects(returnArea, x, y, width, height);
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

    public double getCurrentWorldWidth() {

        return currentWorldWidth;
    }

    public double getCurrentWorldHeight() {

        return currentWorldHeight;
    }

    public List<Interactable>
            getInteractables() {

        return List.copyOf(
                interactables
        );
    }
}
