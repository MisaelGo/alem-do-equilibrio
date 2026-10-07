package com.alemdoequilibrio.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda as habilidades aprendidas e a habilidade equipada pelo Hero.
 */
public class AbilityBook {

    /*
     * POO - ArrayList:
     * a quantidade de habilidades aprendidas pode crescer durante o jogo.
     */
    private final ArrayList<Ability> abilities;
    private Ability equippedAbility;

    public AbilityBook() {
        abilities = new ArrayList<>();
    }

    /**
     * Adiciona uma habilidade ainda nao aprendida.
     *
     * @param ability habilidade a aprender
     */
    public void addAbility(Ability ability) {
        if (ability == null) {
            throw new IllegalArgumentException(
                    "A habilidade nao pode ser nula."
            );
        }

        for (Ability learnedAbility : abilities) {
            if (learnedAbility.getName().equals(ability.getName())) {
                throw new IllegalArgumentException(
                        "O Hero ja aprendeu esta habilidade."
                );
            }
        }

        abilities.add(ability);
    }

    /**
     * Equipa uma habilidade que ja pertence a este livro.
     *
     * @param ability habilidade aprendida a equipar
     */
    public void equipAbility(Ability ability) {
        if (!abilities.contains(ability)) {
            throw new IllegalArgumentException(
                    "O Hero precisa aprender a habilidade antes de equipa-la."
            );
        }

        equippedAbility = ability;
    }

    /**
     * Retorna uma lista que nao permite alterar o conteudo do livro.
     *
     * @return habilidades aprendidas
     */
    public List<Ability> getAbilities() {
        return List.copyOf(abilities);
    }

    /**
     * @return habilidade equipada ou null se nenhuma estiver equipada
     */
    public Ability getEquippedAbility() {
        return equippedAbility;
    }
}
