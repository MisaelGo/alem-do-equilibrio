package com.alemdoequilibrio.game;

/**
 * Representa um inimigo chefe do jogo.
 *
 * POO 7 - Heranca:
 * BossEnemy especializa Enemy e conserva os metodos de identidade, vida e
 * estado da superclasse. O combate passa por tres fases ligadas a sua vida.
 */
public class BossEnemy extends Enemy {

    public enum Phase {
        POLARITY,
        FIELD,
        POTENTIAL
    }

    public enum FieldDirection {
        LEFT,
        RIGHT
    }

    private final float maxHealth;
    private Phase phase;
    private ChargeType chargeType;
    private FieldDirection fieldDirection;
    private boolean potentialShieldActive;
    private boolean potentialProtectionCycle;

    /**
     * Cria um chefe com identidade, nome, nivel e vida definidos.
     *
     * @param enemyId identificador unico do chefe
     * @param name nome exibido do chefe
     * @param level nivel usado no calculo do ataque
     * @param health quantidade inicial de vida
     */
    public BossEnemy(int enemyId, String name, int level, float health) {
        super(enemyId, name, level, health);

        maxHealth = health;
        phase = Phase.POLARITY;
        chargeType = ChargeType.POSITIVE;
        fieldDirection = FieldDirection.LEFT;
        potentialShieldActive = false;
        potentialProtectionCycle = false;
    }

    /**
     * Na fase de potencial, o escudo reduz pela metade um ataque recebido.
     * A fase muda em dois tercos e um terco da vida inicial.
     *
     * @param damage dano recebido
     */
    @Override
    public void takeDamage(float damage) {
        boolean protectedHit = potentialShieldActive && damage > 0.0f;
        float actualDamage = protectedHit ? damage * 0.5f : damage;

        super.takeDamage(actualDamage);

        if (protectedHit) {
            potentialShieldActive = false;
        }

        updatePhase();
    }

    /**
     * A fase atual define o ataque e o estado preparado para o turno seguinte.
     * Os danos sao deliberadamente moderados para permitir vitoria sem habilidades.
     *
     * @param hero personagem que recebera o ataque
     */
    @Override
    public void performTurn(Hero hero) {
        validateTurnTarget(hero);

        if (!canPerformTurn(hero)) {
            return;
        }

        switch (phase) {
            case POLARITY -> {
                hero.takeDamage(8.0f);
                chargeType = chargeType == ChargeType.POSITIVE
                        ? ChargeType.NEGATIVE : ChargeType.POSITIVE;
            }

            case FIELD -> {
                hero.takeDamage(10.0f);
                fieldDirection = fieldDirection == FieldDirection.LEFT
                        ? FieldDirection.RIGHT : FieldDirection.LEFT;
            }

            case POTENTIAL -> {
                hero.takeDamage(12.0f);
                potentialProtectionCycle = !potentialProtectionCycle;
                potentialShieldActive = potentialProtectionCycle;
            }
        }
    }

    private void updatePhase() {
        Phase nextPhase;

        if (getHealth() <= maxHealth / 3.0f) {
            nextPhase = Phase.POTENTIAL;
        } else if (getHealth() <= maxHealth * 2.0f / 3.0f) {
            nextPhase = Phase.FIELD;
        } else {
            nextPhase = Phase.POLARITY;
        }

        if (phase != nextPhase) {
            phase = nextPhase;
            potentialShieldActive = false;
            potentialProtectionCycle = false;
        }
    }

    public Phase getPhase() {
        return phase;
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public FieldDirection getFieldDirection() {
        return fieldDirection;
    }

    public boolean isPotentialShieldActive() {
        return potentialShieldActive;
    }
}
