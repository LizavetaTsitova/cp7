package cp7.repositories;

import cp7.entities.Cash_flows;
import cp7.entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {
    List<Categories> findAllByCompanyIdAndType(Integer id, Integer type);
}
