package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mantem limites validados e classifica a previsao pelo nivel de risco atingido.
 */
public final class Limiares {

    private final TipoSensor tipoSensor;
    private final Map<NivelAlerta, Double> limites;

    private Limiares(TipoSensor tipoSensor, Map<NivelAlerta, Double> limites) {
        this.tipoSensor = tipoSensor;
        this.limites = limites;
    }

    // A criacao recusa limites fora da faixa ou da ordem para evitar classificacoes incoerentes.
    public static Limiares criar(TipoSensor tipo, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        if (tipo == null) {
            throw new DadosInvalidosException(Mensagens.ERRO_LIMIAR_SEM_TIPO);
        }
        if (!Double.isFinite(atencao) || !Double.isFinite(alerta) || !Double.isFinite(critico)) {
            throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_LIMIAR_NAO_NUMERICO, tipo.getNome()));
        }
        if (atencao <= tipo.getMinimoFisico() || critico > tipo.getMaximoFisico()) {
            throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_LIMIAR_FORA_FAIXA, tipo.getNome(),
                    tipo.getMinimoFisico(), tipo.getMaximoFisico(), tipo.getUnidade()));
        }
        if (atencao >= alerta || alerta >= critico) {
            throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_LIMIAR_FORA_DE_ORDEM, tipo.getNome(),
                    atencao, alerta, critico));
        }
        Map<NivelAlerta, Double> limites = new EnumMap<>(NivelAlerta.class);
        limites.put(NivelAlerta.ATENCAO, atencao);
        limites.put(NivelAlerta.ALERTA, alerta);
        limites.put(NivelAlerta.CRITICO, critico);
        return new Limiares(tipo, limites);
    }

    public Optional<NivelAlerta> classificar(double valor) {
        NivelAlerta nivelAtingido = null;
        for (NivelAlerta nivel : NivelAlerta.values()) {
            if (valor >= limites.get(nivel)) {
                nivelAtingido = nivel;
            }
        }
        return Optional.ofNullable(nivelAtingido);
    }

    public double getLimite(NivelAlerta nivel) {
        return limites.get(nivel);
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }
}
