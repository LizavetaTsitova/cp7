package cp7.services;

import cp7.entities.Paym_cal;
import cp7.repositories.PCRepository;
import cp7.repositories.UserRepository;
import cp7.repositories.User_infRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.sql.Date;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class PCService {
    private final PCRepository pcRepository;
    private final User_infRepository user_infRepository;

    public boolean createPC(Integer id, String dateStr) throws ParseException {
        Paym_cal paymCal = new Paym_cal();
        paymCal.setOwnerId(id);
        paymCal.setIdCompany(user_infRepository.findByUserId(id).getIdCompany());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        java.util.Date utilDate = format.parse(dateStr);

        LocalDate localDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        YearMonth yearMonth = YearMonth.of(localDate.getYear(), localDate.getMonthValue());
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        paymCal.setStartDate(Date.valueOf(startOfMonth));
        paymCal.setEndDate(Date.valueOf(endOfMonth));

        if(pcRepository.findByEndDateAndIdCompany((Date.valueOf(endOfMonth)), user_infRepository.findByUserId(id).getIdCompany()) != null){
            return false;
        }

        pcRepository.save(paymCal);
        return true;
    }
}
