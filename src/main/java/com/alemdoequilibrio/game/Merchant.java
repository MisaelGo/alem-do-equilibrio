package com.alemdoequilibrio.game;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Set;

/**
 * Classe concreta que representa o comerciante Íon no mundo do jogo.
 * Ela herda as características físicas de um NPC (posição, colisão, diálogo) 
 * e assina o contrato de QuizProvider para poder aplicar provas.
 * 
 * POO 7.3 - Subtipagem
 * Justificativa: Merchant é uma extensão (especialização) de NPC. Ele reaproveita
 * toda a lógica de renderização e colisão do NPC, adicionando o comportamento
 * exclusivo de avaliar o jogador.
 */
public class Merchant extends NPC implements QuizProvider {

    // POO 5 - Associação Simples / Agregação
    // O comerciante precisa acessar as perguntas, mas ele "não é dono" do banco de dados.
    // Usamos agregação para injetar o QuestionBank, mantendo o acoplamento baixo.
    private QuestionBank questionBank;

    // POO 3 - Construtores em ≥2 classes concretas
    // O construtor espelha exatamente as necessidades da classe mãe (NPC),
    // exigindo coordenadas espaciais para que Íon exista fisicamente no mapa.
    public Merchant(String name, double x, double y, double width, double height, 
                    String dialogueId, QuestionBank questionBank) {
        // O comando 'super' repassa os dados geográficos e de diálogo para a classe NPC.
        super(name, x, y, width, height, dialogueId);
        
        // Vincula o banco de questões instanciado externamente a este NPC.
        this.questionBank = questionBank;
    }

    /**
     * POO 8 - Interfaces (Implementação obrigatória de QuizProvider)
     * 
     * O progresso do capítulo chega de fora. O conjunto de nomes representa as
     * habilidades já aprendidas pelo Hero quando o quiz começa.
     */
    @Override
    public void startQuiz(Hero hero, ChapterProgress progress, Set<String> unlockedAbilities) {
        
        // 1. ANÁLISE DE PROGRESSO: Descobre qual prova o jogador está apto a fazer.
        KnowledgeTopic currentTopic = determineAvailableQuizTopic(progress, unlockedAbilities);
        
        // Se retornar null, o jogador não explorou o suficiente ou já gabaritou tudo.
        if (currentTopic == null) {
            JOptionPane.showMessageDialog(null, 
                "Íon: Volte quando tiver explorado as ruínas e aprendido algo novo.", 
                "Íon, o Comerciante", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. BUSCA DE DADOS: Solicita 4 questões aleatórias filtradas pelo tópico descoberto.
        ArrayList<PhysicsQuestion> quizQuestions = questionBank.getQuestionsForQuiz(currentTopic, 4);
        
        // Trava de segurança: Se o CSV não carregou direito, evita travar o jogo.
        if (quizQuestions.size() < 4) {
            System.err.println("Erro: Questões insuficientes para a prova de " + currentTopic.name());
            return;
        }

        int correctAnswers = 0; // Contador de acertos para a nota de corte
        
        // POO 12 - Entrada via caixa de diálogo (Swing)
        // O uso do JOptionPane cria uma janela modal (trava o jogo de fundo) e opera
        // de forma isolada, não interferindo na renderização do JavaFX.
        JOptionPane.showMessageDialog(null, 
            "Íon: Muito bem! Vamos ver se o que você aprendeu se sustenta na prática.", 
            "Prova de Física", JOptionPane.INFORMATION_MESSAGE);

        // 3. EXECUÇÃO DO QUIZ: Itera sobre as 4 questões sorteadas.
        for (PhysicsQuestion q : quizQuestions) {
            
            // Renderiza a pergunta e transforma o array de Strings (Options) em botões clicáveis.
            int choice = JOptionPane.showOptionDialog(null,
                    q.getPrompt(),
                    "Teste: " + q.getTopic().name(), // Exibe o tópico atual no título da janela
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    q.getOptions(),
                    q.getOptions()[0]); // Opção default (foco do teclado)

            // Tratamento de interrupção: O jogador clicou no 'X' vermelho da janela.
            // CLOSED_OPTION equivale a -1. Se não tratarmos isso, o código tentaria
            // acessar q.getOptions()[-1] e causaria um ArrayIndexOutOfBoundsException.
            if (choice == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, 
                    "Íon: Evitando a pergunta? A física não espera por ninguém.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                return; // Encerra a prova prematuramente, sem dar a recompensa.
            }

            // 4. FEEDBACK PEDAGÓGICO: Resgata a explicação exata da alternativa escolhida.
            String feedback = q.getFeedbackForOption(choice);
            
            // Valida a resposta através do encapsulamento da classe PhysicsQuestion
            if (q.isCorrect(choice)) {
                correctAnswers++;
                JOptionPane.showMessageDialog(null, "Correto!\n\n" + feedback, "Íon - Resultado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Incorreto.\n\n" + feedback, "Íon - Resultado", JOptionPane.ERROR_MESSAGE);
            }
        }

        // 5. RESOLUÇÃO: Aciona o método auxiliar para verificar a nota de corte (3 acertos).
        deliverReward(hero, unlockedAbilities, currentTopic, correctAnswers);
    }

    /**
     * MÉTODO AUXILIAR: Avalia o cenário de trás para a frente (da Prova III para a Prova I).
     * Porquê de trás para a frente? Porque se avaliássemos a Prova I primeiro, um jogador 
     * experiente (que já sabe tudo) ficaria sempre preso na Prova I caso falhasse na verificação.
     */
    private KnowledgeTopic determineAvailableQuizTopic(ChapterProgress progress, Set<String> unlockedAbilities) {
        
        // Verifica Prova III (Tópico de Potencial e habilidade Escudo Equipotencial)
        // Só entra aqui se o jogador estudou o assunto E AINDA NÃO tem a habilidade.
        if (progress.hasLearned(KnowledgeTopic.KNOW_POTENTIAL) && !unlockedAbilities.contains("Escudo Equipotencial")) {
            return KnowledgeTopic.KNOW_POTENTIAL;
        }
        
        // Verifica Prova II (Tópico de Campo Elétrico e habilidade Impulso de Campo)
        if (progress.hasLearned(KnowledgeTopic.KNOW_FIELD) && !unlockedAbilities.contains("Impulso de Campo")) {
            return KnowledgeTopic.KNOW_FIELD;
        }
        
        // Verifica Prova I (Tópico de Sinais de Carga e habilidade Pulso de Repulsão)
        if (progress.hasLearned(KnowledgeTopic.KNOW_CHARGE_SIGNS) && !unlockedAbilities.contains("Pulso de Repulsão")) {
            return KnowledgeTopic.KNOW_CHARGE_SIGNS;
        }

        // Se falhar em todas as verificações, retorna null.
        return null; 
    }

    /**
     * MÉTODO AUXILIAR: Verifica se o jogador atingiu a nota de corte e entrega a recompensa.
     * O livro do Hero guarda a recompensa entre as batalhas.
     */
    private void deliverReward(Hero hero, Set<String> unlockedAbilities, KnowledgeTopic topic, int correctAnswers) {
        
        // A nota de corte definida no roteiro pedagógico é acertar 3 de 4 questões.
        if (correctAnswers >= 3) {
            String rewardMsg = "Brilhante! O seu entendimento compra esta técnica:\n";
            Ability reward = createReward(topic);

            boolean equipNow = hero.getAbilityBook()
                    .getEquippedAbility() == null;

            // A recompensa passa a existir no livro do Hero.
            if (equipNow) {
                hero.learnAbility(reward, true);
            } else {
                hero.learnAbility(reward);
            }
            unlockedAbilities.add(reward.getName());
            rewardMsg += "-> Nova Habilidade Adquirida: "
                    + reward.getName() + "!";
            
            JOptionPane.showMessageDialog(null, rewardMsg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Caso o jogador erre mais de 1 questão, ele não sofre dano, apenas perde a chance.
            JOptionPane.showMessageDialog(null, 
                "Íon: Você acertou " + correctAnswers + " de 4.\n"
              + "As faíscas ainda confundem a sua mente. Tente novamente mais tarde.", 
                "Falha", JOptionPane.ERROR_MESSAGE);
        }
    }

    static Ability createReward(KnowledgeTopic topic) {
        if (topic == null) {
            throw new IllegalArgumentException("O topico nao pode ser nulo.");
        }

        return switch (topic) {
            case KNOW_CHARGE_SIGNS, KNOW_ELECTRIZATION ->
                new RepulsionPulse();
            case KNOW_FIELD, KNOW_COULOMB ->
                new FieldImpulse();
            case KNOW_POTENTIAL ->
                new EquipotentialShield();
        };
    }
}
