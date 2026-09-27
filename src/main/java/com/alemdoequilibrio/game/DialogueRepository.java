package com.alemdoequilibrio.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carrega e disponibiliza os diálogos do jogo.
 *
 * O conteúdo fica separado da lógica para que
 * os textos possam ser alterados sem modificar
 * as classes de exploração.
 */
public class DialogueRepository {

    private static final String DIALOGUE_FILE =
            "/dialogues/dialogues.csv";

    private final Map<String, List<DialogueLine>>
            dialogues;

    public DialogueRepository() {

        this.dialogues =
                new HashMap<>();

        loadDialogues();
    }

    private void loadDialogues() {

        InputStream input =
                DialogueRepository.class
                        .getResourceAsStream(
                                DIALOGUE_FILE
                        );

        if (input == null) {

            throw new IllegalStateException(
                    "Arquivo de diálogos não encontrado: "
                    + DIALOGUE_FILE
            );
        }

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            boolean firstLine = true;

            while ((line = reader.readLine())
                    != null) {

                if (firstLine) {

                    firstLine = false;

                    continue;
                }

                if (line.isBlank()
                        || line.startsWith("#")) {

                    continue;
                }

                /*
                 * Limite 3 permite usar ";" normalmente
                 * dentro do texto da fala.
                 */
                String[] data =
                        line.split(";", 3);

                if (data.length != 3) {

                    throw new IllegalStateException(
                            "Linha de diálogo inválida: "
                            + line
                    );
                }

                String dialogueId =
                        data[0].trim();

                String speaker =
                        data[1].trim();

                String text =
                        data[2].trim();

                dialogues
                        .computeIfAbsent(
                                dialogueId,
                                key ->
                                        new ArrayList<>()
                        )
                        .add(
                                new DialogueLine(
                                        speaker,
                                        text
                                )
                        );
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Erro ao carregar diálogos.",
                    exception
            );
        }
    }

    public List<DialogueLine> getDialogue(
            String dialogueId) {

        List<DialogueLine> dialogue =
                dialogues.get(
                        dialogueId
                );

        if (dialogue == null) {

            throw new IllegalArgumentException(
                    "Diálogo não encontrado: "
                    + dialogueId
            );
        }

        return List.copyOf(
                dialogue
        );
    }
}