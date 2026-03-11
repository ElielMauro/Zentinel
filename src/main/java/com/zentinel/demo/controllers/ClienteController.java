package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Cliente;
import com.zentinel.demo.repositories.ClienteRepository;
import com.zentinel.demo.repositories.TipoClienteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final TipoClienteRepository tipoClienteRepository;

    public ClienteController(ClienteRepository clienteRepository, TipoClienteRepository tipoClienteRepository) {
        this.clienteRepository = clienteRepository;
        this.tipoClienteRepository = tipoClienteRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("listaClientes", clienteRepository.findAll());
        return "clientes";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("tiposCliente", tipoClienteRepository.findAll());
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Cliente cliente) {
        clienteRepository.save(cliente);
        return "redirect:/clientes";
    }
}
