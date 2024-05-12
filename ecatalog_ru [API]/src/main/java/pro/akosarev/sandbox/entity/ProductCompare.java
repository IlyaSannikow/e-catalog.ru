package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_compare")
@Data
public class ProductCompare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String productName;
}
