package com.alemdoequilibrio.core;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Centraliza o controle da janela principal da aplicação.
 *
 * O SceneRouter é responsável por exibir cenas no Stage principal
 * e por alterar propriedades da janela, como o título.
 *
 * Dessa forma, outras classes não precisam manipular o Stage diretamente.
 */
public class SceneRouter {

    /*
     * Janela principal da aplicação JavaFX.
     *
     * A referência é definida no construtor e permanece a mesma
     * durante a vida deste SceneRouter.
     */
    private final Stage stage;

    /**
     * Cria um roteador associado à janela principal da aplicação.
     *
     * @param stage janela principal que será controlada pelo SceneRouter
     */
    public SceneRouter(Stage stage) {
        this.stage = stage;
    }

    /**
     * Exibe uma nova cena na janela principal.
     *
     * @param scene cena que será exibida
     */
    public void show(Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Altera o título da janela principal.
     *
     * @param title novo título que será exibido na janela
     */
    public void setTitle(String title) {
        stage.setTitle(title);
    }
}