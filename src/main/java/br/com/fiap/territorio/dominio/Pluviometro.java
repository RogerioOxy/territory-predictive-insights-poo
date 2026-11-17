package br.com.fiap.territorio.dominio;

import java.util.Random;

/**
 * Simula chuva, especializando a geracao de valores herdada de SensorSimulado.
 */
public class Pluviometro extends SensorSimulado {

    public Pluviometro(String codigo, Random random) {
        this(codigo, random, PROBABILIDADE_FALHA_PADRAO, PROBABILIDADE_LEITURA_ESPURIA_PADRAO);
    }

    public Pluviometro(String codigo, Random random, double probabilidadeFalha, double probabilidadeLeituraEspuria) {
        super(codigo, 5.0, 0.0, 120.0, random, probabilidadeFalha, probabilidadeLeituraEspuria);
    }

    @Override
    protected double proximoValor(double atual, Random random) {
        return atual + random.nextGaussian() * 4.0;
    }

    @Override
    protected double gerarLeituraEspuria(double atual, Random random) {
        return -(1.0 + random.nextDouble() * 10.0);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.PLUVIOMETRO;
    }
}
