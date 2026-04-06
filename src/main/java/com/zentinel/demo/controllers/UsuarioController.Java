package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.services.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarUsuarios(Model model, jakarta.servlet.http.HttpSession session, org.springframework.security.core.Authentication auth) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            model.addAttribute("usuarios", usuarioService.findAll());
        } else if (empresaId != null) {
            model.addAttribute("usuarios", usuarioService.findByEmpresaId(empresaId));
        } else {
            model.addAttribute("usuarios", new java.util.ArrayList<>());
        }
        model.addAttribute("nuevoUsuario", new Usuario());
        return "usuarios";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, jakarta.servlet.http.HttpSession session) {
        // Asignar empresa del contexto si el usuario no tiene una (para creación desde gestión)
        if (usuario.getEmpresa() == null) {
            Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
            if (empresaId != null) {
                Usuario temp = new Usuario();
                com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
                emp.setId(empresaId);
                usuario.setEmpresa(emp);
            }
        }
        usuarioService.save(usuario);
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar")
    public String eliminarUsuario(@RequestParam String id) {
        usuarioService.delete(id);
        return "redirect:/usuarios";
    }
}