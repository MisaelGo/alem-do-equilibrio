package com.alemdoequilibrio.game;

/**
 * Protege o Hero durante um turno inimigo e remove a instabilidade.
 */
public class EquipotentialShield extends Ability {

    public EquipotentialShield() {
        super(
                "Escudo Equipotencial",
                "Reduz pela metade o proximo dano e remove a instabilidade."
        );
    }

    @Override
    protected void execute(BattleController battle) {
        battle.activateEquipotentialShield();
    }
}
