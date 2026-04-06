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
    public String dashboard(Model model, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpSession session) {
        // Detectar si hay una empresa seleccionada en sesión (para Super Admin)
        Empresa sessionEmpresa = (Empresa) session.getAttribute("currentEmpresa");
        
        // Si es Super Admin y no ha elegido empresa, mandarlo al panel maestro
        if (request.isUserInRole("ROLE_SUPER_ADMIN") && sessionEmpresa == null) {
            return "redirect:/zentinel-master/empresas";
        }

        // Determinar ID de empresa a usar
        Integer empresaId = null;
        if (sessionEmpresa != null) {
            empresaId = sessionEmpresa.getId();
        } else {
            // Usuario normal: obtener de su perfil
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof com.zentinel.demo.security.UsuarioPrincipal) {
                empresaId = ((com.zentinel.demo.security.UsuarioPrincipal) principal).getEmpresaId();
            }
        }

        if (empresaId == null) return "redirect:/login";

        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0);
        
        long totalProductos = productoRepository.countByEmpresa_Id(empresaId);
        long entradasMes = entradaRepository.countByEmpresa_IdAndFechaRecepcionAfter(empresaId, startOfMonth);
        long salidasMes = salidaRepository.countByEmpresa_IdAndFechaAfter(empresaId, startOfMonth);

        // Productos con bajo stock (< 5) filtrados por empresa
        List<Inventario> lowStock = inventarioRepository.findByAlmacen_Empresa_Id(empresaId).stream()
                .filter(i -> i.getCantidad().compareTo(new BigDecimal("5")) < 0)
                .collect(Collectors.toList());

        // Gastos por departamento filtrados por empresa
        List<Salida> salidas = salidaRepository.findByEmpresa_Id(empresaId);
        Map<String, BigDecimal> gastosPorDepto = salidas.stream()
                .filter(s -> !s.getCancelado())
                .collect(Collectors.groupingBy(
                        s -> s.getDepartamento() != null ? s.getDepartamento() : "Sin Depto",
                        Collectors.reducing(BigDecimal.ZERO, Salida::getTotal, BigDecimal::add)
                ));

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("entradasMes", entradasMes);
        model.addAttribute("salidasMes", salidasMes);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("gastosPorDepto", gastosPorDepto);
        model.addAttribute("currentEmpresa", sessionEmpresa);

        return "index";
    }
}
