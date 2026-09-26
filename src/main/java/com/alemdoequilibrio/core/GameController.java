package com.alemdoequilibrio.core;

import com.alemdoequilibrio.game.ExplorationController;

import javafx.stage.Stage;

/**
 * Controla o fluxo geral do jogo.
 *
 * Coordena estados e controladores sem implementar
 * diretamente as regras internas de exploração,
 * diálogo, combate ou quiz.
 */
public class GameController {

    private final SceneRouter sceneRouter;

    private final ResourceManager resourceManager;

    /*
     * POO 1.3 - Enum:
     * utiliza GameState para restringir os estados
     * possíveis da aplicação.
     */
    private GameState currentState;

    private final ExplorationController
            explorationController;

    public GameController(Stage stage) {

        this.sceneRouter =
                new SceneRouter(
                        stage
                );

        this.resourceManager =
                new ResourceManager();

        this.currentState =
                GameState.MENU;

        this.explorationController =
                new ExplorationController(

                        resourceManager,

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

    /**
     * Realiza a transição entre os estados globais.
     *
     * Antes de iniciar o novo estado, encerra o sistema
     * incompatível que estava ativo anteriormente.
     */
    public void changeState(
            GameState newState) {

        if (newState == null) {

            throw new IllegalArgumentException(
                    "O novo estado "
                    + "não pode ser nulo."
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
                // Sistemas ainda não implementados.
            }
        }
    }

    private void enterState(
            GameState state) {

        switch (state) {

            case MENU -> {
                // Futuro menu principal.
            }

            case EXPLORATION -> {
                startExploration();
            }

            case DIALOGUE -> {
                /*
                 * A própria cena de exploração permanece
                 * visível, mas o game loop está parado.
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