package com.agrosense.repository;

import com.agrosense.model.Productor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductorRepository extends JpaRepository<Productor, UUID> {
    List<Productor> findAllByOrderByCreatedAtAsc();
}
