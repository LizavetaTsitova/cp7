package cp7.repositories;

import cp7.entities.Companies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Companies, Integer> {
    Companies findByActivationCode(String code);
    List<Companies> findAllByStatus(boolean status);
    Companies findByEmail(String email);
}
