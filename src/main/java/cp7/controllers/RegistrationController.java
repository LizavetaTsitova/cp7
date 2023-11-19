package cp7.controllers;

import cp7.entities.Companies;
import cp7.entities.Users;
import cp7.entities.Users_inf;
import cp7.entities.enums.Role;
import cp7.repositories.CompanyRepository;
import cp7.repositories.UserRepository;
import cp7.repositories.User_infRepository;
import cp7.services.UserService;
import cp7.services.User_infService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {
    @Autowired
    UserService userService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    User_infRepository user_infRepository;


    @GetMapping("/reg")
    public String openRegForm(Model model) {
        List<Companies> companies = companyRepository.findAll();

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

        user_inf.setUser_id(user.getUser_id());
        if(String.valueOf(user.getRole()).contains("PERSONAL")){
            user_inf.setId_company(null);
        }
        user_infRepository.save(user_inf);

        return "redirect:/login";
    }

    @GetMapping("/company_registration")
    public String regCompany(){
        return "redirect:/";
    }
}
