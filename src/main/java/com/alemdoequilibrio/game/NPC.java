package com.alemdoequilibrio.game;

import java.util.Arrays;
import java.util.List;

/**
 * Representa um personagem não controlável.
 */
public class NPC extends GameCharacter
        implements Interactable {

    private final String name;

    private final List<String> dialogues;

    private final double x;
    private final double y;

    private final double width;
    private final double height;

    private final KnowledgeTopic knowledgeTopic;

    public NPC(
            String name,
            double x,
            double y,
            double width,
            double height,
            String... dialogues) {

        this(
                name,
                x,
                y,
                width,
                height,
                null,
                dialogues
        );
    }

    public NPC(
            String name,
            double x,
            double y,
            double width,
            double height,
            KnowledgeTopic knowledgeTopic,
            String... dialogues) {

        this.name = name;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.knowledgeTopic =
                knowledgeTopic;

        this.dialogues =
                List.copyOf(
                        Arrays.asList(
                                dialogues
                        )
                );
    }

    @Override
    public void updateState() {
        // NPC estático nesta etapa.
    }

    @Override
    public String getInteractionName() {
        return name;
    }

    @Override
    public List<String> getInteractionTexts() {
        return dialogues;
    }

    public String getName() {
        return name;
    }

    public KnowledgeTopic getKnowledgeTopic() {
        return knowledgeTopic;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }
}