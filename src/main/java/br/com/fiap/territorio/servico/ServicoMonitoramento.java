package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.Limiares;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.AlertaRepository;
import br.com.fiap.territorio.persistencia.MedicaoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Coordena leituras, previsoes e persistencia mantendo as regras fora do menu.
 */
@Service
public class ServicoMonitoramento {

    private final MedicaoRepository medicaoRepository;
    private final AlertaRepository alertaRepository;
    private final ConfiguracaoSistema configuracao;
    private final FabricaPrevisao fabricaPrevisao;
    private final MapaTerritorio mapa;
    private final Clock relogio;

    public ServicoMonitoramento(MedicaoRepository medicaoRepository, AlertaRepository alertaRepository,
                                ConfiguracaoSistema configuracao, FabricaPrevisao fabricaPrevisao,
                                MapaTerritorio mapa, Clock relogio) {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
        this.configuracao = configuracao;
        this.fabricaPrevisao = fabricaPrevisao;
        this.mapa = mapa;
        this.relogio = relogio;
    }

    public int carregarSementeSeVazio() throws DadosInvalidosException {
        // A semente so entra sem medicoes gravadas, preservando o historico existente.
        if (medicaoRepository.count() > 0) {
            return 0;
        }
        LocalDateTime agora = LocalDateTime.now(relogio);
        List<Medicao> lote = new ArrayList<>();
        for (Sensor sensor : mapa.listarSensores()) {
            double[] serie = SementeHistorico.serieDo(sensor.getCodigo());
            for (int i = 0; i < serie.length; i++) {
                lote.add(new Medicao(sensor.getCodigo(), codigoPontoDe(sensor), sensor.getTipo(), serie[i],
                        agora.minusHours(serie.length - i)));
            }
        }
        medicaoRepository.saveAll(lote);
        return lote.size();
    }

    // Retoma o valor persistido para a serie nao voltar ao ponto inicial a cada execucao.
    public void retomarSimulacaoDoHistorico() {
        for (Sensor sensor : mapa.listarSensores()) {
            medicaoRepository.findFirstByCodigoSensorOrderByDataHoraDescIdDesc(sensor.getCodigo())
                    .ifPresent(ultima -> sensor.retomarDe(ultima.getValor()));
        }
    }

    public Medicao lerERegistrar(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        double valorLido = sensor.lerValor();
        // A validacao ocorre antes do save; se houver excecao, a leitura nao chega ao repository.
        Medicao medicao = new Medicao(sensor.getCodigo(), codigoPontoDe(sensor), sensor.getTipo(), valorLido,
                LocalDateTime.now(relogio));
        return medicaoRepository.save(medicao);
    }

    public List<ResultadoAnalise> rodarPrevisoes() {
        List<EstrategiaPrevisao> estrategias = fabricaPrevisao.criarEstrategiasAtivas();
        List<ResultadoAnalise> resultados = new ArrayList<>();
        for (Sensor sensor : mapa.listarSensores()) {
            List<Medicao> historico = buscarHistoricoPorSensor(sensor.getCodigo());
            List<Double> valores = historico.stream().map(Medicao::getValor).toList();
            Limiares limiares = configuracao.getLimiares(sensor.getTipo());
            for (EstrategiaPrevisao estrategia : estrategias) {
                // A chamada polimorfica usa o calculo da estrategia e depois limita a previsao a faixa fisica.
                ResultadoPrevisao previsao = estrategia.prever(valores).limitadoA(sensor.getTipo());
                resultados.add(analisar(sensor, historico, limiares, previsao));
            }
        }
        return resultados;
    }

    private ResultadoAnalise analisar(Sensor sensor, List<Medicao> historico, Limiares limiares,
                                      ResultadoPrevisao previsao) {
        // Sem historico suficiente nao ha base para classificar risco nem emitir alerta.
        if (!previsao.isSuficiente()) {
            return new ResultadoAnalise(sensor, previsao, limiares, null, null);
        }
        Optional<NivelAlerta> nivel = limiares.classificar(previsao.getValorPrevisto());
        if (nivel.isEmpty()) {
            return new ResultadoAnalise(sensor, previsao, limiares, null, null);
        }
        Long idUltimaMedicao = historico.get(historico.size() - 1).getId();
        // Sensor, estrategia, nivel, limite aplicado e ultima medicao identificam o alerta.
        // Se o limiar mudar, a nova avaliacao precisa ficar registrada.
        double limiteAtingido = limiares.getLimite(nivel.get());
        boolean jaRegistrado = alertaRepository
                .existsByCodigoSensorAndTipoPrevisaoAndNivelAndLimiteUltrapassadoAndIdUltimaMedicao(
                        sensor.getCodigo(), previsao.getTipoPrevisao(), nivel.get(), limiteAtingido,
                        idUltimaMedicao);
        if (jaRegistrado) {
            return new ResultadoAnalise(sensor, previsao, limiares, nivel.get(), null);
        }
        Alerta alerta = new Alerta(sensor.getCodigo(), codigoPontoDe(sensor), sensor.getTipo(),
                previsao.getTipoPrevisao(), nivel.get(), previsao.getValorPrevisto(),
                limiteAtingido, idUltimaMedicao, LocalDateTime.now(relogio));
        alertaRepository.save(alerta);
        return new ResultadoAnalise(sensor, previsao, limiares, nivel.get(), alerta);
    }

    public List<Medicao> buscarHistoricoPorSensor(String codigoSensor) {
        return medicaoRepository.findByCodigoSensorOrderByDataHoraAscIdAsc(codigoSensor);
    }

    public List<Alerta> listarAlertasEmitidos() {
        return alertaRepository.findAllByOrderByDataHoraDescIdDesc();
    }

    public Optional<Alerta> ultimoAlerta() {
        return alertaRepository.findFirstByOrderByDataHoraDescIdDesc();
    }

    public Optional<Medicao> ultimaMedicao(String codigoSensor) {
        return medicaoRepository.findFirstByCodigoSensorOrderByDataHoraDescIdDesc(codigoSensor);
    }

    public long contarMedicoes(String codigoSensor) {
        return medicaoRepository.countByCodigoSensor(codigoSensor);
    }

    public long contarAlertas() {
        return alertaRepository.count();
    }

    public int leiturasNecessariasParaPrever() {
        return fabricaPrevisao.criarEstrategiasAtivas().stream()
                .mapToInt(EstrategiaPrevisao::leiturasNecessarias)
                .max()
                .orElse(configuracao.getMinimoLeituras());
    }

    private String codigoPontoDe(Sensor sensor) {
        return mapa.pontoDoSensor(sensor.getCodigo())
                .orElseThrow(() -> new IllegalStateException(
                        Formatador.f(Mensagens.ERRO_SENSOR_SEM_PONTO, sensor.getCodigo())))
                .getId();
    }
}
