package com.alemdoequilibrio.game;

/**
 * Representa um objeto interativo do cenário.
 */
public class WorldObject
        implements Interactable {

    private final String name;

    private final String dialogueId;

    private final double x;
    private final double y;

    private final double width;
    private final double height;

    public WorldObject(
            String name,
            double x,
            double y,
            double width,
            double height,
            String dialogueId) {

        this.name = name;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.dialogueId =
                dialogueId;
    }

    @Override
    public String getInteractionName() {
        return name;
    }

    @Override
    public String getDialogueId() {
        return dialogueId;
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