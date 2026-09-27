package com.alemdoequilibrio.game;

/**
 * Inimigo medio que alterna sua polaridade depois de cada ataque.
 */
public class CorruptedDipole extends ChargedEnemy {

    private static final String NAME = "Dipolo Corrompido";
    private static final int LEVEL = 3;
    private static final float INITIAL_HEALTH = 45.0f;

    /**
     * Cria um Dipolo Corrompido com polaridade positiva ou negativa.
     *
     * @param enemyId identificador unico desta instancia
     * @param initialCharge polaridade usada no primeiro turno
     */
    public CorruptedDipole(
            int enemyId,
            ChargeType initialCharge) {

        super(
                enemyId,
                NAME,
                LEVEL,
                INITIAL_HEALTH,
                requireNonNeutralCharge(initialCharge)
        );
    }

    /**
     * Ataca o Hero e troca a polaridade para o turno seguinte.
     *
     * @param hero personagem que recebera o ataque
     */
    @Override
    public void performTurn(Hero hero) {
        validateTurnTarget(hero);

        if (!canPerformTurn(hero)) {
            return;
        }

        super.performTurn(hero);
        alternateCharge();
    }

    private void alternateCharge() {
        if (getChargeType() == ChargeType.POSITIVE) {
            changeChargeType(ChargeType.NEGATIVE);
        } else {
            changeChargeType(ChargeType.POSITIVE);
        }
    }
}
