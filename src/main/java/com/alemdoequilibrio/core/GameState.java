package com.alemdoequilibrio.core;

/**
 * Representa os possíveis estados gerais do jogo.
 *
 * O estado atual permite ao GameController identificar em qual
 * modo a aplicação se encontra e controlar as transições entre
 * menu, exploração, diálogo, batalha, quiz e outros estados.
 */
public enum GameState {
    MENU,
    EXPLORATION,
    DIALOGUE,
    BATTLE,
    QUIZ,
    PAUSED,
    CHAPTER_COMPLETE
}
