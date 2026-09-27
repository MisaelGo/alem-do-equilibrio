package com.alemdoequilibrio.game;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Classe responsável por armazenar, gerenciar e sortear as questões do jogo.
 */
public class QuestionBank {

    // POO 1.4 - Atributo ArrayList
    private ArrayList<PhysicsQuestion> questions;

    // POO 3 - Construtor
    public QuestionBank() {
        this.questions = new ArrayList<>();
    }

    // POO 2.1 - Varargs + FOR
    public void addQuestions(PhysicsQuestion... newQuestions) {
        for (PhysicsQuestion q : newQuestions) {
            this.questions.add(q);
        }
    }

    /**
     * Retorna um número específico de questões aleatórias para um determinado tópico.
     * 
     * @param topic O tópico da prova tipado (KnowledgeTopic).
     * @param amount A quantidade de questões exigida.
     * @return Um ArrayList contendo as questões sorteadas.
     */
    public ArrayList<PhysicsQuestion> getQuestionsForQuiz(KnowledgeTopic topic, int amount) {
        ArrayList<PhysicsQuestion> filteredQuestions = new ArrayList<>();
        
        // Filtra o banco, comparando com o tipo forte KnowledgeTopic
        for (PhysicsQuestion q : this.questions) {
            // Em Java, enums podem ser comparados com == com segurança
            if (q.getTopic() == topic) {
                filteredQuestions.add(q);
            }
        }

        Collections.shuffle(filteredQuestions);

        ArrayList<PhysicsQuestion> selectedQuestions = new ArrayList<>();
        int limit = Math.min(amount, filteredQuestions.size());
        for (int i = 0; i < limit; i++) {
            selectedQuestions.add(filteredQuestions.get(i));
        }

        return selectedQuestions;
    }

    /**
     * Lê um arquivo CSV estruturado a partir do classpath e popula o banco.
     * 
     * POO 13 - Arquivo gravado e recuperado
     * @param resourcePath O caminho interno do recurso (ex: "/questions/questions.csv").
     */
    public void loadQuestionsFromFile(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            
            if (is == null) {
                System.err.println("Erro: Arquivo não encontrado no classpath -> " + resourcePath);
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    
                    if (data.length == 13) {
                        String id = data[0].replace("\"", "").trim();
                        
                        // --- AJUSTE DE INTEGRAÇÃO ---
                        // Capturamos a String do CSV e a convertemos para o tipo KnowledgeTopic.
                        // O método valueOf procura a constante exata declarada no enum.
                        String topicString = data[1].replace("\"", "").trim();
                        KnowledgeTopic topic = KnowledgeTopic.valueOf(topicString);
                        // -----------------------------
                        
                        int difficulty = Integer.parseInt(data[2].trim());
                        String prompt = data[3].replace("\"", "").trim();
                        
                        String[] options = { 
                            data[4].replace("\"", "").trim(), data[5].replace("\"", "").trim(), 
                            data[6].replace("\"", "").trim(), data[7].replace("\"", "").trim() 
                        };
                        String[] feedbacks = { 
                            data[8].replace("\"", "").trim(), data[9].replace("\"", "").trim(), 
                            data[10].replace("\"", "").trim(), data[11].replace("\"", "").trim() 
                        };
                        
                        int correctIndex = Integer.parseInt(data[12].trim());

                        PhysicsQuestion question = new PhysicsQuestion(id, topic, difficulty, prompt, options, feedbacks, correctIndex);
                        this.addQuestions(question);
                    } else {
                        System.err.println("Linha ignorada por formatação incorreta (Encontradas " + data.length + " colunas): " + line);
                    }
                }
                System.out.println("Banco de questões carregado com sucesso. Total: " + this.getTotalQuestions());
            }
            
        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar o arquivo de questões: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Este bloco agora também captura falhas no KnowledgeTopic.valueOf()
            // caso o CSV contenha um tópico que não foi declarado no enum KnowledgeTopic.
            System.err.println("Erro de conversão de dados (Número ou Tópico inválido) no arquivo de questões: " + e.getMessage());
        }
    }
    
    public int getTotalQuestions() {
        return this.questions.size();
    }
}