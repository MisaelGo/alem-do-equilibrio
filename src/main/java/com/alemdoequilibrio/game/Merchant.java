package com.alemdoequilibrio.game;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Set;

/**
 * Classe concreta que representa
 * o comerciante Íon no mundo do jogo.
 *
 * POO 7.3 - Subtipagem:
 * Merchant especializa NPC e implementa QuizProvider.
 */
public class Merchant extends NPC
        implements QuizProvider {

    /*
     * POO 5 - Associacao / Agregacao.
     */
    private QuestionBank questionBank;

    public Merchant(
            String name,
            double x,
            double y,
            double width,
            double height,
            String dialogueId,
            QuestionBank questionBank) {

        super(
                name,
                x,
                y,
                width,
                height,
                dialogueId
        );

        this.questionBank =
                questionBank;
    }

    @Override
    public void startQuiz(
            Hero hero,
            ChapterProgress progress,
            Set<String> unlockedAbilities) {

        KnowledgeTopic currentTopic =
                determineAvailableQuizTopic(
                        progress,
                        unlockedAbilities
                );

        if (currentTopic == null) {

            JOptionPane.showMessageDialog(
                    null,
                    "Íon: Volte quando tiver "
                    + "explorado as ruínas "
                    + "e aprendido algo novo.",
                    "Íon, o Comerciante",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        ArrayList<PhysicsQuestion>
                quizQuestions =
                questionBank
                        .getQuestionsForQuiz(
                                currentTopic,
                                4
                        );

        if (quizQuestions.size()
                < 4) {

            System.err.println(
                    "Erro: Questoes insuficientes "
                    + "para a prova de "
                    + currentTopic.name()
            );

            return;
        }

        int correctAnswers = 0;

        JOptionPane.showMessageDialog(
                null,
                "Íon: Muito bem! Vamos ver "
                + "se o que você aprendeu "
                + "se sustenta na prática.",
                "Prova de Física",
                JOptionPane.INFORMATION_MESSAGE
        );

        for (PhysicsQuestion question
                : quizQuestions) {

            int choice =
                    JOptionPane.showOptionDialog(
                            null,
                            question.getPrompt(),
                            "Teste: "
                            + question
                                    .getTopic()
                                    .name(),
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            question.getOptions(),
                            question.getOptions()[0]
                    );

            if (choice
                    == JOptionPane.CLOSED_OPTION) {

                JOptionPane.showMessageDialog(
                        null,
                        "Íon: Evitando a pergunta? "
                        + "A física não espera "
                        + "por ninguém.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            String feedback =
                    question
                            .getFeedbackForOption(
                                    choice
                            );

            if (question
                    .isCorrect(choice)) {

                correctAnswers++;

                JOptionPane.showMessageDialog(
                        null,
                        "Correto!\n\n"
                        + feedback,
                        "Íon - Resultado",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        null,
                        "Incorreto.\n\n"
                        + feedback,
                        "Íon - Resultado",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        deliverReward(
                hero,
                progress,
                unlockedAbilities,
                currentTopic,
                correctAnswers
        );
    }

    private KnowledgeTopic
            determineAvailableQuizTopic(
                    ChapterProgress progress,
                    Set<String> unlockedAbilities) {

        if (progress.hasLearned(
                    KnowledgeTopic.KNOW_POTENTIAL
                )
                && !progress.hasCompletedQuiz(
                        KnowledgeTopic.KNOW_POTENTIAL
                )
                && !unlockedAbilities.contains(
                        "Escudo Equipotencial"
                )) {

            return KnowledgeTopic
                    .KNOW_POTENTIAL;
        }

        if (progress.hasLearned(
                    KnowledgeTopic.KNOW_FIELD
                )
                && !progress.hasCompletedQuiz(
                        KnowledgeTopic.KNOW_FIELD
                )
                && !unlockedAbilities.contains(
                        "Impulso de Campo"
                )) {

            return KnowledgeTopic
                    .KNOW_FIELD;
        }

        if (progress.hasLearned(
                    KnowledgeTopic
                            .KNOW_CHARGE_SIGNS
                )
                && !progress.hasCompletedQuiz(
                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS
                )
                && !unlockedAbilities.contains(
                        "Pulso de Repulsão"
                )) {

            return KnowledgeTopic
                    .KNOW_CHARGE_SIGNS;
        }

        return null;
    }

    private void deliverReward(
            Hero hero,
            ChapterProgress progress,
            Set<String> unlockedAbilities,
            KnowledgeTopic topic,
            int correctAnswers) {

        if (correctAnswers >= 3) {

            String rewardMsg =
                    "Brilhante! O seu entendimento "
                    + "compra esta técnica:\n";

            /*
             * C1-043:
             * a recompensa vem do registro
             * de habilidades do jogo.
             */
            Ability reward =
                    AbilityRegistry.get(
                            getRewardId(topic)
                    );

            boolean equipNow =
                    hero.getAbilityBook()
                            .getEquippedAbility()
                    == null;

            if (equipNow) {

                hero.learnAbility(
                        reward,
                        true
                );

            } else {

                hero.learnAbility(
                        reward
                );
            }

            unlockedAbilities.add(
                    reward.getName()
            );

            progress.completeQuiz(
                    topic
            );

            rewardMsg +=
                    "-> Nova Habilidade Adquirida: "
                    + reward.getName()
                    + "!";

            JOptionPane.showMessageDialog(
                    null,
                    rewardMsg,
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    null,
                    "Íon: Você acertou "
                    + correctAnswers
                    + " de 4.\n"
                    + "As faíscas ainda confundem "
                    + "a sua mente. "
                    + "Tente novamente mais tarde.",
                    "Falha",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String getRewardId(
            KnowledgeTopic topic) {

        if (topic == null) {

            throw new IllegalArgumentException(
                    "O topico nao pode ser nulo."
            );
        }

        return switch (topic) {

            case KNOW_CHARGE_SIGNS,
                 KNOW_ELECTRIZATION ->
                    AbilityRegistry
                            .REPULSION_PULSE_ID;

            case KNOW_FIELD,
                 KNOW_COULOMB ->
                    AbilityRegistry
                            .FIELD_IMPULSE_ID;

            case KNOW_POTENTIAL ->
                    AbilityRegistry
                            .EQUIPOTENTIAL_SHIELD_ID;
        };
    }

    /*
     * Mantido porque CombatScenarioChecks
     * ainda utiliza este metodo.
     */
    static Ability createReward(
            KnowledgeTopic topic) {

        return AbilityRegistry.get(
                getRewardId(topic)
        );
    }
}