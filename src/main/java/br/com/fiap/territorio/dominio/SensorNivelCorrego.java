package br.com.fiap.territorio.dominio;

import java.util.Random;

/**
 * Simula o nivel do corrego reutilizando o controle comum de SensorSimulado.
 */
public class SensorNivelCorrego extends SensorSimulado {

    public SensorNivelCorrego(String codigo, Random random) {
        this(codigo, random, PROBABILIDADE_FALHA_PADRAO, PROBABILIDADE_LEITURA_ESPURIA_PADRAO);
    }

    public SensorNivelCorrego(String codigo, Random random, double probabilidadeFalha,
                              double probabilidadeLeituraEspuria) {
        super(codigo, 1.0, 0.2, 3.6, random, probabilidadeFalha, probabilidadeLeituraEspuria);
    }

    @Override
    protected double proximoValor(double atual, Random random) {
        return atual + random.nextGaussian() * 0.12;
    }

    @Override
    protected double gerarLeituraEspuria(double atual, Random random) {
        return -(0.1 + random.nextDouble());
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.NIVEL_CORREGO;
    }
}
