package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_user_authority")
public class UserAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Метод для совместимости с GrantedAuthority
    public String getAuthority() {
        return role != null ? role.getName() : null;
    }
}
