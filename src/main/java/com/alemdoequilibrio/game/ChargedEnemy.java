package com.alemdoequilibrio.game;

/**
 * Representa um inimigo que possui uma carga eletrica.
 *
 * POO 7 - Heranca e sobrescrita:
 * ChargedEnemy especializa Enemy e redefine o comportamento do turno para
 * executar um ataque carregado.
 */
public class ChargedEnemy extends Enemy {

    /*
     * Multiplicador temporario do ataque carregado. A regra pode ser ajustada
     * pelo BattleController quando as interacoes entre cargas forem definidas.
     */
    private static final float CHARGED_DAMAGE_MULTIPLIER = 1.5f;

    /*
     * POO 1.3 - Atributo enum:
     * define a polaridade eletrica deste inimigo.
     */
    private ChargeType chargeType;

    /**
     * Cria um inimigo com uma polaridade eletrica definida.
     *
     * @param enemyId identificador unico do inimigo
     * @param name nome exibido do inimigo
     * @param level nivel usado no calculo do ataque
     * @param health quantidade inicial de vida
     * @param chargeType polaridade eletrica do inimigo
     */
    public ChargedEnemy(
            int enemyId,
            String name,
            int level,
            float health,
            ChargeType chargeType) {

        super(enemyId, name, level, health);

        this.chargeType = requireChargeType(chargeType);
    }

    /**
     * Executa um ataque carregado contra o Hero.
     *
     * A validacao do alvo e as condicoes do turno sao herdadas de Enemy. O
     * comportamento especializado aplica um multiplicador ao dano padrao.
     *
     * @param hero personagem que recebera o ataque
     */
    @Override
    public void performTurn(Hero hero) {

        validateTurnTarget(hero);

        if (!canPerformTurn(hero)) {
            return;
        }

        float chargedDamage =
                calculateTurnDamage() * CHARGED_DAMAGE_MULTIPLIER;

        hero.takeDamage(chargedDamage);
    }

    /**
     * Retorna a polaridade eletrica deste inimigo.
     *
     * @return tipo de carga eletrica
     */
    public ChargeType getChargeType() {
        return chargeType;
    }

    /**
     * Altera a polaridade do inimigo em subclasses que possuem essa mecanica.
     *
     * @param chargeType nova polaridade eletrica
     */
    protected final void changeChargeType(ChargeType chargeType) {
        this.chargeType = requireChargeType(chargeType);
    }

    protected static ChargeType requireNonNeutralCharge(
            ChargeType chargeType) {

        ChargeType validatedCharge = requireChargeType(chargeType);

        if (validatedCharge == ChargeType.NEUTRAL) {
            throw new IllegalArgumentException(
                    "A carga deve ser positiva ou negativa."
            );
        }

        return validatedCharge;
    }

    private static ChargeType requireChargeType(ChargeType chargeType) {
        if (chargeType == null) {
            throw new IllegalArgumentException(
                    "O tipo de carga do inimigo nao pode ser nulo."
            );
        }

        return chargeType;
    }
}
