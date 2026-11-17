package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a rejeicao de limites invalidos e a classificacao dos niveis de risco.
 */
class LimiaresTest {

    @Test
    void classificaCadaFaixaNoNivelCerto() throws DadosInvalidosException {
        // Arrange
        Limiares chuva = Limiares.criar(TipoSensor.PLUVIOMETRO, 15.0, 30.0, 50.0);

        // Act
        Optional<NivelAlerta> abaixo = chuva.classificar(14.9);
        Optional<NivelAlerta> atencao = chuva.classificar(15.0);
        Optional<NivelAlerta> alerta = chuva.classificar(35.0);
        Optional<NivelAlerta> critico = chuva.classificar(50.0);

        // Assert
        assertTrue(abaixo.isEmpty());
        assertEquals(NivelAlerta.ATENCAO, atencao.orElseThrow());
        assertEquals(NivelAlerta.ALERTA, alerta.orElseThrow());
        assertEquals(NivelAlerta.CRITICO, critico.orElseThrow());
    }

    @Test
    void limiarCriticoAcimaDoMaximoFisicoLancaExcecao() {
        // Arrange
        double criticoImpossivel = 150.0;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> Limiares.criar(TipoSensor.OBSTRUCAO_BUEIRO, 40.0, 60.0, criticoImpossivel));
    }
}
