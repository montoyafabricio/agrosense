package com.agrosense.medicion.repository;

import com.agrosense.medicion.model.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicionRepository extends JpaRepository<Medicion, UUID> {
    List<Medicion> findBySensorIdOrderByFechaDesc(UUID sensorId);
    List<Medicion> findAllByOrderByFechaDesc();
}
