package cp7.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login_error")
    public String loginError(Model model) {
        model.addAttribute("errorMessage", "Пользователь не найден");
        return "login";
    }
}
