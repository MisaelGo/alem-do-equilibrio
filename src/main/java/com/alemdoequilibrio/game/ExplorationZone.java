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

    public ExplorationZone previous() {

        return switch (this) {

            case NEUTRAL_BORDER ->
                null;

            case INSULATOR_TRAIL ->
                NEUTRAL_BORDER;

            case FACTION_CROSSROADS ->
                INSULATOR_TRAIL;

            case COULOMB_VALLEY ->
                FACTION_CROSSROADS;

            case FIELD_PLATEAU ->
                COULOMB_VALLEY;

            case EQUIPOTENTIAL_RUINS ->
                FIELD_PLATEAU;

            case FRAGMENT_CHAMBER ->
                EQUIPOTENTIAL_RUINS;
        };
    }
}