package com.alemdoequilibrio.game;

/**
 * Representa a base comum dos personagens do jogo.
 *
 * POO 10 - Classe abstrata:
 * concentra o estado de atividade compartilhado pelos personagens
 * e exige que cada personagem atualize seu proprio estado.
 */
public abstract class GameCharacter {

    /*
     * Indica se o personagem esta ativo no fluxo atual do jogo.
     */
    private boolean active;

    /**
     * Cria um personagem inicialmente ativo.
     */
    protected GameCharacter() {
        this.active = true;
    }

    /**
     * Informa se o personagem esta ativo.
     *
     * @return true quando o personagem estiver ativo
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Ativa o personagem no fluxo do jogo.
     */
    public void activate() {
        active = true;
    }

    /**
     * Desativa o personagem no fluxo do jogo.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Atualiza o estado especifico do personagem.
     *
     * Cada subclasse deve implementar este comportamento
     * de acordo com suas proprias regras.
     */
    public abstract void updateState();
}
