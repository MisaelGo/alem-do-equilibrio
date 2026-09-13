package com.alemdoequilibrio.core;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação JavaFX.
 *
 * A classe Main é responsável apenas por iniciar o JavaFX
 * e entregar a janela principal ao GameController.
 * A lógica do jogo não deve ser implementada nesta classe.
 */
public class Main extends Application {
    
    /**
     * Método chamado pelo JavaFX após a inicialização da aplicação.
     *
     * @param stage janela principal criada pelo JavaFX
     */
    @Override
    public void start(Stage stage) {
        
        // O GameController recebe a janela e passa a controlar
        // o fluxo geral da aplicação.
        GameController gameController = new GameController(stage);
        
        gameController.startGame();
        
    }
    
    /**
     * Inicia o ciclo de vida do JavaFX.
     *
     * @param args argumentos recebidos pela aplicação
     */
    public static void main(String[] args) {
        launch(args);
    }
}
