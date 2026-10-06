package com.alemdoequilibrio.game;

/**
 * Inimigo medio que alterna entre acumular e liberar energia.
 */
public class UnstableConductor extends Enemy {

    private static final String NAME = "Condutor Instavel";
    private static final int LEVEL = 3;
    private static final float INITIAL_HEALTH = 50.0f;
    private static final float DISCHARGE_MULTIPLIER = 2.0f;

    private boolean energyStored;

    /**
     * Cria um Condutor Instavel sem energia acumulada.
     *
     * @param enemyId identificador unico desta instancia
     */
    public UnstableConductor(int enemyId) {
        super(enemyId, NAME, LEVEL, INITIAL_HEALTH);
        this.energyStored = false;
    }

    /**
     * Acumula energia na primeira acao e descarrega dano na acao seguinte.
     *
     * @param hero personagem que recebera a descarga
     */
    @Override
    public void performTurn(Hero hero) {
        validateTurnTarget(hero);

        if (!canPerformTurn(hero)) {
            return;
        }

        if (!energyStored) {
            energyStored = true;
            return;
        }

        float dischargeDamage =
                calculateTurnDamage() * DISCHARGE_MULTIPLIER;

        boolean shielded = hero.isEquipotentialShieldActive();
        hero.takeDamage(dischargeDamage);

        if (!shielded && hero.isAlive()) {
            hero.markUnstable();
        }

        energyStored = false;
    }

    /**
     * Informa se a proxima acao liberara a energia acumulada.
     *
     * @return true quando o condutor estiver pronto para descarregar
     */
    public boolean hasStoredEnergy() {
        return energyStored;
    }
}
