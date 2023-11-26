package cp7.controllers;

import cp7.entities.*;
import cp7.entities.enums.Role;
import cp7.repositories.*;
import cp7.services.Cash_flowService;
import cp7.services.PCService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

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

    @Autowired
    Cash_flowService cash_flowService;

    @Autowired
    PCService pcService;

    @Autowired
    private HttpSession httpSession;

    @GetMapping("/pc")
    public String openPC(Model model) {
        LocalDate date = (LocalDate) httpSession.getAttribute("date");
        if (date == null) {
            date = LocalDate.now();
            httpSession.setAttribute("date", date);
        }

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
        String owner = users_inf.getFirst_name() + " " + users_inf.getLast_name();

        List<Users> roles = userRepository.findAllByRole(Role.ROLE_OWNER);
        List<Users_inf> usersInfs = new ArrayList<>();

        for (Users role : roles) {
            Users_inf usr = user_infRepository.findByUserIdAndIdCompany(role.getUser_id(), users_inf.getIdCompany());
            if (usr != null) {
                usersInfs.add(usr);
            }
        }

        //Поиск платёжных календарей компании/пользователя
        if (users_inf.getIdCompany() == null) {
            paym_cal = paym_calRepository.findAllByOwnerId(users_inf.getUserId());
        } else {
            paym_cal = paym_calRepository.findAllByIdCompany(users_inf.getIdCompany());
        }

        //Поиск категорий по компании и типу (доход, расход)
        List<Categories> categories_inc = categoryRepository.findAllByCompanyIdAndType(users_inf.getIdCompany(), 0);
        List<Categories> categories_dec = categoryRepository.findAllByCompanyIdAndType(users_inf.getIdCompany(), 1);

        Integer cal_id = null;
        Integer days = null;

        //Поиск платёжного календаря тек. месяца и вычисление количества дней в тек. месяце
        for (Paym_cal paymCal : paym_cal) {
            LocalDate localStartDate = paymCal.getStartDate().toLocalDate();
            LocalDate localEndDate = paymCal.getEndDate().toLocalDate();
            if (date.isAfter(localStartDate) && date.isBefore(localEndDate)) {
                cal_id = paymCal.getCal_id();
                days = Math.toIntExact(ChronoUnit.DAYS.between(localStartDate, localEndDate.plusDays(1)));
            }
        }

        if (cal_id == null) {
            model.addAttribute("errorMessage", "Платёжных календарей не найдено");
            return "payment_calendar";
        }

        //Получение денежных потоков платёжного календаря
        List<Cash_flows> cash_flows_inc = null;
        List<Cash_flows> cash_flows_dec = null;
        if (cal_id != null) {
            cash_flows_inc = cash_flowsRepository.findAllByCalIdAndFlowType1(cal_id, false);
            cash_flows_dec = cash_flowsRepository.findAllByCalIdAndFlowType1(cal_id, true);
        }

        // Создаем карту для хранения сумм плановых и фактических доходов для каждой категории
        Map<Integer, Double> planAmounts_inc = new HashMap<>();
        Map<Integer, Double> factAmounts_inc = new HashMap<>();
        Map<Integer, Double> planAmounts_dec = new HashMap<>();
        Map<Integer, Double> factAmounts_dec = new HashMap<>();

        // Инициализируем суммы для каждой категории в нулевое значение
        for (Categories category : categories_inc) {
            planAmounts_inc.put(category.getCategory_id(), 0.0);
            factAmounts_inc.put(category.getCategory_id(), 0.0);
        }

        for (Categories category : categories_dec) {
            planAmounts_dec.put(category.getCategory_id(), 0.0);
            factAmounts_dec.put(category.getCategory_id(), 0.0);
        }

        // Вычисляем суммы плановых и фактических доходов для каждой категории
        for (Cash_flows cashFlow : cash_flows_inc) {
            int categoryId = cashFlow.getCategoryId();
            float amount = cashFlow.getAmount();
            boolean flowType2 = cashFlow.getFlow_type_2();

            if (!flowType2) { // Плановый доход
                double planAmount = planAmounts_inc.get(categoryId);
                planAmounts_inc.put(categoryId, planAmount + amount);
            } else if (flowType2) { // Фактический доход
                double factAmount = factAmounts_inc.get(categoryId);
                factAmounts_inc.put(categoryId, factAmount + amount);
            }
        }

        for (Cash_flows cashFlow : cash_flows_dec) {
            int categoryId = cashFlow.getCategoryId();
            float amount = cashFlow.getAmount();
            boolean flowType2 = cashFlow.getFlow_type_2();

            if (!flowType2) { // Плановый доход
                double planAmount = planAmounts_dec.get(categoryId);
                planAmounts_dec.put(categoryId, planAmount + amount);
            } else if (flowType2) { // Фактический доход
                double factAmount = factAmounts_dec.get(categoryId);
                factAmounts_dec.put(categoryId, factAmount + amount);
            }
        }

        Map<Integer, Map<Integer, Float>> paymentByDay_inc = new HashMap<>();
        Map<Integer, Map<Integer, Float>> paymentByDay_dec = new HashMap<>();

        // Проходим по каждой категории и заполняем платежи для каждого дня месяца
        for (Categories category : categories_inc) {
            Map<Integer, Float> payments = new HashMap<>();

            for (Cash_flows cashFlow : cash_flows_inc) {
                if (cashFlow.getCategoryId().equals(category.getCategory_id()) && cashFlow.getPaym_date() != null) {
                    LocalDate datee = cashFlow.getPaym_date().toLocalDate();
                    int day = datee.getDayOfMonth();

                    float payment = cashFlow.getAmount();

                    payments.put(day, payment);
                }
            }

            paymentByDay_inc.put(category.getCategory_id(), payments);
        }

        for (Categories category : categories_dec) {
            Map<Integer, Float> payments = new HashMap<>();

            for (Cash_flows cashFlow : cash_flows_dec) {
                if (cashFlow.getCategoryId().equals(category.getCategory_id()) && cashFlow.getPaym_date() != null) {
                    LocalDate datee = cashFlow.getPaym_date().toLocalDate();
                    int day = datee.getDayOfMonth();

                    float payment = cashFlow.getAmount();

                    payments.put(day, payment);
                }
            }

            paymentByDay_dec.put(category.getCategory_id(), payments);
        }

        List<Integer> weekend = new ArrayList<>();
        //Вычисляем выходные дни текущего месяца
        for (int day = 1; day <= days; day++) {
            int year = date.getYear();
            int month = date.getMonthValue();
            LocalDate datee = LocalDate.of(year, month, day);
            DayOfWeek dayOfWeek = datee.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                weekend.add(day);
            }
        }

        // Вычисляем сумму фактических доходов за каждую неделю
        Map<Integer, Double> weeklyFactAmounts_inc = new HashMap<>();
        for (Cash_flows cashFlow : cash_flows_inc) {
            if (cashFlow.getPaym_date() != null) {
                LocalDate datee = cashFlow.getPaym_date().toLocalDate();
                int week = datee.get(WeekFields.of(Locale.getDefault()).weekOfMonth());

                if (!weeklyFactAmounts_inc.containsKey(week)) {
                    weeklyFactAmounts_inc.put(week, 0.0);
                }

                double weekFactAmount = weeklyFactAmounts_inc.get(week);
                weeklyFactAmounts_inc.put(week, weekFactAmount + cashFlow.getAmount());
            }
        }

        Map<Integer, Double> weeklyFactAmounts_dec = new HashMap<>();
        for (Cash_flows cashFlow : cash_flows_dec) {
            if (cashFlow.getPaym_date() != null) {
                LocalDate datee = cashFlow.getPaym_date().toLocalDate();
                int week = datee.get(WeekFields.of(Locale.getDefault()).weekOfMonth());

                if (!weeklyFactAmounts_dec.containsKey(week)) {
                    weeklyFactAmounts_dec.put(week, 0.0);
                }

                double weekFactAmount = weeklyFactAmounts_dec.get(week);
                weeklyFactAmounts_dec.put(week, weekFactAmount + cashFlow.getAmount());
            }
        }

        // Вычисляем сумму всех фактических доходов
        double totalFactAmount_inc = 0.0;
        for (double factAmount : factAmounts_inc.values()) {
            totalFactAmount_inc += factAmount;
        }

        double totalFactAmount_dec = 0.0;
        for (double factAmount : factAmounts_dec.values()) {
            totalFactAmount_dec += factAmount;
        }

        model.addAttribute("weekend", weekend);
        model.addAttribute("days", days);
        model.addAttribute("owner", owner);

        model.addAttribute("categories_inc", categories_inc);
        model.addAttribute("planAmounts_inc", planAmounts_inc);
        model.addAttribute("factAmounts_inc", factAmounts_inc);
        model.addAttribute("paymentByDay_inc", paymentByDay_inc);

        model.addAttribute("weeklyFactAmounts_inc", weeklyFactAmounts_inc);
        model.addAttribute("totalFactAmount_inc", totalFactAmount_inc);

        model.addAttribute("categories_dec", categories_dec);
        model.addAttribute("planAmounts_dec", planAmounts_dec);
        model.addAttribute("factAmounts_dec", factAmounts_dec);
        model.addAttribute("paymentByDay_dec", paymentByDay_dec);

        model.addAttribute("weeklyFactAmounts_dec", weeklyFactAmounts_dec);
        model.addAttribute("totalFactAmount_dec", totalFactAmount_dec);

        model.addAttribute("usersInfs", usersInfs);

        return "payment_calendar";
    }

    @GetMapping("/pc/cal_date")
    public String pcDate(@RequestParam String cal_date) {
        Integer year = Integer.valueOf(cal_date.substring(0, 4));
        Integer month = Integer.valueOf(cal_date.substring(cal_date.length() - 2));
        LocalDate parsedDate = LocalDate.of(year, month, 2);
        httpSession.setAttribute("date", parsedDate);
        return "redirect:/pc";
    }

    @GetMapping("/add_pc")
    public String addPc(RedirectAttributes redirectAttributes, @RequestParam Integer owner, @RequestParam String month) throws ParseException {
        boolean creation = pcService.createPC(owner, month);
        if (!creation) {
            redirectAttributes.addFlashAttribute("save_error", "Платёжный календарь для " + month +
                    " уже существует");
        }
        return "redirect:/pc";
    }
}