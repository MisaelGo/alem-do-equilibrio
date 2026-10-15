package com.alemdoequilibrio.game;

/**
 * Pulso que causa dano leve e repele inimigos da mesma polaridade.
 *
 * A repulsao impede um ataque inimigo. Os valores sao regras de jogo, nao uma
 * simulacao numerica da forca eletrostatica.
 */
public class RepulsionPulse extends Ability {

    private static final float DAMAGE = 8.0f;

    public RepulsionPulse() {
        super(
                "Pulso de Repulsão",
                "Causa dano leve e interrompe um ataque de carga igual."
        );
    }

    @Override
    protected void execute(BattleController battle) {
        Enemy enemy = battle.getEnemy();
        enemy.takeDamage(DAMAGE);

        ChargeType heroCharge = battle.getHero().getChargeType();
        ChargeType pulseCharge = heroCharge == ChargeType.NEUTRAL
                ? ChargeType.POSITIVE : heroCharge;

        boolean matchingChargedEnemy = enemy instanceof ChargedEnemy chargedEnemy
                && chargedEnemy.getChargeType() == pulseCharge;

        boolean matchingBoss = enemy instanceof BossEnemy bossEnemy
                && bossEnemy.getPhase() == BossEnemy.Phase.POLARITY
                && bossEnemy.getChargeType() == pulseCharge;

        if (enemy.isAlive()
                && (matchingChargedEnemy || matchingBoss)) {
            battle.interruptNextEnemyTurn();
        }
    }
}
