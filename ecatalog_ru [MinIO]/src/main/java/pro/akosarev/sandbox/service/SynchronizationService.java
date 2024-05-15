package pro.akosarev.sandbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.JSON;
import pro.akosarev.sandbox.entity.Path;
import pro.akosarev.sandbox.entity.Product;

import java.util.ArrayList;
import java.util.List;

@Service
public class SynchronizationService {

    // syncService.addNewProductsToDatabase(syncService.apiProduct());

    JsonService jsonService;
    ProductService productService;
    public SynchronizationService(JsonService jsonService, ProductService productService){
        this.jsonService = jsonService;
        this.productService = productService;

    }
    public List<Product> apiProduct() throws JsonProcessingException {

        List<JSON> foundJsonProducts = new ArrayList<>();
        List<Path> shopPaths = jsonService.getAPIPaths();


        for(Path shop: shopPaths){
            foundJsonProducts.addAll(jsonService.getJSON(shop));
        }

        return new ArrayList<>(jsonService.convertJsonToProduct(foundJsonProducts));

    }

    public void addNewProductsToDatabase(List<Product> apiProducts) {
        for (Product product : apiProducts) {
            productService.saveProduct(product);
        }
    }
}
