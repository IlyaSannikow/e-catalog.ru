package pro.akosarev.sandbox.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "t_category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public Category() {

    }
}
