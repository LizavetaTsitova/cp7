package cp7.controllers;

import cp7.entities.Categories;
import cp7.entities.Users;
import cp7.entities.Users_inf;
import cp7.repositories.UserRepository;
import cp7.repositories.User_infRepository;
import cp7.services.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    User_infRepository user_infRepository;

    @PostMapping("/add_category")
    public String addCategory(@ModelAttribute Categories category) {
        // Получаем текущую аутентификацию
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;

        // Получаем информацию о пользователе
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
            }
        }

        Users user = userRepository.findByEmail(email);
        Integer compID = user_infRepository.findByUserId(user.getUser_id()).getIdCompany();

        categoryService.addCategory(category, compID);

        return "redirect:/pc";
    }

    @PostMapping("/delete_category")
    public String deleteCategory(@RequestParam Integer categoryID) {
        categoryService.deleteCategory(categoryID);
        return "redirect:/pc";
    }
}
