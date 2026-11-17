package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.servico.ConfiguracaoSistema;
import br.com.fiap.territorio.servico.FabricaPrevisao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a janela recente e o silencio quando faltam leituras.
 */
class PrevisaoMediaMovelTest {

    private final FabricaPrevisao fabrica = new FabricaPrevisao(ConfiguracaoSistema.getInstance());

    @Test
    void usaSomenteAsUltimasCincoLeituras() {
        // Arrange
        EstrategiaPrevisao mediaMovel = fabrica.criar(TipoPrevisao.MEDIA_MOVEL);
        List<Double> historico = List.of(100.0, 100.0, 10.0, 20.0, 30.0, 40.0, 50.0);

        // Act
        ResultadoPrevisao resultado = mediaMovel.prever(historico);

        // Assert
        assertTrue(resultado.isSuficiente());
        assertEquals(30.0, resultado.getValorPrevisto(), 0.0001);
        assertEquals(7, resultado.getLeiturasDisponiveis());
    }

    @Test
    void seCalaComMenosDeCincoLeituras() {
        // Arrange
        EstrategiaPrevisao mediaMovel = fabrica.criar(TipoPrevisao.MEDIA_MOVEL);
        List<Double> historicoCurto = List.of(40.0, 45.0, 50.0, 55.0);

        // Act
        ResultadoPrevisao resultado = mediaMovel.prever(historicoCurto);

        // Assert
        assertFalse(resultado.isSuficiente());
        assertEquals(4, resultado.getLeiturasDisponiveis());
        assertEquals(5, resultado.getLeiturasNecessarias());
        assertTrue(Double.isNaN(resultado.getValorPrevisto()));
    }

    @Test
    void seCalaComHistoricoVazio() {
        // Arrange
        EstrategiaPrevisao mediaMovel = fabrica.criar(TipoPrevisao.MEDIA_MOVEL);

        // Act
        ResultadoPrevisao resultado = mediaMovel.prever(List.of());

        // Assert
        assertFalse(resultado.isSuficiente());
        assertEquals(0, resultado.getLeiturasDisponiveis());
    }
}
