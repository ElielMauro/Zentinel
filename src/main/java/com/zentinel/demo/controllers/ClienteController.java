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
    public String list(Model model, jakarta.servlet.http.HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            model.addAttribute("listaClientes", clienteRepository.findByEmpresa_Id(empresaId));
        } else {
            model.addAttribute("listaClientes", new java.util.ArrayList<>());
        }
        return "clientes";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("tiposCliente", tipoClienteRepository.findAll());
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Cliente cliente, jakarta.servlet.http.HttpSession session) {
        if (cliente.getEmpresa() == null) {
            Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
            if (empresaId != null) {
                com.zentinel.demo.models.Empresa emp = new com.zentinel.demo.models.Empresa();
                emp.setId(empresaId);
                cliente.setEmpresa(emp);
            }
        }
        clienteRepository.save(cliente);
        return "redirect:/clientes";
    }

    @PostMapping("/tipo/guardar")
    public String saveTipo(@RequestParam String nombre) {
        com.zentinel.demo.models.TipoCliente tc = new com.zentinel.demo.models.TipoCliente();
        tc.setNombre(nombre);
        tipoClienteRepository.save(tc);
        return "redirect:/clientes";
    }
}
