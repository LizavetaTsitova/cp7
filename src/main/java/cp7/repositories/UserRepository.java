package cp7.repositories;

import cp7.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByEmail(String email);
    //Users findByUser_id(Integer id);
}
