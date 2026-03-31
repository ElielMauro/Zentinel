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
        System.out.println("DEBUG: Entrando a /productos/nuevo");
        try {
            model.addAttribute("producto", new Producto());
            var cats = tipoProductoRepository.findAll();
            System.out.println("DEBUG: Categorias encontradas: " + (cats != null ? cats.size() : "null"));
            model.addAttribute("categorias", cats);
            return "productos/form";
        } catch (Exception e) {
            System.err.println("DEBUG ERROR en /productos/nuevo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/detalle/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        Producto p = productoService.findBySku(sku);
        if (p == null) return "redirect:/productos";
        model.addAttribute("producto", p);
        return "productos/detalle";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Producto producto) {
        productoService.save(producto);
        return "redirect:/productos";
    }
}
