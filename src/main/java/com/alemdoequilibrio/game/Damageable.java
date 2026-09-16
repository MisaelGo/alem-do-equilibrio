package com.alemdoequilibrio.game;

/**
 * Define o contrato dos personagens que podem receber dano.
 *
 * POO 8 - Interface:
 * permite que Hero e Enemy sejam tratados pelo sistema de combate
 * como alvos de dano, sem depender de uma classe concreta.
 */
public interface Damageable {

    /**
     * Aplica dano ao personagem.
     *
     * @param damage quantidade de dano recebida
     */
    void takeDamage(float damage);

    /**
     * Informa se o personagem ainda possui vida.
     *
     * @return true quando o personagem estiver vivo
     */
    boolean isAlive();
}
