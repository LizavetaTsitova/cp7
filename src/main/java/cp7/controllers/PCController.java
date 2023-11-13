package cp7.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PCController {
    @GetMapping("/pc")
    public String onenPC() {
        return "payment_calendar";
    }
}
