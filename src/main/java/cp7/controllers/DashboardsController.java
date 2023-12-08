package cp7.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardsController {
    @GetMapping("/dashboards")
    public String onenPC(HttpServletRequest request, Model model) {

        model.addAttribute("request", request);
        return "dashboards";
    }
}
