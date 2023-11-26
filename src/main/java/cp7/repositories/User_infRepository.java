package cp7.repositories;

import cp7.entities.Users_inf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface User_infRepository extends JpaRepository<Users_inf, Integer> {
    Users_inf findByUserId(Integer id);
    Users_inf findByUserIdAndIdCompany(Integer id, Integer comp_id);
    List<Users_inf> findAllByIdCompany(Integer id);
}
