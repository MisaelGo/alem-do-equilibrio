package com.alemdoequilibrio.game;

/**
 * Ataque inspirado na acao de um campo eletrico sobre uma carga.
 */
public class FieldImpulse extends Ability {

    public FieldImpulse() {
        super(
                "Impulso de Campo",
                "Causa 12 de dano, ou 26 contra carga negativa ou campo favorável."
        );
    }

    @Override
    protected void execute(BattleController battle) {
        Enemy enemy = battle.getEnemy();
        double damage = 12.0;

        if (enemy instanceof ChargedEnemy chargedEnemy
                && chargedEnemy.getChargeType() == ChargeType.NEGATIVE) {
            damage = 26.0;
        } else if (enemy instanceof BossEnemy bossEnemy
                && bossEnemy.getPhase() == BossEnemy.Phase.POLARITY
                && bossEnemy.getChargeType() == ChargeType.NEGATIVE) {
            damage = 26.0;
        } else if (enemy instanceof BossEnemy bossEnemy
                && bossEnemy.getPhase() == BossEnemy.Phase.FIELD
                && bossEnemy.getFieldDirection()
                        == BossEnemy.FieldDirection.RIGHT) {
            damage = 26.0;
        }

        // POO - Coercao explicita: o calculo em double vira o float do dano.
        enemy.takeDamage((float) damage);
    }
}
