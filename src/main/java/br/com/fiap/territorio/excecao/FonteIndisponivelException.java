package br.com.fiap.territorio.excecao;

/**
 * Sinaliza que o sensor esta desligado ou nao respondeu a tentativa de leitura.
 */
public class FonteIndisponivelException extends Exception {

    private final String codigoSensor;

    public FonteIndisponivelException(String codigoSensor, String mensagem) {
        super(mensagem);
        this.codigoSensor = codigoSensor;
    }

    public String getCodigoSensor() {
        return codigoSensor;
    }
}
