package com.zentinel.demo.repositories;

import com.zentinel.demo.models.Empresa;
package com.zentinel.zentinel.repositories;

import com.zentinel.zentinel.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    // Aquí el ID es Integer
}