package com.alemdoequilibrio.game;

/**
 * Representa um elemento do cenário que pode ser examinado
 * pelo jogador, como casas, árvores e outras estruturas.
 */
public final class SceneryInteractable
        implements Interactable {

    private final String interactionName;

    private final String dialogueId;

    private final double x;

    private final double y;

    private final double width;

    private final double height;

    public SceneryInteractable(
            String interactionName,
            String dialogueId,
            double x,
            double y,
            double width,
            double height) {

        if (interactionName == null
                || interactionName.isBlank()) {

            throw new IllegalArgumentException(
                    "O nome de interação não pode ser vazio."
            );
        }

        if (dialogueId == null
                || dialogueId.isBlank()) {

            throw new IllegalArgumentException(
                    "O ID do diálogo não pode ser vazio."
            );
        }

        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(width)
                || !Double.isFinite(height)
                || width <= 0
                || height <= 0) {

            throw new IllegalArgumentException(
                    "A área interagível é inválida."
            );
        }

        this.interactionName =
                interactionName;

        this.dialogueId =
                dialogueId;

        this.x = x;

        this.y = y;

        this.width = width;

        this.height = height;
    }

    @Override
    public String getInteractionName() {

        return interactionName;
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
