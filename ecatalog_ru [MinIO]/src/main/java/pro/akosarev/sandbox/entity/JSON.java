package pro.akosarev.sandbox.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JSON {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("category")
    private String category;

    @JsonProperty("name")
    private String name;

    @JsonProperty("image") // То что в токене
    private String photo; // То что в java

    @JsonProperty("cost")
    private Long cost;

    @JsonProperty("source")
    private String source;

    @Override
    public String toString() {
        return "ID: " + id + ", Category: " + category + ", Name: " + name + ", Photo: " + photo + ", Cost: " + cost;
    }
}
