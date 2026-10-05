package com.alemdoequilibrio.core;

import com.alemdoequilibrio.game.BattleController;
import com.alemdoequilibrio.game.BattleView;
import com.alemdoequilibrio.game.ChapterProgress;
import com.alemdoequilibrio.game.Enemy;
import com.alemdoequilibrio.game.ExplorationController;

import javafx.stage.Stage;

public class GameController {

    private final SceneRouter
            sceneRouter;

    private final ResourceManager
            resourceManager;

    private final ChapterProgress
            chapterProgress;

    private GameState currentState;

    private final ExplorationController
            explorationController;

    private BattleController
            currentBattleController;

    private BattleView
            currentBattleView;

    public GameController(
            Stage stage) {

        sceneRouter =
                new SceneRouter(
                        stage
                );

        resourceManager =
                new ResourceManager();

        chapterProgress =
                new ChapterProgress();

        currentState =
                GameState.MENU;

        explorationController =
                new ExplorationController(
                        resourceManager,
                        chapterProgress,

                        () -> changeState(
                                GameState.DIALOGUE
                        ),

                        () -> changeState(
                                GameState.EXPLORATION
                        ),

                        this::startBattle
                );
    }

    public void startGame() {

        sceneRouter.setTitle(
                "Além do Equilíbrio"
        );

        changeState(
                GameState.EXPLORATION
        );
    }

    public void changeState(
            GameState newState) {

        if (newState == null) {

            throw new IllegalArgumentException(
                    "O novo estado não pode ser nulo."
            );
        }

        if (currentState == newState) {
            return;
        }

        leaveState(
                currentState
        );

        currentState =
                newState;

        enterState(
                newState
        );
    }

    private void leaveState(
            GameState state) {

        switch (state) {

            case EXPLORATION ->
                explorationController
                        .stop();

            case DIALOGUE ->
                explorationController
                        .closeDialogue();

            case MENU,
                 BATTLE,
                 QUIZ,
                 PAUSED,
                 CHAPTER_COMPLETE -> {
            }
        }
    }

    private void enterState(
            GameState state) {

        switch (state) {

            case EXPLORATION ->
                startExploration();

            case BATTLE ->
                showBattle();

            case MENU,
                 DIALOGUE,
                 QUIZ,
                 PAUSED,
                 CHAPTER_COMPLETE -> {
            }
        }
    }

    private void startExploration() {

        sceneRouter.show(
                explorationController
                        .getScene()
        );

        explorationController
                .start();
    }

    private void startBattle(
            Enemy enemy) {

        if (enemy == null
                || currentState
                != GameState.EXPLORATION) {

            return;
        }

        currentBattleController =
                new BattleController(
                        explorationController
                                .getHero(),

                        enemy,

                        this::finishBattle
                );

        currentBattleView =
                new BattleView(
                        currentBattleController
                );

        changeState(
                GameState.BATTLE
        );
    }

    private void showBattle() {

        if (currentBattleView == null) {

            throw new IllegalStateException(
                    "Não existe batalha ativa."
            );
        }

        sceneRouter.show(
                currentBattleView
                        .getScene()
        );
    }

    private void finishBattle() {

        if (currentBattleController
                == null) {

            return;
        }

        BattleController.BattleResult result =
                currentBattleController
                        .getResult();

        if (result
                == BattleController
                        .BattleResult.VICTORY) {

            explorationController
                    .removeDefeatedEnemy(
                            currentBattleController
                                    .getEnemy()
                    );

            currentBattleController =
                    null;

            currentBattleView =
                    null;

            changeState(
                    GameState.EXPLORATION
            );

            return;
        }

        if (result
                == BattleController
                        .BattleResult.DEFEAT) {

            System.out.println(
                    "O Herói foi derrotado."
            );

            /*
             * Fluxo de derrota/checkpoint
             * será integrado depois.
             */
        }
    }

    public GameState getCurrentState() {

        return currentState;
    }

    public BattleController
            getCurrentBattleController() {

        return currentBattleController;
    }
}