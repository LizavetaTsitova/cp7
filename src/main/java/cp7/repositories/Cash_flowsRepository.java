package cp7.repositories;

import cp7.entities.Cash_flows;
import cp7.entities.Companies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface Cash_flowsRepository extends JpaRepository<Cash_flows, Integer> {
    List<Cash_flows> findAllByCalId(Integer id);

    List<Cash_flows> findAllByCalIdAndFlowType1(Integer id, boolean type);
    List<Cash_flows> findAllByCalIdAndFlowType1AndFlowType2(Integer id, boolean type1, boolean type2);
    List<Cash_flows> findAllByCategoryIdAndFlowType1AndFlowType2(Integer id, boolean type1, boolean type2);
}
