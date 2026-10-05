package com.alemdoequilibrio.game;

import java.util.Set;

/**
 * Interface que define o contrato para entidades capazes de fornecer um quiz ao jogador.
 * 
 * POO 8 - Interfaces
 * Justificativa: Define um contrato claro para a aplicação de testes.
 */
public interface QuizProvider {
    
    /**
     * Inicia a sequência do quiz, testando o conhecimento e recompensando o jogador.
     * 
     * @param hero O herói (jogador) que será submetido à prova.
     * @param progress O registro de tópicos já descobertos (ChapterProgress).
     * @param unlockedAbilities O conjunto de habilidades que o jogador já possui.
     */
    void startQuiz(Hero hero, ChapterProgress progress, Set<String> unlockedAbilities);
    
}