package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.dominio.Pluviometro;
import br.com.fiap.territorio.dominio.PontoVulneravel;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.SensorNivelCorrego;
import br.com.fiap.territorio.dominio.SensorObstrucaoBueiro;
import br.com.fiap.territorio.dominio.TipoPonto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Organiza os pontos e seus sensores para localizar a origem de cada leitura.
 */
public class MapaTerritorio {

    private final List<PontoVulneravel> pontos;

    public MapaTerritorio(List<PontoVulneravel> pontos) {
        this.pontos = List.copyOf(pontos);
    }

    public static MapaTerritorio criarPadrao(Random random) {
        PontoVulneravel bocaDeLobo = new PontoVulneravel("BL-01", Mensagens.NOME_PONTO_BOCA_DE_LOBO,
                Mensagens.REGIAO_PADRAO, TipoPonto.BOCA_DE_LOBO);
        bocaDeLobo.instalarSensor(new Pluviometro("PLU-01", random));
        bocaDeLobo.instalarSensor(new SensorObstrucaoBueiro("OBS-01", random));

        PontoVulneravel corrego = new PontoVulneravel("CR-01", Mensagens.NOME_PONTO_CORREGO,
                Mensagens.REGIAO_PADRAO, TipoPonto.TRECHO_DE_CORREGO);
        corrego.instalarSensor(new SensorNivelCorrego("NIV-01", random));

        return new MapaTerritorio(List.of(bocaDeLobo, corrego));
    }

    public List<PontoVulneravel> getPontos() {
        return pontos;
    }

    public List<Sensor> listarSensores() {
        List<Sensor> sensores = new ArrayList<>();
        for (PontoVulneravel ponto : pontos) {
            sensores.addAll(ponto.getSensores());
        }
        return sensores;
    }

    public Optional<PontoVulneravel> pontoDoSensor(String codigoSensor) {
        return pontos.stream()
                .filter(p -> p.getSensores().stream().anyMatch(s -> s.getCodigo().equals(codigoSensor)))
                .findFirst();
    }
}
