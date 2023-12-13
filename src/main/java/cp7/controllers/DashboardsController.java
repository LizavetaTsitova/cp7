package cp7.controllers;

import cp7.entities.*;
import cp7.repositories.*;
import cp7.services.Cash_flowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardsController {

    @Autowired
    Cash_flowsRepository cash_flowsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    User_infRepository user_infRepository;

    @Autowired
    Paym_calRepository paym_calRepository;

    @Autowired
    Cash_flowService cash_flowService;

    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping("/dashboards")
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
            }
        }

        Users user = userRepository.findByEmail(email);
        Users_inf users_inf = user_infRepository.findByUserId(user.getUser_id());
        paym_cal = paym_calRepository.findAllByIdCompany(users_inf.getIdCompany());
        List<Categories> categories = categoryRepository.findAllByCompanyId(users_inf.getIdCompany());

        List<Cash_flows> cash_flows_inc_plan = new ArrayList<>();
        List<Cash_flows> cash_flows_exp_plan = new ArrayList<>();
        List<Cash_flows> cash_flows_inc_fact = new ArrayList<>();
        List<Cash_flows> cash_flows_exp_fact = new ArrayList<>();
        List<Cash_flows> categories_inc = new ArrayList<>();
        List<Cash_flows> categories_dec = new ArrayList<>();

        for (Paym_cal paymCal : paym_cal) {
            // Получаем список элементов из репозитория
            List<Cash_flows> elementsToAdd1 = cash_flowsRepository.findAllByCalIdAndFlowType1AndFlowType2(paymCal.getCalId(), false, false);
            List<Cash_flows> elementsToAdd2 = cash_flowsRepository.findAllByCalIdAndFlowType1AndFlowType2(paymCal.getCalId(),true, false);
            List<Cash_flows> elementsToAdd3 = cash_flowsRepository.findAllByCalIdAndFlowType1AndFlowType2(paymCal.getCalId(), false, true);
            List<Cash_flows> elementsToAdd4 = cash_flowsRepository.findAllByCalIdAndFlowType1AndFlowType2(paymCal.getCalId(), true, true);

            // Добавляем все элементы из полученного списка в целевой список
            cash_flows_inc_plan.addAll(elementsToAdd1);
            cash_flows_exp_plan.addAll(elementsToAdd2);
            cash_flows_inc_fact.addAll(elementsToAdd3);
            cash_flows_exp_fact.addAll(elementsToAdd4);
        }

        for (Categories category : categories){
            List<Cash_flows> elementsToAdd1 = cash_flowsRepository.findAllByCategoryIdAndFlowType1AndFlowType2(category.getCategoryId(), false, true);
            List<Cash_flows> elementsToAdd2 = cash_flowsRepository.findAllByCategoryIdAndFlowType1AndFlowType2(category.getCategoryId(), true, true);

            categories_inc.addAll(elementsToAdd1);
            categories_dec.addAll(elementsToAdd2);
        }

        // Внутри метода onenPC вашего контроллера
        Map<String, Float> incPlanByMonth = cash_flowService.aggregateDataByMonth(cash_flows_inc_plan);
        Map<String, Float> expPlanByMonth = cash_flowService.aggregateDataByMonth(cash_flows_exp_plan);
        Map<String, Float> incFactByMonth = cash_flowService.aggregateDataByMonth(cash_flows_inc_fact);
        Map<String, Float> expFactByMonth = cash_flowService.aggregateDataByMonth(cash_flows_exp_fact);
        Map<String, Float> categoriesIncome = cash_flowService.aggregateDataByCategory(categories_inc);
        Map<String, Float> categoriesExpense = cash_flowService.aggregateDataByCategory(categories_dec);

        model.addAttribute("categoriesIncome", categoriesIncome);
        model.addAttribute("categoriesExpense", categoriesExpense);
        model.addAttribute("incPlanByMonth", incPlanByMonth);
        model.addAttribute("expPlanByMonth", expPlanByMonth);
        model.addAttribute("incFactByMonth", incFactByMonth);
        model.addAttribute("expFactByMonth", expFactByMonth);

        return "dashboards";
    }


}
