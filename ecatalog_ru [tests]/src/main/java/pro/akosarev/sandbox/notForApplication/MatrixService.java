package pro.akosarev.sandbox.notForApplication;

public class MatrixService {

    public static void main(String[] args) {
        int[][] matrixData = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        Matrix matrix = new Matrix(matrixData);

        double determinant = calculateDeterminant(matrix); // Вычисление определителя матрицы
        System.out.println( "Определитель матрицы: " + determinant);
    }

    // Метод для вычисления определителя матрицы
    public static double calculateDeterminant(Matrix matrix) {

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

    // Класс для представления матрицы
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
}
