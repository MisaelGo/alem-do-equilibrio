package com.alemdoequilibrio.game;

import java.util.EnumSet;
import java.util.Set;

/**
 * Armazena o progresso pedagógico do jogador no capítulo.
 */
public class ChapterProgress {

    private final EnumSet<KnowledgeTopic> learnedTopics;

    public ChapterProgress() {

        this.learnedTopics =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );
    }

    public void learn(
            KnowledgeTopic topic) {

        if (topic == null) {
            throw new IllegalArgumentException(
                    "O tópico não pode ser nulo."
            );
        }

        learnedTopics.add(topic);
    }

    public boolean hasLearned(
            KnowledgeTopic topic) {

        return learnedTopics.contains(topic);
    }

    public Set<KnowledgeTopic> getLearnedTopics() {

        return Set.copyOf(
                learnedTopics
        );
    }
}