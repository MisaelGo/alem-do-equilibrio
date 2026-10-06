package com.alemdoequilibrio.game;

import java.util.Objects;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class BattleView {

    private static final double WIDTH =
            1280;

    private static final double HEIGHT =
            720;

    private static final float PLAYER_DAMAGE =
            20.0f;

    private final BattleController
            battleController;

    private final Label heroHealthLabel;

    private final Label enemyHealthLabel;

    private final Label statusLabel;

    private final Button attackButton;
    private final Button abilityButton;

    private final Scene scene;

    public BattleView(
            BattleController battleController) {

        this.battleController =
                Objects.requireNonNull(
                        battleController
                );

        Label title =
                new Label(
                        "BATALHA"
                );

        title.setStyle(
                "-fx-font-size: 36px;"
                + "-fx-font-weight: bold;"
        );

        Label enemyName =
                new Label(
                        battleController
                                .getEnemy()
                                .getName()
                );

        enemyName.setStyle(
                "-fx-font-size: 24px;"
        );

        heroHealthLabel =
                new Label();

        enemyHealthLabel =
                new Label();

        statusLabel =
                new Label(
                        "Seu turno."
                );

        attackButton =
                new Button(
                        "Atacar"
                );

        attackButton.setOnAction(
                event ->
                        performPlayerTurn(false)
        );

        abilityButton =
                new Button();

        abilityButton.setOnAction(
                event ->
                        performPlayerTurn(true)
        );

        VBox root =
                new VBox(
                        20,
                        title,
                        enemyName,
                        heroHealthLabel,
                        enemyHealthLabel,
                        statusLabel,
                        attackButton,
                        abilityButton
                );

        root.setAlignment(
                Pos.CENTER
        );

        scene =
                new Scene(
                        root,
                        WIDTH,
                        HEIGHT
                );

        updateView();
    }

    private void performPlayerTurn(
            boolean useAbility) {

        if (battleController
                .isBattleFinished()) {

            return;
        }

        if (useAbility) {
            battleController
                    .performPlayerAbility();
        } else {
            battleController
                    .performPlayerAttack(
                            PLAYER_DAMAGE
                    );
        }

        if (battleController
                .isBattleFinished()) {

            updateView();

            return;
        }

        statusLabel.setText(
                "O inimigo atacou."
        );

        battleController
                .performEnemyTurn();

        updateView();

        if (!battleController.isBattleFinished()
                && battleController.wasLastEnemyTurnInterrupted()) {
            statusLabel.setText(
                    "O Pulso interrompeu o ataque. Seu turno."
            );
        }
    }

    private void updateView() {

        Hero hero =
                battleController
                        .getHero();

        Enemy enemy =
                battleController
                        .getEnemy();

        Ability equippedAbility =
                hero.getAbilityBook()
                        .getEquippedAbility();

        abilityButton.setText(
                equippedAbility == null
                        ? "Habilidade indisponível"
                        : "Usar " + equippedAbility.getName()
        );

        abilityButton.setDisable(
                equippedAbility == null
                        || battleController.isBattleFinished()
        );

        heroHealthLabel.setText(
                "HP do Herói: "
                + Math.round(
                        hero.getHealth()
                )
        );

        enemyHealthLabel.setText(
                "HP de "
                + enemy.getName()
                + ": "
                + Math.round(
                        enemy.getHealth()
                )
        );

        if (battleController
                .isBattleFinished()) {

            attackButton.setDisable(
                    true
            );

            switch (battleController
                    .getResult()) {

                case VICTORY ->
                    statusLabel.setText(
                            "Vitória!"
                    );

                case DEFEAT ->
                    statusLabel.setText(
                            "Derrota."
                    );

                case IN_PROGRESS -> {
                }
            }

            return;
        }

        statusLabel.setText(
                "Seu turno."
        );
    }

    public Scene getScene() {

        return scene;
    }
}
