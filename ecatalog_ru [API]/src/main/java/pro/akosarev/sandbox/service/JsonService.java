package pro.akosarev.sandbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.akosarev.sandbox.entity.JSON;
import pro.akosarev.sandbox.entity.Path;
import pro.akosarev.sandbox.entity.Product;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
public class JsonService {

    PathService pathService;

    CategoryService categoryService;

    public JsonService(PathService pathService, CategoryService categoryService){
        this.pathService = pathService;
        this.categoryService = categoryService;
    }

    public List<Path> getAPIPaths() {
        List<Path> apiPaths = new ArrayList<>();

        String mockPath = pathService.findPath("mockApi");

        Path dnsPath = pathService.findPathEntity("dnsApi");
        dnsPath.setPath(mockPath + dnsPath.getPath());

        Path mvideoPath = pathService.findPathEntity("mvideoApi");
        mvideoPath.setPath(mockPath + mvideoPath.getPath());

        apiPaths.add(dnsPath);
        apiPaths.add(mvideoPath);


        return apiPaths;
    }

    public List<JSON> getJSON (Path productToken) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        URI url = UriComponentsBuilder.fromHttpUrl(productToken.getPath()).build().toUri();

        String token = restTemplate.getForObject(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(token, new TypeReference<List<JSON>>() {});

    }

    public List<JSON> getJsonProduct (List<JSON> productJSON, String source, String condition ){

        Iterator<JSON> iterator = productJSON.iterator();

        while (iterator.hasNext()) {
            JSON json = iterator.next();
            if (Objects.equals(json.getName(), condition) ||
                    Objects.equals(categoryService.findCategoryByName(json.getCategory()).getName(), condition)){
                json.setSource(source);
            } else {
                iterator.remove();
            }
        }

        return productJSON;

    }

    public List<Product> convertJsonToProduct(List<JSON> jsonList){

        List<Product> productsFromShop = new ArrayList<>();

        for (JSON json: jsonList){

            Product product = new Product();
            product.setId(Long.valueOf(json.getId()));
            product.setName(json.getName());
            product.setPhoto(json.getPhoto().substring(json.getPhoto().lastIndexOf('/') + 1));
            product.setSource(json.getSource());
            product.setCategory(categoryService.findCategoryByName(json.getCategory()));
            product.setCost(json.getCost());

            productsFromShop.add(product);
        }

        return productsFromShop;
    }
}
