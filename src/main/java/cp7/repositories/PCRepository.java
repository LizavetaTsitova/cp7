package cp7.repositories;

import cp7.entities.Paym_cal;
import cp7.entities.Users_inf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;

public interface PCRepository extends JpaRepository<Paym_cal, Integer> {
    Paym_cal findByEndDateAndIdCompany(Date end, Integer id);

    Paym_cal findByCalId(Integer id);
}
