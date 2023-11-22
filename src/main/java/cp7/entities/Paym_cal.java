package cp7.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Table(name = "paym_cal")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paym_cal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cal_id;

    private Date start_date;
    private Date end_date;
    @Column(name = "id_company")
    private Integer idCompany;
    @Column(name = "owner_id")
    private Integer ownerId;
}
