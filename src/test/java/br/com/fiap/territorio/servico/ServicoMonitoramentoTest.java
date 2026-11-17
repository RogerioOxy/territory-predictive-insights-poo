package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.Pluviometro;
import br.com.fiap.territorio.dominio.PontoVulneravel;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoPonto;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.AlertaRepository;
import br.com.fiap.territorio.persistencia.MedicaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras do servico com repositories simulados pelo Mockito, sem banco.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    private static final Clock RELOGIO_FIXO = Clock.fixed(Instant.parse("2026-11-16T15:00:00Z"), ZoneOffset.UTC);

    @Mock
    private MedicaoRepository medicaoRepository;

    @Mock
    private AlertaRepository alertaRepository;

    private Pluviometro pluviometro;
    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {
        pluviometro = new Pluviometro("PLU-T", new Random(7), 0.0, 0.0);
        PontoVulneravel ponto = new PontoVulneravel("BL-T", "BL-T", "R-T", TipoPonto.BOCA_DE_LOBO);
        ponto.instalarSensor(pluviometro);
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstance();
        servico = new ServicoMonitoramento(medicaoRepository, alertaRepository, configuracao,
                new FabricaPrevisao(configuracao), new MapaTerritorio(List.of(ponto)), RELOGIO_FIXO);
    }

    private List<Medicao> historicoDeChuva(double... valores) throws DadosInvalidosException {
        List<Medicao> historico = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 16, 0, 0);
        for (int i = 0; i < valores.length; i++) {
            historico.add(new Medicao("PLU-T", "BL-T", TipoSensor.PLUVIOMETRO, valores[i], inicio.plusHours(i)));
        }
        return historico;
    }

    @Test
    void previsaoAcimaDoLimiarCriticoGravaAlertaCritico() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAscIdAsc("PLU-T"))
                .thenReturn(historicoDeChuva(55.0, 55.0, 55.0, 55.0, 55.0, 55.0));

        // Act
        List<ResultadoAnalise> resultados = servico.rodarPrevisoes();

        // Assert
        ArgumentCaptor<Alerta> alertaSalvo = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository, times(2)).save(alertaSalvo.capture());
        for (Alerta alerta : alertaSalvo.getAllValues()) {
            assertEquals(NivelAlerta.CRITICO, alerta.getNivel());
            assertEquals("PLU-T", alerta.getCodigoSensor());
            assertEquals(55.0, alerta.getValorPrevisto(), 0.0001);
            assertEquals(50.0, alerta.getLimiteUltrapassado(), 0.0001);
        }
        assertEquals(2, resultados.size());
        assertTrue(resultados.get(0).getAlertaNovo().isPresent());
    }

    @Test
    void previsaoNaFaixaDeAtencaoGravaAlertaDeAtencao() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAscIdAsc("PLU-T"))
                .thenReturn(historicoDeChuva(20.0, 20.0, 20.0, 20.0, 20.0, 20.0));

        // Act
        servico.rodarPrevisoes();

        // Assert
        ArgumentCaptor<Alerta> alertaSalvo = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository, times(2)).save(alertaSalvo.capture());
        assertEquals(NivelAlerta.ATENCAO, alertaSalvo.getValue().getNivel());
        assertEquals(15.0, alertaSalvo.getValue().getLimiteUltrapassado(), 0.0001);
    }

    @Test
    void previsaoAbaixoDoLimiarNaoGravaAlerta() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAscIdAsc("PLU-T"))
                .thenReturn(historicoDeChuva(4.0, 5.0, 6.0, 5.0, 4.0, 5.0));

        // Act
        List<ResultadoAnalise> resultados = servico.rodarPrevisoes();

        // Assert
        verify(alertaRepository, never()).save(any());
        assertTrue(resultados.stream().allMatch(r -> r.getNivel().isEmpty()));
    }

    @Test
    void comPoucasLeiturasOSistemaSeCalaENaoGravaAlerta() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAscIdAsc("PLU-T"))
                .thenReturn(historicoDeChuva(80.0, 90.0, 95.0, 99.0));

        // Act
        List<ResultadoAnalise> resultados = servico.rodarPrevisoes();

        // Assert
        verify(alertaRepository, never()).save(any());
        assertTrue(resultados.stream().noneMatch(r -> r.getPrevisao().isSuficiente()));
    }

    @Test
    void alertaJaRegistradoParaAsMesmasLeiturasNaoDuplica() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.findByCodigoSensorOrderByDataHoraAscIdAsc("PLU-T"))
                .thenReturn(historicoDeChuva(55.0, 55.0, 55.0, 55.0, 55.0, 55.0));
        when(alertaRepository.existsByCodigoSensorAndTipoPrevisaoAndNivelAndLimiteUltrapassadoAndIdUltimaMedicao(
                eq("PLU-T"), any(TipoPrevisao.class), eq(NivelAlerta.CRITICO), eq(50.0), any()))
                .thenReturn(true);

        // Act
        List<ResultadoAnalise> resultados = servico.rodarPrevisoes();

        // Assert
        verify(alertaRepository, never()).save(any());
        assertTrue(resultados.stream().allMatch(r -> r.getNivel().isPresent() && r.getAlertaNovo().isEmpty()));
    }

    @Test
    void sensorDesligadoNaoGravaMedicao() {
        // Arrange
        pluviometro.definirFuncionamento(false);

        // Act
        assertThrows(FonteIndisponivelException.class, () -> servico.lerERegistrar(pluviometro));

        // Assert
        verify(medicaoRepository, never()).save(any());
    }

    @Test
    void leituraEspuriaEDescartadaENaoGravada() {
        // Arrange
        Sensor pluviometroComDefeito = new Pluviometro("PLU-T", new Random(7), 0.0, 1.0);

        // Act
        assertThrows(DadosInvalidosException.class, () -> servico.lerERegistrar(pluviometroComDefeito));

        // Assert
        verify(medicaoRepository, never()).save(any());
    }

    @Test
    void leituraValidaEGravadaComOPontoDoSensor() throws Exception {
        // Arrange
        when(medicaoRepository.save(any(Medicao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        // Act
        Medicao gravada = servico.lerERegistrar(pluviometro);

        // Assert
        verify(medicaoRepository, times(1)).save(any(Medicao.class));
        assertEquals("BL-T", gravada.getCodigoPonto());
        assertEquals(TipoSensor.PLUVIOMETRO, gravada.getTipoSensor());
        assertEquals(LocalDateTime.of(2026, 11, 16, 15, 0), gravada.getDataHora());
    }

    @Test
    void sementeSoEntraComBancoVazio() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.count()).thenReturn(3L);

        // Act
        int carregadas = servico.carregarSementeSeVazio();

        // Assert
        assertEquals(0, carregadas);
        verify(medicaoRepository, never()).saveAll(any());
    }

    @Test
    void retomaASimulacaoDaUltimaLeituraGravada() throws Exception {
        // Arrange
        Medicao ultima = new Medicao("PLU-T", "BL-T", TipoSensor.PLUVIOMETRO, 40.0, LocalDateTime.now(RELOGIO_FIXO));
        when(medicaoRepository.findFirstByCodigoSensorOrderByDataHoraDescIdDesc(anyString()))
                .thenReturn(java.util.Optional.of(ultima));
        when(medicaoRepository.save(any(Medicao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        // Act
        servico.retomarSimulacaoDoHistorico();
        Medicao proxima = servico.lerERegistrar(pluviometro);

        // Assert
        assertTrue(Math.abs(proxima.getValor() - 40.0) < 20.0);
    }
}
