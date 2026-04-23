package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Almacen;
import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.services.AlmacenService;
import com.zentinel.demo.services.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AlmacenService almacenService;

    public UsuarioController(UsuarioService usuarioService, AlmacenService almacenService) {
        this.usuarioService = usuarioService;
        this.almacenService = almacenService;
    }

    @GetMapping
    public String listarUsuarios(Model model, jakarta.servlet.http.HttpSession session, org.springframework.security.core.Authentication auth) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin && empresaId == null) {
            // El Super Admin viendo todos en general (si no ha seleccionado empresa)
            model.addAttribute("usuarios", usuarioService.findAll());
        } else if (empresaId != null) {
            // Todos los demás (o SA con contexto) ven los de la empresa
            List<Usuario> usuarios = usuarioService.findByEmpresaId(empresaId);
            
            // Si NO es Super Admin, ocultamos a los SUPER_ADMIN de la lista
            List<Usuario> listadoFiltrado = usuarios.stream()
                .filter(u -> isSuperAdmin || !"SUPER_ADMIN".equals(u.getRol()))
                .toList();
            
            model.addAttribute("usuarios", listadoFiltrado);
        } else {
            model.addAttribute("usuarios", new java.util.ArrayList<>());
        }

        // Pasar almacenes disponibles de la empresa para asignarlos al usuario nuevo
        if (empresaId != null) {
            model.addAttribute("almacenesDisponibles", almacenService.findByEmpresaId(empresaId));
        } else {
            model.addAttribute("almacenesDisponibles", new java.util.ArrayList<>());
        }

        model.addAttribute("nuevoUsuario", new Usuario());
        return "usuarios";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario,
                                 @RequestParam(value = "almacenIds", required = false) List<Integer> almacenIds,
                                 jakarta.servlet.http.HttpSession session,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            // Asignar empresa del contexto si el usuario no tiene una (para creación desde gestión)
            Integer empresaId = null;
            if (usuario.getEmpresa() == null) {
                empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
                if (empresaId != null) {
                    com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
                    emp.setId(empresaId);
                    usuario.setEmpresa(emp);
                }
            } else {
                empresaId = usuario.getEmpresa().getId();
            }

            if (empresaId == null) {
                throw new RuntimeException("No se detectó una empresa activa para asignar al usuario.");
            }

            // Asignar almacenes seleccionados al usuario
            if (almacenIds != null && !almacenIds.isEmpty()) {
                Set<Almacen> almacenes = new HashSet<>();
                for (Integer almacenId : almacenIds) {
                    Almacen alm = almacenService.findById(almacenId);
                    if (alm != null) {
                        almacenes.add(alm);
                    }
                }
                usuario.setAlmacenes(almacenes);
            } else {
                // Si no se seleccionaron almacenes específicos, asignar TODOS los de la empresa
                List<Almacen> todosAlmacenes = almacenService.findByEmpresaId(empresaId);
                usuario.setAlmacenes(new HashSet<>(todosAlmacenes));
            }

            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario '" + usuario.getUsuario() + "' guardado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar")
    public String eliminarUsuario(@RequestParam String id, org.springframework.security.core.Authentication auth, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        boolean isSuperAdminReq = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Usuario targetUser = usuarioService.findById(id);
        if (targetUser != null && "SUPER_ADMIN".equals(targetUser.getRol()) && !isSuperAdminReq) {
            redirectAttributes.addFlashAttribute("error", "Seguridad: No tienes permiso para deshabilitar una cuenta de nivel maestro.");
            return "redirect:/usuarios";
        }

        usuarioService.delete(id);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario deshabilitado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/activar")
    public String activarUsuario(@RequestParam String id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        usuarioService.activar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario activado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String usuarioId, @RequestParam String nuevaPassword, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Usuario user = usuarioService.findById(usuarioId);
        if (user != null) {
            user.setPassword(nuevaPassword);
            usuarioService.save(user);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña actualizada correctamente.");
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/mi-password")
    public String cambiarMiPassword(@RequestParam String nuevaPassword, org.springframework.security.core.Authentication auth, jakarta.servlet.http.HttpServletRequest request) {
        if (auth != null && auth.getName() != null) {
            Usuario user = usuarioService.findById(auth.getName());
            if (user != null) {
                user.setPassword(nuevaPassword);
                usuarioService.save(user);
            }
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}