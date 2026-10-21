package com.alemdoequilibrio.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import com.alemdoequilibrio.game.BattleController;
import com.alemdoequilibrio.game.BattleView;
import com.alemdoequilibrio.game.BossEnemy;
import com.alemdoequilibrio.game.ChapterCompleteView;
import com.alemdoequilibrio.game.ChapterProgress;
import com.alemdoequilibrio.game.Enemy;
import com.alemdoequilibrio.game.ExplorationController;
import com.alemdoequilibrio.game.PrologueController;

import javafx.stage.Stage;

public class GameController {

    private static final double BATTLE_RESULT_SCREEN_DURATION_MILLIS = 4000.0;

    private final Stage stage;

    private final SceneRouter
            sceneRouter;

    private final MainMenuController
            mainMenuController;

    private final ResourceManager
            resourceManager;

    private final ChapterProgress
            chapterProgress;

    private final SaveManager
            saveManager;

    private final ChapterCompleteView
            chapterCompleteView;

    private final PrologueController
            prologueController;

    private GameState currentState;

    private final ExplorationController
            explorationController;

    private BattleController
            currentBattleController;

    private BattleView
            currentBattleView;

    private boolean battleResultTransitionScheduled;

    public GameController(
            Stage stage) {

        this.stage = Objects.requireNonNull(stage);

        mainMenuController = new MainMenuController(
                this::playGame,
                this::restartGame,
                Platform::exit
        );

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

        prologueController =
                new PrologueController(
                        resourceManager,
                        this::finishPrologue
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

                        () -> changeState(
                                GameState.QUIZ
                        ),

                        () -> changeState(
                                GameState.EXPLORATION
                        ),

                        this::startBattle,

                        this::saveProgress
                );
    }

    /** Abre sempre o menu antes de iniciar a partida. */
    public void startGame() {
        sceneRouter.setTitle("Além do Equilíbrio");
        sceneRouter.show(mainMenuController.getScene());
    }

    /** Continua o progresso salvo ou inicia o prólogo na primeira partida. */
    private void playGame() {
        boolean loaded = loadSavedProgress();

        if (loaded) {
            explorationController.restoreAtCheckpoint();
        }

        if (chapterProgress.hasFragmentObtained()) {
            changeState(GameState.CHAPTER_COMPLETE);
        } else if (loaded) {
            changeState(GameState.EXPLORATION);
        } else {
            changeState(GameState.PROLOGUE);
        }
    }

    /** O prólogo foi concluído; registra o início da aventura no save. */
    private void finishPrologue() {
        saveProgress();
        changeState(GameState.EXPLORATION);
    }

    /**
     * Solicita confirmação com JOptionPane (Swing) e apaga o save.
     * Os componentes Swing precisam rodar na Event Dispatch Thread.
     */
    private void restartGame() {
        if (!confirmRestartWithJOptionPane()) {
            return;
        }

        try {
            saveManager.deleteSave();
        } catch (IOException exception) {
            showRestartErrorWithJOptionPane(exception.getMessage());
            return;
        }

        GameController freshGame = new GameController(stage);
        freshGame.sceneRouter.setTitle("Além do Equilíbrio");
        freshGame.changeState(GameState.PROLOGUE);
    }

    /** Confirmação síncrona na EDT, mantendo a operação modal. */
    private boolean confirmRestartWithJOptionPane() {
        AtomicInteger selection =
                new AtomicInteger(JOptionPane.CLOSED_OPTION);

        try {
            SwingUtilities.invokeAndWait(() -> {
                Object[] options = {"Reiniciar", "Cancelar"};

                int answer = JOptionPane.showOptionDialog(
                        null,
                        "Apagar o progresso salvo e recomeçar desde o prólogo?\n"
                        + "Esta ação não poderá ser desfeita.",
                        "Reiniciar jogo",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[1]
                );

                selection.set(answer);
            });
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Confirmação de reinício interrompida.");
            return false;
        } catch (InvocationTargetException exception) {
            System.err.println(
                    "Não foi possível abrir o JOptionPane: "
                    + exception.getCause()
            );
            return false;
        }

        return selection.get() == JOptionPane.YES_OPTION;
    }

    /** Exibe falhas de exclusão do save também usando JOptionPane. */
    private void showRestartErrorWithJOptionPane(String message) {
        try {
            SwingUtilities.invokeAndWait(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            "Não foi possível apagar o save: " + message,
                            "Erro ao reiniciar",
                            JOptionPane.ERROR_MESSAGE
                    )
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Aviso de erro interrompido: " + message);
        } catch (InvocationTargetException exception) {
            System.err.println(
                    "Falha ao exibir aviso do JOptionPane: "
                    + exception.getCause()
                    + ". Erro original: "
                    + message
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

            case PROLOGUE ->
                prologueController
                        .stop();

            case EXPLORATION ->
                explorationController
                        .stop();

            case DIALOGUE ->
                explorationController
                        .closeDialogue();

            case QUIZ ->
                explorationController
                        .closeQuiz();

            case BATTLE -> {
                if (currentBattleView != null) {
                    currentBattleView.stopAudio();
                }
            }

            case MENU,
                 PAUSED,
                 CHAPTER_COMPLETE -> {
            }
        }
    }

    private void enterState(
            GameState state) {

        switch (state) {

            case PROLOGUE ->
                startPrologue();

            case EXPLORATION ->
                startExploration();

            case BATTLE ->
                showBattle();

            case CHAPTER_COMPLETE ->
                sceneRouter.show(
                        chapterCompleteView
                                .getScene()
                );

            case MENU ->
                sceneRouter.show(
                        mainMenuController.getScene()
                );

            case DIALOGUE,
                 QUIZ,
                 PAUSED -> {
            }
        }
    }

    private void startPrologue() {

        sceneRouter.show(
                prologueController
                        .getScene()
        );

        prologueController
                .start();
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

        battleResultTransitionScheduled = false;

        currentBattleView =
                new BattleView(
                        currentBattleController,
                        resourceManager
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

        if (battleResultTransitionScheduled) {
            return;
        }

        battleResultTransitionScheduled = true;

        PauseTransition resultScreenDuration =
                new PauseTransition(
                        Duration.millis(
                                BATTLE_RESULT_SCREEN_DURATION_MILLIS
                        )
                );

        resultScreenDuration.setOnFinished(
                event -> completeBattleTransition()
        );

        resultScreenDuration.play();
    }

    /**
     * Retorna ao fluxo de exploração depois que a tela de resultado foi vista.
     */
    private void completeBattleTransition() {

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

            if (currentBattleView != null) {
                currentBattleView.stopAudio();
            }

            currentBattleController =
                    null;

            currentBattleView =
                    null;

            battleResultTransitionScheduled = false;

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

            if (currentBattleView != null) {
                currentBattleView.stopAudio();
            }

            currentBattleController =
                    null;

            currentBattleView =
                    null;

            battleResultTransitionScheduled = false;

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
