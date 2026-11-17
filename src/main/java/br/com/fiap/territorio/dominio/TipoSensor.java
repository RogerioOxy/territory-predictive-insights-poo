package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

/**
 * Reune a unidade e a faixa fisica de cada grandeza medida.
 */
public enum TipoSensor {

    PLUVIOMETRO(Mensagens.NOME_PLUVIOMETRO, "mm/h", 0.0, 300.0),
    NIVEL_CORREGO(Mensagens.NOME_NIVEL_CORREGO, "m", 0.0, 6.0),
    OBSTRUCAO_BUEIRO(Mensagens.NOME_OBSTRUCAO_BUEIRO, "%", 0.0, 100.0);

    private final String nome;
    private final String unidade;
    private final double minimoFisico;
    private final double maximoFisico;

    TipoSensor(String nome, String unidade, double minimoFisico, double maximoFisico) {
        this.nome = nome;
        this.unidade = unidade;
        this.minimoFisico = minimoFisico;
        this.maximoFisico = maximoFisico;
    }

    public boolean aceita(double valor) {
        return Double.isFinite(valor) && valor >= minimoFisico && valor <= maximoFisico;
    }

    public double limitarAFaixa(double valor) {
        return Math.max(minimoFisico, Math.min(maximoFisico, valor));
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimoFisico() {
        return minimoFisico;
    }

    public double getMaximoFisico() {
        return maximoFisico;
    }
}
