package cp7.repositories;

import cp7.entities.Companies;
import cp7.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Companies, Integer> {
}
