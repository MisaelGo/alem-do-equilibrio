package com.alemdoequilibrio.core;

/**
 * Classe utilitária concreta responsável por fornecer constantes e
 * métodos físicos de eletrostática para o mundo de "A Terra das Cargas".
 */
public class ElectricPhysics {

    // POO 1.5 - Constante primitiva em classe concreta usada por outra
    // Justificativa: K_COULOMB representa a constante elétrica (k) no vácuo.
    // É um valor primitivo, universal e imutável necessário para os cálculos 
    // da lei de Coulomb por outras classes (ex: cálculo de dano no BattleController).
    public static final double K_COULOMB = 9.0e9; // N * m^2 / C^2

    // POO 1.1 - Encapsulamento
    // Justificativa: O construtor privado impede a instanciação desta classe.
    // Como é uma classe estritamente utilitária, garantimos que ela seja 
    // acessada apenas através de seus métodos e constantes estáticas.
    private ElectricPhysics() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não deve ser instanciada.");
    }

    /**
     * Calcula o módulo da força elétrica entre duas cargas puntiformes.
     * 
     * POO 2.2 - Método static necessário
     * Justificativa: O cálculo da força de Coulomb é uma operação matemática 
     * pura baseada em leis da física. Não depende do estado interno de nenhum 
     * objeto instanciado, justificando plenamente o uso de um método estático.
     * 
     * @param q1 Carga 1 em Coulombs (C)
     * @param q2 Carga 2 em Coulombs (C)
     * @param distance Distância entre as cargas em metros (m)
     * @return O módulo da força elétrica em Newtons (N)
     */
    public static double coulombForce(double q1, double q2, double distance) {
        if (distance <= 0) {
            throw new IllegalArgumentException("A distância r deve ser maior que zero.");
        }
        
        // Aplicação da Lei de Coulomb: F = k * |q1 * q2| / r^2
        return K_COULOMB * (Math.abs(q1 * q2)) / Math.pow(distance, 2);
    }
    
    /**
     * Calcula o módulo do Campo Elétrico gerado por uma carga puntiforme.
     * 
     * @param q Carga fonte em Coulombs (C)
     * @param distance Distância do ponto até a carga em metros (m)
     * @return O módulo do campo elétrico em Newtons por Coulomb (N/C)
     */
    public static double electricFieldMagnitude(double q, double distance) {
        if (distance <= 0) {
            throw new IllegalArgumentException("A distância r deve ser maior que zero.");
        }
        
        // Aplicação do campo elétrico de uma carga puntiforme: E = k * |q| / r^2
        return K_COULOMB * Math.abs(q) / Math.pow(distance, 2);
    }
}