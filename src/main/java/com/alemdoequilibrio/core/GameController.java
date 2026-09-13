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
        this.currentState = GameState.MENU;
        this.explorationController = new ExplorationController(800, 600);
    }

    /**
     * Inicia o jogo no estado de exploração.
     */
    public void startGame() {
        
        changeState(GameState.EXPLORATION);

        sceneRouter.setTitle("Além do Equilíbrio");

        sceneRouter.show(explorationController.getScene());

        explorationController.start();
    }

    /**
     * Altera o estado atual do jogo.
     *
     * @param newState novo estado que o jogo deverá assumir
     */
    public void changeState(GameState newState) {
        this.currentState = newState;
    }
}