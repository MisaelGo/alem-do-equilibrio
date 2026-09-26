package com.alemdoequilibrio.game;

import java.util.List;

/**
 * Define o contrato dos elementos do mundo com os quais
 * o jogador pode interagir.
 *
 * POO 8 - Interface:
 * permite que diferentes objetos sejam tratados pelo mesmo
 * sistema de interação sem depender de suas classes concretas.
 */
public interface Interactable {

    String getInteractionName();

    List<String> getInteractionTexts();

    double getX();

    double getY();

    double getWidth();

    double getHeight();
}