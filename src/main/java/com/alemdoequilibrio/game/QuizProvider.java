package com.alemdoequilibrio.game;

/**
 * Interface que define o contrato para entidades capazes de fornecer um quiz ao herói.
 * 
 * POO 8 - Interfaces
 * Justificativa: Define um contrato claro e permite polimorfismo. Qualquer 
 * entidade do jogo (NPC, objeto de cenário, terminais) pode implementar esta 
 * interface para aplicar testes de conhecimento sem que o GameController precise 
 * saber a classe exata do objeto. A classe Merchant é uma das implementações.
 */
public interface QuizProvider {
    
    /**
     * Inicia a sequência do quiz, testando o conhecimento do herói e 
     * possivelmente recompensando-o com novas habilidades.
     * 
     * @param hero O herói (jogador) que será submetido à prova.
     */
    void startQuiz(Hero hero);
    
}