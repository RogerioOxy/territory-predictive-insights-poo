package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.Medicao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Exporta o historico recebido do menu para permitir sua consulta fora do sistema.
 */
public class ExportadorCsv {

    private static final DateTimeFormatter CARIMBO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final Path pastaDestino;

    public ExportadorCsv(Path pastaDestino) {
        this.pastaDestino = pastaDestino;
    }

    public Path exportarMedicoes(String codigoSensor, List<Medicao> medicoes) throws IOException {
        Path arquivo = prepararArquivo("historico-" + codigoSensor);
        // O try-with-resources fecha o arquivo mesmo se a escrita falhar.
        try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            escritor.write(Mensagens.CSV_CABECALHO_MEDICOES);
            escritor.newLine();
            for (Medicao medicao : medicoes) {
                escritor.write(String.join(";",
                        medicao.getDataHora().format(DATA_HORA),
                        medicao.getCodigoPonto(),
                        medicao.getCodigoSensor(),
                        medicao.getTipoSensor().name(),
                        numero(medicao.getValor()),
                        medicao.getTipoSensor().getUnidade()));
                escritor.newLine();
            }
        }
        return arquivo;
    }

    public Path exportarAlertas(List<Alerta> alertas) throws IOException {
        Path arquivo = prepararArquivo("alertas");
        // O try-with-resources fecha o arquivo mesmo se a escrita falhar.
        try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            escritor.write(Mensagens.CSV_CABECALHO_ALERTAS);
            escritor.newLine();
            for (Alerta alerta : alertas) {
                escritor.write(String.join(";",
                        alerta.getDataHora().format(DATA_HORA),
                        alerta.getNivel().name(),
                        alerta.getCodigoPonto(),
                        alerta.getCodigoSensor(),
                        alerta.getTipoSensor().name(),
                        alerta.getTipoPrevisao().name(),
                        numero(alerta.getValorPrevisto()),
                        numero(alerta.getLimiteUltrapassado()),
                        alerta.getTipoSensor().getUnidade()));
                escritor.newLine();
            }
        }
        return arquivo;
    }

    private Path prepararArquivo(String prefixo) throws IOException {
        Files.createDirectories(pastaDestino);
        return pastaDestino.resolve(prefixo + "-" + LocalDateTime.now().format(CARIMBO) + ".csv");
    }

    private static String numero(double valor) {
        return String.format(PT_BR, "%.2f", valor);
    }
}
