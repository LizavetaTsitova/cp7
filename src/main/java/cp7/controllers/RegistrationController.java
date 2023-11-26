package cp7.controllers;

import cp7.entities.Companies;
import cp7.entities.Users;
import cp7.entities.Users_inf;
import cp7.entities.enums.Role;
import cp7.repositories.CompanyRepository;
import cp7.repositories.User_infRepository;
import cp7.services.CompanyService;
import cp7.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {
    @Autowired
    UserService userService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    User_infRepository user_infRepository;
    @Autowired
    CompanyService companyService;


    @GetMapping("/reg")
    public String openRegForm(Model model) {
        List<Companies> companies = companyRepository.findAllByStatus(true);

        model.addAttribute("companies", companies);
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute Users user, Model model, @RequestParam Map<String, String> form, @ModelAttribute Users_inf user_inf){
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        for (String key : form.keySet()) {
            if (roles.contains(form.get(key))) {
                user.getRole().add(Role.valueOf(form.get(key)));
            }
        }
        if (!userService.createUser(user)) {
            model.addAttribute("errorMessage", "Пользователь с email: " + user.getEmail() +
                    " уже существует");
            return "registration";
        }

        user_inf.setUserId(user.getUser_id());
        if(String.valueOf(user.getRole()).contains("PERSONAL")){
            user_inf.setIdCompany(null);
        }
        user_infRepository.save(user_inf);

        return "redirect:/login";
    }

    @PostMapping("/company_registration")
    public String regCompany(Model model, @ModelAttribute Companies company){
        if (!companyService.registerCompany(company)) {
            model.addAttribute("errorMessage", "Компания с email: " + company.getEmail() +
                    " уже существует");
            return "company_registration";
        }
        return "confirmation_waiting";
    }

    @GetMapping("/activation/{code}")
    public String activate(Model model, @PathVariable String code){
        boolean isActivated = companyService.activateCompany(code);

        if (isActivated) {
            model.addAttribute("message", "Ваша компания успешно подтверждена!");
        } else {
            model.addAttribute("message", "Эта почта уже прошла валидацию");
        }

        return "confirmation_waiting";
    }
}
