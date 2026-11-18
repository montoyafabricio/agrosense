package com.agrosense.gestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, UUID> {
    List<Cultivo> findAllByOrderByCreatedAtAsc();
    List<Cultivo> findByProductorId(UUID productorId);
    long countByProductorId(UUID productorId);
}
