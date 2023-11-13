package cp7.services;

import cp7.entities.Users;
import cp7.entities.enums.Role;
import cp7.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean createUser(Users user) {
        String email = user.getEmail();
        String password = user.getPassword();
        Date date = Date.valueOf(java.time.LocalDate.now());
        user.setCreated_at(date);

        if (userRepository.findByEmail(email) != null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);

        userRepository.save(user);
        return true;
    }
}