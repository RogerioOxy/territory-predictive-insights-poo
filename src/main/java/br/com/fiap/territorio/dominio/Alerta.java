package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.Formatador;
import br.com.fiap.territorio.Mensagens;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade JPA que guarda o risco previsto e a medicao usada como referencia.
 */
@Entity
@Table(name = "alerta")
public class Alerta {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoPrevisao tipoPrevisao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAlerta nivel;

    @Column(nullable = false)
    private double valorPrevisto;

    @Column(nullable = false)
    private double limiteUltrapassado;

    private Long idUltimaMedicao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Alerta() {
    }

    public Alerta(String codigoSensor, String codigoPonto, TipoSensor tipoSensor, TipoPrevisao tipoPrevisao,
                  NivelAlerta nivel, double valorPrevisto, double limiteUltrapassado, Long idUltimaMedicao,
                  LocalDateTime dataHora) {
        this.codigoSensor = Objects.requireNonNull(codigoSensor);
        this.codigoPonto = Objects.requireNonNull(codigoPonto);
        this.tipoSensor = Objects.requireNonNull(tipoSensor);
        this.tipoPrevisao = Objects.requireNonNull(tipoPrevisao);
        this.nivel = Objects.requireNonNull(nivel);
        this.valorPrevisto = valorPrevisto;
        this.limiteUltrapassado = limiteUltrapassado;
        this.idUltimaMedicao = idUltimaMedicao;
        this.dataHora = Objects.requireNonNull(dataHora);
    }

    public String explicar() {
        return Formatador.f(Mensagens.EXPLICACAO_ALERTA, nivel.name(), tipoSensor.getNome(), codigoSensor,
                codigoPonto, tipoPrevisao.getNome(), valorPrevisto, tipoSensor.getUnidade(),
                limiteUltrapassado, tipoSensor.getUnidade(), nivel.getRecomendacao());
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

    public TipoPrevisao getTipoPrevisao() {
        return tipoPrevisao;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteUltrapassado() {
        return limiteUltrapassado;
    }

    public Long getIdUltimaMedicao() {
        return idUltimaMedicao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
