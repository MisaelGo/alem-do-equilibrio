package com.alemdoequilibrio.core;

import com.alemdoequilibrio.game.ExplorationController;
import javafx.stage.Stage;

/**
 * Controla o fluxo geral do jogo.
 *
 * Esta classe coordena os diferentes estados e sistemas da aplicação,
 * delegando responsabilidades específicas para outros controladores.
 *
 * O GameController não deve implementar diretamente regras de exploração,
 * combate, diálogo ou quiz.
 */
public class GameController {

    /*
     * Responsável pela troca das cenas exibidas
     * na janela principal da aplicação.
     */
    private final SceneRouter sceneRouter;

    /*
     * Responsável por carregar e reutilizar Image, AudioClip e CSS.
     *
     * Uma única instância é criada aqui e compartilhada com os
     * controladores que precisarem de recursos, para que o cache
     * do ResourceManager realmente sirva ao jogo inteiro.
     */
    private final ResourceManager resourceManager;

    /*
     * POO 1.3 - Enum:
     * utiliza o enum GameState para restringir o estado atual do jogo
     * aos estados definidos pela aplicação.
     */
    private GameState currentState;

    /*
     * Controla a lógica específica do modo de exploração.
     */
    private final ExplorationController explorationController;

    /**
     * Cria o controlador principal do jogo e inicializa
     * os controladores necessários.
     *
     * @param stage janela principal criada pelo JavaFX
     */
    public GameController(Stage stage) {
        this.sceneRouter = new SceneRouter(stage);
        this.resourceManager = new ResourceManager();
        this.currentState = GameState.MENU;
        this.explorationController = new ExplorationController(resourceManager);
    }

    /**
     * Inicia o jogo.
     *
     * Enquanto o menu principal ainda não estiver implementado,
     * o jogo inicia diretamente no modo de exploração.
     */
    public void startGame() {

        sceneRouter.setTitle("Além do Equilíbrio");

        changeState(GameState.EXPLORATION);
    }

    /**
     * Altera o estado atual do jogo e executa
     * o comportamento correspondente ao novo estado.
     *
     * @param newState novo estado que o jogo deverá assumir
     */
    public void changeState(GameState newState) {

        this.currentState = newState;

        switch (newState) {

            case MENU -> {
                // Futuramente: iniciar/exibir o menu principal.
            }

            case EXPLORATION -> {
                startExploration();
            }

            case DIALOGUE -> {
                // Futuramente: iniciar o sistema de diálogo.
            }

            case BATTLE -> {
                // Futuramente: iniciar o sistema de combate.
            }

            case QUIZ -> {
                // Futuramente: iniciar o sistema de quiz.
            }

            case PAUSED -> {
                // Futuramente: pausar o jogo e exibir o menu de pausa.
            }

            case CHAPTER_COMPLETE -> {
                // Futuramente: exibir a conclusão do capítulo.
            }
        }
    }

    /**
     * Inicia o modo de exploração.
     */
    private void startExploration() {

        sceneRouter.show(explorationController.getScene());

        explorationController.start();
    }

}