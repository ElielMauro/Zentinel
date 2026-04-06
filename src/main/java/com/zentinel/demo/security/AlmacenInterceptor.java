package com.zentinel.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AlmacenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        // Excluir rutas públicas, selector y panel maestro
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img") ||
                uri.startsWith("/login") || uri.startsWith("/error") || uri.startsWith("/almacen-selector") ||
                uri.startsWith("/zentinel-master") || uri.equals("/logout")) {
            return true;
        }

        // Si es SUPER_ADMIN, no obligar a seleccionar almacén para navegar por lo general
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }

        if (request.getSession().getAttribute("activeAlmacen") == null) {
            response.sendRedirect("/almacen-selector");
            return false;
        }

        return true;
    }
}
