package com.alemdoequilibrio.game;

/**
 * Define os dados e o comportamento comuns das habilidades de combate.
 *
 * POO 10 - Classe abstrata:
 * cada habilidade concreta deve implementar seu proprio efeito em execute.
 */
public abstract class Ability {

    private final String name;
    private final String description;

    /**
     * Cria uma habilidade com nome e descricao.
     *
     * @param name nome exibido ao jogador
     * @param description explicacao do efeito da habilidade
     */
    protected Ability(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da habilidade nao pode ser vazio."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descricao da habilidade nao pode ser vazia."
            );
        }

        this.name = name;
        this.description = description;
    }

    /**
     * Executa o efeito da habilidade na batalha atual.
     *
     * O BattleController controla de quem e o turno. As habilidades concretas
     * definirao o efeito depois que o uso de habilidades for integrado a ele.
     *
     * @param battle batalha em que a habilidade sera usada
     */
    public abstract void execute(BattleController battle);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
