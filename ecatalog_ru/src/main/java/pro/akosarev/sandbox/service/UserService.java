package pro.akosarev.sandbox.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import pro.akosarev.sandbox.entity.Role;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.entity.UserAuthority;
import pro.akosarev.sandbox.entity.UserInfo;
import pro.akosarev.sandbox.repository.RoleRepository;
import pro.akosarev.sandbox.repository.UserAuthorityRepository;
import pro.akosarev.sandbox.repository.UserInfoRepository;
import pro.akosarev.sandbox.repository.UserRepository;

import java.util.Date;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;

    private final UserInfoRepository userInfoRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserAuthorityRepository userAuthorityRepository,
                       UserInfoRepository userInfoRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthorityRepository = userAuthorityRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userInfoRepository = userInfoRepository;
    }

    @Transactional
    public void registerUser(String username, String password, String recaptchaResponse) {
        // Проверки остаются те же

        // Создание пользователя
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setHaveProfileImage(false);
        userRepository.save(user);

        // Находим роль USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USER not found"));

        // Создаем связь пользователя с ролью
        UserAuthority authority = new UserAuthority();
        authority.setUser(user);
        authority.setRole(userRole);
        userAuthorityRepository.save(authority);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateUserInfo(User user, String urlImage) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUrlImage(urlImage);
        userInfo.setUrlLastUpdate(new Date());

        userInfo.setUser(user);
        userInfoRepository.save(userInfo);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new SecurityException("Текущий пароль неверен");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("Новый пароль должен отличаться от текущего");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
}
