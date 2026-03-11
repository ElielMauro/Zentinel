package com.zentinel.demo.controllers;

import com.zentinel.demo.models.TipoProducto;
import com.zentinel.demo.repositories.TipoProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class TipoProductoController {

    private final TipoProductoRepository tipoProductoRepository;

    public TipoProductoController(TipoProductoRepository tipoProductoRepository) {
        this.tipoProductoRepository = tipoProductoRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("lista", tipoProductoRepository.findAll());
        return "categorias";
    }

    @GetMapping("/nueva")
    public String form(Model model) {
        model.addAttribute("categoria", new TipoProducto());
        return "categorias/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute TipoProducto categoria) {
        tipoProductoRepository.save(categoria);
        return "redirect:/categorias";
    }
}
