package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

/**
 * Representa quanto do componente esta sendo monitorado pelos sensores.
 */
public enum StatusComponente {

    OPERANDO(Mensagens.STATUS_OPERANDO),
    PARCIAL(Mensagens.STATUS_PARCIAL),
    SEM_MONITORAMENTO(Mensagens.STATUS_SEM_MONITORAMENTO);

    private final String descricao;

    StatusComponente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
