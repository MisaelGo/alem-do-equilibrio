package com.alemdoequilibrio.core;

/**
 * C1-055 - Excecao customizada.
 *
 * Representa erros semanticos ou incompatibilidades
 * no formato do arquivo de save.
 *
 * Erros de acesso ao arquivo continuam sendo tratados
 * como IOException.
 */
public class SaveFormatException
        extends Exception {

    public SaveFormatException(
            String message) {

        super(message);
    }

    public SaveFormatException(
            String message,
            Throwable cause) {

        super(
                message,
                cause
        );
    }
}