package cp7.repositories;

import cp7.entities.Cash_flows;
import cp7.entities.Companies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Cash_flowsRepository extends JpaRepository<Cash_flows, Integer> {
    List<Cash_flows> findAllByCalId(Integer id);
}
