# Diagrama para a apresentação dos 22 itens de POO

Abra [`diagrama-classes-apresentacao.puml`](diagrama-classes-apresentacao.puml) no plugin PlantUML do NetBeans.
Os **60 arquivos Java** do repositório (57 de produção e 3 de testes) e os **8 tipos internos** estão representados no mesmo diagrama.
Classes em destaque amarelo foram selecionadas para explicar os conceitos; as demais ficam em cinza e não exigem uma seta.
Não há pacotes artificiais: apenas `core` e `game`.

## Roteiro de localização

| Item | Onde apontar no diagrama | O que demonstrar no código |
|---|---|---|
| 1.1 | `Enemy` | Atributos de instância privados garantem controle de identidade, vida e nível |
| 1.2 | `Enemy` | name (String), level (int) e health (float) na mesma classe |
| 1.3 | `Hero` | chargeType utiliza ChargeType, enum definido no jogo |
| 1.4 | `AbilityBook` | abilities usa ArrayList<Ability> para crescer com habilidades aprendidas |
| 1.5 | `ChapterProgress` | CERAMIC_BRIDGE_PLATES constante int utilizada em outras classes |
| 2.1 | `Merchant` | normalizePrerequisites(KnowledgeTopic...) com for sobre os argumentos |
| 2.2 | `Merchant` | normalizePrerequisites é estático e chamado pelo construtor |
| 3 | `Enemy` | Construtor concreto Enemy(...); Hero(...) também possui construtor |
| 4 | `Hero + AbilityBook` | Hero constrói e possui AbilityBook; composição marcada por losango |
| 5 | `MapEnemySpawner + ResourceManager` | Dependência injetada, reutiliza gerenciador existente; agregação |
| 6.1 | `Enemy` | enemyId final na classe concreta base de várias subclasses |
| 6.2 | `MapEnemySpawner + WanderingSpark + Enemy` | bridgeSpark é filha concreta e usa getEnemyId herdado sem sobrescrever |
| 7.1 | `Hero` | Métodos restoreHealth() e restoreHealth(double) sobrecarregados |
| 7.2 | `Hero` | No corpo restoreHealth(double): health = (float) recoveredHealth |
| 7.3 | `MapEnemySpawner + Enemy` | spawnEnemy(Enemy...) recebe subclasses concretas (WanderingSpark, BossEnemy etc.) |
| 7.4 | `AbilityRegistry` | Campo REGISTRY declarado como GameRegistry<Ability> |
| 8 | `Hero + Enemy + NPC` | Hero usa Damageable e Movable; Enemy Damageable e NPC Interactable |
| 9 | `core + game` | Dois pacotes próprios exatamente; Main.main é o único ponto de entrada |
| 10 | `GameCharacter + Ability` | Duas classes abstratas, com updateState e execute abstratos; subclasses concretas |
| 11 | `SaveFormatException + SaveManager` | Exceção própria criada, lançada por leitura inválida e tratada em GameController |
| 12 | `GameController` | JOptionPane.showInputDialog exige resposta textual REINICIAR |
| 13 | `SaveManager` | save escreve dados do jogo; load recupera e usa no checkpoint |

## Como apresentar

1. **Mostre o diagrama completo** e explique que todos os tipos do projeto estão incluídos.
2. Para cada requisito, encontre **a classe amarela indicada na tabela**, não qualquer exemplo espalhado pelo código.
3. No NetBeans, abra o arquivo Java correspondente e mostre o atributo, método, construtor ou trecho com comentário POO e sua necessidade real.
4. Demonstre o jogo quando relevante: monólito para cura e casting, batalhas para herança/subtipagem, Íon para varargs, salvar/continuar para arquivo, reiniciar para entrada por caixa de diálogo.
5. Exporte em **SVG** para ter zoom sem perda de qualidade; se necessário, gere também um PDF para o ZIP.

**Atenção:** o slide exige que o diagrama inclua todas as classes e que o índice liste os 22 itens. O arquivo compacto substitui somente a visualização, não o índice completo [`indice-22-itens-poo.md`](indice-22-itens-poo.md).
