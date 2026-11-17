package br.com.fiap.territorio.dominio;

import java.util.Random;

/**
 * Simula a obstrucao da boca de lobo reutilizando a base dos sensores simulados.
 */
public class SensorObstrucaoBueiro extends SensorSimulado {

    public SensorObstrucaoBueiro(String codigo, Random random) {
        this(codigo, random, PROBABILIDADE_FALHA_PADRAO, PROBABILIDADE_LEITURA_ESPURIA_PADRAO);
    }

    public SensorObstrucaoBueiro(String codigo, Random random, double probabilidadeFalha,
                                 double probabilidadeLeituraEspuria) {
        super(codigo, 30.0, 0.0, 100.0, random, probabilidadeFalha, probabilidadeLeituraEspuria);
    }

    @Override
    protected double proximoValor(double atual, Random random) {
        return atual - 1.5 + random.nextDouble() * 5.5;
    }

    @Override
    protected double gerarLeituraEspuria(double atual, Random random) {
        return 105.0 + random.nextDouble() * 50.0;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.OBSTRUCAO_BUEIRO;
    }
}
