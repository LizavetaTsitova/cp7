package cp7.repositories;

import cp7.entities.Paym_cal;
import cp7.entities.Users_inf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Paym_calRepository extends JpaRepository<Paym_cal, Integer> {
    List<Paym_cal> findAllByIdCompany(Integer id);
    List<Paym_cal> findAllByOwnerId(Integer id);
}
