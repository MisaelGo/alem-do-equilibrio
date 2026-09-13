package com.alemdoequilibrio.game;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Controla o modo de exploração do jogo.
 *
 * Esta classe é responsável por criar a cena de exploração,
 * receber as entradas do teclado, executar o loop de atualização
 * e manter a representação visual do Hero sincronizada com
 * sua posição lógica.
 *
 * O GameController apenas inicia ou interrompe a exploração,
 * enquanto os detalhes desse modo ficam concentrados nesta classe.
 */
public class ExplorationController {

    /*
     * Dimensões da área disponível para exploração.
     */
    private final double gameWidth;
    private final double gameHeight;

    /*
     * Personagem controlado pelo jogador.
     *
     * O Hero mantém o estado lógico do personagem,
     * como posição e velocidade.
     */
    private final Hero hero;

    /*
     * Representação visual provisória do Hero na cena.
     *
     * A posição deste Rectangle é atualizada a partir
     * das coordenadas armazenadas no objeto Hero.
     */
    private final Rectangle heroView;

    /*
     * Contêiner dos elementos visuais da exploração.
     */
    private final Pane root;

    /*
     * Cena exibida enquanto o jogador está explorando.
     */
    private final Scene scene;

    /*
     * Armazena o estado atual das teclas de movimento.
     *
     * Índices:
     * 0 = cima
     * 1 = baixo
     * 2 = esquerda
     * 3 = direita
     */
    private final boolean[] keys;

    /*
     * Loop responsável pelas atualizações contínuas
     * durante a exploração.
     */
    private final AnimationTimer gameLoop;

    /*
     * Instante do frame anterior, em nanossegundos.
     *
     * É utilizado para calcular o deltaTime entre
     * duas atualizações consecutivas.
     */
    private long lastTime;

    /**
     * Cria e configura o controlador da exploração.
     *
     * @param gameWidth largura da área de exploração
     * @param gameHeight altura da área de exploração
     */
    public ExplorationController(double gameWidth, double gameHeight) {

        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;

        this.hero = new Hero(100, 100, 200);

        this.heroView = new Rectangle(40, 40);

        /*
         * A representação visual deve começar na mesma
         * posição armazenada pelo Hero.
         */
        heroView.relocate(
                hero.getX(),
                hero.getY()
        );

        this.root = new Pane();
        root.getChildren().add(heroView);

        this.scene = new Scene(
                root,
                gameWidth,
                gameHeight
        );

        /*
         * Cada posição do vetor representa uma direção
         * que pode estar pressionada ou liberada.
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
     * Configura os eventos de pressionar e soltar
     * as teclas usadas para movimentação.
     *
     * Os eventos apenas registram o estado das teclas.
     * O movimento propriamente dito é realizado no game loop.
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
     *
     * A cada frame, o loop interpreta as teclas pressionadas,
     * calcula o tempo decorrido e solicita ao Hero que atualize
     * sua posição.
     */
    private AnimationTimer createGameLoop() {

        return new AnimationTimer() {

            @Override
            public void handle(long now) {

                /*
                 * No primeiro frame não existe um frame anterior.
                 * Portanto, ainda não é possível calcular o deltaTime.
                 */
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                /*
                 * AnimationTimer fornece o tempo em nanossegundos.
                 * A divisão converte o intervalo para segundos.
                 */
                double deltaTime
                        = (now - lastTime) / 1_000_000_000.0;

                lastTime = now;

                double directionX = 0;
                double directionY = 0;

                /*
                 * As teclas pressionadas são convertidas
                 * em um vetor de direção para o Hero.
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
                 * Atualiza a posição lógica do personagem.
                 *
                 * As dimensões do Rectangle são descontadas para
                 * impedir que o Hero ultrapasse os limites da cena.
                 */
                hero.updateMovement(
                        directionX,
                        directionY,
                        deltaTime,
                        gameWidth - heroView.getWidth(),
                        gameHeight - heroView.getHeight()
                );

                /*
                 * Sincroniza a representação visual com
                 * a nova posição lógica do Hero.
                 */
                heroView.relocate(
                        hero.getX(),
                        hero.getY()
                );
            }
        };
    }

    /**
     * Inicia o loop de atualização da exploração.
     *
     * O tempo anterior é zerado para impedir que o período
     * em que a exploração esteve parada seja considerado
     * no próximo cálculo de deltaTime.
     */
    public void start() {
        lastTime = 0;
        gameLoop.start();
    }

    /**
     * Interrompe o loop de atualização da exploração.
     */
    public void stop() {
        gameLoop.stop();
    }
}