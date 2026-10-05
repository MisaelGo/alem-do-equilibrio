package com.alemdoequilibrio.game;

import java.util.Objects;

import com.alemdoequilibrio.core.ResourceManager;

import javafx.scene.image.ImageView;

/**
 * Representação visual e máquina de animação do Herói.
 *
 * Responsável por gerenciar o nó gráfico (ImageView), as trocas de sprites nas
 * 4 direções, a alternância de passadas e o efeito de movimento orgânico (bobbing).
 *
 * Desacopla os detalhes visuais do ExplorationController e preserva o modelo Hero.
 */
public class HeroView {

    /**
     * Direção do olhar e deslocamento do personagem.
     */
    public enum Direction {
        DOWN, UP, LEFT, RIGHT
    }

    private static final String DEFAULT_IDLE_SPRITE = "/images/hero_idle_right.png";
    private static final double FRAME_INTERVAL = 0.16;
    private static final double BOB_AMPLITUDE = -2.5;

    /*
     * POO 1.1 - Encapsulamento:
     * todos os atributos de estado visual e controle de quadros são privados.
     */
    private final ImageView imageView;
    private final ResourceManager resourceManager;

    private Direction facingDirection;
    private double stepTimer;
    private int currentFrame;
    private double walkCycle;

    /**
     * POO 3 - Construtor:
     * Inicializa a visão do herói com o tamanho do tile e o sprite inicial.
     *
     * @param resourceManager gerenciador central para cache de imagens
     * @param tileSize tamanho de renderização do personagem na tela
     */
    public HeroView(ResourceManager resourceManager, double tileSize) {
        this.resourceManager = Objects.requireNonNull(
                resourceManager,
                "O ResourceManager não pode ser nulo."
        );

        this.imageView = new ImageView(
                resourceManager.getImage(DEFAULT_IDLE_SPRITE)
        );
        this.imageView.setFitWidth(tileSize);
        this.imageView.setFitHeight(tileSize);
        this.imageView.setPreserveRatio(false);

        this.facingDirection = Direction.RIGHT;
        this.stepTimer = 0.0;
        this.currentFrame = 1;
        this.walkCycle = 0.0;
    }

    /**
     * Atualiza a posição, direção e quadro de animação da caminhada.
     *
     * @param directionX entrada no eixo horizontal (-1, 0, 1)
     * @param directionY entrada no eixo vertical (-1, 0, 1)
     * @param deltaTime tempo decorrido desde o último frame (em segundos)
     * @param x coordenada X atual do modelo lógico do Hero
     * @param y coordenada Y atual do modelo lógico do Hero
     */
    public void update(
            double directionX,
            double directionY,
            double deltaTime,
            double x,
            double y) {

        // Posiciona no mundo conforme as coordenadas do Hero
        imageView.relocate(x, y);

        boolean isMoving = (directionX != 0 || directionY != 0);

        if (isMoving) {
            // Atualiza a direção prioritária
            if (directionY < 0) {
                facingDirection = Direction.UP;
            } else if (directionY > 0) {
                facingDirection = Direction.DOWN;
            } else if (directionX < 0) {
                facingDirection = Direction.LEFT;
            } else if (directionX > 0) {
                facingDirection = Direction.RIGHT;
            }

            stepTimer += deltaTime;
            walkCycle += deltaTime * 12.0;

            if (stepTimer >= FRAME_INTERVAL) {
                currentFrame = (currentFrame == 1) ? 2 : 1;
                stepTimer = 0.0;
            }

            // Balanço vertical suave que elimina a sensação de boneco engessado
            double bob = Math.abs(Math.sin(walkCycle)) * BOB_AMPLITUDE;
            imageView.setTranslateY(bob);
        } else {
            // Parado: retorna à pose estática sem balanço
            currentFrame = 1;
            stepTimer = 0.0;
            walkCycle = 0.0;
            imageView.setTranslateY(0.0);
        }

        applySprite(isMoving);
    }

    /**
     * Define a posição inicial ou de reposicionamento (ex.: transição de mapa).
     *
     * @param x posição no eixo X
     * @param y posição no eixo Y
     */
    public void setPosition(double x, double y) {
        imageView.relocate(x, y);
        imageView.setTranslateY(0.0);
    }

    /**
     * Aplica o sprite adequado à direção e estado de movimento atuais.
     */
    private void applySprite(boolean isMoving) {
        String spritePath;

        switch (facingDirection) {
            case UP -> {
                spritePath = isMoving
                        ? "/images/hero_walk_up_0" + currentFrame + ".png"
                        : "/images/hero_idle_up.png";
                imageView.setScaleX(1.0);
            }
            case DOWN -> {
                spritePath = isMoving
                        ? "/images/hero_walk_down_0" + currentFrame + ".png"
                        : "/images/hero_idle_down.png";
                imageView.setScaleX(1.0);
            }
            case RIGHT -> {
                spritePath = isMoving
                        ? "/images/hero_walk_right_0" + currentFrame + ".png"
                        : "/images/hero_idle_right.png";
                imageView.setScaleX(1.0);
            }
            case LEFT -> {
                // Reutiliza os sprites da direita através de espelhamento horizontal
                spritePath = isMoving
                        ? "/images/hero_walk_right_0" + currentFrame + ".png"
                        : "/images/hero_idle_right.png";
                imageView.setScaleX(-1.0);
            }
            default -> {
                spritePath = DEFAULT_IDLE_SPRITE;
                imageView.setScaleX(1.0);
            }
        }

        imageView.setImage(
                resourceManager.getImage(spritePath)
        );
    }

    /**
     * Retorna o nó gráfico JavaFX para adição na camada do mundo.
     *
     * @return nó ImageView do herói
     */
    public ImageView getNode() {
        return imageView;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }
}
