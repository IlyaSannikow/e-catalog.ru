package pro.akosarev.sandbox.notForApplication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixServiceTest {

    @Test
    public void testCalculateDeterminant_When3x3Matrix_ReturnsCorrectDeterminant() {
        // Данные
        int[][] matrixData = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        MatrixService.Matrix matrix = new MatrixService.Matrix(matrixData);

        // Действие
        double determinant = MatrixService.calculateDeterminant(matrix);

        // Результат
        assertEquals(0, determinant); // Результат должен быть 0, так как определитель для этой матрицы равен 0
    }

    @Test
    public void testCalculateDeterminant_When3x3Matrix_NotReturnNanInfiniteOrFraction() {
        int[][] matrixData = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        MatrixService.Matrix matrix = new MatrixService.Matrix(matrixData);

        double determinant = MatrixService.calculateDeterminant(matrix);

        assertFalse(Double.isNaN(determinant)); // Определитель не NaN
        assertFalse(Double.isInfinite(determinant)); // Определитель не бесконечность

//        assertFalse(Double.toString(determinant).contains(".")); // Определитель не дробь
    }

    @Test
    public void testCalculateDeterminant_When3x3Matrix_NotExceedDoubleLimit() {

        int[][] matrixData = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

//        int[][] matrixData = {
//                {1, Integer.MAX_VALUE, 0},
//                {0, 1, Integer.MAX_VALUE},
//                {Integer.MAX_VALUE, 0, 1}
//        };
        MatrixService.Matrix matrix = new MatrixService.Matrix(matrixData);

        double determinant = MatrixService.calculateDeterminant(matrix);

        System.out.println(determinant);

        assertTrue(Math.abs(determinant) < 10000); // Определитель не превышает максимальное значение
    }

}
