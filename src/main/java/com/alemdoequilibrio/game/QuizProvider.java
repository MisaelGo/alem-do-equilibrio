package com.alemdoequilibrio.game;

import java.util.List;

/**
 * Interface que define o contrato para entidades
 * capazes de fornecer um quiz ao jogador.
 *
 * POO 8 - Interfaces:
 * o controlador visual pode pedir as questões
 * sem conhecer a implementação concreta do provedor.
 */
public interface QuizProvider {

    /**
     * Informa se o quiz deste provedor pode ser iniciado.
     *
     * @param progress progresso atual do capítulo
     * @return true quando todos os pré-requisitos foram aprendidos
     */
    boolean canStartQuiz(
            ChapterProgress progress);

    /**
     * Monta uma tentativa do quiz.
     *
     * @param progress progresso atual do capítulo
     * @param amount quantidade de questões desejada
     * @return questões sorteadas para a tentativa
     */
    List<PhysicsQuestion> startQuiz(
            ChapterProgress progress,
            int amount);
}
