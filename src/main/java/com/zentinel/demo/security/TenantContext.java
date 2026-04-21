package com.zentinel.demo.security;

import com.zentinel.demo.models.Almacen;
import com.zentinel.demo.models.Empresa;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;

public class TenantContext {

    public static Integer getCurrentEmpresaId(HttpSession session) {
        // 1. Prioridad: Empresa seleccionada en sesión (Contexto de Gestión para Super
        // Admin)
        Empresa sessionEmpresa = (Empresa) session.getAttribute("currentEmpresa");
        if (sessionEmpresa != null) {
            return sessionEmpresa.getId();
        }

        // 2. Si no hay sesión (o es usuario normal), obtener de UsuarioPrincipal
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UsuarioPrincipal) {
                return ((UsuarioPrincipal) principal).getEmpresaId();
            }
        }

        return null;
    }

    public static Almacen getActiveAlmacen(HttpSession session) {
        return (Almacen) session.getAttribute("activeAlmacen");
    }
}
