package com.alemdoequilibrio.game;

/**
 * Define o contrato dos elementos do mundo
 * com os quais o jogador pode interagir.
 *
 * POO 8 - Interface:
 * diferentes tipos utilizam o mesmo fluxo
 * de interação.
 */
public interface Interactable {

    String getInteractionName();

    String getDialogueId();

    double getX();

    double getY();

    double getWidth();

    double getHeight();
}