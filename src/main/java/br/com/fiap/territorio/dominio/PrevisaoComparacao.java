package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;

import java.util.List;

/**
 * Especializa a previsao projetando a continuidade da variacao entre dois periodos.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    public PrevisaoComparacao(int janela, int minimoLeituras) {
        // super inicializa os parametros comuns na base abstrata.
        super(TipoPrevisao.COMPARACAO, janela, minimoLeituras);
    }

    @Override
    protected int leiturasDoCalculo() {
        return getJanela() * 2;
    }

    @Override
    public ResultadoPrevisao prever(List<Double> historicoCronologico) {
        // Com janela de tres, exige seis leituras para comparar dois periodos completos.
        if (!temLeiturasSuficientes(historicoCronologico)) {
            return silencio(historicoCronologico);
        }
        int total = historicoCronologico.size();
        List<Double> periodoAtual = ultimas(historicoCronologico, getJanela());
        List<Double> periodoAnterior = historicoCronologico.subList(total - 2 * getJanela(), total - getJanela());
        double mediaAtual = media(periodoAtual);
        double mediaAnterior = media(periodoAnterior);
        double variacao = mediaAtual - mediaAnterior;
        // Projeta a mesma variacao mais uma vez, sem permitir resultado negativo.
        double valorPrevisto = Math.max(0.0, mediaAtual + variacao);
        String comoFoiCalculado = Formatador.f(Mensagens.CALCULO_COMPARACAO, getJanela(), mediaAnterior,
                mediaAtual, variacao);
        return ResultadoPrevisao.calculada(getTipo(), valorPrevisto, total, leiturasNecessarias(), comoFoiCalculado);
    }
}
