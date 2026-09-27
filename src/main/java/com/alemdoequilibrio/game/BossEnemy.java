package com.alemdoequilibrio.game;

/**
 * Representa um inimigo chefe do jogo.
 *
 * POO 7 - Heranca:
 * BossEnemy especializa Enemy e reutiliza seu estado e seu comportamento de
 * combate. As mecanicas exclusivas do chefe poderao ser adicionadas quando as
 * regras do BattleController forem definidas.
 */
public class BossEnemy extends Enemy {

    /**
     * Cria um chefe com identidade, nome, nivel e vida definidos.
     *
     * @param enemyId identificador unico do chefe
     * @param name nome exibido do chefe
     * @param level nivel usado no calculo do ataque
     * @param health quantidade inicial de vida
     */
    public BossEnemy(int enemyId, String name, int level, float health) {
        super(enemyId, name, level, health);
    }
}
