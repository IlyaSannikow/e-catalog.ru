package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_user_logout_event")
public class UserLogoutEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "logout_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutTime;

    @Column(name = "token", length = 1024)
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
// Конструкторы, геттеры и сеттеры

    public UserLogoutEvent() {
    }

    public UserLogoutEvent(String userId, Date logoutTime) {
        this.userId = userId;
        this.logoutTime = logoutTime;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }
}
