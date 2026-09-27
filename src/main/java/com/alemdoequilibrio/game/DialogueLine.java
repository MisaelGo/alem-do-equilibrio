package com.alemdoequilibrio.game;

/**
 * Representa uma fala individual de um diálogo.
 */
public class DialogueLine {

    private final String speaker;
    private final String text;

    public DialogueLine(
            String speaker,
            String text) {

        if (speaker == null
                || speaker.isBlank()) {

            throw new IllegalArgumentException(
                    "O nome do personagem não pode ser vazio."
            );
        }

        if (text == null
                || text.isBlank()) {

            throw new IllegalArgumentException(
                    "O texto do diálogo não pode ser vazio."
            );
        }

        this.speaker = speaker;
        this.text = text;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getText() {
        return text;
    }
}