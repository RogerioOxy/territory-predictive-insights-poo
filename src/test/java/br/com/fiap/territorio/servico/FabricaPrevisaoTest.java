package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Testa a criacao configurada das estrategias e a identidade do Singleton.
 */
class FabricaPrevisaoTest {

    @Test
    void criaCadaEstrategiaComAJanelaDaConfiguracao() {
        // Arrange
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstance();
        FabricaPrevisao fabrica = new FabricaPrevisao(configuracao);

        // Act
        EstrategiaPrevisao mediaMovel = fabrica.criar(TipoPrevisao.MEDIA_MOVEL);
        EstrategiaPrevisao comparacao = fabrica.criar(TipoPrevisao.COMPARACAO);

        // Assert
        assertEquals(TipoPrevisao.MEDIA_MOVEL, mediaMovel.getTipo());
        assertEquals(configuracao.getJanelaMediaMovel(), mediaMovel.getJanela());
        assertEquals(TipoPrevisao.COMPARACAO, comparacao.getTipo());
        assertEquals(configuracao.getJanelaComparacao(), comparacao.getJanela());
    }

    @Test
    void criaAsEstrategiasAtivasNaOrdemDaConfiguracao() {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao(ConfiguracaoSistema.getInstance());

        // Act
        List<EstrategiaPrevisao> ativas = fabrica.criarEstrategiasAtivas();

        // Assert
        assertEquals(2, ativas.size());
        assertEquals(TipoPrevisao.MEDIA_MOVEL, ativas.get(0).getTipo());
        assertEquals(TipoPrevisao.COMPARACAO, ativas.get(1).getTipo());
        assertNotSame(ativas.get(0), fabrica.criar(TipoPrevisao.MEDIA_MOVEL));
    }

    @Test
    void configuracaoSistemaEInstanciaUnica() {
        // Arrange
        ConfiguracaoSistema primeira = ConfiguracaoSistema.getInstance();

        // Act
        ConfiguracaoSistema segunda = ConfiguracaoSistema.getInstance();

        // Assert
        assertSame(primeira, segunda);
    }
}
