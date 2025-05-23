package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "t_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<UserAuthority> authorities; // Список авторизаций

    @OneToOne(mappedBy = "user") // Обратная связь с UserInfo
    private UserInfo userInfo;

    @Column(name = "blocked") // Обратите внимание на имя столбца в БД
    private boolean blocked = false;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    private boolean haveProfileImage = false; // Поле для указания наличия профильного изображения

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHaveProfileImage() {
        return haveProfileImage;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(List<UserAuthority> authorities) {
        this.authorities = authorities;
    }
    public void setHaveProfileImage(boolean haveProfileImage) {
        this.haveProfileImage = haveProfileImage;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(authority -> (GrantedAuthority) () -> authority.getAuthority())
                .collect(Collectors.toList());
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Реализуйте свою логику
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked; // Реализуйте свою логику
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Реализуйте свою логику
    }

    @Override
    public boolean isEnabled() {
        return true; // Реализуйте свою логику
    }
}
