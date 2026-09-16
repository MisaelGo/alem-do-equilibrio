package com.alemdoequilibrio.game;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

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
     * Personagem controlado pelo jogador.
     */
    private final Hero hero;

    /*
     * Representação visual provisória do Hero.
     */
    private final Rectangle heroView;

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

    /**
     * Cria e configura o modo de exploração.
     */
    public ExplorationController() {

        /*
         * Criação do Hero lógico.
         */
        this.hero =
                new Hero(100, WORLD_HEIGHT - 100, 200);

        /*
         * Representação visual temporária.
         */
        this.heroView =
                new Rectangle(
                        TILE_SIZE,
                        TILE_SIZE
                );

        heroView.relocate(
                hero.getX(),
                hero.getY()
        );

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
         * ==========================
         * CAMADA DO HUD
         * ==========================
         */

        this.hudLayer = new Pane();

        hudLayer.setPrefSize(
                LOGICAL_WIDTH,
                LOGICAL_HEIGHT
        );

        /*
         * Elementos da interface permanecem
         * fixos mesmo quando a câmera se move.
         */
        CameraTestOverlay.renderHud(
                hudLayer
        );

        /*
         * ==========================
         * TELA LÓGICA
         * ==========================
         */

        this.gameRoot = new Pane();

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

        this.gameLoop =
                createGameLoop();
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
                        - heroView.getWidth(),
                        WORLD_HEIGHT
                        - heroView.getHeight()
                );

                /*
                 * Sincroniza a representação visual
                 * com a posição lógica do Hero.
                 */
                heroView.relocate(
                        hero.getX(),
                        hero.getY()
                );

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
                + heroView.getWidth() / 2
                - LOGICAL_WIDTH / 2;

        double targetY =
                hero.getY()
                + heroView.getHeight() / 2
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