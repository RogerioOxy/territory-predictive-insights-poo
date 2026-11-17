package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

/**
 * Distingue os pontos de boca de lobo e de trecho de corrego.
 */
public enum TipoPonto {

    BOCA_DE_LOBO(Mensagens.TIPO_PONTO_BOCA_DE_LOBO),
    TRECHO_DE_CORREGO(Mensagens.TIPO_PONTO_TRECHO_CORREGO);

    private final String descricao;

    TipoPonto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
