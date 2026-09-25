package com.alemdoequilibrio.game;

/**
 * Enumeração que representa a polaridade das cargas elétricas no jogo.
 * 
 * POO 1.3 - Atributo enum
 * Justificativa: Define os tipos específicos e imutáveis de carga (POSITIVE,
 * NEGATIVE, NEUTRAL) previstos no domínio da Física III para o Capítulo 1.
 * Ao ser utilizado como atributo em Hero e Enemy, garante segurança de tipo
 * nas interações e simplifica a lógica de atração/repulsão no combate.
 */
public enum ChargeType {
    
    POSITIVE(1, "+", "Positiva"),
    NEGATIVE(-1, "-", "Negativa"),
    NEUTRAL(0, "0", "Neutra");

    // POO 1.1 - Encapsulamento
    // Justificativa: Atributos declarados como private e final. O estado 
    // interno das constantes do enum é protegido e acessível apenas via getters.
    private final int signValue;
    private final String symbol;
    private final String description;

    ChargeType(int signValue, String symbol, String description) {
        this.signValue = signValue;
        this.symbol = symbol;
        this.description = description;
    }

    public int getSignValue() {
        return signValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }
}