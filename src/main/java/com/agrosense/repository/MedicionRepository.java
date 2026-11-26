package com.agrosense.repository;

import com.agrosense.model.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicionRepository extends JpaRepository<Medicion, UUID> {
    List<Medicion> findAllByOrderByFechaDesc();
    List<Medicion> findBySensorIdOrderByFechaDesc(UUID sensorId);
    long countBySensorId(UUID sensorId);
}
