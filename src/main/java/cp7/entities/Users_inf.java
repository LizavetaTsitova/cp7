package cp7.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users_inf")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Users_inf {
    @Id
    @Column(name = "user_id")
    private Integer userId;
    private String first_name;
    private String last_name;
    private Integer id_company;
}
