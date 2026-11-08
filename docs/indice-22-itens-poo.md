# Índice dos 22 requisitos de Programação Orientada a Objetos

**Projeto:** Além do Equilíbrio — Java / JavaFX (NetBeans e Maven)  
**Referência:** slides de POO do professor Roberto Tenório, 17/08/2026, páginas 6–13.  
**Pacotes próprios:** `com.alemdoequilibrio.core` e `com.alemdoequilibrio.game` (exatamente dois).  
**Ponto de entrada:** exclusivamente `com.alemdoequilibrio.core.Main.main`.  
**Diagrama:** [diagrama-classes.md](diagrama-classes.md) (visualização GitHub) e [diagrama-classes.dot](diagrama-classes.dot) (geração de PDF).  

> **Atenção à correção:** cada linha identifica a classe e o uso real no código. Os
> comentários `POO <número>` no programa justificam a necessidade do recurso.
> É preciso mostrar, na apresentação, o próprio método ou atributo e a interação
> correspondente em execução; comentários isolados não substituem implementação.

| Item | Requisito do professor | Classe(s) / membro(s) a demonstrar | Justificativa funcional |
|---|---|---|---|
| **1.1** | Encapsulamento: atributos de instância `private` | `Enemy.name/level/health/enemyId`, `Hero.health/chargeType/abilityBook`; demais classes do sistema | Protege vida, habilidades, relações e progresso contra mutação arbitrária. O teste `PooRubricChecks` verifica campos de instância em todas as classes fonte. |
| **1.2** | `String`, `int` e `float` como atributos NÃO estáticos da mesma classe | `Enemy.name`, `Enemy.level`, `Enemy.health` | Nome identifica o inimigo no HUD; nível define força de ataque; vida controla derrotas. |
| **1.3** | Atributo do tipo `enum` criado no projeto | `Hero.chargeType: ChargeType` | Restringe a polaridade a valores válidos usados na seleção de estratégias elétricas do combate. |
| **1.4** | Atributo `ArrayList` | `AbilityBook.abilities: ArrayList<Ability>` | Armazena um número variável de habilidades adquiridas, preservando sua ordem. |
| **1.5** | Constante primitiva declarada em classe concreta, inicializada e usada noutra classe | `ChapterProgress.CERAMIC_BRIDGE_PLATES = 9` (usada por `ExplorationController` e `SaveManager`) | Número máximo fixo de placas da ponte; impede save inválido e controla as viagens e o HUD. |
| **2.1** | Método com argumentos variados e **FOR** sobre os recebidos, utilizado pelo sistema | `Merchant.normalizePrerequisites(KnowledgeTopic, KnowledgeTopic...)` e seu `for (KnowledgeTopic topic : topics)` | Normaliza requisitos para liberar quizzes, rejeitando nulos e removendo duplicatas. É chamado pelo construtor de `Merchant`. |
| **2.2** | Método estático necessário e chamado | `Merchant.normalizePrerequisites(...)` (chamado no construtor); `ElectricPhysics.coulombForce(...)` | Pré-requisitos são normalizados independentemente do estado de um comerciante; cálculos físicos puros também dispensam instância. |
| **3** | Construtores declarados em pelo menos duas classes concretas e usados | `Hero(double,double,double)`; `Enemy(int,String,int,float)` | Criação validada dos personagens com vida, nível e posição inicial. |
| **4** | Associação por **composição**, usando construtor, não genérica | `Hero.abilityBook` construído por `new AbilityBook()` no construtor de `Hero` | O livro de habilidades pertence exclusivamente ao herói e não é compartilhado com outro herói. |
| **5** | Associação por **agregação** com construtores efetivamente utilizados | `MapEnemySpawner(Pane, ResourceManager)`, atributo `resourceManager`; construção em `ExplorationController` | O controlador reutiliza um `ResourceManager` já existente e compartilhado entre componentes visuais. Não é uma instância criada como parte do próprio spawner. |
| **6.1** | Atributo `final` em superclasse **concreta**, diferente de 1.5 | `Enemy.enemyId: final int` (superclasse de `WanderingSpark`, `ChargedEnemy`, etc.) | O identificador de inimigo não pode mudar, pois referencia derrotas persistidas no save. |
| **6.2** | Acoplamento dinâmico: pai concreto, filho sem sobrescrever o método, atributo da filha na terceira classe | `Enemy.getEnemyId()`; `WanderingSpark` herda sem sobrescrever; `MapEnemySpawner.bridgeSpark: WanderingSpark` e `bridgeSpark.getEnemyId()` | O gerenciador usa o identificador herdado da faísca para impedir reaparições e instanciar um encontro novo quando necessário. |
| **7.1** | Polimorfismo por **sobrecarga** | `Hero.restoreHealth()` e `Hero.restoreHealth(double)` | Mesmo nome, duas operações úteis: ressurgir com vida completa e recuperar parcialmente no monólito. |
| **7.2** | Polimorfismo de **coerção explícita**, valor de tipo diferente atribuído a atributo do tipo de destino | `Hero.restoreHealth(double)`: `double recoveredHealth` → `health = (float) recoveredHealth` | O cálculo em double é limitado pela vida máxima antes de ser armazenado no atributo float usado em combate. Não usa funções de conversão. |
| **7.3** | Polimorfismo de **subtipagem** entre pai e filhos concretos | `MapEnemySpawner.spawnEnemy(Enemy,double,double)`, chamadas com `WanderingSpark`, `BossEnemy`, `UncontrolledCharge` | Um método de geração trata inimigos diferentes através da classe-base concreta `Enemy`. |
| **7.4** | Polimorfismo **paramétrico** com tipo genérico explícito, em classe concreta | `AbilityRegistry.REGISTRY: GameRegistry<Ability>`; `GameRegistry<T>` | Habilidades concretas de efeitos diferentes são armazenadas e recuperadas com segurança de tipos pelo mesmo registro. O tipo é explícito na declaração do atributo, não apenas no construtor. |
| **8** | Ao menos 3 classes implementam interface; uma implementa duas | `Hero implements Damageable, Movable`; `Enemy implements Damageable`; `NPC implements Interactable`; `Merchant implements QuizProvider` | O herói move-se e recebe dano; inimigos recebem dano; NPCs interagem; comerciante fornece quiz. Interfaces não possuem getters/setters nem construtores. |
| **9** | Criar **exatamente dois pacotes** | `com.alemdoequilibrio.core` e `com.alemdoequilibrio.game` | Separa ciclo de vida, recursos e persistência da lógica de personagens, exploração e combate. Testes JUnit reutilizam `game`, sem criar terceiro pacote. |
| **10** | Duas classes abstratas, cada qual com método abstrato funcional, concretas implementadas | `GameCharacter.updateState()` implementado em `Hero`/`Enemy`/`NPC`; `Ability.execute(BattleController)` implementado em `RepulsionPulse`, `FieldImpulse` e `EquipotentialShield` | Os personagens atualizam estado específico; cada habilidade define um efeito diferente. Os métodos não são construtores, getters ou setters. |
| **11** | Exceção própria **criada, lançada e tratada** | `SaveFormatException`, lançada por `SaveManager.load(...)` e tratada em `GameController.loadSavedProgress()` | Interrompe a leitura de saves corrompidos, evitando partidas com estado incoerente. |
| **12** | **Entrada** por caixa de diálogo (não somente interface gráfica) | `GameController.confirmRestartWithJOptionPane()` → `JOptionPane.showInputDialog(...)` | O usuário precisa digitar **REINICIAR**; apenas a resposta correta autoriza apagar o save. É entrada de texto real via caixa de diálogo. |
| **13** | Informações gravadas em arquivo, recuperadas e utilizadas | `SaveManager.save(ChapterProgress,Hero)` e `load(ChapterProgress,Hero)` | Guarda vida, checkpoint, conceitos, habilidades, cerâmicas e inimigos derrotados; restaura esses valores ao continuar o jogo. |

**Total: 22 itens** = 5 atributos + 2 métodos + construtor + composição + agregação + 2 heranças + 4 polimorfismos + interface + pacotes + classes abstratas + exceção + entrada por diálogo + arquivo.

## Onde mostrar no NetBeans

1. Abra o arquivo indicado na coluna **Classe(s) / membro(s)**.
2. Pesquise pelo comentário `POO 1.1`, `POO 6.2` etc., que explicita o nome do conceito e por que ele é necessário.
3. Demonstre o uso real: por exemplo, monólito para casting/overload, área de combate para subtipagem, Íon para varargs, reinício para `JOptionPane`, continuar para arquivo.
4. Tenha o [diagrama](diagrama-classes.md) aberto, com marcações dos mesmos números, e mostre relações e pacotes.
5. Execute `mvn verify` antes de apresentar. Há verificações automatizadas em `PooRubricChecks`, `PersistenceScenarioChecks` e `CombatScenarioChecks`.

## Arquivos de entrega e penalidades a evitar

- Enviar **projeto completo**, **diagrama com todas as classes** e **este índice** em **um único ZIP**, até o dia anterior à apresentação.
- O ZIP deve ter os **nomes iniciais dos integrantes**, separados por sublinhado, conforme o professor.
- Pelo menos dois pacotes não bastam: devem ser **exatamente** dois; não criar um pacote extra para testes, helpers ou UI.
- Não introduzir segundo método `main`: os cenários de verificação usam JUnit.
- Todos os requisitos devem aparecer **também no diagrama**; se não estiverem no índice **e** no diagrama, o slide informa que o item será considerado ausente.
- O professor desconta 1,0 ponto por item ausente/indevido, por erro no diagrama, por falha de execução e por falhas de apresentação; mais de um `Main` ou erro de compilação implicam penalidade elevada.
- Executar o jogo do início ao fim antes da apresentação; aprovação em teste automático não garante ausência de problemas de UI.

Para montar a entrega: `python scripts/gerar-entrega.py Nome1_Nome2_Nome3_Nome4`.
O script gera os arquivos PDF/SVG do diagrama e o ZIP com o projeto, desde que
**Graphviz** esteja instalado (`dot -V`).
