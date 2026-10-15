package com.alemdoequilibrio.game;

import java.util.EnumSet;

/**
 * Verificacao executavel do combate sem dependencias de teste adicionais.
 *
 * Executar apos mvn test-compile com:
 * java -cp "target/classes;target/test-classes"
 *     com.alemdoequilibrio.game.CombatScenarioChecks
 */
public final class CombatScenarioChecks {

    private CombatScenarioChecks() {
    }

    public static void main(String[] args) {
        checkBossIsWinnableWithoutAbilities();
        checkRepulsionAndTurnOrder();
        checkChargeSelectionDuringBattle();
        checkFieldImpulseAndShield();
        checkPotentialPhaseAlternates();
        checkDefeatRecovery();
        checkQuizQuestionAvailability();
        checkQuizRewards();
        System.out.println("Combate: todos os cenarios passaram.");
    }

    private static void checkBossIsWinnableWithoutAbilities() {
        Hero hero = new Hero(0, 0, 200);
        BossEnemy boss = new BossEnemy(701, "Guardiao", 5, 100.0f);
        BattleController battle = new BattleController(hero, boss);
        EnumSet<BossEnemy.Phase> phases = EnumSet.noneOf(
                BossEnemy.Phase.class
        );

        for (int turn = 0; turn < 12 && !battle.isBattleFinished(); turn++) {
            phases.add(boss.getPhase());
            battle.performPlayerAttack(20.0f);

            if (!battle.isBattleFinished()) {
                phases.add(boss.getPhase());
                battle.performEnemyTurn();
            }
        }

        check(battle.getResult() == BattleController.BattleResult.VICTORY,
                "O chefe deve ser vencivel sem habilidades.");
        check(hero.isAlive(), "O Hero deve sobreviver ao chefe basico.");
        check(phases.size() == 3, "As tres fases devem aparecer.");
    }

    private static void checkRepulsionAndTurnOrder() {
        Hero hero = new Hero(0, 0, 200);
        hero.learnAbility(new RepulsionPulse(), true);
        BattleController battle = new BattleController(
                hero,
                new UncontrolledCharge(1, ChargeType.POSITIVE)
        );

        battle.performPlayerAbility();
        check(battle.getEnemy().getHealth() == 22.0f,
                "O Pulso deve causar 8 de dano.");
        check(battle.getCurrentTurn() == BattleController.BattleTurn.ENEMY_TURN,
                "A habilidade deve consumir o turno do jogador.");
        battle.performEnemyTurn();
        check(hero.getHealth() == 100.0f
                        && battle.wasLastEnemyTurnInterrupted(),
                "A mesma polaridade deve interromper o ataque.");

        battle.performPlayerAbility();
        battle.performEnemyTurn();
        check(hero.getHealth() == 86.5f
                        && !battle.wasLastEnemyTurnInterrupted(),
                "A interrupcao nao pode ocorrer em rodadas consecutivas.");

        Hero negativeHero = new Hero(0, 0, 200);
        negativeHero.setChargeType(ChargeType.NEGATIVE);
        negativeHero.learnAbility(new RepulsionPulse(), true);
        BattleController negativeBattle = new BattleController(
                negativeHero,
                new UncontrolledCharge(2, ChargeType.NEGATIVE)
        );
        negativeBattle.performPlayerAbility();
        negativeBattle.performEnemyTurn();
        check(negativeHero.getHealth() == 100.0f,
                "O Pulso deve usar a carga nao neutra do Hero.");
    }

    private static void checkFieldImpulseAndShield() {
        Hero attacker = new Hero(0, 0, 200);
        attacker.learnAbility(new FieldImpulse(), true);
        BattleController impulseBattle = new BattleController(
                attacker,
                new UncontrolledCharge(3, ChargeType.NEGATIVE)
        );
        impulseBattle.performPlayerAbility();
        check(impulseBattle.getEnemy().getHealth() == 4.0f,
                "O Impulso deve ter bonus contra carga negativa.");

        Hero defender = new Hero(0, 0, 200);
        defender.markUnstable();
        defender.learnAbility(new EquipotentialShield(), true);
        BattleController shieldBattle = new BattleController(
                defender,
                new Enemy(4, "Teste", 1, 20.0f)
        );
        shieldBattle.performPlayerAbility();
        shieldBattle.performEnemyTurn();
        check(defender.getHealth() == 96.5f,
                "O Escudo deve reduzir pela metade o dano de 7.");
        check(!defender.isUnstable() && !defender.isEquipotentialShieldActive(),
                "Instabilidade e protecao devem terminar corretamente.");

        BossEnemy boss = new BossEnemy(8, "Guardiao", 5, 100.0f);
        boss.takeDamage(40.0f);
        boss.performTurn(attacker);
        check(boss.getPhase() == BossEnemy.Phase.FIELD
                        && boss.getFieldDirection()
                                == BossEnemy.FieldDirection.RIGHT,
                "O campo do chefe deve alternar a direcao.");
        BattleController fieldBattle = new BattleController(attacker, boss);
        fieldBattle.performPlayerAbility();
        check(boss.getHealth() == 34.0f,
                "O Impulso deve aproveitar o campo favoravel do chefe.");

        Hero protectedHero = new Hero(0, 0, 200);
        protectedHero.learnAbility(new EquipotentialShield(), true);
        UnstableConductor conductor = new UnstableConductor(9);
        conductor.performTurn(protectedHero);
        BattleController conductorBattle = new BattleController(
                protectedHero, conductor
        );
        conductorBattle.performPlayerAbility();
        conductorBattle.performEnemyTurn();
        check(protectedHero.getHealth() == 89.0f
                        && !protectedHero.isUnstable(),
                "O Escudo deve reduzir a descarga e impedir instabilidade.");
    }

    private static void checkChargeSelectionDuringBattle() {
        Hero hero = new Hero(0, 0, 200);
        hero.learnAbility(new RepulsionPulse(), true);
        BattleController battle = new BattleController(
                hero, new UncontrolledCharge(10, ChargeType.NEGATIVE)
        );

        battle.setHeroChargeType(ChargeType.NEGATIVE);
        check(hero.getChargeType() == ChargeType.NEGATIVE,
                "A polaridade escolhida deve chegar ao Hero.");
        battle.performPlayerAbility();

        boolean rejectedDuringEnemyTurn = false;
        try {
            battle.setHeroChargeType(ChargeType.POSITIVE);
        } catch (IllegalStateException expected) {
            rejectedDuringEnemyTurn = true;
        }
        check(rejectedDuringEnemyTurn,
                "Nao pode mudar a carga durante o turno inimigo.");

        battle.performEnemyTurn();
        check(battle.wasLastEnemyTurnInterrupted()
                        && hero.getHealth() == hero.getMaxHealth(),
                "O Pulso deve interromper o inimigo com a carga escolhida.");

        battle.setHeroChargeType(ChargeType.POSITIVE);
        check(hero.getChargeType() == ChargeType.POSITIVE,
                "A carga deve poder mudar no turno seguinte.");

        boolean rejectedNullCharge = false;
        try {
            battle.setHeroChargeType(null);
        } catch (IllegalArgumentException expected) {
            rejectedNullCharge = true;
        }
        check(rejectedNullCharge,
                "Uma carga nula nao pode ser selecionada.");

        battle.performPlayerAttack(30.0f);
        check(battle.getResult() == BattleController.BattleResult.VICTORY,
                "O ataque deve encerrar a batalha.");

        boolean rejectedAfterVictory = false;
        try {
            battle.setHeroChargeType(ChargeType.NEUTRAL);
        } catch (IllegalStateException expected) {
            rejectedAfterVictory = true;
        }
        check(rejectedAfterVictory,
                "Nao pode mudar a carga depois da batalha.");
    }

    private static void checkPotentialPhaseAlternates() {
        BossEnemy boss = new BossEnemy(5, "Guardiao", 5, 100.0f);
        Hero hero = new Hero(0, 0, 200);
        boss.takeDamage(70.0f);
        check(boss.getPhase() == BossEnemy.Phase.POTENTIAL,
                "O chefe deve chegar a fase de potencial.");

        boss.performTurn(hero);
        check(boss.isPotentialShieldActive(),
                "O escudo deve ficar ativo no primeiro turno da fase.");
        boss.takeDamage(10.0f);
        check(boss.getHealth() == 25.0f,
                "O escudo deve reduzir um ataque de 10 para 5.");

        boss.performTurn(hero);
        check(!boss.isPotentialShieldActive(),
                "O turno seguinte da fase deve deixar o escudo inativo.");
        boss.takeDamage(10.0f);
        check(boss.getHealth() == 15.0f,
                "Sem escudo, o dano deve ser integral.");
    }

    private static void checkDefeatRecovery() {
        Hero hero = new Hero(0, 0, 200, 5.0f);
        hero.learnAbility(new RepulsionPulse());
        BattleController battle = new BattleController(
                hero,
                new WanderingSpark(6)
        );
        battle.performPlayerAttack(0.0f);
        battle.performEnemyTurn();
        check(battle.getResult() == BattleController.BattleResult.DEFEAT,
                "O controlador deve reconhecer a derrota.");

        hero.restoreHealth();
        check(hero.isAlive() && hero.isActive() && hero.getHealth() == 5.0f,
                "A recuperacao deve restaurar o Hero.");
        check(hero.getAbilityBook().getAbilities().size() == 1,
                "A recuperacao nao deve apagar habilidades.");
    }

    private static void checkQuizQuestionAvailability() {
        QuestionBank bank = new QuestionBank();
        bank.loadQuestionsFromFile("/questions/questions.csv");

        check(bank.getQuestionsForQuiz(
                KnowledgeTopic.KNOW_CHARGE_SIGNS, 4).size() == 4,
                "O primeiro quiz precisa de quatro questoes.");
        check(bank.getQuestionsForQuiz(
                KnowledgeTopic.KNOW_FIELD, 4).size() == 4,
                "O segundo quiz precisa de quatro questoes.");
        check(bank.getQuestionsForQuiz(
                KnowledgeTopic.KNOW_POTENTIAL, 4).size() == 4,
                "O terceiro quiz precisa de quatro questoes.");
    }

    private static void checkQuizRewards() {
        check(Merchant.createReward(KnowledgeTopic.KNOW_CHARGE_SIGNS)
                        instanceof RepulsionPulse,
                "O primeiro quiz deve conceder Pulso de Repulsao.");
        check(Merchant.createReward(KnowledgeTopic.KNOW_FIELD)
                        instanceof FieldImpulse,
                "O segundo quiz deve conceder Impulso de Campo.");
        check(Merchant.createReward(KnowledgeTopic.KNOW_POTENTIAL)
                        instanceof EquipotentialShield,
                "O terceiro quiz deve conceder Escudo Equipotencial.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
