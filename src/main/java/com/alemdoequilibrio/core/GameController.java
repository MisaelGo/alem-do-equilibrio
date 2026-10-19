package com.alemdoequilibrio.core;

import java.io.IOException;

import com.alemdoequilibrio.game.BattleController;
import com.alemdoequilibrio.game.BattleView;
import com.alemdoequilibrio.game.BossEnemy;
import com.alemdoequilibrio.game.ChapterCompleteView;
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

    private final SaveManager
            saveManager;

    private final ChapterCompleteView
            chapterCompleteView;

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

        saveManager =
                new SaveManager();

        chapterCompleteView =
                new ChapterCompleteView(
                        () -> changeState(
                                GameState.EXPLORATION
                        )
                );

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

                        this::startBattle,

                        this::saveProgress
                );
    }

    public void startGame() {

        sceneRouter.setTitle(
                "Além do Equilíbrio"
        );

        boolean loaded =
                loadSavedProgress();

        if (loaded) {

            explorationController
                    .restoreAtCheckpoint();
        }

        if (chapterProgress
                .hasFragmentObtained()) {

            changeState(
                    GameState.CHAPTER_COMPLETE
            );

        } else {

            changeState(
                    GameState.EXPLORATION
            );
        }
    }

    public void changeState(
            GameState newState) {

        if (newState == null) {

            throw new IllegalArgumentException(
                    "O novo estado nao pode ser nulo."
            );
        }

        if (currentState
                == newState) {

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

            case CHAPTER_COMPLETE ->
                sceneRouter.show(
                        chapterCompleteView
                                .getScene()
                );

            case MENU,
                 DIALOGUE,
                 QUIZ,
                 PAUSED -> {
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

        if (currentBattleView
                == null) {

            throw new IllegalStateException(
                    "Nao existe batalha ativa."
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

        BattleController.BattleResult
                result =
                currentBattleController
                        .getResult();

        if (result
                == BattleController
                        .BattleResult
                        .VICTORY) {

            boolean bossVictory =
                    currentBattleController
                            .getEnemy()
                    instanceof BossEnemy;

            if (bossVictory) {

                chapterProgress
                        .markBossDefeated();

                /*
                 * C1-053:
                 * o fragmento e obtido
                 * ao derrotar o Guardiao.
                 */
                chapterProgress
                        .markFragmentObtained();
            }

            explorationController
                    .removeDefeatedEnemy(
                            currentBattleController
                                    .getEnemy()
                    );

            saveProgress();

            currentBattleController =
                    null;

            currentBattleView =
                    null;

            changeState(
                    bossVictory
                            ? GameState
                                    .CHAPTER_COMPLETE
                            : GameState
                                    .EXPLORATION
            );

            return;
        }

        if (result
                == BattleController
                        .BattleResult
                        .DEFEAT) {

            currentBattleController =
                    null;

            currentBattleView =
                    null;

            explorationController
                    .respawnAtCheckpoint();

            saveProgress();

            changeState(
                    GameState.EXPLORATION
            );
        }
    }

    /**
     * Salva o progresso atual.
     *
     * IOException representa problema
     * de acesso ao arquivo.
     *
     * SaveFormatException representa
     * estado incompativel com o formato.
     */
    private void saveProgress() {

        try {

            saveManager.save(
                    chapterProgress,
                    explorationController
                            .getHero()
            );

        } catch (IOException exception) {

            System.err.println(
                    "Nao foi possivel salvar o jogo: "
                    + exception.getMessage()
            );

        } catch (SaveFormatException exception) {

            System.err.println(
                    "O progresso nao pode ser "
                    + "convertido para o formato de save: "
                    + exception.getMessage()
            );
        }
    }

    /**
     * Tenta recuperar um save existente.
     *
     * Um arquivo invalido nao derruba o jogo:
     * o jogador simplesmente inicia com
     * um estado novo.
     */
    private boolean loadSavedProgress() {

        if (!saveManager.exists()) {

            return false;
        }

        try {

            saveManager.load(
                    chapterProgress,
                    explorationController
                            .getHero()
            );

            System.out.println(
                    "Save carregado com sucesso."
            );

            return true;

        } catch (IOException exception) {

            System.err.println(
                    "Nao foi possivel ler o save: "
                    + exception.getMessage()
            );

        } catch (SaveFormatException exception) {

            System.err.println(
                    "Save invalido ou incompativel: "
                    + exception.getMessage()
            );
        }

        return false;
    }

    public GameState
            getCurrentState() {

        return currentState;
    }

    public BattleController
            getCurrentBattleController() {

        return currentBattleController;
    }
}