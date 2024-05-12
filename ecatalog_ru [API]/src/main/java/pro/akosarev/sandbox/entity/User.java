package pro.akosarev.sandbox.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "t_user")
public class User {

    @Id
    private String id;

    @OneToMany(mappedBy = "user")
    Set<ProductCompare> productCompares;

    public User(String id){
        this.id = id;
    }
    public User(){}
}
