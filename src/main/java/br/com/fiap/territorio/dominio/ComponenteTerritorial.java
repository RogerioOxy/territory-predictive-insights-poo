package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Mensagens;

import java.util.Objects;

/**
 * Base abstrata dos componentes do territorio: cada filha define como verificar seu status.
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private final String nome;
    private final String regiao;
    private StatusComponente status;

    protected ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = Objects.requireNonNull(id, Mensagens.ERRO_COMPONENTE_SEM_ID);
        this.nome = Objects.requireNonNull(nome, Mensagens.ERRO_COMPONENTE_SEM_NOME);
        this.regiao = Objects.requireNonNull(regiao, Mensagens.ERRO_COMPONENTE_SEM_REGIAO);
        this.status = StatusComponente.SEM_MONITORAMENTO;
    }

    // A base nao conhece os sensores de cada componente; a filha deve definir essa verificacao.
    public abstract StatusComponente verificarStatus();

    protected void atualizarStatus(StatusComponente novoStatus) {
        this.status = Objects.requireNonNull(novoStatus);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public StatusComponente getStatus() {
        return status;
    }
}
