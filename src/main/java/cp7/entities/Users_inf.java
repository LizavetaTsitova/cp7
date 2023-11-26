package cp7.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_inf")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Users_inf {
    @Id
    @Column(name = "user_id")
    private Integer userId;
    private String first_name;
    private String last_name;
    @Column(name = "id_company")
    private Integer idCompany;
}
