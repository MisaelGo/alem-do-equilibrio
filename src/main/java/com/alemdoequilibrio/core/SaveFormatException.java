package com.alemdoequilibrio.core;

/**
 * POO 11 - Exceção customizada, levantada e tratada.
 * SaveManager lança SaveFormatException quando um arquivo salvo
 * possui campos inválidos; GameController a captura ao tentar
 * continuar a partida, evitando carregar estado corrompido.
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