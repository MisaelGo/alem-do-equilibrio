package com.alemdoequilibrio.game;

/**
 * Pulso positivo que causa dano leve e repele inimigos da mesma polaridade.
 *
 * A repulsao impede um ataque inimigo. Os valores sao regras de jogo, nao uma
 * simulacao numerica da forca eletrostatica.
 */
public class RepulsionPulse extends Ability {

    private static final float DAMAGE = 8.0f;

    public RepulsionPulse() {
        super(
                "Pulso de Repulsão",
                "Causa dano leve e interrompe o ataque de um inimigo positivo."
        );
    }

    @Override
    protected void execute(BattleController battle) {
        Enemy enemy = battle.getEnemy();
        enemy.takeDamage(DAMAGE);

        if (enemy.isAlive()
                && enemy instanceof ChargedEnemy chargedEnemy
                && chargedEnemy.getChargeType() == ChargeType.POSITIVE) {
            battle.interruptNextEnemyTurn();
        }
    }
}
