package cp7.controllers;

import cp7.entities.*;
import cp7.repositories.*;
import cp7.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Controller
public class PCController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    User_infRepository user_infRepository;

    @Autowired
    Paym_calRepository paym_calRepository;

    @Autowired
    Cash_flowsRepository cash_flowsRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping("/pc")
    public String onenPC(Model model) {
        String email = null;
        List<Paym_cal> paym_cal;

        // Получаем текущую аутентификацию
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Получаем информацию о пользователе
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
                // Вы можете получить другие данные о пользователе, такие как роли, авторизации и т.д.
            }
        }

        Users user = userRepository.findByEmail(email);
        Users_inf users_inf = user_infRepository.findByUserId(user.getUser_id());
        if(users_inf.getId_company() == null){
            paym_cal = paym_calRepository.findAllByOwnerId(users_inf.getUserId());
        }
        else{
            paym_cal = paym_calRepository.findAllByIdCompany(users_inf.getId_company());
        }
        List<Categories> categories_inc = categoryRepository.findAllByCompanyIdAndType(users_inf.getId_company(), 0);

        Integer cal_id = null;
        Integer days = null;
        for (Paym_cal paymCal : paym_cal) {
            LocalDate localStartDate = paymCal.getStart_date().toLocalDate();
            LocalDate localEndDate = paymCal.getEnd_date().toLocalDate();
            LocalDate currentDate = LocalDate.now();
            if (currentDate.isAfter(localStartDate) && currentDate.isBefore(localEndDate)) {
                cal_id = paymCal.getCal_id();
                days = Math.toIntExact(ChronoUnit.DAYS.between(localStartDate, localEndDate.plusDays(1)));
            }
        }
        List<Cash_flows> cash_flows = null;
        if (cal_id != null){
            cash_flows = cash_flowsRepository.findAllByCalId(cal_id);
        }

        model.addAttribute("cash_flows",cash_flows);
        model.addAttribute("days", days);
        model.addAttribute("categories_inc", categories_inc);

        return "payment_calendar";
    }
}
