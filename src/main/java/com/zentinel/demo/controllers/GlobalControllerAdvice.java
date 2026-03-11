package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Almacen;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("activeAlmacen")
    public Almacen getActiveAlmacen(HttpSession session) {
        return (Almacen) session.getAttribute("activeAlmacen");
    }
}
