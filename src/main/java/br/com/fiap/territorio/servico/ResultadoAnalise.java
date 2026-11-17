package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.Limiares;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;

import java.util.Optional;

/**
 * Entrega ao menu a previsao, sua classificacao e o eventual alerta novo.
 */
public final class ResultadoAnalise {

    private final Sensor sensor;
    private final ResultadoPrevisao previsao;
    private final Limiares limiares;
    private final NivelAlerta nivel;
    private final Alerta alertaNovo;

    ResultadoAnalise(Sensor sensor, ResultadoPrevisao previsao, Limiares limiares, NivelAlerta nivel,
                     Alerta alertaNovo) {
        this.sensor = sensor;
        this.previsao = previsao;
        this.limiares = limiares;
        this.nivel = nivel;
        this.alertaNovo = alertaNovo;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public ResultadoPrevisao getPrevisao() {
        return previsao;
    }

    public Limiares getLimiares() {
        return limiares;
    }

    public Optional<NivelAlerta> getNivel() {
        return Optional.ofNullable(nivel);
    }

    public Optional<Alerta> getAlertaNovo() {
        return Optional.ofNullable(alertaNovo);
    }
}
