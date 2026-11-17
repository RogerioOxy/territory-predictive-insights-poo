package br.com.fiap.territorio.excecao;

/**
 * Sinaliza dados recusados pelas regras do dominio para o menu explicar o problema.
 */
public class DadosInvalidosException extends Exception {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
