package com.zentinel.demo.controllers;

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
        // Obtenemos los usuarios de la base de datos y los pasamos a la vista
        model.addAttribute("listaUsuarios", usuarioRepository.findAll());
        return "usuarios"; // Nombre del archivo HTML en templates
    }
}