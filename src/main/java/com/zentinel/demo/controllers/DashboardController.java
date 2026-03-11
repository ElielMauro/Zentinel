package com.zentinel.demo.controllers;

import com.zentinel.demo.models.*;
import com.zentinel.demo.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ProductoRepository productoRepository;
    private final EntradaRepository entradaRepository;
    private final SalidaRepository salidaRepository;
    private final InventarioRepository inventarioRepository;
    private final AreaRepository areaRepository;

    public DashboardController(ProductoRepository productoRepository,
                               EntradaRepository entradaRepository,
                               SalidaRepository salidaRepository,
                               InventarioRepository inventarioRepository,
                               AreaRepository areaRepository) {
        this.productoRepository = productoRepository;
        this.entradaRepository = entradaRepository;
        this.salidaRepository = salidaRepository;
        this.inventarioRepository = inventarioRepository;
        this.areaRepository = areaRepository;
    }

    @GetMapping({"", "/", "/index"})
    public String dashboard(Model model) {
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0);
        
        long totalProductos = productoRepository.count();
        long entradasMes = entradaRepository.countByFechaRecepcionAfter(startOfMonth);
        long salidasMes = salidaRepository.countByFechaAfter(startOfMonth);

        // Productos con bajo stock (< 5)
        List<Inventario> lowStock = inventarioRepository.findAll().stream()
                .filter(i -> i.getCantidad().compareTo(new BigDecimal("5")) < 0)
                .collect(Collectors.toList());

        // Gastos por departamento
        List<Salida> salidas = salidaRepository.findAll();
        Map<String, BigDecimal> gastosPorDepto = salidas.stream()
                .filter(s -> !s.getCancelado())
                .collect(Collectors.groupingBy(
                        s -> s.getDepartamento() != null ? s.getDepartamento() : "Sin Depto",
                        Collectors.reducing(BigDecimal.ZERO, Salida::getTotal, BigDecimal::add)
                ));

        // Áreas
        List<Area> areas = areaRepository.findAll();

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("entradasMes", entradasMes);
        model.addAttribute("salidasMes", salidasMes);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("gastosPorDepto", gastosPorDepto);
        model.addAttribute("areas", areas);

        return "index";
    }
}
