package com.alemdoequilibrio.game;

public enum ExplorationZone {

    NEUTRAL_BORDER,
    INSULATOR_TRAIL,
    FACTION_CROSSROADS,
    COULOMB_VALLEY,
    FIELD_PLATEAU,
    EQUIPOTENTIAL_RUINS,
    FRAGMENT_CHAMBER;

    public ExplorationZone next() {

        return switch (this) {

            case NEUTRAL_BORDER ->
                INSULATOR_TRAIL;

            case INSULATOR_TRAIL ->
                FACTION_CROSSROADS;

            case FACTION_CROSSROADS ->
                COULOMB_VALLEY;

            case COULOMB_VALLEY ->
                FIELD_PLATEAU;

            case FIELD_PLATEAU ->
                EQUIPOTENTIAL_RUINS;

            case EQUIPOTENTIAL_RUINS ->
                FRAGMENT_CHAMBER;

            case FRAGMENT_CHAMBER ->
                null;
        };
    }
}