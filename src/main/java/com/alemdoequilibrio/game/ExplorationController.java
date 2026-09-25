package com.alemdoequilibrio.game;

import com.alemdoequilibrio.core.ResourceManager;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * Controla o modo de exploração do jogo.
 *
 * Esta classe é responsável pela cena de exploração,
 * entrada do teclado, atualização do Hero, câmera,
 * mundo e resolução lógica.
 */
public class ExplorationController {

    /*
     * Resolução lógica do jogo.
     *
     * Todo o conteúdo é desenvolvido considerando
     * uma tela de 1280 × 720, independentemente
     * da resolução física do monitor.
     */
    private static final double LOGICAL_WIDTH = 1280;
    private static final double LOGICAL_HEIGHT = 720;

    /*
     * Dimensões do mundo explorável.
     *
     * O mundo é maior que a área visível pela câmera.
     */
    private static final double WORLD_WIDTH = 3200;
    private static final double WORLD_HEIGHT = 1800;

    /*
     * Tamanho de referência dos tiles temporários.
     */
    private static final double TILE_SIZE = 40;

    /*
     * Caminho do sprite do herói dentro de resources.
     *
     * Segue a convenção do Art Bible: snake_case + ação + frame.
     * Por enquanto é apenas um frame parado (idle), sem animação.
     */
    private static final String HERO_SPRITE_PATH = "/images/hero_idle_01.png";

    /*
     * Responsável por carregar e reutilizar imagens, evitando
     * que o sprite do Hero (ou de qualquer outra entidade) seja
     * lido do disco mais de uma vez.
     */
    private final ResourceManager resourceManager;

    /*
     * Personagem controlado pelo jogador.
     */
    private final Hero hero;

    /*
     * Representação visual do Hero.
     *
     * Antes era um Rectangle de placeholder; agora usa o sprite
     * carregado pelo ResourceManager.
     */
    private final ImageView heroView;

    /*
     * Camada que contém os elementos pertencentes
     * ao mundo do jogo.
     *
     * Esta camada é movimentada pela câmera.
     */
    private final Pane worldLayer;

    /*
     * Camada fixa da interface do jogador.
     *
     * Não acompanha a movimentação da câmera.
     */
    private final Pane hudLayer;

    /*
     * Contêiner da tela lógica completa.
     */
    private final Pane gameRoot;

    /*
     * Contêiner externo responsável por posicionar
     * a tela lógica dentro da janela real.
     */
    private final StackPane screenRoot;

    /*
     * Cena da exploração.
     */
    private final Scene scene;

    /*
     * Estado atual das teclas de movimento.
     *
     * 0 = cima
     * 1 = baixo
     * 2 = esquerda
     * 3 = direita
     */
    private final boolean[] keys;

    /*
     * Loop principal da exploração.
     */
    private final AnimationTimer gameLoop;

    /*
     * Momento do frame anterior em nanossegundos.
     */
    private long lastTime;

    /*
     * Posição da câmera dentro do mundo.
     */
    private double cameraX;
    private double cameraY;
    
    /*
     *Atributos do NPC
     */
    private NPC villageNPC;
    private Rectangle npcView;
    
    /**
     * Componenetes temporarios de interface para caixa de dialogo
     */
    
    private javafx.scene.control.Label dialogueLabel;
    private javafx.scene.layout.StackPane dialogueBox;
            
    

    /**
     * Cria e configura o modo de exploração.
     *
     * @param resourceManager responsável por carregar e reutilizar
     *                        as imagens usadas na exploração
     */
    public ExplorationController(ResourceManager resourceManager) {

        this.resourceManager = resourceManager;
        
        /*
         * ==========================
         * TELA LÓGICA
         * ==========================
         */

        this.gameRoot = new Pane();

        /*
         * Criação do Hero lógico.
         */
        this.hero =
                new Hero(100, WORLD_HEIGHT - 100, 200);

        /*
         * Representação visual do Hero, carregada via ResourceManager
         * para reaproveitar a mesma Image caso outra parte do jogo
         * precise do mesmo sprite.
         */
        Image heroSprite =
                resourceManager.getImage(HERO_SPRITE_PATH);

        this.heroView =
                new ImageView(heroSprite);

        heroView.setFitWidth(TILE_SIZE);
        heroView.setFitHeight(TILE_SIZE);
        heroView.setPreserveRatio(false);

        heroView.relocate(
                hero.getX(),
                hero.getY()
        );
        
        /*
         *Representacao visual e logica no NPC
         */
        this.villageNPC = new NPC("A", 100, WORLD_HEIGHT - 100, 30, 50, "Hello World!");
        this.npcView = new Rectangle(villageNPC.getWidth(), villageNPC.getHeight());
        this.npcView.setFill(Color.GOLD);
        this.npcView.relocate(villageNPC.getX(), villageNPC.getY());
        

        /*
         * ==========================
         * CAMADA DO MUNDO
         * ==========================
         */

        this.worldLayer = new Pane();

        worldLayer.setPrefSize(
                WORLD_WIDTH,
                WORLD_HEIGHT
        );

        /*
         * Grade temporária usada para visualizar
         * o tamanho completo do mundo.
         */
        DebugGridRenderer.render(
                worldLayer,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                TILE_SIZE
        );

        /*
         * Elementos temporários que pertencem
         * ao mundo e acompanham a câmera.
         */
        CameraTestOverlay.renderWorld(
                worldLayer,
                TILE_SIZE
        );

        /*
         * O Hero também pertence ao mundo.
         */
        worldLayer.getChildren().add(
                heroView
        );
        
        /*
         * O NPC também pertence ao mundo.
         */
        worldLayer.getChildren().add(
                npcView
        );
        

        /*
         * ==========================
         * CAMADA DO HUD
         * ==========================
         */

        this.hudLayer = new Pane();

        hudLayer.setPrefSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );
        
        this.dialogueLabel = new javafx.scene.control.Label("");
        this.dialogueLabel.setTextFill(Color.BLACK);
        
        //fonte
        this.dialogueLabel.setStyle(
                "-fx-font-family: 'Courier New', monospace" + 
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold;"        
        );
        
        javafx.scene.control.Label arrowIndicator = new javafx.scene.control.Label("▼");
        arrowIndicator.setTextFill(Color.BLACK);
        arrowIndicator.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        StackPane.setAlignment(arrowIndicator, javafx.geometry.Pos.BOTTOM_RIGHT);
        
        //Conteudo do balao principal
        this.dialogueBox = new javafx.scene.layout.StackPane(dialogueLabel, arrowIndicator);
        this.dialogueBox.setPrefWidth(900);
        this.dialogueBox.setPrefHeight(140);
        this.dialogueBox.setPadding(new javafx.geometry.Insets(15, 25, 15, 25));
        StackPane.setAlignment(dialogueLabel, javafx.geometry.Pos.TOP_LEFT);
        
        //Estilizacao retro
        this.dialogueBox.setStyle(
                "-fx-background-color: #F8F8F8; " +
                " -fx-border-color: black #000000 ; " +
                " -fx-border-width: 6px; " +
                " -fx-border-style: solid; " +
                "-fx-background-insets: 0; " +
                "-fx-effect: innershadow(three-pass-box, #000000, 0, 0, 0, 0);"
        );        
        this.dialogueBox.relocate((LOGICAL_WIDTH - 900) / 2, LOGICAL_HEIGHT - 170);
        this.dialogueBox.setVisible(false);
        
        this.hudLayer.getChildren().add(this.dialogueBox);
        /*
         * Elementos da interface permanecem
         * fixos mesmo quando a câmera se move.
         */
        CameraTestOverlay.renderHud(
                hudLayer
        );

        /*
         * A tela lógica deve permanecer
         * exatamente em 1280 × 720.
         */
        gameRoot.setPrefSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        gameRoot.setMinSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        gameRoot.setMaxSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        /*
         * Define a ordem das camadas.
         *
         * O HUD é colocado depois do mundo
         * para ser desenhado sobre ele.
         */
        gameRoot.getChildren().addAll(
                worldLayer,
                hudLayer
        );

        /*
         * Recorta tudo que estiver fora
         * da região visível da câmera.
         */
        Rectangle viewportClip =
                new Rectangle(
                        LOGICAL_WIDTH,
                        LOGICAL_HEIGHT
                );

        gameRoot.setClip(viewportClip);

        /*
         * ==========================
         * ESCALA DA TELA
         * ==========================
         */

        this.screenRoot =
                new StackPane(gameRoot);

        this.scene =
                new Scene(
                        screenRoot,
                        LOGICAL_WIDTH,
                        LOGICAL_HEIGHT
                );

        /*
         * Calcula a escala necessária para manter
         * a proporção 16:9 da resolução lógica.
         *
         * Utilizamos a menor escala entre largura
         * e altura para impedir deformações.
         */
        NumberBinding scale = Bindings.min(
                scene.widthProperty()
                        .divide(LOGICAL_WIDTH),

                scene.heightProperty()
                        .divide(LOGICAL_HEIGHT)
        );

        gameRoot.scaleXProperty().bind(scale);
        gameRoot.scaleYProperty().bind(scale);

        /*
         * ==========================
         * INPUT E GAME LOOP
         * ==========================
         */

        this.keys = new boolean[4];

        configureInput();

        this.gameLoop = createGameLoop();
    }

    /**
     * Retorna a cena utilizada durante a exploração.
     *
     * @return cena da exploração
     */
    public Scene getScene() {
        return scene;
    }

    /*
     * Configura os eventos de teclado.
     *
     * Os eventos apenas registram quais teclas
     * estão pressionadas. A movimentação acontece
     * dentro do game loop.
     */
    private void configureInput() {

        scene.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.W
                    || event.getCode() == KeyCode.UP) {

                keys[0] = true;
            }

            if (event.getCode() == KeyCode.S
                    || event.getCode() == KeyCode.DOWN) {

                keys[1] = true;
            }

            if (event.getCode() == KeyCode.A
                    || event.getCode() == KeyCode.LEFT) {

                keys[2] = true;
            }

            if (event.getCode() == KeyCode.D
                    || event.getCode() == KeyCode.RIGHT) {

                keys[3] = true;
            }
            
            if (event.getCode() == KeyCode.E) {
                if(dialogueBox.isVisible()){
                    dialogueBox.setVisible(false);
                }
                else if (hero.isCloseTo(villageNPC, 80.0)) {
                    String fala = villageNPC.interact();
                    dialogueLabel.setText(villageNPC.getName() + ": " + fala);
                    dialogueBox.setVisible(true);
                }
            }
        });

        scene.setOnKeyReleased(event -> {

            if (event.getCode() == KeyCode.W
                    || event.getCode() == KeyCode.UP) {

                keys[0] = false;
            }

            if (event.getCode() == KeyCode.S
                    || event.getCode() == KeyCode.DOWN) {

                keys[1] = false;
            }

            if (event.getCode() == KeyCode.A
                    || event.getCode() == KeyCode.LEFT) {

                keys[2] = false;
            }

            if (event.getCode() == KeyCode.D
                    || event.getCode() == KeyCode.RIGHT) {

                keys[3] = false;
            }
        });
    }

    /*
     * Cria o loop de atualização da exploração.
     */
    private AnimationTimer createGameLoop() {

        return new AnimationTimer() {

            @Override
            public void handle(long now) {

                /*
                 * No primeiro frame ainda não existe
                 * um frame anterior para calcular
                 * o deltaTime.
                 */
                if (lastTime == 0) {

                    lastTime = now;

                    return;
                }

                /*
                 * AnimationTimer utiliza nanossegundos.
                 *
                 * A divisão transforma o intervalo
                 * em segundos.
                 */
                double deltaTime =
                        (now - lastTime)
                        / 1_000_000_000.0;

                lastTime = now;

                double directionX = 0;
                double directionY = 0;

                /*
                 * Converte as teclas pressionadas
                 * em um vetor de direção.
                 */
                if (keys[0]) {
                    directionY -= 1;
                }

                if (keys[1]) {
                    directionY += 1;
                }

                if (keys[2]) {
                    directionX -= 1;
                }

                if (keys[3]) {
                    directionX += 1;
                }

                /*
                 * O Hero agora é limitado pelo tamanho
                 * do MUNDO, e não mais pelo tamanho
                 * da janela.
                 */
                hero.updateMovement(
                        directionX,
                        directionY,
                        deltaTime,
                        WORLD_WIDTH
                        - TILE_SIZE,
                        WORLD_HEIGHT
                        - TILE_SIZE
                );

                /*
                 * Sincroniza a representação visual
                 * com a posição lógica do Hero.
                 */
                heroView.relocate(
                        hero.getX(),
                        hero.getY()
                );
                
                if (dialogueBox.isVisible() && !hero.isCloseTo(villageNPC, 80.0)) {
                    dialogueBox.setVisible(false);
                }

                /*
                 * Atualiza a câmera após a movimentação.
                 */
                updateCamera();
            }
        };
    }

    /**
     * Atualiza a posição da câmera para acompanhar o Hero.
     *
     * A câmera tenta manter o personagem centralizado,
     * mas nunca pode mostrar uma região fora dos limites
     * do mundo.
     */
    private void updateCamera() {

        /*
         * Posição desejada da câmera caso o Hero
         * estivesse exatamente no centro da tela.
         */
        double targetX =
                hero.getX()
                + TILE_SIZE / 2
                - LOGICAL_WIDTH / 2;

        double targetY =
                hero.getY()
                + TILE_SIZE / 2
                - LOGICAL_HEIGHT / 2;

        /*
         * Impede a câmera de ultrapassar
         * os limites horizontais do mundo.
         */
        cameraX = Math.max(
                0,
                Math.min(
                        targetX,
                        WORLD_WIDTH
                        - LOGICAL_WIDTH
                )
        );

        /*
         * Impede a câmera de ultrapassar
         * os limites verticais do mundo.
         */
        cameraY = Math.max(
                0,
                Math.min(
                        targetY,
                        WORLD_HEIGHT
                        - LOGICAL_HEIGHT
                )
        );

        /*
         * A câmera anda em uma direção,
         * então o mundo é deslocado visualmente
         * na direção contrária.
         */
        worldLayer.setTranslateX(
                -cameraX
        );

        worldLayer.setTranslateY(
                -cameraY
        );
    }

    /**
     * Inicia o loop de atualização da exploração.
     */
    public void start() {

        /*
         * Reinicia a referência temporal para impedir
         * um deltaTime muito grande após uma pausa.
         */
        lastTime = 0;

        updateCamera();

        gameLoop.start();
    }

    /**
     * Interrompe o loop de atualização da exploração.
     */
    public void stop() {

        gameLoop.stop();
    }
}