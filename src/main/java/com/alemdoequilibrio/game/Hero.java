package com.alemdoequilibrio.game;

/**
 * Representa o personagem controlado pelo jogador.
 *
 * O Hero mantém seu estado lógico, como posição e velocidade,
 * e é responsável por atualizar sua própria movimentação.
 *
 * A representação visual do personagem é tratada por outra classe.
 */
/*
 * POO 8 - Interface:
 * Hero implementa Damageable porque pode receber dano durante o combate.
 */
public class Hero implements Damageable, Movable {

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

    /*
     * Quantidade atual de vida do personagem.
     */
    private float health;

    /**
     * Cria um Hero com posição inicial e velocidade definidas.
     *
     * @param x posição horizontal inicial
     * @param y posição vertical inicial
     * @param speed velocidade de movimento do personagem
     */
    public Hero(double x, double y, double speed) {
        this(x, y, speed, 100.0f);
    }

    /**
     * Cria um Hero com posicao, velocidade e vida inicial definidas.
     *
     * @param x posicao horizontal inicial
     * @param y posicao vertical inicial
     * @param speed velocidade de movimento do personagem
     * @param health quantidade inicial de vida
     */
    public Hero(double x, double y, double speed, float health) {

        if (!Float.isFinite(health) || health < 0.0f) {
            throw new IllegalArgumentException(
                    "A vida inicial deve ser um valor finito e nao negativo."
            );
        }

        this.x = x;
        this.y = y;
        this.speed = speed;
        this.health = health;
    }

    /**
     * Reduz a vida do Hero sem permitir que ela fique negativa.
     *
     * @param damage quantidade de dano recebida
     */
    @Override
    public void takeDamage(float damage) {

        if (!Float.isFinite(damage) || damage < 0.0f) {
            throw new IllegalArgumentException(
                    "O dano deve ser um valor finito e nao negativo."
            );
        }

        health = Math.max(0.0f, health - damage);
    }

    /**
     * Verifica se o Hero ainda possui vida.
     *
     * @return true quando a vida for maior que zero
     */
    @Override
    public boolean isAlive() {
        return health > 0.0f;
    }

    /**
     * Retorna a quantidade atual de vida do Hero.
     *
     * @return vida atual
     */
    public float getHealth() {
        return health;
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
    @Override
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
