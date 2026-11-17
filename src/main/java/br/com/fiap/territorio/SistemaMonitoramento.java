package br.com.fiap.territorio;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.Limiares;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.PontoVulneravel;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.AcessoNegadoException;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;
import br.com.fiap.territorio.servico.ExportadorCsv;
import br.com.fiap.territorio.servico.MapaTerritorio;
import br.com.fiap.territorio.servico.ResultadoAnalise;
import br.com.fiap.territorio.servico.ServicoMonitoramento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Conduz o menu de console e apresenta os resultados e erros ao usuario.
 */
@SpringBootApplication
public class SistemaMonitoramento implements CommandLineRunner {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAXIMO_CICLOS = 20;

    private final ServicoMonitoramento servico;
    private final MapaTerritorio mapa;
    private final ConfiguracaoSistema configuracao;
    private final ExportadorCsv exportador;
    private final boolean sementeAtiva;
    private Scanner entrada;
    private boolean entradaEncerrada;

    public SistemaMonitoramento(ServicoMonitoramento servico, MapaTerritorio mapa, ConfiguracaoSistema configuracao,
                                ExportadorCsv exportador,
                                @Value("${territorio.semente.ativa:true}") boolean sementeAtiva) {
        this.servico = servico;
        this.mapa = mapa;
        this.configuracao = configuracao;
        this.exportador = exportador;
        this.sementeAtiva = sementeAtiva;
    }

    public static void main(String[] args) {
        SpringApplication.run(SistemaMonitoramento.class, args);
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            this.entrada = scanner;
            mostrar(Mensagens.BOAS_VINDAS);
            prepararHistorico();
            boolean continuar = true;
            while (continuar && !entradaEncerrada) {
                mostrarMenuPrincipal();
                Optional<String> opcao = lerLinha();
                if (opcao.isEmpty()) {
                    mostrar(Mensagens.FIM_DA_ENTRADA);
                    break;
                }
                continuar = executarOpcao(opcao.get().trim());
            }
        }
        mostrar(Mensagens.ENCERRANDO);
    }

    private void prepararHistorico() {
        try {
            if (sementeAtiva) {
                int carregadas = servico.carregarSementeSeVazio();
                if (carregadas > 0) {
                    mostrar(Formatador.f(Mensagens.SEMENTE_CARREGADA, carregadas));
                }
            }
            servico.retomarSimulacaoDoHistorico();
        } catch (DadosInvalidosException e) {
            mostrar(Formatador.f(Mensagens.ERRO_SEMENTE, e.getMessage()));
        }
    }

    private void mostrarMenuPrincipal() {
        mostrar(Mensagens.MENU_PRINCIPAL);
        mostrarSemQuebra(Mensagens.PEDIR_OPCAO);
    }

    private boolean executarOpcao(String opcao) {
        switch (opcao) {
            case "1" -> verificarSensores();
            case "2" -> rodarPrevisoes();
            case "3" -> verHistorico();
            case "4" -> gerenciarDadosTerritorio();
            case "5" -> mostrarStatusCompleto();
            case "0" -> {
                return false;
            }
            default -> mostrar(Formatador.f(Mensagens.OPCAO_INVALIDA, opcao));
        }
        return true;
    }

    private void verificarSensores() {
        mostrar(Mensagens.TITULO_VERIFICAR_SENSORES);
        for (Sensor sensor : mapa.listarSensores()) {
            mostrar(Formatador.f(Mensagens.LINHA_FUNCIONAMENTO_SENSOR, sensor.getCodigo(), sensor.getTipo().getNome(),
                    sensor.verificarFuncionamento() ? Mensagens.SENSOR_FUNCIONANDO : Mensagens.SENSOR_DESLIGADO));
        }
        Optional<Integer> ciclos = lerInteiro(Formatador.f(Mensagens.PEDIR_CICLOS, MAXIMO_CICLOS), 1, MAXIMO_CICLOS);
        if (ciclos.isEmpty()) {
            return;
        }
        int gravadas = 0;
        int descartadas = 0;
        int falhas = 0;
        for (int ciclo = 1; ciclo <= ciclos.get(); ciclo++) {
            mostrar(Formatador.f(Mensagens.CABECALHO_CICLO, ciclo, ciclos.get()));
            for (Sensor sensor : mapa.listarSensores()) {
                try {
                    Medicao medicao = servico.lerERegistrar(sensor);
                    mostrar(Formatador.f(Mensagens.LEITURA_GRAVADA, sensor.getCodigo(), sensor.getTipo().getNome(),
                            medicao.getValor(), sensor.getTipo().getUnidade()));
                    gravadas++;
                // O menu explica a falha e continua as tentativas dos demais sensores.
                } catch (FonteIndisponivelException e) {
                    mostrar(Formatador.f(Mensagens.LEITURA_FALHOU, e.getMessage()));
                    falhas++;
                // A leitura recusada e informada ao usuario sem ser incluida no historico.
                } catch (DadosInvalidosException e) {
                    mostrar(Formatador.f(Mensagens.LEITURA_DESCARTADA, e.getMessage()));
                    descartadas++;
                }
            }
        }
        mostrar(Formatador.f(Mensagens.RESUMO_LEITURAS, gravadas, descartadas, falhas));
    }

    private void rodarPrevisoes() {
        mostrar(Mensagens.TITULO_PREVISOES);
        int alertasNovos = 0;
        for (ResultadoAnalise analise : servico.rodarPrevisoes()) {
            Sensor sensor = analise.getSensor();
            ResultadoPrevisao previsao = analise.getPrevisao();
            TipoSensor tipo = sensor.getTipo();
            mostrar(Formatador.f(Mensagens.CABECALHO_PREVISAO, sensor.getCodigo(), tipo.getNome(),
                    previsao.getTipoPrevisao().getNome()));
            if (!previsao.isSuficiente()) {
                mostrar(Formatador.f(Mensagens.PREVISAO_SILENCIO, previsao.getLeiturasDisponiveis(),
                        previsao.getLeiturasNecessarias()));
                continue;
            }
            mostrar(Formatador.f(Mensagens.PREVISAO_VALOR, previsao.getValorPrevisto(), tipo.getUnidade()));
            mostrar(Formatador.f(Mensagens.PREVISAO_COMO_CALCULOU, previsao.getComoFoiCalculado()));
            Optional<NivelAlerta> nivel = analise.getNivel();
            if (nivel.isEmpty()) {
                mostrar(Formatador.f(Mensagens.PREVISAO_SIGNIFICADO_NORMAL,
                        analise.getLimiares().getLimite(NivelAlerta.ATENCAO), tipo.getUnidade()));
                continue;
            }
            mostrar(Formatador.f(Mensagens.PREVISAO_SIGNIFICADO_NIVEL, nivel.get().name(),
                    analise.getLimiares().getLimite(nivel.get()), tipo.getUnidade(), nivel.get().getRecomendacao()));
            Optional<Alerta> alertaNovo = analise.getAlertaNovo();
            if (alertaNovo.isPresent()) {
                mostrar(Formatador.f(Mensagens.ALERTA_EMITIDO, alertaNovo.get().explicar()));
                alertasNovos++;
            } else {
                mostrar(Mensagens.ALERTA_JA_REGISTRADO);
            }
        }
        mostrar(Formatador.f(Mensagens.RESUMO_PREVISOES, alertasNovos));
        mostrar(Mensagens.LEMBRETE_DECISAO_HUMANA);
    }

    private void verHistorico() {
        List<Sensor> sensores = mapa.listarSensores();
        mostrar(Mensagens.TITULO_HISTORICO);
        for (int i = 0; i < sensores.size(); i++) {
            mostrar(Formatador.f(Mensagens.ITEM_SENSOR_NUMERADO, i + 1, sensores.get(i).getCodigo(),
                    sensores.get(i).getTipo().getNome()));
        }
        int opcaoAlertas = sensores.size() + 1;
        mostrar(Formatador.f(Mensagens.ITEM_ALERTAS_NUMERADO, opcaoAlertas));
        mostrar(Mensagens.ITEM_VOLTAR);
        Optional<Integer> escolha = lerInteiro(Mensagens.PEDIR_OPCAO, 0, opcaoAlertas);
        if (escolha.isEmpty() || escolha.get() == 0) {
            return;
        }
        if (escolha.get() == opcaoAlertas) {
            listarAlertas();
        } else {
            listarHistoricoDoSensor(sensores.get(escolha.get() - 1));
        }
    }

    private void listarHistoricoDoSensor(Sensor sensor) {
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getCodigo());
        mostrar(Formatador.f(Mensagens.CABECALHO_HISTORICO_SENSOR, sensor.getCodigo(), sensor.getTipo().getNome(),
                historico.size()));
        if (historico.isEmpty()) {
            mostrar(Mensagens.HISTORICO_VAZIO);
            return;
        }
        for (Medicao medicao : historico) {
            mostrar(Formatador.f(Mensagens.LINHA_MEDICAO, medicao.getDataHora().format(DATA_HORA),
                    medicao.getValor(), medicao.getTipoSensor().getUnidade()));
        }
        if (confirmar(Mensagens.PERGUNTA_EXPORTAR_CSV)) {
            try {
                Path arquivo = exportador.exportarMedicoes(sensor.getCodigo(), historico);
                mostrar(Formatador.f(Mensagens.CSV_GERADO, arquivo.toAbsolutePath()));
            } catch (IOException e) {
                mostrar(Formatador.f(Mensagens.ERRO_EXPORTAR_CSV, e.getMessage()));
            }
        }
    }

    private void listarAlertas() {
        List<Alerta> alertas = servico.listarAlertasEmitidos();
        mostrar(Formatador.f(Mensagens.CABECALHO_ALERTAS, alertas.size()));
        if (alertas.isEmpty()) {
            mostrar(Mensagens.SEM_ALERTAS);
            return;
        }
        for (Alerta alerta : alertas) {
            mostrar(Formatador.f(Mensagens.LINHA_ALERTA, alerta.getDataHora().format(DATA_HORA), alerta.explicar()));
        }
        if (confirmar(Mensagens.PERGUNTA_EXPORTAR_CSV)) {
            try {
                Path arquivo = exportador.exportarAlertas(alertas);
                mostrar(Formatador.f(Mensagens.CSV_GERADO, arquivo.toAbsolutePath()));
            } catch (IOException e) {
                mostrar(Formatador.f(Mensagens.ERRO_EXPORTAR_CSV, e.getMessage()));
            }
        }
    }

    private void gerenciarDadosTerritorio() {
        boolean continuar = true;
        while (continuar && !entradaEncerrada) {
            mostrar(Mensagens.MENU_TERRITORIO);
            Optional<Integer> escolha = lerInteiro(Mensagens.PEDIR_OPCAO, 0, 4);
            if (escolha.isEmpty()) {
                continue;
            }
            switch (escolha.get()) {
                case 1 -> mostrarDadosTerritorio();
                case 2 -> alterarPopulacao();
                case 3 -> alterarLimiares();
                case 4 -> alternarFuncionamentoSensor();
                default -> continuar = false;
            }
        }
    }

    private void mostrarDadosTerritorio() {
        DadosTerritorio dados = configuracao.getDadosTerritorio();
        mostrar(Formatador.f(Mensagens.DADOS_TERRITORIO, dados.getRegiao(), dados.getPopulacaoExposta()));
        for (TipoSensor tipo : TipoSensor.values()) {
            Limiares limiares = configuracao.getLimiares(tipo);
            mostrar(Formatador.f(Mensagens.LINHA_LIMIARES, tipo.getNome(),
                    limiares.getLimite(NivelAlerta.ATENCAO), limiares.getLimite(NivelAlerta.ALERTA),
                    limiares.getLimite(NivelAlerta.CRITICO), tipo.getUnidade()));
        }
    }

    private void alterarPopulacao() {
        Optional<Integer> populacao = lerInteiro(Mensagens.PEDIR_POPULACAO, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (populacao.isEmpty()) {
            return;
        }
        try {
            configuracao.getDadosTerritorio().setPopulacaoExposta(populacao.get());
            mostrar(Formatador.f(Mensagens.POPULACAO_ALTERADA, populacao.get()));
        } catch (DadosInvalidosException e) {
            mostrar(Formatador.f(Mensagens.ERRO_VALIDACAO, e.getMessage()));
        }
    }

    private void alterarLimiares() {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            mostrar(Formatador.f(Mensagens.ITEM_TIPO_SENSOR_NUMERADO, i + 1, tipos[i].getNome(), tipos[i].getUnidade()));
        }
        Optional<Integer> escolha = lerInteiro(Mensagens.PEDIR_TIPO_SENSOR, 1, tipos.length);
        if (escolha.isEmpty()) {
            return;
        }
        TipoSensor tipo = tipos[escolha.get() - 1];
        mostrarSemQuebra(Mensagens.PEDIR_CODIGO_ACESSO);
        Optional<String> codigo = lerLinha();
        if (codigo.isEmpty()) {
            return;
        }
        DadosTerritorio dados = configuracao.getDadosTerritorio();
        try {
            dados.validarCodigoAcesso(codigo.get().trim());
            Optional<Double> atencao = lerDecimal(Formatador.f(Mensagens.PEDIR_LIMIAR, NivelAlerta.ATENCAO.name(),
                    tipo.getUnidade()));
            Optional<Double> alerta = atencao.isEmpty() ? Optional.empty()
                    : lerDecimal(Formatador.f(Mensagens.PEDIR_LIMIAR, NivelAlerta.ALERTA.name(), tipo.getUnidade()));
            Optional<Double> critico = alerta.isEmpty() ? Optional.empty()
                    : lerDecimal(Formatador.f(Mensagens.PEDIR_LIMIAR, NivelAlerta.CRITICO.name(), tipo.getUnidade()));
            if (critico.isEmpty()) {
                mostrar(Mensagens.LIMIARES_NAO_ALTERADOS);
                return;
            }
            dados.alterarLimiares(codigo.get().trim(), tipo, atencao.get(), alerta.get(), critico.get());
            mostrar(Formatador.f(Mensagens.LIMIARES_ALTERADOS, tipo.getNome()));
        } catch (AcessoNegadoException e) {
            mostrar(Formatador.f(Mensagens.ERRO_ACESSO, e.getMessage()));
        } catch (DadosInvalidosException e) {
            mostrar(Formatador.f(Mensagens.ERRO_VALIDACAO, e.getMessage()));
        }
    }

    private void alternarFuncionamentoSensor() {
        List<Sensor> sensores = mapa.listarSensores();
        for (int i = 0; i < sensores.size(); i++) {
            Sensor sensor = sensores.get(i);
            mostrar(Formatador.f(Mensagens.ITEM_SENSOR_COM_ESTADO, i + 1, sensor.getCodigo(), sensor.getTipo().getNome(),
                    sensor.verificarFuncionamento() ? Mensagens.SENSOR_FUNCIONANDO : Mensagens.SENSOR_DESLIGADO));
        }
        Optional<Integer> escolha = lerInteiro(Mensagens.PEDIR_SENSOR, 1, sensores.size());
        if (escolha.isEmpty()) {
            return;
        }
        Sensor sensor = sensores.get(escolha.get() - 1);
        sensor.definirFuncionamento(!sensor.verificarFuncionamento());
        mostrar(Formatador.f(Mensagens.SENSOR_ALTERNADO, sensor.getCodigo(),
                sensor.verificarFuncionamento() ? Mensagens.SENSOR_FUNCIONANDO : Mensagens.SENSOR_DESLIGADO));
    }

    private void mostrarStatusCompleto() {
        mostrar(Mensagens.TITULO_STATUS);
        int necessarias = servico.leiturasNecessariasParaPrever();
        for (PontoVulneravel ponto : mapa.getPontos()) {
            mostrar(Formatador.f(Mensagens.LINHA_PONTO, ponto.getId(), ponto.getNome(), ponto.getTipo().getDescricao(),
                    ponto.getRegiao(), ponto.verificarStatus().getDescricao()));
            for (Sensor sensor : ponto.getSensores()) {
                long quantidade = servico.contarMedicoes(sensor.getCodigo());
                mostrar(Formatador.f(Mensagens.LINHA_SENSOR_STATUS, sensor.getCodigo(), sensor.getTipo().getNome(),
                        sensor.verificarFuncionamento() ? Mensagens.SENSOR_FUNCIONANDO : Mensagens.SENSOR_DESLIGADO,
                        quantidade));
                Optional<Medicao> ultima = servico.ultimaMedicao(sensor.getCodigo());
                ultima.ifPresent(m -> mostrar(Formatador.f(Mensagens.LINHA_ULTIMA_LEITURA, m.getValor(),
                        m.getTipoSensor().getUnidade(), m.getDataHora().format(DATA_HORA))));
                if (quantidade >= necessarias) {
                    mostrar(Mensagens.SENSOR_PRONTO_PARA_PREVER);
                } else {
                    mostrar(Formatador.f(Mensagens.SENSOR_FALTAM_LEITURAS, necessarias - quantidade));
                }
            }
        }
        mostrarDadosTerritorio();
        mostrar(Formatador.f(Mensagens.LINHA_CONFIGURACAO, configuracao.getMinimoLeituras(),
                configuracao.getJanelaMediaMovel(), configuracao.getJanelaComparacao()));
        for (TipoPrevisao tipo : configuracao.getEstrategiasAtivas()) {
            mostrar(Formatador.f(Mensagens.LINHA_ESTRATEGIA_ATIVA, tipo.getNome()));
        }
        mostrar(Formatador.f(Mensagens.TOTAL_ALERTAS, servico.contarAlertas()));
        servico.ultimoAlerta().ifPresent(a -> mostrar(Formatador.f(Mensagens.ULTIMO_ALERTA,
                a.getDataHora().format(DATA_HORA), a.explicar())));
    }

    private Optional<String> lerLinha() {
        if (entradaEncerrada || !entrada.hasNextLine()) {
            entradaEncerrada = true;
            return Optional.empty();
        }
        return Optional.of(entrada.nextLine().replace("\uFEFF", ""));
    }

    private Optional<Integer> lerInteiro(String pergunta, int minimo, int maximo) {
        mostrarSemQuebra(pergunta);
        Optional<String> linha = lerLinha();
        if (linha.isEmpty()) {
            return Optional.empty();
        }
        String texto = linha.get().trim();
        try {
            int valor = Integer.parseInt(texto);
            if (valor < minimo || valor > maximo) {
                mostrar(Formatador.f(Mensagens.ERRO_FORA_DO_INTERVALO, valor, minimo, maximo));
                return Optional.empty();
            }
            return Optional.of(valor);
        } catch (NumberFormatException e) {
            mostrar(Formatador.f(Mensagens.ERRO_NUMERO_INTEIRO, texto));
            return Optional.empty();
        }
    }

    private Optional<Double> lerDecimal(String pergunta) {
        mostrarSemQuebra(pergunta);
        Optional<String> linha = lerLinha();
        if (linha.isEmpty()) {
            return Optional.empty();
        }
        String texto = linha.get().trim();
        try {
            return Optional.of(Double.parseDouble(texto.replace(',', '.')));
        } catch (NumberFormatException e) {
            mostrar(Formatador.f(Mensagens.ERRO_NUMERO_DECIMAL, texto));
            return Optional.empty();
        }
    }

    private boolean confirmar(String pergunta) {
        mostrarSemQuebra(pergunta);
        Optional<String> resposta = lerLinha();
        return resposta.isPresent() && resposta.get().trim().equalsIgnoreCase("s");
    }

    private void mostrar(String texto) {
        System.out.println(texto);
    }

    private void mostrarSemQuebra(String texto) {
        System.out.print(texto);
    }
}
