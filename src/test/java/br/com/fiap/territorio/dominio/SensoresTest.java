package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa falhas simuladas, faixas de leitura e o status dos pontos.
 */
class SensoresTest {

    @Test
    void sensorDesligadoLancaFonteIndisponivel() {
        // Arrange
        Sensor pluviometro = new Pluviometro("PLU-T", new Random(1), 0.0, 0.0);
        pluviometro.definirFuncionamento(false);

        // Act
        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class, pluviometro::lerValor);

        // Assert
        assertEquals("PLU-T", erro.getCodigoSensor());
    }

    @Test
    void falhaDeComunicacaoSimuladaLancaFonteIndisponivel() {
        // Arrange
        Sensor nivel = new SensorNivelCorrego("NIV-T", new Random(1), 1.0, 0.0);

        // Act + Assert
        assertThrows(FonteIndisponivelException.class, nivel::lerValor);
    }

    @Test
    void leituraEspuriaDeObstrucaoPassaDeCemPorCento() throws FonteIndisponivelException {
        // Arrange
        Sensor obstrucao = new SensorObstrucaoBueiro("OBS-T", new Random(1), 0.0, 1.0);

        // Act
        double valor = obstrucao.lerValor();

        // Assert
        assertTrue(valor > 100.0);
    }

    @Test
    void leiturasNormaisFicamDentroDaFaixaFisica() throws FonteIndisponivelException {
        // Arrange
        List<Sensor> sensores = List.of(
                new Pluviometro("PLU-T", new Random(3), 0.0, 0.0),
                new SensorNivelCorrego("NIV-T", new Random(3), 0.0, 0.0),
                new SensorObstrucaoBueiro("OBS-T", new Random(3), 0.0, 0.0));

        // Act + Assert
        for (Sensor sensor : sensores) {
            for (int i = 0; i < 500; i++) {
                double valor = sensor.lerValor();
                assertTrue(sensor.getTipo().aceita(valor), sensor.getCodigo() + " " + valor);
            }
        }
    }

    @Test
    void pontoComUmSensorDesligadoFicaParcial() {
        // Arrange
        PontoVulneravel ponto = new PontoVulneravel("BL-T", "BL-T", "R-T", TipoPonto.BOCA_DE_LOBO);
        Sensor pluviometro = new Pluviometro("PLU-T", new Random(1), 0.0, 0.0);
        Sensor obstrucao = new SensorObstrucaoBueiro("OBS-T", new Random(1), 0.0, 0.0);
        ponto.instalarSensor(pluviometro);
        ponto.instalarSensor(obstrucao);
        obstrucao.definirFuncionamento(false);

        // Act
        StatusComponente status = ponto.verificarStatus();

        // Assert
        assertEquals(StatusComponente.PARCIAL, status);
        assertEquals(StatusComponente.PARCIAL, ponto.getStatus());
    }

    @Test
    void pontoSemSensorFuncionandoFicaSemMonitoramento() {
        // Arrange
        PontoVulneravel ponto = new PontoVulneravel("CR-T", "CR-T", "R-T", TipoPonto.TRECHO_DE_CORREGO);
        Sensor nivel = new SensorNivelCorrego("NIV-T", new Random(1), 0.0, 0.0);
        ponto.instalarSensor(nivel);
        nivel.definirFuncionamento(false);

        // Act
        StatusComponente status = ponto.verificarStatus();

        // Assert
        assertEquals(StatusComponente.SEM_MONITORAMENTO, status);
    }
}
