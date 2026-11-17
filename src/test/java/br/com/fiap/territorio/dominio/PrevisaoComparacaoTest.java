package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.servico.ConfiguracaoSistema;
import br.com.fiap.territorio.servico.FabricaPrevisao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a projecao entre periodos e a exigencia de historico suficiente.
 */
class PrevisaoComparacaoTest {

    private final FabricaPrevisao fabrica = new FabricaPrevisao(ConfiguracaoSistema.getInstance());

    @Test
    void tendenciaDeAltaContinuaNaPrevisao() {
        // Arrange
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO);
        List<Double> historico = List.of(10.0, 10.0, 10.0, 20.0, 20.0, 20.0);

        // Act
        ResultadoPrevisao resultado = comparacao.prever(historico);

        // Assert
        assertTrue(resultado.isSuficiente());
        assertEquals(30.0, resultado.getValorPrevisto(), 0.0001);
    }

    @Test
    void quedaForteNaoGeraValorNegativo() {
        // Arrange
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO);
        List<Double> historico = List.of(30.0, 30.0, 30.0, 5.0, 5.0, 5.0);

        // Act
        ResultadoPrevisao resultado = comparacao.prever(historico);

        // Assert
        assertEquals(0.0, resultado.getValorPrevisto(), 0.0001);
    }

    @Test
    void precisaDeDoisPeriodosCompletosParaComparar() {
        // Arrange
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO);
        List<Double> cincoLeituras = List.of(10.0, 12.0, 14.0, 16.0, 18.0);

        // Act
        ResultadoPrevisao resultado = comparacao.prever(cincoLeituras);

        // Assert
        assertFalse(resultado.isSuficiente());
        assertEquals(6, resultado.getLeiturasNecessarias());
    }

    @Test
    void sementeDoPluviometroPreveChuvaAcimaDeCinquenta() {
        // Arrange
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO);
        List<Double> sementePluviometro = List.of(3.2, 5.0, 7.8, 12.5, 18.0, 24.6, 31.2, 38.5);

        // Act
        ResultadoPrevisao resultado = comparacao.prever(sementePluviometro);

        // Assert
        assertEquals(50.1, resultado.getValorPrevisto(), 0.0001);
    }
}
