package com.alemdoequilibrio.game;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class ChapterProgress {

    public static final int CERAMIC_BRIDGE_PLATES = 9;

    private int ceramicBridgePlatesPlaced;

    private boolean carryingCeramicPlate;

    private boolean monolithActivated;

    private final Set<Integer> defeatedEnemyIds;

    private final EnumSet<KnowledgeTopic>
            learnedTopics;

    private final EnumSet<KnowledgeTopic>
            completedQuizzes;

    private ExplorationZone checkpoint;

    private boolean bossDefeated;

    private boolean fragmentObtained;

    private boolean neutralIonMet;

    private boolean neutralBorderIntroSeen;

    private boolean insulatorTrailIntroSeen;

    public ChapterProgress() {

        learnedTopics =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );

        completedQuizzes =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );

        checkpoint =
                ExplorationZone
                        .NEUTRAL_BORDER;

        bossDefeated = false;

        fragmentObtained = false;

        neutralIonMet = false;

        neutralBorderIntroSeen = false;

        insulatorTrailIntroSeen = false;

        ceramicBridgePlatesPlaced = 0;
        carryingCeramicPlate = false;

        monolithActivated = false;
        defeatedEnemyIds = new HashSet<>();
    }

    /** Ativa apenas uma vez a recuperação e o checkpoint do monólito. */
    public boolean activateMonolithCheckpoint() {
        if (monolithActivated) {
            return false;
        }
        monolithActivated = true;
        checkpoint = ExplorationZone.INSULATOR_TRAIL;
        return true;
    }

    public boolean isMonolithActivated() {
        return monolithActivated;
    }

    public boolean markEnemyDefeated(int enemyId) {
        if (enemyId <= 0) {
            throw new IllegalArgumentException("ID de inimigo inválido.");
        }
        return defeatedEnemyIds.add(enemyId);
    }

    public Set<Integer> getDefeatedEnemyIds() {
        return Set.copyOf(defeatedEnemyIds);
    }

    /** Coleta uma placa por viagem, respeitando o estoque finito. */
    public boolean pickUpCeramicPlate() {
        if (carryingCeramicPlate || getRemainingCeramicPlates() == 0) {
            return false;
        }

        carryingCeramicPlate = true;
        return true;
    }

    /** Consome a placa carregada e conclui mais uma parte da ponte. */
    public boolean installCeramicPlate() {
        if (!carryingCeramicPlate
                || ceramicBridgePlatesPlaced >= CERAMIC_BRIDGE_PLATES) {
            return false;
        }

        ceramicBridgePlatesPlaced++;
        carryingCeramicPlate = false;
        return true;
    }

    public int getBridgePlatesPlaced() {
        return ceramicBridgePlatesPlaced;
    }

    public boolean isCarryingCeramicPlate() {
        return carryingCeramicPlate;
    }

    public boolean isCeramicBridgeComplete() {
        return ceramicBridgePlatesPlaced == CERAMIC_BRIDGE_PLATES;
    }

    public int getRemainingCeramicPlates() {
        return CERAMIC_BRIDGE_PLATES - ceramicBridgePlatesPlaced
                - (carryingCeramicPlate ? 1 : 0);
    }

    public void learn(
            KnowledgeTopic topic) {

        if (topic == null) {

            throw new IllegalArgumentException(
                    "O topico nao pode ser nulo."
            );
        }

        learnedTopics.add(topic);
    }

    public boolean hasLearned(
            KnowledgeTopic topic) {

        return learnedTopics
                .contains(topic);
    }

    public Set<KnowledgeTopic>
            getLearnedTopics() {

        return Set.copyOf(
                learnedTopics
        );
    }

    public void completeQuiz(
            KnowledgeTopic topic) {

        if (topic == null) {

            throw new IllegalArgumentException(
                    "O topico do quiz nao pode ser nulo."
            );
        }

        completedQuizzes.add(topic);
    }

    public boolean hasCompletedQuiz(
            KnowledgeTopic topic) {

        return completedQuizzes
                .contains(topic);
    }

    public Set<KnowledgeTopic>
            getCompletedQuizzes() {

        return Set.copyOf(
                completedQuizzes
        );
    }

    public boolean reachCheckpoint(
            ExplorationZone zone) {

        if (zone == null) {

            throw new IllegalArgumentException(
                    "O checkpoint nao pode ser nulo."
            );
        }

        if (checkpoint == zone) {

            return false;
        }

        checkpoint = zone;

        return true;
    }

    public ExplorationZone
            getCheckpoint() {

        return checkpoint;
    }

    public void markBossDefeated() {

        bossDefeated = true;
    }

    public boolean isBossDefeated() {

        return bossDefeated;
    }

    public void markFragmentObtained() {

        fragmentObtained = true;
    }

    public boolean hasFragmentObtained() {

        return fragmentObtained;
    }

    public void markNeutralIonMet() {

        neutralIonMet = true;
    }

    public boolean hasMetNeutralIon() {

        return neutralIonMet;
    }

    public void markNeutralBorderIntroSeen() {
        neutralBorderIntroSeen = true;
    }

    public boolean hasSeenNeutralBorderIntro() {
        return neutralBorderIntroSeen;
    }

    public void markInsulatorTrailIntroSeen() {
        insulatorTrailIntroSeen = true;
    }

    public boolean hasSeenInsulatorTrailIntro() {
        return insulatorTrailIntroSeen;
    }

    /**
     * Mantém compatibilidade com chamadas antigas que não
     * armazenavam o encontro com Íon.
     */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained) {

        restoreFromSave(
                checkpoint,
                learnedTopics,
                completedQuizzes,
                bossDefeated,
                fragmentObtained,
                false
        );
    }

    /**
     * Restaura o progresso validado pelo SaveManager.
     */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained,
            boolean neutralIonMet) {

        restoreFromSave(
                checkpoint,
                learnedTopics,
                completedQuizzes,
                bossDefeated,
                fragmentObtained,
                neutralIonMet,
                false
        );
    }

    /** Inclui a introdução da Fronteira Neutra no progresso persistido. */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained,
            boolean neutralIonMet,
            boolean neutralBorderIntroSeen) {

        restoreFromSave(
                checkpoint,
                learnedTopics,
                completedQuizzes,
                bossDefeated,
                fragmentObtained,
                neutralIonMet,
                neutralBorderIntroSeen,
                false
        );
    }

    /** Restaura as duas introduções, mantendo os saves antigos válidos. */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained,
            boolean neutralIonMet,
            boolean neutralBorderIntroSeen,
            boolean insulatorTrailIntroSeen) {

        restoreFromSave(
                checkpoint,
                learnedTopics,
                completedQuizzes,
                bossDefeated,
                fragmentObtained,
                neutralIonMet,
                neutralBorderIntroSeen,
                insulatorTrailIntroSeen,
                0,
                false
        );
    }

    /** Mantém compatibilidade com o estado anterior. */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained,
            boolean neutralIonMet,
            boolean neutralBorderIntroSeen,
            boolean insulatorTrailIntroSeen,
            int ceramicBridgePlatesPlaced,
            boolean carryingCeramicPlate) {

        restoreFromSave(
                checkpoint, learnedTopics, completedQuizzes,
                bossDefeated, fragmentObtained, neutralIonMet,
                neutralBorderIntroSeen, insulatorTrailIntroSeen,
                ceramicBridgePlatesPlaced, carryingCeramicPlate,
                false, Set.of()
        );
    }

    /** Restaura monólito, combate e construção no mesmo save. */
    public void restoreFromSave(
            ExplorationZone checkpoint,
            Set<KnowledgeTopic> learnedTopics,
            Set<KnowledgeTopic> completedQuizzes,
            boolean bossDefeated,
            boolean fragmentObtained,
            boolean neutralIonMet,
            boolean neutralBorderIntroSeen,
            boolean insulatorTrailIntroSeen,
            int ceramicBridgePlatesPlaced,
            boolean carryingCeramicPlate,
            boolean monolithActivated,
            Set<Integer> defeatedEnemyIds) {

        if (defeatedEnemyIds == null
                || defeatedEnemyIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("IDs de inimigos inválidos.");
        }

        if (ceramicBridgePlatesPlaced < 0
                || ceramicBridgePlatesPlaced > CERAMIC_BRIDGE_PLATES
                || ceramicBridgePlatesPlaced + (carryingCeramicPlate ? 1 : 0)
                        > CERAMIC_BRIDGE_PLATES) {
            throw new IllegalArgumentException(
                    "Quantidade inválida de cerâmica no save."
            );
        }

        if (checkpoint == null
                || learnedTopics == null
                || completedQuizzes == null) {

            throw new IllegalArgumentException(
                    "Os dados restaurados nao podem ser nulos."
            );
        }

        this.learnedTopics.clear();

        this.learnedTopics.addAll(
                learnedTopics
        );

        this.completedQuizzes.clear();

        this.completedQuizzes.addAll(
                completedQuizzes
        );

        this.checkpoint =
                checkpoint;

        this.bossDefeated =
                bossDefeated;

        this.fragmentObtained =
                fragmentObtained;

        this.neutralIonMet =
                neutralIonMet;

        this.neutralBorderIntroSeen =
                neutralBorderIntroSeen;

        this.insulatorTrailIntroSeen =
                insulatorTrailIntroSeen;

        this.ceramicBridgePlatesPlaced = ceramicBridgePlatesPlaced;
        this.carryingCeramicPlate = carryingCeramicPlate;

        this.monolithActivated = monolithActivated;
        this.defeatedEnemyIds.clear();
        this.defeatedEnemyIds.addAll(defeatedEnemyIds);
    }
}
