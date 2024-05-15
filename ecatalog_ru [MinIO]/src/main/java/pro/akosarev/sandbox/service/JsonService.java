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
    MinIoService minIoService;

    public JsonService(PathService pathService, CategoryService categoryService, MinIoService minIoService){
        this.pathService = pathService;
        this.categoryService = categoryService;
        this.minIoService = minIoService;
    }

    public List<Path> getAPIPaths() {
        List<Path> apiPaths = new ArrayList<>();

        Path dnsPath = pathService.findPathEntity("dnsApi");
        Path mvideoPath = pathService.findPathEntity("mvideoApi");

        apiPaths.add(dnsPath);
        apiPaths.add(mvideoPath);


        return apiPaths;
    }

    public List<JSON> getJSON (Path productToken) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        URI url = UriComponentsBuilder.fromHttpUrl(productToken.getPath()).build().toUri();

        String token = restTemplate.getForObject(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        List<JSON> jsonList = objectMapper.readValue(token, new TypeReference<List<JSON>>() {});

        for (JSON json : jsonList) {
            json.setSource(productToken.getSource());
        }

        return jsonList;

    }

    public List<Product> convertJsonToProduct(List<JSON> jsonList){

        List<Product> productsFromShop = new ArrayList<>();

        for (JSON json: jsonList){

            Product product = new Product();
            product.setName(json.getName());
            product.setPhoto(json.getPhoto().substring(json.getPhoto().lastIndexOf('/') + 1));
            product.setSource(json.getSource());
            product.setCategory(categoryService.findCategoryByName(json.getCategory()));
            product.setCost(json.getCost());
            product.setExternalId(String.valueOf(json.getId()));

            minIoService.uploadImageToMinioFromUrl(json.getPhoto(), product.getPhoto());

            productsFromShop.add(product);
        }

        return productsFromShop;
    }
}
