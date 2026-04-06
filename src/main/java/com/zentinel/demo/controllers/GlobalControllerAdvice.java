package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Almacen;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final com.zentinel.demo.services.AlmacenService almacenService;

    public GlobalControllerAdvice(com.zentinel.demo.services.AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @ModelAttribute("activeAlmacen")
    public Almacen getActiveAlmacen(HttpSession session) {
        return (Almacen) session.getAttribute("activeAlmacen");
    }

    @ModelAttribute("currentEmpresa")
    public com.zentinel.demo.models.Empresa getCurrentEmpresa(HttpSession session) {
        return (com.zentinel.demo.models.Empresa) session.getAttribute("currentEmpresa");
    }

    @ModelAttribute("listaAlmacenesSidebar")
    public java.util.List<Almacen> getListaAlmacenesSidebar(HttpSession session) {
        Integer empresaId = com.zentinel.demo.security.TenantContext.getCurrentEmpresaId(session);
        if (empresaId != null) {
            return almacenService.findByEmpresaId(empresaId);
        }
        return new java.util.ArrayList<>();
    }
}

