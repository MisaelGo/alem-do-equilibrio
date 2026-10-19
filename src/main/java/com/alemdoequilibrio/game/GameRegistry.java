package com.alemdoequilibrio.game;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro genérico de elementos do jogo.
 *
 * POO 7.4 - Polimorfismo paramétrico:
 * a classe pode armazenar diferentes tipos por meio do parâmetro T.
 *
 * No Capítulo 1, ela é utilizada concretamente como
 * GameRegistry<Ability>.
 *
 * @param <T> tipo de elemento armazenado
 */
public class GameRegistry<T> {

    private final Map<String, T> entries;

    public GameRegistry() {
        entries = new LinkedHashMap<>();
    }

    public void register(
            String id,
            T value) {

        String validId =
                validateId(id);

        if (value == null) {
            throw new IllegalArgumentException(
                    "O valor registrado nao pode ser nulo."
            );
        }

        if (entries.containsKey(validId)) {
            throw new IllegalArgumentException(
                    "Ja existe um elemento registrado com o ID: "
                    + validId
            );
        }

        entries.put(
                validId,
                value
        );
    }

    public T get(
            String id) {

        String validId =
                validateId(id);

        T value =
                entries.get(validId);

        if (value == null) {
            throw new IllegalArgumentException(
                    "Nenhum elemento registrado com o ID: "
                    + validId
            );
        }

        return value;
    }

    public boolean contains(
            String id) {

        if (id == null
                || id.isBlank()) {

            return false;
        }

        return entries.containsKey(
                id.trim()
        );
    }

    public List<T> getAll() {

        return List.copyOf(
                entries.values()
        );
    }

    public int size() {

        return entries.size();
    }

    private String validateId(
            String id) {

        if (id == null
                || id.isBlank()) {

            throw new IllegalArgumentException(
                    "O ID nao pode ser nulo ou vazio."
            );
        }

        return id.trim();
    }
}