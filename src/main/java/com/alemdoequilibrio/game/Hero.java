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
    private final float maxHealth;
    private boolean equipotentialShieldActive;
    private boolean unstable;

    /* POO 1.3 - Enum: restringe a polaridade do Hero a valores conhecidos. */
    private ChargeType chargeType;

    /*
     * POO - Composicao:
     * o Hero cria e possui seu proprio AbilityBook.
     */
    private final AbilityBook abilityBook;

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
        this.maxHealth = health;
        this.abilityBook = new AbilityBook();
        this.chargeType = ChargeType.NEUTRAL;

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

        float actualDamage = damage;

        if (damage > 0.0f && unstable) {
            actualDamage += 2.0f;
            unstable = false;
        }

        if (damage > 0.0f && equipotentialShieldActive) {
            actualDamage *= 0.5f;
            equipotentialShieldActive = false;
        }

        health =
                Math.max(
                        0.0f,
                        health - actualDamage
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

    public float getMaxHealth() {
        return maxHealth;
    }

    /**
     * Recupera o Hero depois de uma derrota sem perder suas habilidades.
     */
    public void restoreHealth() {
        health = maxHealth;
        unstable = false;
        equipotentialShieldActive = false;

        if (isAlive()) {
            activate();
        }
    }

    void activateEquipotentialShield() {
        equipotentialShieldActive = true;
        unstable = false;
    }

    void expireEquipotentialShield() {
        equipotentialShieldActive = false;
    }

    public boolean isEquipotentialShieldActive() {
        return equipotentialShieldActive;
    }

    public void markUnstable() {
        unstable = true;
    }

    public boolean isUnstable() {
        return unstable;
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public void setChargeType(ChargeType chargeType) {
        if (chargeType == null) {
            throw new IllegalArgumentException(
                    "A carga do Hero nao pode ser nula."
            );
        }

        this.chargeType = chargeType;
    }

    /**
     * Aprende uma habilidade sem equipa-la automaticamente.
     *
     * @param ability habilidade a aprender
     */
    public void learnAbility(Ability ability) {
        abilityBook.addAbility(ability);
    }

    /**
     * Aprende uma habilidade e opcionalmente a equipa.
     *
     * POO - Sobrecarga: este metodo tem o mesmo nome do metodo acima, mas
     * recebe um parametro adicional para decidir se a habilidade sera equipada.
     *
     * @param ability habilidade a aprender
     * @param equipNow true para equipar a habilidade imediatamente
     */
    public void learnAbility(Ability ability, boolean equipNow) {
        learnAbility(ability);

        if (equipNow) {
            abilityBook.equipAbility(ability);
        }
    }

    public AbilityBook getAbilityBook() {
        return abilityBook;
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
