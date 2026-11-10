package dev.vant.logistica.carga;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoCargaRepository extends JpaRepository<HistoricoCarga, Long> {

    List<HistoricoCarga> findByCargaIdOrderByDataAlteracaoDesc(Long cargaId);
}
