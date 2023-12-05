package cp7.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardsController {
    @GetMapping("/dashboards")
    public String onenPC() {
        return "dashboards";
    }
}
