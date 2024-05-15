package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "t_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long cost;
    private String source;
    private String photo;

    private String externalId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
