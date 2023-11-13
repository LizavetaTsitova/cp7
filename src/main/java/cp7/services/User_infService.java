package cp7.services;

import cp7.entities.Users;
import cp7.entities.Users_inf;
import cp7.repositories.User_infRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class User_infService {
    private final User_infRepository user_infRepository;

    public boolean addInfo(Users_inf users_inf) {

        return true;
    }
}
