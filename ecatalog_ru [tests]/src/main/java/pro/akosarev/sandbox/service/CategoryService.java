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
    public CategoryService(){
        this.categoryRepository = categoryRepository;
    }
    public Category findCategoryById(Long categoryId) {
        Optional<Category> categoryFromDb = categoryRepository.findById(categoryId);
        return categoryFromDb.orElse(new Category());
    }
    public Category findCategoryByName(String name) {
        return categoryRepository.findByName(name);
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

    public boolean forTestCategory(int[][] matrixData) {

        Matrix matrix = new Matrix(matrixData);

        double determinant = calculateDeterminant(matrix); // Вычисление определителя матрицы
        System.out.println( "Определитель матрицы: " + determinant);

        return false;
    }

    public double calculateDeterminant(Matrix matrix) {

        int size = matrix.getSize();

        if (size == 1) {
            return matrix.getElement(0, 0);
        }

        if (size == 2) {
            return matrix.getElement(0, 0) * matrix.getElement(1, 1) -
                    matrix.getElement(0, 1) * matrix.getElement(1, 0);
        }

        double determinant = 0; // Определитель

        for (int j = 0; j < size; j++) {
            determinant += Math.pow(-1, j) * matrix.getElement(0, j) *
                    calculateDeterminant(matrix.getSubMatrix(0, j));
        }

        return determinant;
    }

    static class Matrix {
        private final int[][] data;

        public Matrix(int[][] data) {
            this.data = data;
        }

        public int getElement(int row, int col) {
            return data[row][col];
        }

        public int getSize() {
            return data.length;
        }

        public Matrix getSubMatrix(int excludingRow, int excludingCol) {
            int size = data.length - 1;
            int[][] newData = new int[size][size];
            int newRow = 0;
            for (int i = 0; i < data.length; i++) {
                if (i == excludingRow)
                    continue;
                int newCol = 0;
                for (int j = 0; j < data.length; j++) {
                    if (j == excludingCol)
                        continue;
                    newData[newRow][newCol] = data[i][j];
                    newCol++;
                }
                newRow++;
            }
            return new Matrix(newData);
        }
    }


    public List<Category> categorygtList(Long idMin) {
        return em.createQuery("SELECT u FROM Category u WHERE u.id > :paramId", Category.class)
                .setParameter("paramId", idMin).getResultList();
    }
}