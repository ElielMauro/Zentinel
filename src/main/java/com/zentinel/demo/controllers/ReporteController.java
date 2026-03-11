package com.zentinel.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReporteController {

    @GetMapping("/reportes")
    public String reportes() {
        return "reportes";
    }
}
