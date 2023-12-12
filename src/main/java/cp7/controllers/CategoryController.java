package cp7.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryController {
    @GetMapping("/add_category")
    public String addCategory() {

        return "redirect:/pc";
    }

    @GetMapping("/delete_category")
    public String deleteCategory() {

        return "redirect:/pc";
    }
}
