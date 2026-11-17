package br.com.fiap.territorio.servico;

import java.util.Map;

/**
 * Fornece exemplos iniciais para demonstrar previsao, alerta e falta de historico.
 */
public final class SementeHistorico {

    // Chuva e nivel recebem oito leituras; obstrucao recebe tres para demonstrar o silencio.
    private static final Map<String, double[]> SERIES = Map.of(
            "PLU-01", new double[] {3.2, 5.0, 7.8, 12.5, 18.0, 24.6, 31.2, 38.5},
            "NIV-01", new double[] {0.9, 1.0, 1.1, 1.3, 1.5, 1.6, 1.8, 2.0},
            "OBS-01", new double[] {30.0, 34.0, 37.0});

    private SementeHistorico() {
    }

    public static double[] serieDo(String codigoSensor) {
        double[] serie = SERIES.get(codigoSensor);
        return serie == null ? new double[0] : serie.clone();
    }
}
