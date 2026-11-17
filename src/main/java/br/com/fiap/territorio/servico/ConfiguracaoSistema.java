package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.Limiares;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton que compartilha parametros e DadosTerritorio, onde ficam os limiares.
 */
public final class ConfiguracaoSistema {

    public static final String CODIGO_ACESSO_PADRAO = "ZELADORIA-2026";

    private static final ConfiguracaoSistema INSTANCIA = new ConfiguracaoSistema();

    private final DadosTerritorio dadosTerritorio;
    private final int minimoLeituras = 5;
    private final int janelaMediaMovel = 5;
    private final int janelaComparacao = 3;
    private final List<TipoPrevisao> estrategiasAtivas = List.of(TipoPrevisao.MEDIA_MOVEL, TipoPrevisao.COMPARACAO);

    // O construtor privado impede novas instancias externas; getInstance entrega a instancia estatica.
    private ConfiguracaoSistema() {
        this.dadosTerritorio = criarDadosPadrao();
    }

    public static ConfiguracaoSistema getInstance() {
        return INSTANCIA;
    }

    private static DadosTerritorio criarDadosPadrao() {
        try {
            Map<TipoSensor, Limiares> limiares = new EnumMap<>(TipoSensor.class);
            limiares.put(TipoSensor.PLUVIOMETRO, Limiares.criar(TipoSensor.PLUVIOMETRO, 15.0, 30.0, 50.0));
            limiares.put(TipoSensor.NIVEL_CORREGO, Limiares.criar(TipoSensor.NIVEL_CORREGO, 1.8, 2.4, 2.8));
            limiares.put(TipoSensor.OBSTRUCAO_BUEIRO, Limiares.criar(TipoSensor.OBSTRUCAO_BUEIRO, 40.0, 60.0, 80.0));
            return new DadosTerritorio(Mensagens.REGIAO_PADRAO, 1200, CODIGO_ACESSO_PADRAO, limiares);
        } catch (DadosInvalidosException e) {
            throw new IllegalStateException(Mensagens.ERRO_CONFIGURACAO_PADRAO + e.getMessage(), e);
        }
    }

    public Limiares getLimiares(TipoSensor tipo) {
        return dadosTerritorio.getLimiares(tipo);
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }

    public int getJanelaMediaMovel() {
        return janelaMediaMovel;
    }

    public int getJanelaComparacao() {
        return janelaComparacao;
    }

    public List<TipoPrevisao> getEstrategiasAtivas() {
        return estrategiasAtivas;
    }
}
