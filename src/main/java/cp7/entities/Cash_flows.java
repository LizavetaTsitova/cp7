package cp7.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Table(name = "cash_flows")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cash_flows {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flow_id;

    @Column(name = "flow_type_1")
    private Boolean flowType1;
    @Column(name = "flow_type_2")
    private Boolean flowType2;
    @Column(name = "cal_id")
    private Integer calId;
    private Date paym_date;
    private Float amount;
    @Column(name = "category_id")
    private Integer categoryId;
}
