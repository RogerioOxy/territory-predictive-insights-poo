package br.com.fiap.territorio.servico;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Random;

/**
 * Disponibiliza as dependencias para o Spring injetar nos componentes da aplicacao.
 */
@Configuration
public class ConfiguracaoAplicacao {

    @Bean
    public ConfiguracaoSistema configuracaoSistema() {
        return ConfiguracaoSistema.getInstance();
    }

    @Bean
    public MapaTerritorio mapaTerritorio() {
        return MapaTerritorio.criarPadrao(new Random());
    }

    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ExportadorCsv exportadorCsv() {
        return new ExportadorCsv(Path.of("exportacoes"));
    }
}
