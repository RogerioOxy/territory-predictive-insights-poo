package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;

import java.util.List;

/**
 * Especializa a previsao usando a media das leituras mais recentes.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    public PrevisaoMediaMovel(int janela, int minimoLeituras) {
        // super inicializa os parametros comuns na base abstrata.
        super(TipoPrevisao.MEDIA_MOVEL, janela, minimoLeituras);
    }

    @Override
    protected int leiturasDoCalculo() {
        return getJanela();
    }

    @Override
    public ResultadoPrevisao prever(List<Double> historicoCronologico) {
        // Sem a quantidade necessaria, informa silencio em vez de calcular com uma janela incompleta.
        if (!temLeiturasSuficientes(historicoCronologico)) {
            return silencio(historicoCronologico);
        }
        double mediaRecente = media(ultimas(historicoCronologico, getJanela()));
        String comoFoiCalculado = Formatador.f(Mensagens.CALCULO_MEDIA_MOVEL, getJanela(), mediaRecente);
        return ResultadoPrevisao.calculada(getTipo(), mediaRecente, historicoCronologico.size(),
                leiturasNecessarias(), comoFoiCalculado);
    }
}
