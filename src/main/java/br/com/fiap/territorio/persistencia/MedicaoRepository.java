package br.com.fiap.territorio.persistencia;

import br.com.fiap.territorio.dominio.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Usa Spring Data JPA para consultar e persistir leituras sem montar SQL manualmente.
 */
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {

    List<Medicao> findByCodigoSensorOrderByDataHoraAscIdAsc(String codigoSensor);

    Optional<Medicao> findFirstByCodigoSensorOrderByDataHoraDescIdDesc(String codigoSensor);

    long countByCodigoSensor(String codigoSensor);
}
