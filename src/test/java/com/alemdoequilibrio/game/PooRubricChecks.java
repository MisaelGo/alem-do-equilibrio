package com.alemdoequilibrio.game;

import com.alemdoequilibrio.core.ElectricPhysics;
import com.alemdoequilibrio.core.GameController;
import com.alemdoequilibrio.core.SaveFormatException;
import com.alemdoequilibrio.core.SaveManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Auditoria automatizada dos requisitos dos slides do professor Tenório.
 * Confere estrutura, assinatura e comportamento sem adicionar um segundo main.
 * O índice e a justificativa pedagógica de cada exemplo estão em
 * docs/indice-22-itens-poo.md; o diagrama completo, em docs/diagrama-classes.dot.
 */
public class PooRubricChecks {

    @Test
    void atributosEncapsulamentoEConstrutores() throws Exception {
        // POO 1.1: todos os atributos de instância de classes do projeto são privados.
        try (Stream<Path> sources = Files.walk(Path.of("src/main/java"))) {
            for (Path path : sources.filter(p -> p.toString().endsWith(".java")).toList()) {
                String content = Files.readString(path);
                Matcher match = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+);").matcher(content);
                assertTrue(match.find(), "Pacote ausente: " + path);
                String className = path.getFileName().toString().replace(".java", "");
                Class<?> type = Class.forName(match.group(1) + "." + className,
                        false, getClass().getClassLoader());
                for (Field f : type.getDeclaredFields()) {
                    if (!f.isSynthetic() && !Modifier.isStatic(f.getModifiers())) {
                        assertTrue(Modifier.isPrivate(f.getModifiers()),
                                type.getSimpleName() + "." + f.getName()
                                + " precisa ser private");
                    }
                }
            }
        }
        // POO 1.2: três tipos em Enemy; 1.3: enum; 1.4: ArrayList;
        // 1.5: constante int primitiva; 3: construtores concretos.
        assertEquals(String.class, Enemy.class.getDeclaredField("name").getType());
        assertEquals(int.class, Enemy.class.getDeclaredField("level").getType());
        assertEquals(float.class, Enemy.class.getDeclaredField("health").getType());
        assertEquals(ChargeType.class, Hero.class.getDeclaredField("chargeType").getType());
        assertEquals(java.util.ArrayList.class,
                AbilityBook.class.getDeclaredField("abilities").getType());
        Field plates = ChapterProgress.class.getDeclaredField("CERAMIC_BRIDGE_PLATES");
        assertEquals(int.class, plates.getType());
        assertTrue(Modifier.isStatic(plates.getModifiers())
                && Modifier.isFinal(plates.getModifiers()));
        assertNotNull(new Hero(0, 0, 200));
        assertNotNull(new Enemy(999, "Teste", 1, 10.0f));
    }

    @Test
    void argumentosVariadosMetodoEstaticoEComposicao() throws Exception {
        // POO 2.1: varargs efetivo no quiz e laço for documentado no método.
        Method args = Merchant.class.getDeclaredMethod("normalizePrerequisites",
                KnowledgeTopic.class, KnowledgeTopic[].class);
        assertTrue(args.isVarArgs());
        // POO 2.2: estático usado por cálculos de Física.
        assertTrue(Modifier.isStatic(
                ElectricPhysics.class.getMethod("coulombForce",
                        double.class, double.class, double.class).getModifiers()));
        // POO 4: o livro pertence exclusivamente ao Hero que o constrói.
        Hero first = new Hero(0, 0, 200);
        Hero second = new Hero(0, 0, 200);
        assertNotSame(first.getAbilityBook(), second.getAbilityBook());
        // POO 5: agregação injeta recurso preexistente no MapEnemySpawner.
        assertNotNull(MapEnemySpawner.class.getConstructor(
                javafx.scene.layout.Pane.class,
                com.alemdoequilibrio.core.ResourceManager.class));
    }

    @Test
    void herancaPolimorfismoEInterfaces() throws Exception {
        // POO 6.1: atributo final na superclasse concreta.
        assertFalse(Modifier.isAbstract(Enemy.class.getModifiers()));
        assertTrue(Modifier.isFinal(
                Enemy.class.getDeclaredField("enemyId").getModifiers()));
        // POO 6.2: atributo da filha concreta, método herdado da pai concreta.
        assertEquals(WanderingSpark.class,
                MapEnemySpawner.class.getDeclaredField("bridgeSpark").getType());
        assertEquals(Enemy.class,
                WanderingSpark.class.getMethod("getEnemyId").getDeclaringClass());
        // POO 7.1: sobrecarga real da operação de recuperação.
        assertNotNull(Hero.class.getMethod("restoreHealth"));
        assertNotNull(Hero.class.getMethod("restoreHealth", double.class));
        // POO 7.2: cálculo double armazenado em atributo float health.
        Hero hero = new Hero(0, 0, 200);
        hero.takeDamage(12.25f);
        hero.restoreHealth(10.5);
        assertEquals(98.25f, hero.getHealth(), 0.001f);
        // POO 7.3: Enemy, WanderingSpark e método receptor da classe concreta.
        assertTrue(Enemy.class.isAssignableFrom(WanderingSpark.class));
        assertNotNull(MapEnemySpawner.class.getDeclaredMethod(
                "spawnEnemy", Enemy.class, double.class, double.class));
        // POO 7.4: GameRegistry<Ability> com tipo genérico explícito.
        Field registry = AbilityRegistry.class.getDeclaredField("REGISTRY");
        assertTrue(registry.getGenericType() instanceof ParameterizedType);
        assertEquals(Ability.class,
                ((ParameterizedType) registry.getGenericType())
                        .getActualTypeArguments()[0]);
        // POO 8: três classes usam interfaces e uma implementa duas.
        assertTrue(Damageable.class.isAssignableFrom(Enemy.class));
        assertTrue(Interactable.class.isAssignableFrom(NPC.class));
        assertTrue(Damageable.class.isAssignableFrom(Hero.class));
        assertTrue(Movable.class.isAssignableFrom(Hero.class));
        assertEquals(2, Hero.class.getInterfaces().length);
        // POO 10: duas classes abstratas com métodos abstratos próprios.
        assertTrue(Modifier.isAbstract(GameCharacter.class.getModifiers()));
        assertTrue(Modifier.isAbstract(Ability.class.getModifiers()));
        assertTrue(Modifier.isAbstract(
                GameCharacter.class.getDeclaredMethod("updateState").getModifiers()));
        assertTrue(Modifier.isAbstract(
                Ability.class.getDeclaredMethod("execute",
                        BattleController.class).getModifiers()));
    }

    @Test
    void somenteDoisPacotesEUmMain() throws IOException {
        // POO 9: exatamente dois pacotes de produção.
        Set<String> packages = new HashSet<>();
        int mainCount = 0;
        for (String root : new String[] {"src/main/java", "src/test/java"}) {
            try (Stream<Path> stream = Files.walk(Path.of(root))) {
                for (Path path : stream.filter(p -> p.toString().endsWith(".java")).toList()) {
                    String content = Files.readString(path);
                    Matcher found = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+);")
                            .matcher(content);
                    assertTrue(found.find(), "Pacote ausente: " + path);
                    packages.add(found.group(1));
                    Matcher main = Pattern.compile(
                            "\\bstatic\\s+void\\s+main\\s*\\(").matcher(content);
                    while (main.find()) {
                        mainCount++;
                    }
                }
            }
        }
        assertEquals(Set.of("com.alemdoequilibrio.core",
                        "com.alemdoequilibrio.game"), packages);
        assertEquals(1, mainCount,
                "O professor prevê penalidade se houver mais de um main.");
    }

    @Test
    void excecaoEntradaGraficaEArquivo() throws Exception {
        // POO 11: exceção criada, declarada em load() e tratada no controlador.
        assertTrue(Exception.class.isAssignableFrom(SaveFormatException.class));
        assertTrue(java.util.Arrays.asList(SaveManager.class.getMethod("load",
                ChapterProgress.class, Hero.class).getExceptionTypes())
                .contains(SaveFormatException.class));
        // POO 12: entrada de dados do jogador via JOptionPane.
        String controller = Files.readString(Path.of(
                "src/main/java/com/alemdoequilibrio/core/GameController.java"));
        assertTrue(controller.contains("JOptionPane.showInputDialog("));
        // POO 13: arquivo gravado e recuperado; há teste funcional de round trip
        // no PersistenceScenarioChecks.
        assertNotNull(SaveManager.class.getMethod("save",
                ChapterProgress.class, Hero.class));
        assertNotNull(SaveManager.class.getMethod("load",
                ChapterProgress.class, Hero.class));
    }
}
