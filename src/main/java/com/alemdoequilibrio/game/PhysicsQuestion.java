package com.alemdoequilibrio.game;

/**
 * Modelo de dados para as questões dos quizzes do comerciante Íon.
 * A classe foi projetada para suportar exatamente 4 alternativas,
 * cada uma com seu respectivo feedback pedagógico.
 */
public class PhysicsQuestion {

    // POO 1.1 - Encapsulamento
    // Justificativa: Todos os atributos são privados para proteger a integridade
    // da pergunta, das alternativas e do gabarito (correctIndex). O acesso 
    // a essas informações ocorre estritamente via métodos getters.
    private String id;
    private String topic; // Refere-se às flags de conhecimento, ex: "KNOW_CHARGE_SIGNS"
    private int difficulty;
    private String prompt;
    private String[] options;
    private String[] feedbacks;
    private int correctIndex;

    // POO 3 - Construtores
    // Justificativa: O construtor obriga que a questão seja instanciada com
    // estado completo e válido. Isso evita que objetos incompletos 
    // entrem no QuestionBank e causem NullPointerExceptions no meio do quiz.
    public PhysicsQuestion(String id, String topic, int difficulty, String prompt, 
                           String[] options, String[] feedbacks, int correctIndex) {
        
        if (options == null || options.length != 4) {
            throw new IllegalArgumentException("O quiz deve conter exatamente 4 alternativas.");
        }
        if (feedbacks == null || feedbacks.length != 4) {
            throw new IllegalArgumentException("O quiz deve conter exatamente 4 feedbacks.");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("O índice correto deve estar entre 0 e 3.");
        }
        
        this.id = id;
        this.topic = topic;
        this.difficulty = difficulty;
        this.prompt = prompt;
        this.options = options;
        this.feedbacks = feedbacks;
        this.correctIndex = correctIndex;
    }

    // Getters
    public String getId() { return id; }
    public String getTopic() { return topic; }
    public int getDifficulty() { return difficulty; }
    public String getPrompt() { return prompt; }
    public String[] getOptions() { return options; }
    public String[] getFeedbacks() { return feedbacks; }
    public int getCorrectIndex() { return correctIndex; }

    /**
     * Verifica se o índice escolhido pelo jogador corresponde ao gabarito.
     * @param selectedIndex O índice da alternativa (0 a 3) escolhida pelo jogador.
     * @return true se for a alternativa correta, false caso contrário.
     */
    public boolean isCorrect(int selectedIndex) {
        return this.correctIndex == selectedIndex;
    }

    /**
     * Retorna o feedback pedagógico para a alternativa selecionada pelo jogador.
     * @param selectedIndex O índice da alternativa (0 a 3) escolhida pelo jogador.
     * @return A string contendo o feedback específico da alternativa.
     */
    public String getFeedbackForOption(int selectedIndex) {
        if (selectedIndex >= 0 && selectedIndex < feedbacks.length) {
            return feedbacks[selectedIndex];
        }
        return "Índice de alternativa inválido.";
    }
}