package pro.akosarev.sandbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.security.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "t_deactivated_token")
public class DeactivatedToken {

    @Id
    private UUID id;
    @Column(name = "c_keep_until")
    private Timestamp keepUntil;
}
