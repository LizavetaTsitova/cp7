package cp7.services;

import cp7.entities.IncomeFormData;
import cp7.entities.Paym_cal;
import cp7.repositories.CategoryRepository;
import cp7.repositories.PCRepository;
import org.apache.poi.ss.usermodel.Row;
import cp7.entities.Cash_flows;
import cp7.repositories.Cash_flowsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class Cash_flowService {

    private final Cash_flowsRepository cash_flowsRepository;

    private final CategoryRepository categoryRepository;

    private final PCRepository pcRepository;

    public void fillPrevMonth(MultipartFile incomeFile, boolean type, Integer calId) {
        try (InputStream inputStream = incomeFile.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Integer id_comp = pcRepository.findByCalId(calId).getIdCompany();

            Iterator<Row> iterator = sheet.iterator();
            // Пропускаем первую строку с заголовками
            if (iterator.hasNext()) {
                iterator.next();
            }
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Cash_flows cashFlows = createCashFlowsFromRow(currentRow, type, calId, id_comp);
                cash_flowsRepository.save(cashFlows);
            }
        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
            // Обработка ошибок чтения файла
        }
    }

    private Cash_flows createCashFlowsFromRow(Row row, boolean type, Integer cal_id, Integer id_comp) {
        Cash_flows cashFlows = new Cash_flows();

        // Установка значений в объект Cash_flows из строки Excel
        cashFlows.setFlowType1(type);
        cashFlows.setFlowType2(true);
        // Установка calId в зависимости от айди выбранного календаря
        cashFlows.setCalId(cal_id);

        Cell categoryCell = row.getCell(0); // Категория находится в первой колонке
        String categoryName = categoryCell.getStringCellValue();
        Integer categoryId = (categoryRepository.findByCompanyIdAndName(id_comp, categoryName)).getCategoryId();
        cashFlows.setCategoryId(categoryId);

        Cell dateCell = row.getCell(1); // Дата находится во второй колонке
        cashFlows.setPaym_date(new java.sql.Date(dateCell.getDateCellValue().getTime()));

        Cell amountCell = row.getCell(2); // Сумма находится в третьей колонке
        cashFlows.setAmount((float) amountCell.getNumericCellValue());

        return cashFlows;
    }

    public boolean isValidMonth(MultipartFile incomeFile, Integer calId) {
        try (InputStream inputStream = incomeFile.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            int startRow = 1; // Указывает, с какой строки начинать обработку

            if (sheet.getPhysicalNumberOfRows() > startRow) {
                Row firstRow = sheet.getRow(startRow);
                Cell dateCell = firstRow.getCell(1); // Предполагая, что дата находится во второй колонке
                Date fileMonth = new java.sql.Date(dateCell.getDateCellValue().getTime());

                Paym_cal paymentCalendar = pcRepository.findByCalId(calId);

                if (paymentCalendar != null) {
                    LocalDate calendarMonth = paymentCalendar.getStartDate().toLocalDate();
                    if (fileMonth.toLocalDate().getMonth() != calendarMonth.getMonth()) {
                        return false; // Несоответствие месяца
                    }
                }
            }
        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
            // Обработка ошибок чтения файла
        }
        return true;
    }

    public void addPlanInc(IncomeFormData data, Integer paycalID) {
        List<Integer> categoriesID = data.getCategoryId();
        List<LocalDate> dates = data.getPaymentDate();
        List<Float> amounts = data.getAmount();

        for (int i = 0; i < categoriesID.size(); i++) {
            Cash_flows cash_flow = new Cash_flows();
            cash_flow.setFlowType1(false);
            cash_flow.setFlowType2(false);
            cash_flow.setCalId(paycalID);
            cash_flow.setPaym_date(Date.valueOf(dates.get(i)));
            cash_flow.setAmount(amounts.get(i));
            cash_flow.setCategoryId(categoriesID.get(i));

            cash_flowsRepository.save(cash_flow);
        }
    }

    public void addPlanExp(IncomeFormData data, Integer paycalID) {
        List<Integer> categoriesID = data.getCategoryId();
        List<LocalDate> dates = data.getPaymentDate();
        List<Float> amounts = data.getAmount();

        for (int i = 0; i < categoriesID.size(); i++) {
            Cash_flows cash_flow = new Cash_flows();
            cash_flow.setFlowType1(true);
            cash_flow.setFlowType2(false);
            cash_flow.setCalId(paycalID);
            cash_flow.setPaym_date(Date.valueOf(dates.get(i)));
            cash_flow.setAmount(amounts.get(i));
            cash_flow.setCategoryId(categoriesID.get(i));

            cash_flowsRepository.save(cash_flow);
            distributeExpenses(paycalID, categoriesID.get(i), cash_flow);
        }

        // Проверка итогового сальдо
        Map<LocalDate, List<Double>> incomeMap = loadPlan(paycalID, false);
        Map<LocalDate, List<Double>> expenseMap = loadPlan(paycalID, true);

        checkBalances(incomeMap, expenseMap);
    }

    private void distributeExpenses(Integer paycalID, Integer catID, Cash_flows cash_flow) {
        // Загрузка данных о плановых доходах и расходах
        Map<LocalDate, List<Double>> incomeMap = loadPlan(paycalID, false);
        Map<LocalDate, List<Double>> expenseMap = loadPlan(paycalID, true);

        // Распределение расходов
        for (Map.Entry<LocalDate, List<Double>> entry : expenseMap.entrySet()) {
            LocalDate date = entry.getKey();
            List<Double> expensesList = entry.getValue();
            Double expense = 0.0;

            for (double expenses : expensesList) {
                expense = expense + expenses;
            }

            double totalIncomeForDate = incomeMap.getOrDefault(date, Collections.singletonList(0.0)).stream().mapToDouble(Double::doubleValue).sum();
            if (totalIncomeForDate - expense < 0) {
                // Перенос расходов на другие дни
                adjustExpenses(expenseMap, incomeMap, date, expense - totalIncomeForDate, paycalID, catID, cash_flow);
            }

        }

    }

    // Метод для загрузки плановых доходов/расходов
    private Map<LocalDate, List<Double>> loadPlan(Integer paycalID, Boolean type) {
        List<Cash_flows> cash_flows = cash_flowsRepository.findAllByCalIdAndFlowType1AndFlowType2(paycalID, type, false);

        Map<LocalDate, List<Double>> planMap = new HashMap<>();
        for (Cash_flows cash_flow : cash_flows) {
            LocalDate date = cash_flow.getPaym_date().toLocalDate(); // Преобразование в LocalDate
            double amount = cash_flow.getAmount().doubleValue(); // Преобразование в Double

            // Если ключ уже существует, добавляем в список, иначе создаём новый список
            planMap.computeIfAbsent(date, k -> new ArrayList<>()).add(amount);
        }
        // Здесь должен быть ваш код для загрузки плановых доходов из базы данных
        return planMap;
    }

    // Метод для переноса расходов на другие дни
    private void adjustExpenses(Map<LocalDate, List<Double>> expenseMap, Map<LocalDate, List<Double>> incomeMap, LocalDate negativeBalanceDate, double deficit, Integer paycalID, Integer categID, Cash_flows cash_flow) {
        // Перебор дат
        for (LocalDate date : incomeMap.keySet()) {
            if (!date.equals(negativeBalanceDate)) {
                double totalIncomeForDate = incomeMap.getOrDefault(date, Collections.singletonList(0.0)).stream().mapToDouble(Double::doubleValue).sum();
                double totalExpensesForDate = expenseMap.getOrDefault(date, Collections.singletonList(0.0)).stream().mapToDouble(Double::doubleValue).sum();

                double available = totalIncomeForDate - totalExpensesForDate;

                if (available >= deficit) {
                    if(expenseMap.get(date) == null){
                        // Если записи нет, создаем новую с начальным значением 0.0
                        expenseMap.put(date, new ArrayList<>(Arrays.asList(0.0)));
                    }
                    // Достаточно средств для покрытия дефицита, перераспределяем расходы

                    expenseMap.get(date).add(deficit); // Добавляем дефицит к расходам этой даты
                    Cash_flows cash_flows_diff = new Cash_flows();
                    cash_flows_diff.setFlowType1(true);
                    cash_flows_diff.setFlowType2(false);
                    cash_flows_diff.setCalId(paycalID);
                    cash_flows_diff.setPaym_date(Date.valueOf(date));
                    cash_flows_diff.setAmount((float) deficit);
                    cash_flows_diff.setCategoryId(categID);
                    cash_flowsRepository.save(cash_flows_diff);

                    expenseMap.get(negativeBalanceDate).remove(deficit); // Удаляем дефицит из исходной даты
                    cash_flow.setAmount((float) (cash_flow.getAmount() - deficit));
                    cash_flowsRepository.save(cash_flow);

                    System.out.println("Расходы в размере " + deficit + " перенесены с " + negativeBalanceDate + " на " + date);
                    break; // Выходим из цикла после успешного переноса
                }
            }
        }
    }


    // Метод для проверки итогового сальдо
    private void checkBalances(Map<LocalDate, List<Double>> incomeMap, Map<LocalDate, List<Double>> expenseMap) {
        for (LocalDate date : expenseMap.keySet()) {
            double totalIncome = incomeMap.getOrDefault(date, Collections.singletonList(0.0)).stream().mapToDouble(Double::doubleValue).sum();
            double totalExpenses = expenseMap.getOrDefault(date, Collections.singletonList(0.0)).stream().mapToDouble(Double::doubleValue).sum();

            if (totalIncome - totalExpenses < 0) {
                log.info("Отрицательное сальдо невозможно избежать на " + date);
            }
        }
    }

    // Агрегация данных по месяцам
    public Map<String, Float> aggregateDataByMonth(List<Cash_flows> cashFlows) {
        Map<String, Float> monthlyData = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM");

        for (Cash_flows flow : cashFlows) {
            if (flow.getPaym_date() != null) {
                String month = formatter.format(flow.getPaym_date());
                monthlyData.put(month, monthlyData.getOrDefault(month, 0f) + flow.getAmount());
            }
        }
        return monthlyData;
    }

    public Map<String, Float> aggregateDataByCategory(List<Cash_flows> cashFlows) {
        Map<String, Float> categoryTotals = new HashMap<>();

        for (Cash_flows flow : cashFlows) {
            String categoryName = categoryRepository.findByCategoryId(flow.getCategoryId()).getName(); // Предположим, что у Cash_flows есть метод getCategory(), который возвращает объект Categories, и у Categories есть метод getName()
            categoryTotals.put(categoryName, categoryTotals.getOrDefault(categoryName, 0f) + flow.getAmount());
        }
        return categoryTotals;
    }


}

