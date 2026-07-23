package br.com.crdc.f1.commons.cadastro.exception;

public class CadastroClientException extends RuntimeException {

    public CadastroClientException(String message) {
        super(message);
    }

    public CadastroClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
