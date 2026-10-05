package com.alemdoequilibrio.game;

import java.util.EnumSet;
import java.util.Set;

public class ChapterProgress {

    private final EnumSet<KnowledgeTopic> learnedTopics;

    private ExplorationZone checkpoint;

    public ChapterProgress() {

        this.learnedTopics =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );

        this.checkpoint =
                ExplorationZone.NEUTRAL_BORDER;
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

    public boolean reachCheckpoint(
            ExplorationZone zone) {

        if (zone == null) {

            throw new IllegalArgumentException(
                    "O checkpoint não pode ser nulo."
            );
        }

        if (checkpoint == zone) {
            return false;
        }

        checkpoint = zone;

        return true;
    }

    public ExplorationZone getCheckpoint() {

        return checkpoint;
    }
}