package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma leitura validada, com sensor, ponto e horario.
 */
@Entity
@Table(name = "medicao")
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String codigoSensor;

    @Column(nullable = false, length = 20)
    private String codigoPonto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoSensor tipoSensor;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Medicao() {
    }

    public Medicao(String codigoSensor, String codigoPonto, TipoSensor tipoSensor, double valor,
                   LocalDateTime dataHora) throws DadosInvalidosException {
        if (codigoSensor == null || codigoSensor.isBlank() || codigoPonto == null || codigoPonto.isBlank()) {
            throw new DadosInvalidosException(Mensagens.ERRO_MEDICAO_SEM_ORIGEM);
        }
        if (tipoSensor == null || dataHora == null) {
            throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_MEDICAO_INCOMPLETA, codigoSensor));
        }
        // A excecao interrompe a criacao antes que uma leitura fora da faixa possa ser gravada.
        if (!tipoSensor.aceita(valor)) {
            throw new DadosInvalidosException(Formatador.f(Mensagens.ERRO_MEDICAO_FORA_DA_FAIXA,
                    tipoSensor.getNome(), codigoSensor, valor, tipoSensor.getUnidade(),
                    tipoSensor.getMinimoFisico(), tipoSensor.getMaximoFisico(), tipoSensor.getUnidade()));
        }
        this.codigoSensor = codigoSensor;
        this.codigoPonto = codigoPonto;
        this.tipoSensor = tipoSensor;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoSensor() {
        return codigoSensor;
    }

    public String getCodigoPonto() {
        return codigoPonto;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
