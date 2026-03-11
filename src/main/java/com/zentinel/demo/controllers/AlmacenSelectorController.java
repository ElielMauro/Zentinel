package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Almacen;
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
    public String showSelector(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findById(principal.getName()).orElse(null);
        if (usuario == null)
            return "redirect:/login";

        java.util.List<Almacen> almacenes = almacenService.findByUser(usuario);
        if (almacenes.size() == 1) {
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
            boolean hasAccess = "ADMIN".equals(usuario.getRol()) || usuario.getAlmacenes().contains(almacen);
            if (hasAccess) {
                session.setAttribute("activeAlmacen", almacen);
            }
        }
        return "redirect:/";
    }
}
