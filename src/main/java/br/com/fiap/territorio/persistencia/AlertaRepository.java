package br.com.fiap.territorio.persistencia;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Usa Spring Data JPA para persistir alertas e derivar consultas pelos nomes dos metodos.
 */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findAllByOrderByDataHoraDescIdDesc();

    Optional<Alerta> findFirstByOrderByDataHoraDescIdDesc();

    boolean existsByCodigoSensorAndTipoPrevisaoAndNivelAndLimiteUltrapassadoAndIdUltimaMedicao(
            String codigoSensor, TipoPrevisao tipoPrevisao, NivelAlerta nivel, double limiteUltrapassado,
            Long idUltimaMedicao);
}
