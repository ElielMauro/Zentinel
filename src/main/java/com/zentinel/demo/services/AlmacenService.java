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
        if ("ADMIN".equals(usuario.getRol())) {
            return findAll();
        }
        return new ArrayList<>(usuario.getAlmacenes());
    }
}
