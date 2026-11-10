package dev.vant.logistica.motorista;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    long countByStatus(StatusMotorista status);
}
