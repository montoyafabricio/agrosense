package com.agrosense.repository;

import com.agrosense.model.Alerta;
import com.agrosense.model.AlertaPrioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
    List<Alerta> findAllByOrderByFechaDesc();

    List<Alerta> findByCultivoIdOrderByFechaDesc(UUID cultivoId);

    List<Alerta> findByPrioridadOrderByFechaDesc(AlertaPrioridad prioridad);

    List<Alerta> findByCultivoIdAndPrioridadOrderByFechaDesc(UUID cultivoId, AlertaPrioridad prioridad);

    long countByCultivoId(UUID cultivoId);

    @Query("SELECT a FROM Alerta a WHERE a.cultivoId = :cultivoId AND a.tipo = :tipo " +
           "AND a.fecha BETWEEN :fromTime AND :toTime ORDER BY a.fecha DESC")
    List<Alerta> findDuplicates(
        @Param("cultivoId") UUID cultivoId,
        @Param("tipo") String tipo,
        @Param("fromTime") OffsetDateTime fromTime,
        @Param("toTime") OffsetDateTime toTime
    );
}
