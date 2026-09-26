package com.alemdoequilibrio.game;

import java.util.Arrays;
import java.util.List;

/**
 * Representa um objeto simples do cenário que pode ser inspecionado.
 */
public class WorldObject implements Interactable {

    private final String name;
    private final List<String> interactionTexts;

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
            String... interactionTexts) {

        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.interactionTexts =
                List.copyOf(Arrays.asList(interactionTexts));
    }

    @Override
    public String getInteractionName() {
        return name;
    }

    @Override
    public List<String> getInteractionTexts() {
        return interactionTexts;
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