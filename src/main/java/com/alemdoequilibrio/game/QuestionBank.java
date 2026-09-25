package com.alemdoequilibrio.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe responsável por armazenar, gerenciar e sortear as questões do jogo.
 */
public class QuestionBank {

    // POO 1.4 - Atributo ArrayList
    // Justificativa: Necessário para manter uma coleção dinâmica de perguntas,
    // permitindo adições flexíveis e fácil manipulação estrutural (como embaralhar
    // para o sorteio das provas) sem a limitação de tamanho de um vetor (array) nativo.
    private ArrayList<PhysicsQuestion> questions;

    // POO 3 - Construtor
    // Justificativa: Inicializa a coleção instanciando o ArrayList para 
    // evitar NullPointerException ao adicionar a primeira questão.
    public QuestionBank() {
        this.questions = new ArrayList<>();
    }

    // POO 2.1 - Varargs + FOR
    // Justificativa: O uso de varargs (PhysicsQuestion...) permite passar um número
    // variável de questões de forma limpa. O laço 'for' aprimorado (foreach) é a
    // estrutura ideal para iterar sobre esse conjunto de argumentos e adicioná-los à lista.
    public void addQuestions(PhysicsQuestion... newQuestions) {
        for (PhysicsQuestion q : newQuestions) {
            this.questions.add(q);
        }
    }

    /**
     * Retorna um número específico de questões aleatórias para um determinado tópico.
     * Garante que as tentativas de quiz não sejam repetitivas caso o jogador falhe.
     * 
     * @param topic O tópico da prova, que corresponde à flag (ex: "KNOW_CHARGE_SIGNS").
     * @param amount A quantidade de questões exigida (no nosso caso, 4).
     * @return Um ArrayList contendo as questões sorteadas.
     */
    public ArrayList<PhysicsQuestion> getQuestionsForQuiz(String topic, int amount) {
        ArrayList<PhysicsQuestion> filteredQuestions = new ArrayList<>();
        
        // Filtra o banco, garantindo que nenhuma questão seja oferecida
        // antes do tópico correspondente estar marcado como aprendido
        for (PhysicsQuestion q : this.questions) {
            if (q.getTopic().equals(topic)) {
                filteredQuestions.add(q);
            }
        }

        // Embaralha a lista filtrada para criar a aleatoriedade solicitada
        Collections.shuffle(filteredQuestions);

        // Seleciona a quantidade desejada (ou todas, se o banco tiver menos que o pedido)
        ArrayList<PhysicsQuestion> selectedQuestions = new ArrayList<>();
        int limit = Math.min(amount, filteredQuestions.size());
        for (int i = 0; i < limit; i++) {
            selectedQuestions.add(filteredQuestions.get(i));
        }

        return selectedQuestions;
    }

    /**
     * Lê um arquivo CSV estruturado e popula o banco de questões.
     * 
     * POO 13 - Arquivo gravado e recuperado
     * Justificativa: Cumpre a exigência de manipulação de arquivos (leitura)
     * e garante que o conteúdo científico não fique hard-coded no sistema.
     * 
     * @param filePath O caminho do arquivo contendo as questões (ex: "src/main/resources/data/questoes.csv").
     */
    public void loadQuestionsFromFile(String filePath) {
        // Utilizamos try-with-resources para garantir o fechamento do BufferedReader
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // Pula a primeira linha (cabeçalho do CSV)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Ignora linhas vazias
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Divide a linha usando o ponto e vírgula como delimitador
                String[] data = line.split(";");
                
                // Verifica se a linha contém exatamente as 13 colunas esperadas
                if (data.length == 13) {
                    String id = data[0].trim();
                    String topic = data[1].trim();
                    int difficulty = Integer.parseInt(data[2].trim());
                    String prompt = data[3].trim();
                    
                    // Arrays de opções e feedbacks
                    String[] options = { data[4].trim(), data[5].trim(), data[6].trim(), data[7].trim() };
                    String[] feedbacks = { data[8].trim(), data[9].trim(), data[10].trim(), data[11].trim() };
                    
                    int correctIndex = Integer.parseInt(data[12].trim());

                    // Instancia a questão e a adiciona ao banco usando o varargs
                    PhysicsQuestion question = new PhysicsQuestion(id, topic, difficulty, prompt, options, feedbacks, correctIndex);
                    this.addQuestions(question);
                } else {
                    System.err.println("Linha ignorada por formatação incorreta (Encontradas " + data.length + " colunas): " + line);
                }
            }
            System.out.println("Banco de questões carregado com sucesso. Total: " + this.getTotalQuestions());
            
        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar o arquivo de questões: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter número no arquivo de questões: " + e.getMessage());
        }
    }
    
    /**
     * Retorna o total de questões atualmente carregadas no banco.
     * 
     * @return inteiro correspondente a quantidade atual de questões no banco.
     */
    public int getTotalQuestions() {
        return this.questions.size();
    }
}