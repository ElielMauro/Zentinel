package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Proveedor;
import com.zentinel.demo.repositories.ProveedorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    public ProveedorController(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("listaProveedores", proveedorRepository.findAll());
        return "proveedores";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Proveedor proveedor) {
        proveedorRepository.save(proveedor);
        return "redirect:/proveedores";
    }
}
