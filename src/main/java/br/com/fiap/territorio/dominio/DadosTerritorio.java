package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.excecao.AcessoNegadoException;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.Map;

/**
 * Encapsula os dados da regiao e protege a troca dos limiares com codigo de acesso.
 */
public class DadosTerritorio {

    private final String regiao;
    private int populacaoExposta;
    private final String codigoAcesso;
    private final Map<TipoSensor, Limiares> limiares = new EnumMap<>(TipoSensor.class);

    public DadosTerritorio(String regiao, int populacaoExposta, String codigoAcesso,
                           Map<TipoSensor, Limiares> limiaresIniciais) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException(Mensagens.ERRO_REGIAO_VAZIA);
        }
        if (codigoAcesso == null || codigoAcesso.isBlank()) {
            throw new DadosInvalidosException(Mensagens.ERRO_CODIGO_ACESSO_VAZIO);
        }
        // Recusa limiares iniciais ausentes, nulos ou de outro tipo de sensor.
        for (TipoSensor tipo : TipoSensor.values()) {
            Limiares recebidos = limiaresIniciais == null ? null : limiaresIniciais.get(tipo);
            if (recebidos == null || recebidos.getTipoSensor() != tipo) {
                throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_LIMIAR_AUSENTE, tipo.getNome()));
            }
            limiares.put(tipo, recebidos);
        }
        this.regiao = regiao;
        this.codigoAcesso = codigoAcesso;
        setPopulacaoExposta(populacaoExposta);
    }

    // Valida antes de atribuir para uma populacao negativa nao substituir o valor atual.
    public void setPopulacaoExposta(int populacaoExposta) throws DadosInvalidosException {
        if (populacaoExposta < 0) {
            throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_POPULACAO_NEGATIVA, populacaoExposta));
        }
        this.populacaoExposta = populacaoExposta;
    }

    public void validarCodigoAcesso(String codigoInformado) throws AcessoNegadoException {
        if (codigoInformado == null || !MessageDigest.isEqual(
                codigoAcesso.getBytes(StandardCharsets.UTF_8),
                codigoInformado.getBytes(StandardCharsets.UTF_8))) {
            throw new AcessoNegadoException(Mensagens.ERRO_CODIGO_ACESSO_INCORRETO);
        }
    }

    public void alterarLimiares(String codigoInformado, TipoSensor tipo, double atencao, double alerta,
                                double critico) throws AcessoNegadoException, DadosInvalidosException {
        // So troca os limites depois de conferir o acesso e validar o novo conjunto.
        validarCodigoAcesso(codigoInformado);
        Limiares novos = Limiares.criar(tipo, atencao, alerta, critico);
        limiares.put(tipo, novos);
    }

    public Limiares getLimiares(TipoSensor tipo) {
        return limiares.get(tipo);
    }

    public String getRegiao() {
        return regiao;
    }

    public int getPopulacaoExposta() {
        return populacaoExposta;
    }
}
