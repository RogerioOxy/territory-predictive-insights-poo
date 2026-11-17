package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

import java.util.List;
import java.util.Objects;

/**
 * Base abstrata que compartilha as regras de historico e deixa o calculo para as filhas.
 */
public abstract class EstrategiaPrevisao {

    private final TipoPrevisao tipo;
    private final int janela;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(TipoPrevisao tipo, int janela, int minimoLeituras) {
        if (janela < 1 || minimoLeituras < 1) {
            throw new IllegalArgumentException(Mensagens.ERRO_JANELA_INVALIDA);
        }
        this.tipo = Objects.requireNonNull(tipo);
        this.janela = janela;
        this.minimoLeituras = minimoLeituras;
    }

    public abstract ResultadoPrevisao prever(List<Double> historicoCronologico);

    protected abstract int leiturasDoCalculo();

    // Respeita tanto o minimo configurado quanto a quantidade exigida pelo calculo.
    public int leiturasNecessarias() {
        return Math.max(minimoLeituras, leiturasDoCalculo());
    }

    protected boolean temLeiturasSuficientes(List<Double> historico) {
        return historico != null && historico.size() >= leiturasNecessarias();
    }

    protected ResultadoPrevisao silencio(List<Double> historico) {
        int disponiveis = historico == null ? 0 : historico.size();
        return ResultadoPrevisao.insuficiente(tipo, disponiveis, leiturasNecessarias());
    }

    protected double media(List<Double> valores) {
        double soma = 0.0;
        for (double valor : valores) {
            soma += valor;
        }
        return soma / valores.size();
    }

    protected List<Double> ultimas(List<Double> historico, int quantidade) {
        return historico.subList(historico.size() - quantidade, historico.size());
    }

    public TipoPrevisao getTipo() {
        return tipo;
    }

    public int getJanela() {
        return janela;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }
}
