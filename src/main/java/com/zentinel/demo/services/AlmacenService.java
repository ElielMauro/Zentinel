package com.zentinel.demo.services;

import com.zentinel.demo.models.*;
import com.zentinel.demo.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class AlmacenService {
    private final AlmacenRepository almacenRepository;

    public AlmacenService(AlmacenRepository almacenRepository) {
        this.almacenRepository = almacenRepository;
    }

    public List<Almacen> findAll() {
        return almacenRepository.findAll();
    }

    public Almacen findById(Integer id) {
        return almacenRepository.findById(id).orElse(null);
    }

    public List<Almacen> findByUser(Usuario usuario) {
        if (usuario == null)
            return new ArrayList<>();
        if ("SUPER_ADMIN".equals(usuario.getRol())) {
            return findAll();
        }
        if (("ADMIN_EMPRESA".equals(usuario.getRol()) || "ADMIN".equals(usuario.getRol())) && usuario.getEmpresa() != null) {
            return findByEmpresaId(usuario.getEmpresa().getId());
        }
        return new ArrayList<>(usuario.getAlmacenes());
    }

    public List<Almacen> findByEmpresaId(Integer empresaId) {
        return almacenRepository.findByEmpresaId(empresaId);
    }

    public Almacen save(Almacen almacen) {
        return almacenRepository.save(almacen);
    }

    public void deleteById(Integer id) {
        almacenRepository.deleteById(id);
    }
}

