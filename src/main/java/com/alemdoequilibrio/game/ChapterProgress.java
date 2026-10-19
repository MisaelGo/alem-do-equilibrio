package com.alemdoequilibrio.game;

import java.util.EnumSet;
import java.util.Set;

public class ChapterProgress {

    private final EnumSet<KnowledgeTopic>
            learnedTopics;

    private final EnumSet<KnowledgeTopic>
            completedQuizzes;

    private ExplorationZone checkpoint;

    private boolean bossDefeated;

    private boolean fragmentObtained;

    public ChapterProgress() {

        learnedTopics =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );

        completedQuizzes =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );

        checkpoint =
                ExplorationZone
                        .NEUTRAL_BORDER;

        bossDefeated = false;

        fragmentObtained = false;
    }

    public void learn(
            KnowledgeTopic topic) {

        if (topic == null) {

            throw new IllegalArgumentException(
                    "O topico nao pode ser nulo."
            );
        }

        learnedTopics.add(topic);
    }

    public boolean hasLearned(
            KnowledgeTopic topic) {

        return learnedTopics
                .contains(topic);
    }

    public Set<KnowledgeTopic>
            getLearnedTopics() {

        return Set.copyOf(
                learnedTopics
        );
    }

    public void completeQuiz(
            KnowledgeTopic topic) {

        if (topic == null) {

            throw new IllegalArgumentException(
                    "O topico do quiz nao pode ser nulo."
            );
        }

        completedQuizzes.add(topic);
    }

    public boolean hasCompletedQuiz(
            KnowledgeTopic topic) {

        return completedQuizzes
                .contains(topic);
    }

    public Set<KnowledgeTopic>
            getCompletedQuizzes() {

        return Set.copyOf(
                completedQuizzes
        );
    }

    public boolean reachCheckpoint(
            ExplorationZone zone) {

        if (zone == null) {

            throw new IllegalArgumentException(
                    "O checkpoint nao pode ser nulo."
            );
        }

        if (checkpoint == zone) {

            return false;
        }

        checkpoint = zone;

        return true;
    }

    public ExplorationZone
            getCheckpoint() {

        return checkpoint;
    }

    public void markBossDefeated() {

        bossDefeated = true;
    }

    public boolean isBossDefeated() {

        return bossDefeated;
    }

    public void markFragmentObtained() {

        fragmentObtained = true;
    }

    public boolean hasFragmentObtained() {

        return fragmentObtained;
    }

    /**
     * Restaura o progresso validado pelo SaveManager.
     */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained) {

        if (checkpoint == null
                || learnedTopics == null
                || completedQuizzes == null) {

            throw new IllegalArgumentException(
                    "Os dados restaurados nao podem ser nulos."
            );
        }

        this.learnedTopics.clear();

        this.learnedTopics.addAll(
                learnedTopics
        );

        this.completedQuizzes.clear();

        this.completedQuizzes.addAll(
                completedQuizzes
        );

        this.checkpoint =
                checkpoint;

        this.bossDefeated =
                bossDefeated;

        this.fragmentObtained =
                fragmentObtained;
    }
}