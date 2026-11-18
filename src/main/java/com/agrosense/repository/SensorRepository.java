package com.agrosense.repository;

import com.agrosense.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    List<Sensor> findAllByOrderByCreatedAtAsc();
    List<Sensor> findByCultivoIdOrderByCreatedAtAsc(UUID cultivoId);
    long countByCultivoId(UUID cultivoId);
}
