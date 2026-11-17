package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.FonteIndisponivelException;

/**
 * Contrato comum para ler e controlar sensores sem depender de sua classe concreta.
 */
public interface Sensor {

    double lerValor() throws FonteIndisponivelException;

    boolean verificarFuncionamento();

    TipoSensor getTipo();

    String getCodigo();

    void definirFuncionamento(boolean funcionando);

    default void retomarDe(double ultimoValorRegistrado) {
    }
}
