package cp7.repositories;

import cp7.entities.Users;
import cp7.entities.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByEmail(String email);
    //Users findByUser_id(Integer id);
    List<Users> findAllByRole(Role role);
}
