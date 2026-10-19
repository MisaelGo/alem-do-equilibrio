package com.alemdoequilibrio.game;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.alemdoequilibrio.core.SaveFormatException;
import com.alemdoequilibrio.core.SaveManager;

/**
 * Verificacoes estruturais de registry,
 * progresso e persistencia.
 */
public final class PersistenceScenarioChecks {

    private PersistenceScenarioChecks() {
    }

    public static void main(
            String[] args)
            throws Exception {

        checkGameRegistry();

        checkProgressFlags();

        checkSaveAndLoad();

        checkInvalidSave();

        System.out.println(
                "Persistencia: todos os cenarios passaram."
        );
    }

    private static void
            checkGameRegistry() {

        GameRegistry<Ability> registry =
                new GameRegistry<>();

        registry.register(
                "PULSE",
                new RepulsionPulse()
        );

        registry.register(
                "FIELD",
                new FieldImpulse()
        );

        check(
                registry.size() == 2,
                "O registry deve possuir dois elementos."
        );

        check(
                registry.contains("PULSE"),
                "O registry deve localizar um ID registrado."
        );

        check(
                registry.get("PULSE")
                        instanceof RepulsionPulse,
                "O registry deve recuperar a habilidade correta."
        );

        boolean duplicateRejected =
                false;

        try {

            registry.register(
                    "PULSE",
                    new RepulsionPulse()
            );

        } catch (IllegalArgumentException expected) {

            duplicateRejected =
                    true;
        }

        check(
                duplicateRejected,
                "IDs duplicados devem ser rejeitados."
        );
    }

    private static void
            checkProgressFlags() {

        ChapterProgress progress =
                new ChapterProgress();

        progress.learn(
                KnowledgeTopic
                        .KNOW_CHARGE_SIGNS
        );

        progress.completeQuiz(
                KnowledgeTopic
                        .KNOW_CHARGE_SIGNS
        );

        progress.markBossDefeated();

        progress.markFragmentObtained();

        check(
                progress.hasLearned(
                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS
                ),
                "O conhecimento deve ser registrado."
        );

        check(
                progress.hasCompletedQuiz(
                        KnowledgeTopic
                                .KNOW_CHARGE_SIGNS
                ),
                "A prova deve ser registrada."
        );

        check(
                progress.isBossDefeated(),
                "A derrota do boss deve ser registrada."
        );

        check(
                progress.hasFragmentObtained(),
                "O fragmento deve ser registrado."
        );
    }

    private static void
            checkSaveAndLoad()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "alem-equilibrio-save-",
                        ".txt"
                );

        try {

            SaveManager saveManager =
                    new SaveManager(
                            file
                    );

            ChapterProgress originalProgress =
                    new ChapterProgress();

            originalProgress.learn(
                    KnowledgeTopic
                            .KNOW_CHARGE_SIGNS
            );

            originalProgress.learn(
                    KnowledgeTopic
                            .KNOW_FIELD
            );

            originalProgress.completeQuiz(
                    KnowledgeTopic
                            .KNOW_CHARGE_SIGNS
            );

            originalProgress.reachCheckpoint(
                    ExplorationZone
                            .EQUIPOTENTIAL_RUINS
            );

            Hero originalHero =
                    new Hero(
                            0,
                            0,
                            200
                    );

            originalHero.takeDamage(
                    25.0f
            );

            originalHero.learnAbility(
                    AbilityRegistry.get(
                            AbilityRegistry
                                    .REPULSION_PULSE_ID
                    ),
                    true
            );

            saveManager.save(
                    originalProgress,
                    originalHero
            );

            ChapterProgress
                    loadedProgress =
                    new ChapterProgress();

            Hero loadedHero =
                    new Hero(
                            0,
                            0,
                            200
                    );

            saveManager.load(
                    loadedProgress,
                    loadedHero
            );

            check(
                    loadedProgress
                            .getCheckpoint()
                    == ExplorationZone
                            .EQUIPOTENTIAL_RUINS,
                    "O checkpoint deve ser restaurado."
            );

            check(
                    loadedProgress
                            .hasLearned(
                                    KnowledgeTopic
                                            .KNOW_FIELD
                            ),
                    "Os conhecimentos devem ser restaurados."
            );

            check(
                    loadedProgress
                            .hasCompletedQuiz(
                                    KnowledgeTopic
                                            .KNOW_CHARGE_SIGNS
                            ),
                    "As provas concluidas devem ser restauradas."
            );

            check(
                    loadedHero.getHealth()
                    == 75.0f,
                    "A vida deve ser restaurada."
            );

            check(
                    loadedHero
                            .getAbilityBook()
                            .getAbilities()
                            .size()
                    == 1,
                    "As habilidades devem ser restauradas."
            );

            check(
                    loadedHero
                            .getAbilityBook()
                            .getAbilities()
                            .get(0)
                    instanceof RepulsionPulse,
                    "A habilidade correta deve ser restaurada."
            );

        } finally {

            Files.deleteIfExists(
                    file
            );
        }
    }

    /*
     * C1-055:
     * a SaveFormatException e lancada
     * e tratada explicitamente.
     */
    private static void
            checkInvalidSave()
            throws Exception {

        Path file =
                Files.createTempFile(
                        "alem-equilibrio-invalid-",
                        ".txt"
                );

        try {

            String invalidSave =
                    "saveVersion=999\n"
                    + "chapter=1\n"
                    + "checkpoint=NEUTRAL_BORDER\n"
                    + "health=100.0\n"
                    + "learnedTopics=\n"
                    + "abilities=\n"
                    + "completedQuizzes=\n"
                    + "bossDefeated=false\n"
                    + "fragmentObtained=false\n";

            Files.writeString(
                    file,
                    invalidSave,
                    StandardCharsets.UTF_8
            );

            SaveManager saveManager =
                    new SaveManager(
                            file
                    );

            boolean exceptionHandled =
                    false;

            try {

                saveManager.load(
                        new ChapterProgress(),
                        new Hero(
                                0,
                                0,
                                200
                        )
                );

            } catch (SaveFormatException expected) {

                exceptionHandled =
                        true;
            }

            check(
                    exceptionHandled,
                    "SaveFormatException deve ser "
                    + "lancada para um save invalido."
            );

        } finally {

            Files.deleteIfExists(
                    file
            );
        }
    }

    private static void check(
            boolean condition,
            String message) {

        if (!condition) {

            throw new AssertionError(
                    message
            );
        }
    }
}