package com.alemdoequilibrio.game;

/**
 * Catálogo central das habilidades do Capítulo 1.
 *
 * As habilidades atuais não possuem estado interno mutável,
 * portanto podem ser compartilhadas pelo registro.
 */
public final class AbilityRegistry {

    public static final String
            REPULSION_PULSE_ID =
            "REPULSION_PULSE";

    public static final String
            FIELD_IMPULSE_ID =
            "FIELD_IMPULSE";

    public static final String
            EQUIPOTENTIAL_SHIELD_ID =
            "EQUIPOTENTIAL_SHIELD";

    /*
     * C1-043 - Uso concreto do generic.
     *
     * GameRegistry<T> é utilizado como
     * GameRegistry<Ability> em um fluxo real do jogo.
     */
    private static final GameRegistry<Ability>
            REGISTRY =
            createRegistry();

    private AbilityRegistry() {
    }

    private static GameRegistry<Ability>
            createRegistry() {

        GameRegistry<Ability> registry =
                new GameRegistry<>();

        registry.register(
                REPULSION_PULSE_ID,
                new RepulsionPulse()
        );

        registry.register(
                FIELD_IMPULSE_ID,
                new FieldImpulse()
        );

        registry.register(
                EQUIPOTENTIAL_SHIELD_ID,
                new EquipotentialShield()
        );

        return registry;
    }

    public static Ability get(
            String id) {

        return REGISTRY.get(id);
    }

    public static boolean contains(
            String id) {

        return REGISTRY.contains(id);
    }

    /**
     * Retorna o identificador persistente de uma habilidade.
     */
    public static String getId(
            Ability ability) {

        if (ability == null) {
            throw new IllegalArgumentException(
                    "A habilidade nao pode ser nula."
            );
        }

        if (ability
                instanceof RepulsionPulse) {

            return REPULSION_PULSE_ID;
        }

        if (ability
                instanceof FieldImpulse) {

            return FIELD_IMPULSE_ID;
        }

        if (ability
                instanceof EquipotentialShield) {

            return EQUIPOTENTIAL_SHIELD_ID;
        }

        throw new IllegalArgumentException(
                "Habilidade nao registrada: "
                + ability.getName()
        );
    }
}