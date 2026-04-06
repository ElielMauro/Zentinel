package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Almacen;
import com.zentinel.demo.models.Empresa;
import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.repositories.UsuarioRepository;
import com.zentinel.demo.services.AlmacenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/almacen-selector")
public class AlmacenSelectorController {

    private final AlmacenService almacenService;
    private final UsuarioRepository usuarioRepository;

    public AlmacenSelectorController(AlmacenService almacenService, UsuarioRepository usuarioRepository) {
        this.almacenService = almacenService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String showSelector(Model model, Principal principal, HttpSession session) {
        Usuario usuario = usuarioRepository.findById(principal.getName()).orElse(null);
        if (usuario == null)
            return "redirect:/login";

        java.util.List<Almacen> almacenes;
        Empresa companyToUse = usuario.getEmpresa();
        
        // Si es Super Admin, usar la empresa elegida en la sesión
        if ("SUPER_ADMIN".equals(usuario.getRol())) {
            companyToUse = (Empresa) session.getAttribute("currentEmpresa");
        }

        if (companyToUse != null && ("ADMIN_EMPRESA".equals(usuario.getRol()) || "SUPER_ADMIN".equals(usuario.getRol()))) {
            almacenes = companyToUse.getAlmacenes();
        } else {
            almacenes = almacenService.findByUser(usuario);
        }

        if (almacenes != null && almacenes.size() == 1) {
            // Auto-seleccionar si solo tiene uno
            return "redirect:/almacen-selector/seleccionar/" + almacenes.get(0).getId();
        }

        model.addAttribute("almacenes", almacenes);
        return "almacen_selector";
    }

    @GetMapping("/seleccionar/{id}")
    public String seleccionar(@PathVariable Integer id, HttpSession session, Principal principal) {
        Almacen almacen = almacenService.findById(id);
        Usuario usuario = usuarioRepository.findById(principal.getName()).orElse(null);

        if (usuario != null) {
            String rol = usuario.getRol();
            boolean isManager = "ADMIN".equals(rol) || "ADMIN_EMPRESA".equals(rol) || "SUPER_ADMIN".equals(rol);
            boolean hasAccess = isManager || (usuario.getAlmacenes() != null && usuario.getAlmacenes().contains(almacen));
            
            if (hasAccess) {
                session.setAttribute("activeAlmacen", almacen);
                // Si es superadmin y no tiene empresa en sesión, fijar la del almacén
                if ("SUPER_ADMIN".equals(rol) && session.getAttribute("currentEmpresa") == null) {
                    session.setAttribute("currentEmpresa", almacen.getEmpresa());
                }
            }
        }
        return "redirect:/";
    }
}
