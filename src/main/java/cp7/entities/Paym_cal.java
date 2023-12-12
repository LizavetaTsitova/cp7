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
    @Column(name = "cal_id")
    private Integer calId;

    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @Column(name = "id_company")
    private Integer idCompany;
    @Column(name = "owner_id")
    private Integer ownerId;
}
