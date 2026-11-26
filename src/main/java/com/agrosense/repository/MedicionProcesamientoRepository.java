package com.agrosense.repository;

import com.agrosense.model.MedicionProcesamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MedicionProcesamientoRepository extends JpaRepository<MedicionProcesamiento, UUID> {
}
