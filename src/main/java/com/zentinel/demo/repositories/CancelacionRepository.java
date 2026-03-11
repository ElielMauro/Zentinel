package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Cancelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelacionRepository extends JpaRepository<Cancelacion, Integer> {
}
