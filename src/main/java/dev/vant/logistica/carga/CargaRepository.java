package dev.vant.logistica.carga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface CargaRepository extends JpaRepository<Carga, Long>, JpaSpecificationExecutor<Carga> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByClienteId(Long clienteId);

    boolean existsByMotoristaId(Long motoristaId);

    boolean existsByVeiculoId(Long veiculoId);

    long countByStatus(StatusCarga status);

    List<Carga> findTop5ByOrderByCriadaEmDesc();

    @Query("""
            select count(c) from Carga c
            where c.previsaoEntrega < :hoje
              and c.status in :status
            """)
    long contarAtrasadas(@Param("hoje") LocalDate hoje, @Param("status") Collection<StatusCarga> status);
}
