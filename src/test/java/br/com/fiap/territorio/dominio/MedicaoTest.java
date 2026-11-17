package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa as faixas fisicas aceitas e as excecoes para leituras invalidas.
 */
class MedicaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 12, 0);

    @Test
    void chuvaNegativaLancaDadosInvalidos() {
        // Arrange
        double chuvaNegativa = -3.5;

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("PLU-01", "BL-01", TipoSensor.PLUVIOMETRO, chuvaNegativa, AGORA));

        // Assert
        assertTrue(erro.getMessage().contains("PLU-01"));
    }

    @Test
    void obstrucaoAcimaDeCemPorCentoLancaDadosInvalidos() {
        // Arrange
        double obstrucaoImpossivel = 120.0;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("OBS-01", "BL-01", TipoSensor.OBSTRUCAO_BUEIRO, obstrucaoImpossivel, AGORA));
    }

    @Test
    void valorNaoNumericoLancaDadosInvalidos() {
        // Arrange
        double valorNaoNumerico = Double.NaN;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("NIV-01", "CR-01", TipoSensor.NIVEL_CORREGO, valorNaoNumerico, AGORA));
    }

    @Test
    void medicaoValidaGuardaOsDados() throws DadosInvalidosException {
        // Arrange
        double nivel = 1.75;

        // Act
        Medicao medicao = new Medicao("NIV-01", "CR-01", TipoSensor.NIVEL_CORREGO, nivel, AGORA);

        // Assert
        assertEquals("NIV-01", medicao.getCodigoSensor());
        assertEquals("CR-01", medicao.getCodigoPonto());
        assertEquals(TipoSensor.NIVEL_CORREGO, medicao.getTipoSensor());
        assertEquals(1.75, medicao.getValor(), 0.0001);
        assertEquals(AGORA, medicao.getDataHora());
    }
}
