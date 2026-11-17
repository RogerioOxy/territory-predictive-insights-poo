package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

/**
 * Identifica as estrategias que a fabrica pode criar para o monitoramento.
 */
public enum TipoPrevisao {

    MEDIA_MOVEL(Mensagens.NOME_MEDIA_MOVEL),
    COMPARACAO(Mensagens.NOME_COMPARACAO);

    private final String nome;

    TipoPrevisao(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
