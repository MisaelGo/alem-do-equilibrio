package com.alemdoequilibrio.core;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.alemdoequilibrio.game.Ability;
import com.alemdoequilibrio.game.AbilityRegistry;
import com.alemdoequilibrio.game.ChapterProgress;
import com.alemdoequilibrio.game.ExplorationZone;
import com.alemdoequilibrio.game.Hero;
import com.alemdoequilibrio.game.KnowledgeTopic;

/**
 * C1-054 - Gerenciador de persistencia.
 *
 * Grava e recupera o progresso essencial
 * do Capitulo 1 em um arquivo texto versionado.
 */
public class SaveManager {

    private static final int
            SAVE_VERSION = 1;

    private static final int
            CHAPTER = 1;

    private final Path savePath;

    public SaveManager() {

        this(
                Path.of(
                        "savegame.txt"
                )
        );
    }

    public SaveManager(
            Path savePath) {

        if (savePath == null) {

            throw new IllegalArgumentException(
                    "O caminho do save nao pode ser nulo."
            );
        }

        this.savePath =
                savePath;
    }

    public boolean exists() {

        return Files.exists(
                savePath
        );
    }

    public void save(
            ChapterProgress progress,
            Hero hero)
            throws IOException,
            SaveFormatException {

        if (progress == null
                || hero == null) {

            throw new IllegalArgumentException(
                    "O estado salvo nao pode ser nulo."
            );
        }

        List<String> lines =
                new ArrayList<>();

        lines.add(
                "saveVersion="
                + SAVE_VERSION
        );

        lines.add(
                "chapter="
                + CHAPTER
        );

        lines.add(
                "checkpoint="
                + progress
                        .getCheckpoint()
                        .name()
        );

        lines.add(
                "health="
                + Float.toString(
                        hero.getHealth()
                )
        );

        lines.add(
                "learnedTopics="
                + joinTopics(
                        progress
                                .getLearnedTopics()
                )
        );

        lines.add(
                "abilities="
                + joinAbilities(
                        hero
                                .getAbilityBook()
                                .getAbilities()
                )
        );

        lines.add(
                "completedQuizzes="
                + joinTopics(
                        progress
                                .getCompletedQuizzes()
                )
        );

        lines.add(
                "bossDefeated="
                + progress
                        .isBossDefeated()
        );

        lines.add(
                "fragmentObtained="
                + progress
                        .hasFragmentObtained()
        );

        lines.add(
                "neutralIonMet="
                + progress
                        .hasMetNeutralIon()
        );

        Path absolutePath =
                savePath
                        .toAbsolutePath();

        Path parent =
                absolutePath
                        .getParent();

        if (parent != null) {

            Files.createDirectories(
                    parent
            );
        }

        Files.write(
                savePath,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public void load(
            ChapterProgress progress,
            Hero hero)
            throws IOException,
            SaveFormatException {

        if (progress == null
                || hero == null) {

            throw new IllegalArgumentException(
                    "O estado restaurado nao pode ser nulo."
            );
        }

        if (!exists()) {

            throw new IOException(
                    "Arquivo de save nao encontrado."
            );
        }

        Map<String, String> values =
                readValues();

        int saveVersion =
                parseInteger(
                        required(
                                values,
                                "saveVersion"
                        ),
                        "saveVersion"
                );

        if (saveVersion
                != SAVE_VERSION) {

            throw new SaveFormatException(
                    "Versao de save incompatível: "
                    + saveVersion
            );
        }

        int chapter =
                parseInteger(
                        required(
                                values,
                                "chapter"
                        ),
                        "chapter"
                );

        if (chapter != CHAPTER) {

            throw new SaveFormatException(
                    "Capitulo de save incompatível: "
                    + chapter
            );
        }

        ExplorationZone checkpoint =
                parseCheckpoint(
                        required(
                                values,
                                "checkpoint"
                        )
                );

        float health =
                parseHealth(
                        required(
                                values,
                                "health"
                        ),
                        hero.getMaxHealth()
                );

        EnumSet<KnowledgeTopic>
                learnedTopics =
                parseTopics(
                        required(
                                values,
                                "learnedTopics"
                        ),
                        "learnedTopics"
                );

        List<String> abilityIds =
                parseAbilityIds(
                        required(
                                values,
                                "abilities"
                        )
                );

        EnumSet<KnowledgeTopic>
                completedQuizzes =
                parseTopics(
                        required(
                                values,
                                "completedQuizzes"
                        ),
                        "completedQuizzes"
                );

        if (!learnedTopics
                .containsAll(
                        completedQuizzes
                )) {

            throw new SaveFormatException(
                    "O save possui prova concluida "
                    + "sem o respectivo conhecimento."
            );
        }

        boolean bossDefeated =
                parseBoolean(
                        required(
                                values,
                                "bossDefeated"
                        ),
                        "bossDefeated"
                );

        boolean fragmentObtained =
                parseBoolean(
                        required(
                                values,
                                "fragmentObtained"
                        ),
                        "fragmentObtained"
                );

        String neutralIonMetValue =
                values.get(
                        "neutralIonMet"
                );

        boolean neutralIonMet =
                neutralIonMetValue != null
                        ? parseBoolean(
                                neutralIonMetValue,
                                "neutralIonMet"
                        )
                        : false;

        if (fragmentObtained
                && !bossDefeated) {

            throw new SaveFormatException(
                    "O fragmento nao pode ter sido "
                    + "obtido antes da derrota do boss."
            );
        }

        /*
         * Somente depois de validar o arquivo inteiro
         * alteramos o estado real do jogo.
         */
        progress.restoreFromSave(
                checkpoint,
                learnedTopics,
                completedQuizzes,
                bossDefeated,
                fragmentObtained,
                neutralIonMet
        );

        hero.clearAbilities();

        boolean equipFirst =
                true;

        for (String abilityId
                : abilityIds) {

            Ability ability =
                    AbilityRegistry.get(
                            abilityId
                    );

            hero.learnAbility(
                    ability,
                    equipFirst
            );

            equipFirst = false;
        }

        hero.setHealth(
                health
        );
    }

    private Map<String, String>
            readValues()
            throws IOException,
            SaveFormatException {

        Map<String, String> values =
                new LinkedHashMap<>();

        List<String> lines =
                Files.readAllLines(
                        savePath,
                        StandardCharsets.UTF_8
                );

        for (String rawLine
                : lines) {

            String line =
                    rawLine.trim();

            if (line.isEmpty()
                    || line.startsWith("#")) {

                continue;
            }

            int separator =
                    line.indexOf('=');

            if (separator <= 0) {

                throw new SaveFormatException(
                        "Linha de save invalida: "
                        + line
                );
            }

            String key =
                    line.substring(
                            0,
                            separator
                    ).trim();

            String value =
                    line.substring(
                            separator + 1
                    ).trim();

            if (values.containsKey(
                    key
            )) {

                throw new SaveFormatException(
                        "Chave duplicada no save: "
                        + key
                );
            }

            values.put(
                    key,
                    value
            );
        }

        return values;
    }

    private String required(
            Map<String, String> values,
            String key)
            throws SaveFormatException {

        if (!values.containsKey(
                key
        )) {

            throw new SaveFormatException(
                    "Campo obrigatorio ausente: "
                    + key
            );
        }

        return values.get(
                key
        );
    }

    private int parseInteger(
            String value,
            String field)
            throws SaveFormatException {

        try {

            return Integer.parseInt(
                    value
            );

        } catch (NumberFormatException exception) {

            throw new SaveFormatException(
                    "Valor inteiro invalido em "
                    + field
                    + ": "
                    + value,
                    exception
            );
        }
    }

    private float parseHealth(
            String value,
            float maxHealth)
            throws SaveFormatException {

        try {

            float health =
                    Float.parseFloat(
                            value
                    );

            if (!Float.isFinite(health)
                    || health <= 0.0f
                    || health > maxHealth) {

                throw new SaveFormatException(
                        "Vida invalida no save: "
                        + value
                );
            }

            return health;

        } catch (NumberFormatException exception) {

            throw new SaveFormatException(
                    "Vida invalida no save: "
                    + value,
                    exception
            );
        }
    }

    private ExplorationZone
            parseCheckpoint(
                    String value)
            throws SaveFormatException {

        try {

            return ExplorationZone
                    .valueOf(value);

        } catch (IllegalArgumentException exception) {

            throw new SaveFormatException(
                    "Checkpoint desconhecido: "
                    + value,
                    exception
            );
        }
    }

    private EnumSet<KnowledgeTopic>
            parseTopics(
                    String value,
                    String field)
            throws SaveFormatException {

        EnumSet<KnowledgeTopic> topics =
                EnumSet.noneOf(
                        KnowledgeTopic.class
                );

        if (value.isBlank()) {

            return topics;
        }

        String[] parts =
                value.split(",");

        for (String part
                : parts) {

            String topicName =
                    part.trim();

            if (topicName.isEmpty()) {

                continue;
            }

            try {

                topics.add(
                        KnowledgeTopic
                                .valueOf(
                                        topicName
                                )
                );

            } catch (IllegalArgumentException exception) {

                throw new SaveFormatException(
                        "Topico desconhecido em "
                        + field
                        + ": "
                        + topicName,
                        exception
                );
            }
        }

        return topics;
    }

    private List<String>
            parseAbilityIds(
                    String value)
            throws SaveFormatException {

        List<String> abilityIds =
                new ArrayList<>();

        Set<String> foundIds =
                new HashSet<>();

        if (value.isBlank()) {

            return abilityIds;
        }

        String[] parts =
                value.split(",");

        for (String part
                : parts) {

            String id =
                    part.trim();

            if (id.isEmpty()) {

                continue;
            }

            if (!AbilityRegistry.contains(
                    id
            )) {

                throw new SaveFormatException(
                        "Habilidade desconhecida: "
                        + id
                );
            }

            if (!foundIds.add(
                    id
            )) {

                throw new SaveFormatException(
                        "Habilidade duplicada no save: "
                        + id
                );
            }

            abilityIds.add(
                    id
            );
        }

        return abilityIds;
    }

    private boolean parseBoolean(
            String value,
            String field)
            throws SaveFormatException {

        if ("true".equalsIgnoreCase(
                value
        )) {

            return true;
        }

        if ("false".equalsIgnoreCase(
                value
        )) {

            return false;
        }

        throw new SaveFormatException(
                "Valor booleano invalido em "
                + field
                + ": "
                + value
        );
    }

    private String joinTopics(
            Set<KnowledgeTopic> topics) {

        StringJoiner joiner =
                new StringJoiner(",");

        for (KnowledgeTopic topic
                : topics) {

            joiner.add(
                    topic.name()
            );
        }

        return joiner.toString();
    }

    private String joinAbilities(
            List<Ability> abilities)
            throws SaveFormatException {

        StringJoiner joiner =
                new StringJoiner(",");

        for (Ability ability
                : abilities) {

            try {

                joiner.add(
                        AbilityRegistry
                                .getId(
                                        ability
                                )
                );

            } catch (IllegalArgumentException exception) {

                throw new SaveFormatException(
                        "Existe uma habilidade "
                        + "nao registrada no Hero.",
                        exception
                );
            }
        }

        return joiner.toString();
    }
}