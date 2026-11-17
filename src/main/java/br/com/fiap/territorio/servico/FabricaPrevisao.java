package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.PrevisaoComparacao;
import br.com.fiap.territorio.dominio.PrevisaoMediaMovel;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Factory que concentra a criacao das estrategias com os parametros da configuracao.
 */
@Component
public class FabricaPrevisao {

    private final ConfiguracaoSistema configuracao;
    private final Map<TipoPrevisao, Function<ConfiguracaoSistema, EstrategiaPrevisao>> criadores =
            new EnumMap<>(TipoPrevisao.class);

    public FabricaPrevisao(ConfiguracaoSistema configuracao) {
        this.configuracao = Objects.requireNonNull(configuracao);
        criadores.put(TipoPrevisao.MEDIA_MOVEL,
                c -> new PrevisaoMediaMovel(c.getJanelaMediaMovel(), c.getMinimoLeituras()));
        criadores.put(TipoPrevisao.COMPARACAO,
                c -> new PrevisaoComparacao(c.getJanelaComparacao(), c.getMinimoLeituras()));
    }

    public EstrategiaPrevisao criar(TipoPrevisao tipo) {
        // O EnumMap associa cada tipo ao seu criador, dispensando if de tipo e instanceof.
        Function<ConfiguracaoSistema, EstrategiaPrevisao> criador = criadores.get(tipo);
        if (criador == null) {
            throw new IllegalArgumentException(Formatador.f(Mensagens.ERRO_PREVISAO_DESCONHECIDA, tipo));
        }
        return criador.apply(configuracao);
    }

    public List<EstrategiaPrevisao> criarEstrategiasAtivas() {
        return configuracao.getEstrategiasAtivas().stream().map(this::criar).toList();
    }
}
