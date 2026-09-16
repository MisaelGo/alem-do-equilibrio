package com.alemdoequilibrio.game;

/**
 * Define o contrato dos objetos que podem se movimentar.
 *
 * POO 8 - Interface:
 * permite representar objetos móveis sem depender
 * diretamente da classe Hero.
 */
public interface Movable {

    /**
     * Atualiza a posição do objeto.
     *
     * @param directionX direção horizontal do movimento
     * @param directionY direção vertical do movimento
     * @param deltaTime tempo transcorrido desde o último frame
     * @param maxX limite máximo no eixo X
     * @param maxY limite máximo no eixo Y
     */
    void updateMovement(
            double directionX,
            double directionY,
            double deltaTime,
            double maxX,
            double maxY
    );
}