package br.com.fiap.territorio.dominio;

/**
 * Transporta o calculo ou a falta de historico suficiente, junto da explicacao.
 */
public final class ResultadoPrevisao {

    private final TipoPrevisao tipoPrevisao;
    private final boolean suficiente;
    private final double valorPrevisto;
    private final int leiturasDisponiveis;
    private final int leiturasNecessarias;
    private final String comoFoiCalculado;

    private ResultadoPrevisao(TipoPrevisao tipoPrevisao, boolean suficiente, double valorPrevisto,
                              int leiturasDisponiveis, int leiturasNecessarias, String comoFoiCalculado) {
        this.tipoPrevisao = tipoPrevisao;
        this.suficiente = suficiente;
        this.valorPrevisto = valorPrevisto;
        this.leiturasDisponiveis = leiturasDisponiveis;
        this.leiturasNecessarias = leiturasNecessarias;
        this.comoFoiCalculado = comoFoiCalculado;
    }

    public static ResultadoPrevisao calculada(TipoPrevisao tipo, double valorPrevisto, int leiturasDisponiveis,
                                              int leiturasNecessarias, String comoFoiCalculado) {
        return new ResultadoPrevisao(tipo, true, valorPrevisto, leiturasDisponiveis, leiturasNecessarias,
                comoFoiCalculado);
    }

    public static ResultadoPrevisao insuficiente(TipoPrevisao tipo, int leiturasDisponiveis, int leiturasNecessarias) {
        return new ResultadoPrevisao(tipo, false, Double.NaN, leiturasDisponiveis, leiturasNecessarias, "");
    }

    public ResultadoPrevisao limitadoA(TipoSensor tipoSensor) {
        if (!suficiente) {
            return this;
        }
        return new ResultadoPrevisao(tipoPrevisao, true, tipoSensor.limitarAFaixa(valorPrevisto),
                leiturasDisponiveis, leiturasNecessarias, comoFoiCalculado);
    }

    public TipoPrevisao getTipoPrevisao() {
        return tipoPrevisao;
    }

    public boolean isSuficiente() {
        return suficiente;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public int getLeiturasDisponiveis() {
        return leiturasDisponiveis;
    }

    public int getLeiturasNecessarias() {
        return leiturasNecessarias;
    }

    public String getComoFoiCalculado() {
        return comoFoiCalculado;
    }
}
