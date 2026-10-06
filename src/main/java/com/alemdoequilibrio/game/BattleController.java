package com.alemdoequilibrio.game;

/**
 * Controla as regras e a ordem dos turnos de uma batalha.
 *
 * Esta classe nao cria elementos visuais. O HUD pode consultar seu estado e
 * solicitar as acoes de combate sem misturar JavaFX com as regras da batalha.
 */
public class BattleController {

    /**
     * Indica quem pode agir no momento atual da batalha.
     */
    public enum BattleTurn {
        PLAYER_TURN,
        ENEMY_TURN,
        FINISHED
    }

    /**
     * Representa o resultado atual da batalha.
     */
    public enum BattleResult {
        IN_PROGRESS,
        VICTORY,
        DEFEAT
    }

    /*
     * POO 9 - Agregacao:
     * Hero e Enemy sao criados fora do BattleController e recebidos pelo
     * construtor. Eles continuam existindo independentemente da batalha.
     */
    private final Hero hero;
    private final Enemy enemy;

    /*
     * A transicao de estado e fornecida pelo GameController. Assim, esta
     * classe nao depende diretamente da interface grafica nem do pacote core.
     */
    private final Runnable onBattleFinished;

    private BattleTurn currentTurn;
    private BattleResult result;
    private int completedRounds;
    private boolean interruptNextEnemyTurn;
    private boolean lastEnemyTurnInterrupted;
    private int nextInterruptAvailableRound;

    /**
     * Cria uma batalha sem uma acao automatica de encerramento.
     *
     * @param hero personagem controlado pelo jogador
     * @param enemy inimigo enfrentado
     */
    public BattleController(Hero hero, Enemy enemy) {
        this(hero, enemy, () -> { });
    }

    /**
     * Cria uma batalha e define a acao executada ao seu encerramento.
     *
     * @param hero personagem controlado pelo jogador
     * @param enemy inimigo enfrentado
     * @param onBattleFinished acao externa que retorna ao fluxo do jogo
     */
    public BattleController(
            Hero hero,
            Enemy enemy,
            Runnable onBattleFinished) {

        if (hero == null) {
            throw new IllegalArgumentException(
                    "O Hero da batalha nao pode ser nulo."
            );
        }

        if (enemy == null) {
            throw new IllegalArgumentException(
                    "O Enemy da batalha nao pode ser nulo."
            );
        }

        if (onBattleFinished == null) {
            throw new IllegalArgumentException(
                    "A acao de encerramento nao pode ser nula."
            );
        }

        if (!hero.isActive() || !hero.isAlive()) {
            throw new IllegalArgumentException(
                    "O Hero deve estar ativo e vivo para iniciar a batalha."
            );
        }

        if (!enemy.isActive() || !enemy.isAlive()) {
            throw new IllegalArgumentException(
                    "O Enemy deve estar ativo e vivo para iniciar a batalha."
            );
        }

        this.hero = hero;
        this.enemy = enemy;
        this.onBattleFinished = onBattleFinished;
        this.currentTurn = BattleTurn.PLAYER_TURN;
        this.result = BattleResult.IN_PROGRESS;
        this.completedRounds = 0;
        this.interruptNextEnemyTurn = false;
        this.lastEnemyTurnInterrupted = false;
        this.nextInterruptAvailableRound = 0;
    }

    /**
     * Aplica o ataque do jogador ao inimigo e avanca o fluxo da batalha.
     *
     * @param damage dano causado pelo jogador
     */
    public void performPlayerAttack(float damage) {
        ensureBattleInProgress();
        ensureTurn(BattleTurn.PLAYER_TURN);

        if (!Float.isFinite(damage) || damage < 0.0f) {
            throw new IllegalArgumentException(
                    "O dano deve ser um valor finito e nao negativo."
            );
        }

        enemy.takeDamage(damage);

        if (!enemy.isAlive()) {
            finishBattle(BattleResult.VICTORY);
            return;
        }

        currentTurn = BattleTurn.ENEMY_TURN;
    }

    /**
     * Usa a habilidade equipada do Hero como sua acao neste turno.
     */
    public void performPlayerAbility() {
        ensureBattleInProgress();
        ensureTurn(BattleTurn.PLAYER_TURN);

        Ability ability = hero.getAbilityBook().getEquippedAbility();

        if (ability == null) {
            throw new IllegalStateException(
                    "O Hero nao possui habilidade equipada."
            );
        }

        ability.execute(this);

        if (!enemy.isAlive()) {
            finishBattle(BattleResult.VICTORY);
            return;
        }

        currentTurn = BattleTurn.ENEMY_TURN;
    }

    /**
     * Escolhe a polaridade do Hero antes de agir. Ajustar a carga nao consome
     * o turno, mas so e permitido enquanto a batalha espera o jogador.
     *
     * @param chargeType nova polaridade do Hero
     */
    public void setHeroChargeType(ChargeType chargeType) {
        ensureBattleInProgress();
        ensureTurn(BattleTurn.PLAYER_TURN);
        hero.setChargeType(chargeType);
    }

    /**
     * Permite que uma habilidade interrompa o proximo ataque do inimigo.
     */
    void interruptNextEnemyTurn() {
        ensureBattleInProgress();
        ensureTurn(BattleTurn.PLAYER_TURN);

        if (completedRounds >= nextInterruptAvailableRound) {
            interruptNextEnemyTurn = true;
            nextInterruptAvailableRound = completedRounds + 2;
        }
    }

    void activateEquipotentialShield() {
        ensureBattleInProgress();
        ensureTurn(BattleTurn.PLAYER_TURN);
        hero.activateEquipotentialShield();
    }

    /**
     * Executa o comportamento polimorfico do inimigo e conclui a rodada.
     */
    public void performEnemyTurn() {
        ensureBattleInProgress();
        ensureTurn(BattleTurn.ENEMY_TURN);

        lastEnemyTurnInterrupted = interruptNextEnemyTurn;
        interruptNextEnemyTurn = false;

        if (!lastEnemyTurnInterrupted) {
            enemy.performTurn(hero);
        }

        if (!hero.isAlive()) {
            finishBattle(BattleResult.DEFEAT);
            return;
        }

        updateTemporaryEffects();
        completedRounds++;
        currentTurn = BattleTurn.PLAYER_TURN;
    }

    /**
     * Ponto central para atualizar efeitos no fim da rodada.
     */
    private void updateTemporaryEffects() {
        hero.expireEquipotentialShield();
    }

    private void ensureBattleInProgress() {
        if (result != BattleResult.IN_PROGRESS) {
            throw new IllegalStateException(
                    "A batalha ja foi encerrada."
            );
        }
    }

    private void ensureTurn(BattleTurn expectedTurn) {
        if (currentTurn != expectedTurn) {
            throw new IllegalStateException(
                    "A acao nao pode ser executada neste turno."
            );
        }
    }

    private void finishBattle(BattleResult finalResult) {
        result = finalResult;
        currentTurn = BattleTurn.FINISHED;
        onBattleFinished.run();
    }

    public Hero getHero() {
        return hero;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public BattleTurn getCurrentTurn() {
        return currentTurn;
    }

    public BattleResult getResult() {
        return result;
    }

    public int getCompletedRounds() {
        return completedRounds;
    }

    public boolean isBattleFinished() {
        return result != BattleResult.IN_PROGRESS;
    }

    public boolean wasLastEnemyTurnInterrupted() {
        return lastEnemyTurnInterrupted;
    }
}
