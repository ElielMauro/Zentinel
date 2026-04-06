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
    public String list(Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            model.addAttribute("listaAlmacenes", almacenService.findByEmpresaId(empresaId));
        } else {
            model.addAttribute("listaAlmacenes", new java.util.ArrayList<>());
        }
        return "almacenes";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("almacen", new Almacen());
        return "almacenes/form";
    }

    @GetMapping("/editar/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Almacen almacen = almacenService.findById(id);
        if (almacen == null) return "redirect:/almacenes";
        model.addAttribute("almacen", almacen);
        return "almacenes/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Almacen almacen, jakarta.servlet.http.HttpSession session) {
        if (almacen.getEmpresa() == null) {
            Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
            if (empresaId != null) {
                com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
                emp.setId(empresaId);
                almacen.setEmpresa(emp);
            }
        }
        
        // Asignar ID solo si es nuevo
        if (almacen.getId() == null) {
            java.util.List<Almacen> existing = almacenService.findByEmpresaId(almacen.getEmpresa().getId());
            almacen.setId(almacen.getEmpresa().getId() * 100 + existing.size() + 1);
        }
        
        almacenService.save(almacen);
        return "redirect:/almacenes";
    }

    @GetMapping("/eliminar/{id}")
    public String delete(@PathVariable Integer id, jakarta.servlet.http.HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        Almacen a = almacenService.findById(id);
        
        if (a != null && a.getEmpresa() != null && a.getEmpresa().getId().equals(empresaId)) {
            try {
                almacenService.deleteById(id);
                redirectAttributes.addFlashAttribute("mensaje", "Almacén eliminado correctamente.");
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar el almacén porque tiene movimientos (entradas, salidas o inventario) vinculados.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este almacén.");
        }
        return "redirect:/almacenes";
    }
}

