package com.alemdoequilibrio.game;

/**
 * Regiões de exploração disponíveis nesta etapa.
 */
public enum ExplorationZone {

    NEUTRAL_BORDER,
    INSULATOR_TRAIL,
    FACTION_CROSSROADS;

    public ExplorationZone next() {

        return switch (this) {

            case NEUTRAL_BORDER ->
                INSULATOR_TRAIL;

            case INSULATOR_TRAIL ->
                FACTION_CROSSROADS;

            case FACTION_CROSSROADS ->
                null;
        };
    }
}