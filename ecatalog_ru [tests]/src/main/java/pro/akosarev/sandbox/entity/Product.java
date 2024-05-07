package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "t_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long cost;
    private String source;

    @ManyToOne
    @JoinColumn(name = "category_id") // Поле для связи с таблицей категорий
    private Category category;
}
