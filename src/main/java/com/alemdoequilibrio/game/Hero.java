package com.alemdoequilibrio.game;

/**
 * Representa o personagem controlado pelo jogador.
 *
 * O Hero mantém seu estado lógico, como posição e velocidade,
 * e é responsável por atualizar sua própria movimentação.
 *
 * A representação visual do personagem é tratada por outra classe.
 */
public class Hero {

    /*
     * Posição horizontal atual do personagem.
     */
    private double x;

    /*
     * Posição vertical atual do personagem.
     */
    private double y;

    /*
     * Velocidade de movimento em unidades por segundo.
     */
    private double speed;

    /**
     * Cria um Hero com posição inicial e velocidade definidas.
     *
     * @param x posição horizontal inicial
     * @param y posição vertical inicial
     * @param speed velocidade de movimento do personagem
     */
    public Hero(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    /**
     * Atualiza a posição do personagem com base na direção,
     * na velocidade e no tempo decorrido entre os frames.
     *
     * A posição final também é limitada à área permitida.
     *
     * @param directionX direção horizontal do movimento
     * @param directionY direção vertical do movimento
     * @param deltaTime tempo decorrido desde a última atualização, em segundos
     * @param maxX limite máximo permitido no eixo X
     * @param maxY limite máximo permitido no eixo Y
     */
    public void updateMovement(
            double directionX,
            double directionY,
            double deltaTime,
            double maxX,
            double maxY) {

        /*
         * Calcula o comprimento do vetor de direção.
         *
         * Exemplo:
         * direita = (1, 0) -> comprimento 1
         * diagonal = (1, 1) -> comprimento √2
         */
        double length = Math.sqrt(
                directionX * directionX
                + directionY * directionY
        );

        /*
         * Normaliza o vetor para que o personagem não se mova
         * mais rápido na diagonal.
         *
         * O teste evita divisão por zero quando não há movimento.
         */
        if (length > 0) {
            directionX /= length;
            directionY /= length;
        }

        /*
         * Calcula a nova posição usando:
         *
         * posição = posição atual + direção × velocidade × tempo
         */
        double newX = x + directionX * speed * deltaTime;
        double newY = y + directionY * speed * deltaTime;

        /*
         * Mantém o personagem dentro dos limites permitidos.
         *
         * Math.min impede ultrapassar o limite máximo.
         * Math.max impede valores menores que zero.
         */
        x = Math.max(0, Math.min(newX, maxX));
        y = Math.max(0, Math.min(newY, maxY));
    }

    /**
     * Retorna a posição horizontal atual do personagem.
     *
     * @return posição atual no eixo X
     */
    public double getX() {
        return x;
    }

    /**
     * Retorna a posição vertical atual do personagem.
     *
     * @return posição atual no eixo Y
     */
    public double getY() {
        return y;
    }
}