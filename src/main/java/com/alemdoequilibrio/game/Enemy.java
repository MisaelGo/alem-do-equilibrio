package com.alemdoequilibrio.game;

/**
 * Representa um inimigo concreto com comportamento padrao de combate.
 *
 * Enemy herda o estado comum de GameCharacter e implementa Damageable
 * porque pode receber dano durante uma batalha.
 */
public class Enemy extends GameCharacter implements Damageable {

    /*
     * POO 1.2 - String, int e float na mesma classe:
     * os atributos representam dados efetivamente usados pelo inimigo.
     */
    private String name;
    private int level;
    private float health;

    /*
     * POO 6.1 - Atributo final em superclasse concreta:
     * a identidade do inimigo nao pode mudar depois de sua criacao.
     */
    private final int enemyId;

    /**
     * Cria um inimigo com identidade, nome, nivel e vida definidos.
     *
     * @param enemyId identificador unico do inimigo
     * @param name nome exibido do inimigo
     * @param level nivel usado no calculo do ataque padrao
     * @param health quantidade inicial de vida
     */
    public Enemy(int enemyId, String name, int level, float health) {

        if (enemyId <= 0) {
            throw new IllegalArgumentException(
                    "O identificador do inimigo deve ser positivo."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do inimigo nao pode ser vazio."
            );
        }

        if (level <= 0) {
            throw new IllegalArgumentException(
                    "O nivel do inimigo deve ser positivo."
            );
        }

        if (!Float.isFinite(health) || health < 0.0f) {
            throw new IllegalArgumentException(
                    "A vida inicial deve ser um valor finito e nao negativo."
            );
        }

        this.enemyId = enemyId;
        this.name = name;
        this.level = level;
        this.health = health;

        if (!isAlive()) {
            deactivate();
        }
    }

    /**
     * Executa o turno padrao do inimigo contra o Hero.
     *
     * Inimigos derrotados ou inativos nao podem atacar. O dano base
     * aumenta de acordo com o nivel do inimigo.
     *
     * @param hero personagem que recebera o ataque
     */
    public void performTurn(Hero hero) {

        validateTurnTarget(hero);

        if (!canPerformTurn(hero)) {
            return;
        }

        hero.takeDamage(calculateTurnDamage());
    }

    /**
     * Valida o alvo antes da execucao de um turno.
     *
     * O acesso protegido permite que subclasses reutilizem a mesma regra.
     *
     * @param hero personagem escolhido como alvo
     */
    protected void validateTurnTarget(Hero hero) {

        if (hero == null) {
            throw new IllegalArgumentException(
                    "O alvo do turno nao pode ser nulo."
            );
        }
    }

    /**
     * Verifica se este inimigo pode atacar o alvo informado.
     *
     * @param hero personagem escolhido como alvo
     * @return true quando o inimigo e o alvo estao aptos para o turno
     */
    protected boolean canPerformTurn(Hero hero) {
        return isActive() && isAlive() && hero.isAlive();
    }

    /**
     * Calcula o dano do ataque padrao deste inimigo.
     *
     * @return dano calculado a partir do nivel
     */
    protected float calculateTurnDamage() {
        return 5.0f + level * 2.0f;
    }

    /**
     * Reduz a vida do inimigo sem permitir que ela fique negativa.
     *
     * @param damage quantidade de dano recebida
     */
    @Override
    public void takeDamage(float damage) {

        if (!Float.isFinite(damage) || damage < 0.0f) {
            throw new IllegalArgumentException(
                    "O dano deve ser um valor finito e nao negativo."
            );
        }

        health = Math.max(0.0f, health - damage);

        updateState();
    }

    /**
     * Informa se o inimigo ainda possui vida.
     *
     * @return true quando a vida for maior que zero
     */
    @Override
    public boolean isAlive() {
        return health > 0.0f;
    }

    /**
     * Atualiza a atividade do inimigo de acordo com sua vida.
     */
    @Override
    public void updateState() {

        if (!isAlive()) {
            deactivate();
        }
    }

    /**
     * Retorna o identificador do inimigo.
     *
     * @return identificador imutavel
     */
    public int getEnemyId() {
        return enemyId;
    }

    /**
     * Retorna o nome do inimigo.
     *
     * @return nome atual
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna o nivel do inimigo.
     *
     * @return nivel atual
     */
    public int getLevel() {
        return level;
    }

    /**
     * Retorna a vida atual do inimigo.
     *
     * @return quantidade atual de vida
     */
    public float getHealth() {
        return health;
    }
}
