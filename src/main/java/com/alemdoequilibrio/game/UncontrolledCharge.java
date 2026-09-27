package com.alemdoequilibrio.game;

/**
 * Inimigo fraco que possui polaridade positiva ou negativa.
 *
 * A Carga Descontrolada reutiliza o ataque carregado de ChargedEnemy.
 */
public class UncontrolledCharge extends ChargedEnemy {

    private static final String NAME = "Carga Descontrolada";
    private static final int LEVEL = 2;
    private static final float INITIAL_HEALTH = 30.0f;

    /**
     * Cria uma Carga Descontrolada com a polaridade informada.
     *
     * @param enemyId identificador unico desta instancia
     * @param chargeType carga positiva ou negativa
     */
    public UncontrolledCharge(int enemyId, ChargeType chargeType) {
        super(
                enemyId,
                NAME,
                LEVEL,
                INITIAL_HEALTH,
                requireNonNeutralCharge(chargeType)
        );
    }
}
