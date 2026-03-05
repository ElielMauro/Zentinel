package com.zentinel.demo.controlers;

import com.zentinel.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        // Obtenemos todos los usuarios de la DB y los enviamos a la vista
        model.addAttribute("listaUsuarios", usuarioRepository.findAll());
        return "usuarios"; // Esto buscará un archivo llamado usuarios.html en templates
    }
}