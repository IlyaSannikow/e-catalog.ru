package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "t_user_info")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String urlImage;

    @Temporal(TemporalType.TIMESTAMP)
    private Date urlLastUpdate;

    @OneToOne // Предполагаем, что один пользователь имеет одну запись в t_user_info
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Date getUrlLastUpdate() {
        return urlLastUpdate;
    }

    public void setUrlLastUpdate(Date urlLastUpdate) {
        this.urlLastUpdate = urlLastUpdate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
