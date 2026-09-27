package com.alemdoequilibrio.core;

import com.alemdoequilibrio.game.ChapterProgress;
import com.alemdoequilibrio.game.ExplorationController;

import javafx.stage.Stage;

/**
 * Controla o fluxo geral do jogo.
 */
public class GameController {

    private final SceneRouter sceneRouter;

    private final ResourceManager resourceManager;

    private final ChapterProgress chapterProgress;

    /*
     * POO 1.3 - Enum:
     * restringe os estados possíveis do jogo.
     */
    private GameState currentState;

    private final ExplorationController
            explorationController;

    public GameController(Stage stage) {

        this.sceneRouter =
                new SceneRouter(stage);

        this.resourceManager =
                new ResourceManager();

        this.chapterProgress =
                new ChapterProgress();

        this.currentState =
                GameState.MENU;

        this.explorationController =
                new ExplorationController(

                        resourceManager,

                        chapterProgress,

                        () -> changeState(
                                GameState.DIALOGUE
                        ),

                        () -> changeState(
                                GameState.EXPLORATION
                        )
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

            case EXPLORATION -> {

                explorationController.stop();
            }

            case DIALOGUE -> {

                explorationController
                        .closeDialogue();
            }

            case MENU,
                 BATTLE,
                 QUIZ,
                 PAUSED,
                 CHAPTER_COMPLETE -> {

                // Sistemas ainda serão integrados.
            }
        }
    }

    private void enterState(
            GameState state) {

        switch (state) {

            case MENU -> {

                // Futuro menu.
            }

            case EXPLORATION -> {

                startExploration();
            }

            case DIALOGUE -> {

                /*
                 * A cena de exploração continua
                 * visível, mas o loop é pausado.
                 */
            }

            case BATTLE -> {

                // Futuro BattleController.
            }

            case QUIZ -> {

                // Futuro sistema de quiz.
            }

            case PAUSED -> {

                // Futuro menu de pausa.
            }

            case CHAPTER_COMPLETE -> {

                // Futuro encerramento do capítulo.
            }
        }
    }

    private void startExploration() {

        sceneRouter.show(
                explorationController
                        .getScene()
        );

        explorationController.start();
    }

    public GameState getCurrentState() {

        return currentState;
    }
}