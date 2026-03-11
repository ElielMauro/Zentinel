package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Producto;
import com.zentinel.demo.services.ProductoService;
import com.zentinel.demo.repositories.TipoProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final TipoProductoRepository tipoProductoRepository;

    public ProductoController(ProductoService productoService, TipoProductoRepository tipoProductoRepository) {
        this.productoService = productoService;
        this.tipoProductoRepository = tipoProductoRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("listaProductos", productoService.findAll());
        return "productos";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", tipoProductoRepository.findAll());
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Producto producto) {
        // productoService.save(producto);
        return "redirect:/productos";
    }
}
