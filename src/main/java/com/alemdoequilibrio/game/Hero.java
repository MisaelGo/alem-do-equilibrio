package com.alemdoequilibrio.game;

/**
 * Representa o personagem controlado pelo jogador.
 */
public class Hero extends GameCharacter
        implements Damageable, Movable {

    private double x;
    private double y;
    private double speed;

    private float health;

    public Hero(
            double x,
            double y,
            double speed) {

        this(
                x,
                y,
                speed,
                100.0f
        );
    }

    public Hero(
            double x,
            double y,
            double speed,
            float health) {

        if (!Float.isFinite(health)
                || health < 0.0f) {

            throw new IllegalArgumentException(
                    "A vida inicial deve ser "
                    + "um valor finito e nao negativo."
            );
        }

        this.x = x;
        this.y = y;
        this.speed = speed;
        this.health = health;

        if (!isAlive()) {
            deactivate();
        }
    }

    @Override
    public void takeDamage(float damage) {

        if (!Float.isFinite(damage)
                || damage < 0.0f) {

            throw new IllegalArgumentException(
                    "O dano deve ser um valor "
                    + "finito e nao negativo."
            );
        }

        health =
                Math.max(
                        0.0f,
                        health - damage
                );

        updateState();
    }

    @Override
    public boolean isAlive() {
        return health > 0.0f;
    }

    @Override
    public void updateState() {

        if (!isAlive()) {
            deactivate();
        }
    }

    @Override
    public void updateMovement(
            double directionX,
            double directionY,
            double deltaTime,
            double maxX,
            double maxY) {

        double length =
                Math.sqrt(
                        directionX * directionX
                        + directionY * directionY
                );

        if (length > 0) {

            directionX /= length;
            directionY /= length;
        }

        double newX =
                x
                + directionX
                * speed
                * deltaTime;

        double newY =
                y
                + directionY
                * speed
                * deltaTime;

        x = Math.max(
                0,
                Math.min(
                        newX,
                        maxX
                )
        );

        y = Math.max(
                0,
                Math.min(
                        newY,
                        maxY
                )
        );
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public float getHealth() {
        return health;
    }
    
    public void setPosition(
        double x,
        double y) {

        if (!Double.isFinite(x)
            || !Double.isFinite(y)
            || x < 0
            || y < 0) {

            throw new IllegalArgumentException(
                    "A posição deve ser válida."
            );
        }

        this.x = x;
        this.y = y;
    }
    
}