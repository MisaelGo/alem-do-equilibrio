package com.alemdoequilibrio.game;

/**
 * Representa um personagem não controlável.
 */
public class NPC extends GameCharacter
        implements Interactable {

    private final String name;

    private final String dialogueId;

    private final double x;
    private final double y;

    private final double width;
    private final double height;

    private final KnowledgeTopic
            knowledgeTopic;

    public NPC(
            String name,
            double x,
            double y,
            double width,
            double height,
            String dialogueId) {

        this(
                name,
                x,
                y,
                width,
                height,
                null,
                dialogueId
        );
    }

    public NPC(
            String name,
            double x,
            double y,
            double width,
            double height,
            KnowledgeTopic knowledgeTopic,
            String dialogueId) {

        this.name = name;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.knowledgeTopic =
                knowledgeTopic;

        this.dialogueId =
                dialogueId;
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
    public String getDialogueId() {
        return dialogueId;
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