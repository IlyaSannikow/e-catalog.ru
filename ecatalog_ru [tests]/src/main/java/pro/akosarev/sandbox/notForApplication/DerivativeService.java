package pro.akosarev.sandbox.notForApplication;

import java.util.function.DoubleUnaryOperator;

public class DerivativeService {
    public static double derivative(DoubleUnaryOperator function, double x, double h, int order) {
        if (order == 1) {
            return (function.applyAsDouble(Math.toRadians(x) + h) - function.applyAsDouble(Math.toRadians(x))) / h;
        } else if (order == 2) {
            return (function.applyAsDouble(Math.toRadians(x) + h) - 2 * function.applyAsDouble(Math.toRadians(x)) + function.applyAsDouble(Math.toRadians(x) - h)) / (h * h);
        }

        return Double.NaN;
    }

    public static void main(String[] args) {
        double x = 30;
        double h = 0.00001; // Шаг для вычисления производной
        DoubleUnaryOperator sinFunction = Math::sin;

        // Вычисление первой производной
        double firstDerivative = derivative(sinFunction, x, h, 1);
        System.out.println("f'(x) = " + firstDerivative);

        // Вычисление второй производной
        double secondDerivative = derivative(sinFunction, x, h, 2);
        System.out.println("f''(x) = " + secondDerivative);
    }
}
