package br.com.fiap.territorio.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Especializa ComponenteTerritorial e avalia o monitoramento pelos sensores instalados.
 */
public class PontoVulneravel extends ComponenteTerritorial {

    private final TipoPonto tipo;
    private final List<Sensor> sensores = new ArrayList<>();

    public PontoVulneravel(String id, String nome, String regiao, TipoPonto tipo) {
        super(id, nome, regiao);
        this.tipo = Objects.requireNonNull(tipo);
    }

    public void instalarSensor(Sensor sensor) {
        sensores.add(Objects.requireNonNull(sensor));
    }

    @Override
    public StatusComponente verificarStatus() {
        // Usa o contrato Sensor para consultar todas as implementacoes da mesma forma.
        long funcionando = sensores.stream().filter(Sensor::verificarFuncionamento).count();
        StatusComponente novoStatus;
        if (funcionando == 0) {
            novoStatus = StatusComponente.SEM_MONITORAMENTO;
        } else if (funcionando == sensores.size()) {
            novoStatus = StatusComponente.OPERANDO;
        } else {
            novoStatus = StatusComponente.PARCIAL;
        }
        atualizarStatus(novoStatus);
        return novoStatus;
    }

    public TipoPonto getTipo() {
        return tipo;
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }
}
