package pro.akosarev.sandbox.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.Category;
import pro.akosarev.sandbox.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService  {
    @PersistenceContext
    private EntityManager em;
    CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }
    public Category findCategoryById(Long categoryId) {
        Optional<Category> categoryFromDb = categoryRepository.findById(categoryId);
        return categoryFromDb.orElse(new Category());
    }

    public List<Category> allCategories() {
        return categoryRepository.findAll();
    }
    public List<String> findAllName(){ return categoryRepository.findAllCategories();}
    public List<Long> findAllId(){ return categoryRepository.findAllId();}

    public Category findCategoryId(String name){
        return categoryRepository.findByName(name);}

    public boolean saveCategory(Category category) {
        Optional<Category> categoryFromDB = categoryRepository.findById(category.getId());

        if (categoryFromDB.isPresent()) {
            return false;
        }

        categoryRepository.save(category);

        return true;
    }

    public boolean updateCategory(Category category, String categoryName) {

        category.setName(categoryName);
        categoryRepository.saveAndFlush(category);

        return true;
    }
    public boolean deleteCategory(Long categoryId) {
        if (categoryRepository.findById(categoryId).isPresent()) {
            categoryRepository.deleteById(categoryId);
            return true;
        }
        return false;
    }

    public List<Category> categorygtList(Long idMin) {
        return em.createQuery("SELECT u FROM Category u WHERE u.id > :paramId", Category.class)
                .setParameter("paramId", idMin).getResultList();
    }
}