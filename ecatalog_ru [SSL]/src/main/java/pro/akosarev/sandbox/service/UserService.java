package pro.akosarev.sandbox.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.entity.UserAuthority;
import pro.akosarev.sandbox.repository.UserAuthorityRepository;
import pro.akosarev.sandbox.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserAuthorityRepository userAuthorityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthorityRepository = userAuthorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Сохранение пользователя
        userRepository.save(user);

        // Создаем новую роль
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUser(user);
        userAuthority.setAuthority("ROLE_USER");

        // Сохранение роли по умолчанию
        userAuthorityRepository.save(userAuthority);
    }
}
