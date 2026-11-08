package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa o comerciante Íon.
 *
 * O Merchant mantém as regras do quiz e da recompensa,
 * enquanto a apresentação visual fica no QuizController.
 */
public class Merchant extends NPC
        implements QuizProvider {

    private static final int
            MIN_CORRECT_ANSWERS = 3;

    private final QuestionBank
            questionBank;

    private final KnowledgeTopic
            quizTopic;

    private final String
            rewardAbilityId;

    private final String
            successDialogueId;

    private final String
            failureDialogueId;

    private final KnowledgeTopic[]
            requiredTopics;

    public Merchant(
            String name,
            double x,
            double y,
            double width,
            double height,
            String dialogueId,
            QuestionBank questionBank,
            KnowledgeTopic quizTopic,
            String rewardAbilityId,
            String successDialogueId,
            String failureDialogueId,
            KnowledgeTopic... requiredTopics) {

        super(
                name,
                x,
                y,
                width,
                height,
                dialogueId
        );

        this.questionBank =
                Objects.requireNonNull(
                        questionBank
                );

        this.quizTopic =
                Objects.requireNonNull(
                        quizTopic
                );

        if (rewardAbilityId == null
                || rewardAbilityId.isBlank()) {

            throw new IllegalArgumentException(
                    "O ID da recompensa não pode ser vazio."
            );
        }

        this.rewardAbilityId =
                rewardAbilityId;

        this.successDialogueId =
                successDialogueId;

        this.failureDialogueId =
                failureDialogueId;

        this.requiredTopics = normalizePrerequisites(
                quizTopic, requiredTopics
        );
    }

    /**
     * POO 2.1 - Método com argumentos variados (varargs) e laço FOR.
     * O comerciante pode exigir um ou vários conceitos antes do quiz.
     * O for valida os conceitos e evita requisitos repetidos, que
     * tornariam a liberação de habilidades inconsistentes.
     *
     * POO 2.2 - Método estático: esta normalização independe do estado
     * do comerciante e é utilizada em seu construtor.
     */
    private static KnowledgeTopic[] normalizePrerequisites(
            KnowledgeTopic defaultTopic, KnowledgeTopic... topics) {
        Objects.requireNonNull(defaultTopic, "Conceito do quiz obrigatório");
        if (topics == null || topics.length == 0) {
            return new KnowledgeTopic[] { defaultTopic };
        }

        ArrayList<KnowledgeTopic> uniqueTopics = new ArrayList<>();
        for (KnowledgeTopic topic : topics) {
            Objects.requireNonNull(topic, "Pré-requisito não pode ser nulo");
            if (!uniqueTopics.contains(topic)) {
                uniqueTopics.add(topic);
            }
        }
        return uniqueTopics.toArray(new KnowledgeTopic[0]);
    }

    @Override
    public boolean canStartQuiz(
            ChapterProgress progress) {

        Objects.requireNonNull(
                progress
        );

        if (progress.hasCompletedQuiz(
                quizTopic
        )) {

            return false;
        }

        for (KnowledgeTopic topic
                : requiredTopics) {

            if (!progress.hasLearned(
                    topic
            )) {

                return false;
            }
        }

        return true;
    }

    @Override
    public List<PhysicsQuestion> startQuiz(
            ChapterProgress progress,
            int amount) {

        if (!canStartQuiz(
                progress
        )) {

            return List.of();
        }

        if (amount <= 0) {

            throw new IllegalArgumentException(
                    "A quantidade de questões deve ser positiva."
            );
        }

        return questionBank
                .getQuestionsForQuiz(
                        quizTopic,
                        amount
                );
    }

    /**
     * Finaliza uma tentativa.
     *
     * @return true quando o jogador atingiu a nota mínima
     * e recebeu a habilidade.
     */
    public boolean completeQuiz(
            Hero hero,
            ChapterProgress progress,
            int correctAnswers) {

        Objects.requireNonNull(
                hero
        );

        Objects.requireNonNull(
                progress
        );

        if (correctAnswers
                < MIN_CORRECT_ANSWERS) {

            return false;
        }

        if (progress.hasCompletedQuiz(
                quizTopic
        )) {

            return true;
        }

        Ability reward =
                AbilityRegistry.get(
                        rewardAbilityId
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

        progress.completeQuiz(
                quizTopic
        );

        return true;
    }

    public boolean isQuizCompleted(
            ChapterProgress progress) {

        Objects.requireNonNull(
                progress
        );

        return progress.hasCompletedQuiz(
                quizTopic
        );
    }

    public KnowledgeTopic getQuizTopic() {

        return quizTopic;
    }

    public String getSuccessDialogueId() {

        return successDialogueId;
    }

    public String getFailureDialogueId() {

        return failureDialogueId;
    }

    /*
     * Mantido porque CombatScenarioChecks
     * utiliza este método.
     */
    static Ability createReward(
            KnowledgeTopic topic) {

        return AbilityRegistry.get(
                getRewardId(
                        topic
                )
        );
    }

    private static String getRewardId(
            KnowledgeTopic topic) {

        if (topic == null) {

            throw new IllegalArgumentException(
                    "O tópico não pode ser nulo."
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

            case KNOW_FACTION_CONFLICT ->
                    throw new IllegalArgumentException(
                            "O quiz das facções ainda não possui recompensa definida."
                    );
        };
    }
}
