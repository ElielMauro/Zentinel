package com.zentinel.demo.controllers;

import com.zentinel.demo.models.Area;
import com.zentinel.demo.repositories.AreaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/areas")
public class AreaController {

    private final AreaRepository areaRepository;

    public AreaController(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("lista", areaRepository.findAll());
        return "areas";
    }

    @GetMapping("/nueva")
    public String form(Model model) {
        model.addAttribute("area", new Area());
        return "areas/form";
    }

    @PostMapping("/guardar")
    public String save(@ModelAttribute Area area) {
        areaRepository.save(area);
        return "redirect:/areas";
    }
}
