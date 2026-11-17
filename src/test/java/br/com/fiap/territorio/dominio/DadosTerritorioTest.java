package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.AcessoNegadoException;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testa a validacao dos dados e a protecao da alteracao de limiares.
 */
class DadosTerritorioTest {

    private static final String CODIGO = "COD-TESTE";

    private DadosTerritorio dados;

    @BeforeEach
    void preparar() throws DadosInvalidosException {
        dados = new DadosTerritorio("R-TESTE", 500, CODIGO, limiaresPadrao());
    }

    private static Map<TipoSensor, Limiares> limiaresPadrao() throws DadosInvalidosException {
        Map<TipoSensor, Limiares> limiares = new EnumMap<>(TipoSensor.class);
        limiares.put(TipoSensor.PLUVIOMETRO, Limiares.criar(TipoSensor.PLUVIOMETRO, 15.0, 30.0, 50.0));
        limiares.put(TipoSensor.NIVEL_CORREGO, Limiares.criar(TipoSensor.NIVEL_CORREGO, 1.8, 2.4, 2.8));
        limiares.put(TipoSensor.OBSTRUCAO_BUEIRO, Limiares.criar(TipoSensor.OBSTRUCAO_BUEIRO, 40.0, 60.0, 80.0));
        return limiares;
    }

    @Test
    void populacaoNegativaLancaExcecaoEMantemValorAnterior() {
        // Arrange
        int populacaoNegativa = -10;

        // Act
        assertThrows(DadosInvalidosException.class, () -> dados.setPopulacaoExposta(populacaoNegativa));

        // Assert
        assertEquals(500, dados.getPopulacaoExposta());
    }

    @Test
    void codigoErradoNaoAlteraLimiares() {
        // Arrange
        String codigoErrado = "0000";

        // Act
        assertThrows(AcessoNegadoException.class,
                () -> dados.alterarLimiares(codigoErrado, TipoSensor.PLUVIOMETRO, 5.0, 10.0, 20.0));

        // Assert
        assertEquals(15.0, dados.getLimiares(TipoSensor.PLUVIOMETRO).getLimite(NivelAlerta.ATENCAO), 0.0001);
    }

    @Test
    void limiaresForaDeOrdemLancamDadosInvalidos() {
        // Arrange
        double atencao = 40.0;
        double alerta = 30.0;
        double critico = 50.0;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> dados.alterarLimiares(CODIGO, TipoSensor.PLUVIOMETRO, atencao, alerta, critico));
    }

    @Test
    void codigoCertoComLimiaresValidosAltera() throws Exception {
        // Arrange
        double novoCritico = 45.0;

        // Act
        dados.alterarLimiares(CODIGO, TipoSensor.PLUVIOMETRO, 10.0, 25.0, novoCritico);

        // Assert
        assertEquals(novoCritico, dados.getLimiares(TipoSensor.PLUVIOMETRO).getLimite(NivelAlerta.CRITICO), 0.0001);
    }

    @Test
    void faltandoLimiarDeUmTipoNaoCriaOTerritorio() throws DadosInvalidosException {
        // Arrange
        Map<TipoSensor, Limiares> incompletos = limiaresPadrao();
        incompletos.remove(TipoSensor.OBSTRUCAO_BUEIRO);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new DadosTerritorio("R-TESTE", 10, CODIGO, incompletos));
    }

    // Confere que um limiar de outro tipo de sensor impede a criacao do territorio.
    @Test
    void limiarDeOutroTipoDeSensorNaoCriaOTerritorio() throws DadosInvalidosException {
        // Arrange
        Map<TipoSensor, Limiares> trocados = limiaresPadrao();
        trocados.put(TipoSensor.NIVEL_CORREGO, trocados.get(TipoSensor.PLUVIOMETRO));

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new DadosTerritorio("R-TESTE", 10, CODIGO, trocados));
    }
}
