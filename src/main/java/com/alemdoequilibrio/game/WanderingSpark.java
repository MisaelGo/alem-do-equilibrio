package com.alemdoequilibrio.game;

/**
 * Inimigo fraco usado para apresentar o funcionamento basico dos turnos.
 *
 * A Faisca Errante herda o ataque simples de Enemy sem altera-lo.
 */
public class WanderingSpark extends Enemy {

    private static final String NAME = "Faisca Errante";
    private static final int LEVEL = 1;
    private static final float INITIAL_HEALTH = 20.0f;

    /**
     * Cria uma Faisca Errante com os atributos basicos do tipo.
     *
     * @param enemyId identificador unico desta instancia
     */
    public WanderingSpark(int enemyId) {
        super(enemyId, NAME, LEVEL, INITIAL_HEALTH);
    }
}
