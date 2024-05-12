package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.parameters.P;

@Entity
@Data
@Table(name = "t_path")
public class Path {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String path;
    private String source;

    public Path(){

    }
}
