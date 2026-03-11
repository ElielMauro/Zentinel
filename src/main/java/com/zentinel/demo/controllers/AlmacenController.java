package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Almacen;
import com.zentinel.demo.services.AlmacenService;
import com.zentinel.demo.repositories.EmpresaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/almacenes")
public class AlmacenController {

    private final AlmacenService almacenService;
    private final EmpresaRepository empresaRepository;

    public AlmacenController(AlmacenService almacenService, EmpresaRepository empresaRepository) {
        this.almacenService = almacenService;
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("listaAlmacenes", almacenService.findAll());
        return "almacenes";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("almacen", new Almacen());
        model.addAttribute("empresas", empresaRepository.findAll());
        return "almacenes/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Almacen almacen) {
        // Forzar empresa 1 por simplicidad en demo si no viene
        if (almacen.getEmpresa() == null) {
            almacen.setEmpresa(empresaRepository.findById(1).orElse(null));
        }
        // almacenService.save(almacen); // Implementar en service si es necesario
        return "redirect:/almacenes";
    }
}
