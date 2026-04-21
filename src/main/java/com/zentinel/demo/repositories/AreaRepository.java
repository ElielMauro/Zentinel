package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
    java.util.List<Area> findByEmpresa_Id(Integer empresaId);
}
