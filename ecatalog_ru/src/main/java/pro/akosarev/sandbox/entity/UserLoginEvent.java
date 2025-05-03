package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_login_events")
public class UserLoginEvent {

    @Id
    @Column(name = "event_id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "login_time", nullable = false)
    private Date loginTime;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    public UserLoginEvent() {
    }

    public UserLoginEvent(String userId, String token, Date loginTime, String ipAddress, String userAgent) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.token = token;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.loginTime == null) {
            this.loginTime = new Date();
        }
    }
}
