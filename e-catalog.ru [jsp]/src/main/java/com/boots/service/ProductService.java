package com.boots.service;

import com.boots.entity.Product;
import com.boots.entity.Role;
import com.boots.entity.User;
import com.boots.repository.ProductRepository;
import com.boots.repository.RoleRepository;
import com.boots.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService  {
    @PersistenceContext
    private EntityManager em;
    ProductRepository productRepository;
    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }
    public Product findProductById(Long productId) {
        Optional<Product> productFromDb = productRepository.findById(productId);
        return productFromDb.orElse(new Product());
    }

    public List<Product> allProducts() {
        return productRepository.findAll();
    }

    public boolean saveProduct(Product product) {
        Product productFromDB = productRepository.findByName(product.getName());

        if (productFromDB != null) {
            return false;
        }

        productRepository.save(product);

        return true;
    }

    public boolean updateProduct(Product product, String name, String category, Long cost, String source) {

        product.setName(name);
        product.setCategory(category);
        product.setCost(cost);
        product.setSource(source);
        productRepository.saveAndFlush(product);

        return true;
    }
    public boolean deleteProduct(Long productId) {
        if (productRepository.findById(productId).isPresent()) {
            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }

    public List<Product> productgtList(Long idMin) {
        return em.createQuery("SELECT u FROM Product u WHERE u.id > :paramId", Product.class)
                .setParameter("paramId", idMin).getResultList();
    }
}
