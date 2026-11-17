package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

/**
 * Define os niveis de risco em ordem crescente e a recomendacao de cada um.
 */
public enum NivelAlerta {

    ATENCAO(Mensagens.RECOMENDACAO_ATENCAO),
    ALERTA(Mensagens.RECOMENDACAO_ALERTA),
    CRITICO(Mensagens.RECOMENDACAO_CRITICO);

    private final String recomendacao;

    NivelAlerta(String recomendacao) {
        this.recomendacao = recomendacao;
    }

    public String getRecomendacao() {
        return recomendacao;
    }
}
