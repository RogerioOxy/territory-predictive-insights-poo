package br.com.fiap.territorio.excecao;

/**
 * Indica que o codigo informado nao permite alterar os limiares do territorio.
 */
public class AcessoNegadoException extends Exception {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
