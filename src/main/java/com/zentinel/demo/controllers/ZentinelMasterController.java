package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Empresa;
import com.zentinel.demo.services.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/zentinel-master")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ZentinelMasterController {

    private final EmpresaService empresaService;

    @GetMapping("/empresas")
    public String listEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.findAll());
        return "zentinel_master/empresas";
    }

    @GetMapping("/empresas/nueva")
    public String nuevaEmpresaForm(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "zentinel_master/empresa_form";
    }

    @PostMapping("/empresas/guardar")
    public String guardarEmpresa(@ModelAttribute Empresa empresa,
                                 @RequestParam String adminNombre,
                                 @RequestParam String adminEmail,
                                 @RequestParam String adminPassword) {
        empresaService.save(empresa, adminNombre, adminEmail, adminPassword);
        return "redirect:/zentinel-master/empresas?success";
    }

    @GetMapping("/empresas/editar/{id}")
    public String editarEmpresaForm(@PathVariable Integer id, Model model) {
        Empresa empresa = empresaService.findById(id);
        if (empresa == null) return "redirect:/zentinel-master/empresas?error=notfound";
        model.addAttribute("empresa", empresa);
        return "zentinel_master/empresa_form";
    }

    @GetMapping("/empresas/eliminar/{id}")
    public String eliminarEmpresa(@PathVariable Integer id) {
        empresaService.delete(id);
        return "redirect:/zentinel-master/empresas?deleted";
    }
}
